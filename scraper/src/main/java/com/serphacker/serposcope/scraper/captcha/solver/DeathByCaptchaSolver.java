/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.serposcope.scraper.captcha.Captcha;
import static com.serphacker.serposcope.scraper.captcha.Captcha.Error.EXCEPTION;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.CaptchaRecaptcha;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathByCaptchaSolver implements CaptchaSolver {

    final static Logger LOG = LoggerFactory.getLogger(DeathByCaptchaSolver.class);

    public final static long POLLING_PAUSE_MS = 2500l;
    public final static long DEFAULT_TIMEOUT_MS = 60000;

    private String apiUrl = "http://api.dbcapi.me/api/";
    private String login;
    private String password;
    private long timeoutMS;
    private int maxRetryOnOverload;
    Random random = new Random();

    AtomicInteger captchaCount = new AtomicInteger();

    public DeathByCaptchaSolver(String login, String password) {
        this(login, password, DEFAULT_TIMEOUT_MS);
    }

    public DeathByCaptchaSolver(String login, String password, long timeoutMS) {
        this(login, password, timeoutMS, 5);
    }

    public DeathByCaptchaSolver(String login, String password, long timeoutMS, int maxRetryOnOverload) {
        this.login = login;
        this.password = password;
        this.timeoutMS = timeoutMS;
        this.maxRetryOnOverload = maxRetryOnOverload;
    }

    public long getTimeoutMS() {
        return timeoutMS;
    }

    public void setTimeoutMS(long timeoutMS) {
        this.timeoutMS = timeoutMS;
    }

    public int getMaxRetryOnError() {
        return maxRetryOnOverload;
    }

    public void setMaxRetryOnError(int maxRetryOnError) {
        this.maxRetryOnOverload = maxRetryOnError;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean solve(Captcha cap) {
        if (!(cap instanceof CaptchaImage || cap instanceof CaptchaRecaptcha)) {
            cap.setError(Captcha.Error.UNSUPPORTED_TYPE);
            return false;
        }

        captchaCount.incrementAndGet();
        cap.setLastSolver(this);
        cap.setStatus(Captcha.Status.CREATED);

        Map<String, Object> data = getMapWithCredentials();
        if (cap instanceof CaptchaImage) {
            CaptchaImage captcha = (CaptchaImage) cap;

            String filename = null;
            String textMimeType = captcha.getMimes()[0];
            String[] mimes = null;

            if (textMimeType != null) {
                mimes = textMimeType.split("/");
            } else {
                textMimeType = "application/octet-stream";
            }
            if (mimes != null && mimes.length == 2) {
                if (isValidImageExtension(mimes[1])) {
                    filename = "image." + mimes[1];
                }
            } else {
                filename = "image.png";
            }

            data.put("captchafile", new ByteArrayBody(captcha.getImage(), ContentType.create(textMimeType), filename));
        }

        if (cap instanceof CaptchaRecaptcha) {
            data.put("type", "4");
            try {
                Map<String, String> json = new HashMap<>();
                json.put("pageurl", ((CaptchaRecaptcha) cap).getUrl());
                json.put("googlekey", ((CaptchaRecaptcha) cap).getChallenge());                
                data.put("token_params", new ObjectMapper().writeValueAsString(json));
            } catch (Exception ex) {
                LOG.warn("json error", ex);
                cap.setError(EXCEPTION);
                return false;
            }
        }

        long started = System.currentTimeMillis();
        cap.setStatus(Captcha.Status.SUBMITTED);
        try (ScrapClient http = new ScrapClient()) {
            int httpStatus = 0;
            int retry = 0;
            while (true) {
                httpStatus = http.post(apiUrl + "captcha", data, ScrapClient.PostType.MULTIPART);
                if (!isRetryableStatus(httpStatus)) {
                    break;
                }

                if (++retry > maxRetryOnOverload) {
                    break;
                }

                try {
                    Long sleep = 5000l * retry;
                    LOG.debug("server is overloaded, sleeping {} ms", sleep);
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    break;
                }
            }

            if (httpStatus >= 500) {
                cap.setError(Captcha.Error.SERVICE_OVERLOADED);
                return false;
            } else if (httpStatus == 403) {
                cap.setError(Captcha.Error.INVALID_CREDENTIALS);
                return false;
            } else if (httpStatus == 400) {
                cap.setError(Captcha.Error.INVALID_CREDENTIALS);
                return false;
            } else if (httpStatus != 303) {
                cap.setError(Captcha.Error.NETWORK_ERROR);
                return false;
            }

            String location = http.getResponseHeader("location");
            if (location == null || !location.startsWith(apiUrl)) {
                LOG.warn("invalid location : {}", location);
                cap.setError(Captcha.Error.NETWORK_ERROR);
                return false;
            }

            String captchaId = extractId(location);
            if (captchaId == null) {
                cap.setError(Captcha.Error.NETWORK_ERROR);
                return false;
            }
            cap.setId(captchaId);

            long timeLimit = System.currentTimeMillis() + timeoutMS;
            while (System.currentTimeMillis() < timeLimit) {

                int status = http.get(location + "?" + random.nextInt(Integer.MAX_VALUE));
                if (status == 200) {
                    System.out.println(http.getContentAsString());
                    Map<String, String> answer = parseAnswer(http.getContentAsString());
                    if (answer.get("text") != null && !answer.get("text").isEmpty()) {
                        
                        if (cap instanceof CaptchaImage) {
                            ((CaptchaImage) cap).setResponse(answer.get("text"));
                        }
                        
                        if(cap instanceof CaptchaRecaptcha){
                            if("0".equals(answer.get("is_correct"))){
                                cap.setError(Captcha.Error.SERVICE_OVERLOADED);
                                cap.setStatus(Captcha.Status.ERROR);
                                return false;
                            }
                            ((CaptchaRecaptcha) cap).setResponse(answer.get("text"));
                        }
                        
                        cap.setStatus(Captcha.Status.SOLVED);
                        return true;
                    }
                }

                try {
                    Thread.sleep(POLLING_PAUSE_MS);
                } catch (InterruptedException ex) {
                    break;
                }
            }

            cap.setError(Captcha.Error.TIMEOUT);
            cap.setStatus(Captcha.Status.ERROR);

        } catch (IOException ex) {
            LOG.error("io exception", ex);
            cap.setError(EXCEPTION);
        } finally {
            cap.setSolveDuration(System.currentTimeMillis() - started);
        }

        return false;
    }

    public boolean isRetryableStatus(int status) {
        return status == 0 || status >= 500 && status <= 599;
    }

    private final static Pattern pExtractId = Pattern.compile("([0-9]+)$");

    protected String extractId(String location) {
        Matcher matcher = pExtractId.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public boolean reportIncorrect(Captcha captcha) {
        try (ScrapClient http = new ScrapClient()) {
            return http.get(apiUrl + "/captcha/" + captcha.getId() + "/report") == 200;
        } catch (Exception ex) {
            LOG.warn("exception ", ex);
        }
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "deathbycaptcha";
    }

    @Override
    public boolean testLogin() {
        Map<String, String> userData = getUserData();
        if (userData == null) {
            return false;
        }
        return userData.containsKey("user");
    }

    @Override
    public float getCredit() {
        Map<String, String> userData = getUserData();
        if (userData == null) {
            return 0;
        }
        try {
            return Float.parseFloat(userData.get("balance"));
        } catch (Exception ex) {
            return 0;
        }
    }

    protected Map<String, String> getUserData() {
        Map<String, String> userData = new HashMap<>();
        try (ScrapClient cli = new ScrapClient()) {
            int status = cli.post(apiUrl + "user", getMapWithCredentials(), ScrapClient.PostType.URL_ENCODED);
            if (status != 200) {
                return userData;
            }

            userData = parseAnswer(cli.getContentAsString());

        } catch (Exception ex) {
            LOG.error("exception", ex);
        }
        return userData;
    }

    @Override
    public boolean hasCredit() {
        Map<String, String> userData = getUserData();
        if (userData == null) {
            return false;
        }

        try {
            float rate = Float.parseFloat(userData.get("rate"));
            float balance = Float.parseFloat(userData.get("balance"));
            return rate < balance;
        } catch (Exception ex) {
            return false;
        }

    }

    @Override
    public void close() throws IOException {
    }

    protected Map<String, String> parseAnswer(String answer) {
        Map<String, String> data = new HashMap<>();
        if (answer == null) {
            return data;
        }
        String[] pairs = answer.split("&");
        for (String pair : pairs) {
            String[] keyvalue = pair.split("=");
            if (keyvalue.length == 1) {
                data.put(keyvalue[0], "");
            } else if (keyvalue.length == 2) {
                data.put(keyvalue[0], keyvalue[1]);
            }
        }
        return data;
    }

    protected Map<String, Object> getMapWithCredentials() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", this.login);
        data.put("password", this.password);
        return data;
    }

    public static boolean isValidImageExtension(String ext) {
        if (ext.equalsIgnoreCase("jpg")
            || ext.equalsIgnoreCase("gif")
            || ext.equalsIgnoreCase("jpeg")
            || ext.equalsIgnoreCase("png")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public int getCaptchaCount() {
        return captchaCount.get();
    }

    @Override
    public void resetCaptchaCount() {
        captchaCount.set(0);
    }

}
