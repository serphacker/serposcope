/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import static com.serphacker.serposcope.scraper.captcha.Captcha.Error.EXCEPTION;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.CaptchaRecaptcha;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTyperzSolver implements CaptchaSolver {

    final static int SOFT_ID = 315783;

    final static Logger LOG = LoggerFactory.getLogger(ImageTyperzSolver.class);

    public final static long POLLING_PAUSE_MS = 2500l;
    public final static long DEFAULT_TIMEOUT_MS = 180000l;

    private String apiKey;
    private long timeoutMS;
    private int maxRetryOnOverload;
    Random random = new Random();

    AtomicInteger captchaCount = new AtomicInteger();

    public ImageTyperzSolver(String apiKey) {
        this(apiKey, DEFAULT_TIMEOUT_MS);
    }

    public ImageTyperzSolver(String apiKey, long timeoutMS) {
        this(apiKey, timeoutMS, 5);
    }

    public ImageTyperzSolver(String apiKey, long timeoutMS, int maxRetryOnOverload) {
        this.apiKey = apiKey;
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean solve(Captcha captcha) {
        if (captcha instanceof CaptchaImage) {
            return solveImage((CaptchaImage) captcha);
        }
        
        if (captcha instanceof CaptchaRecaptcha){
            return solveRecaptcha((CaptchaRecaptcha) captcha);
        }
        
        return false;

    }

    public boolean solveImage(CaptchaImage captcha) {
        captchaCount.incrementAndGet();

        captcha.setLastSolver(this);
        captcha.setStatus(Captcha.Status.CREATED);

        Map<String, Object> createTaskMap = new HashMap<>();
        createTaskMap.put("token", apiKey);
        createTaskMap.put("affiliateid", "" + SOFT_ID);
        createTaskMap.put("action", "UPLOADCAPTCHA");
        createTaskMap.put("file", Base64.encode(captcha.getImage()));
        createTaskMap.put("chkCase", "0");

        long started = System.currentTimeMillis();
        captcha.setStatus(Captcha.Status.SUBMITTED);
        try (ScrapClient http = new ScrapClient()) {
            http.setTimeout((int) DEFAULT_TIMEOUT_MS);
//            http.setInsecureSSL(true);
//            http.setProxy(new HttpProxy("127.0.0.1", 8080));
            String response;
            int retry = 0;
            while (true) {
                http.post("http://captchatypers.com/Forms/UploadFileAndGetTextNEWToken.ashx", createTaskMap, ScrapClient.PostType.URL_ENCODED);
                response = http.getContentAsString();
                if (!isRetryable(response)) {
                    break;
                }

                if (++retry > maxRetryOnOverload) {
                    break;
                }

                try {
                    Long sleep = 5000l * retry;
                    LOG.debug("server is overloaded \"{}\", sleeping {} ms", response, sleep);
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    break;
                }
            }

            if (isError(response)) {
                captcha.setError(errorCode(response));
                return false;
            }

            String[] split = response.split("\\|");
            if (split.length == 2) {
                captcha.setStatus(Captcha.Status.SOLVED);
                captcha.setId(split[0]);
                captcha.setResponse(split[1]);
                return true;
            }

        } catch (IOException ex) {
            LOG.error("io exception", ex);
            captcha.setError(EXCEPTION);
        } finally {
            captcha.setSolveDuration(System.currentTimeMillis() - started);
        }

        return false;
    }

    public boolean solveRecaptcha(CaptchaRecaptcha captcha) {
        captchaCount.incrementAndGet();

        captcha.setLastSolver(this);
        captcha.setStatus(Captcha.Status.CREATED);

        Map<String, Object> createTaskMap = new HashMap<>();
        createTaskMap.put("token", apiKey);
        createTaskMap.put("affiliateid", "" + SOFT_ID);
        createTaskMap.put("action", "UPLOADCAPTCHA");
        createTaskMap.put("pageurl", captcha.getUrl());
        createTaskMap.put("googlekey", captcha.getChallenge());

        long started = System.currentTimeMillis();
        captcha.setStatus(Captcha.Status.SUBMITTED);
        try (ScrapClient http = new ScrapClient()) {
//            http.setInsecureSSL(true);
//            http.setProxy(new HttpProxy("127.0.0.1", 8080));
            String submitResponse;
            int retry = 0;
            while (true) {
                http.post("http://captchatypers.com/captchaapi/UploadRecaptchaToken.ashx", createTaskMap, ScrapClient.PostType.URL_ENCODED);
                submitResponse = http.getContentAsString();
                if (!isRetryable(submitResponse)) {
                    break;
                }

                if (++retry > maxRetryOnOverload) {
                    break;
                }

                try {
                    Long sleep = 5000l * retry;
                    LOG.debug("server is overloaded \"{}\", sleeping {} ms", submitResponse, sleep);
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    break;
                }
            }

            if (isError(submitResponse)) {
                captcha.setError(errorCode(submitResponse));
                return false;
            }

            Integer captchaId = null;
            try {
                captchaId = Integer.parseInt(submitResponse);
            } catch (Exception ex) {
            }
            captcha.setId("" + captchaId);

            if (captchaId == null || captchaId < 1) {
                captcha.setError(Captcha.Error.NETWORK_ERROR);
                return false;
            }

            Map<String, Object> retrieveResponse = new HashMap<>();
            retrieveResponse.put("action", "GETTEXT");
            retrieveResponse.put("token", apiKey);
            retrieveResponse.put("captchaid", "" + captchaId);

            long timeLimit = System.currentTimeMillis() + timeoutMS;
            while (System.currentTimeMillis() < timeLimit) {

                http.post("http://captchatypers.com/captchaapi/GetRecaptchaTextToken.ashx",
                    retrieveResponse,
                    ScrapClient.PostType.URL_ENCODED
                );

                String getTextResponse = http.getContentAsString();
                if (getTextResponse == null) {
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                    return false;
                }

                if (!getTextResponse.contains("NOT_DECODED")) {
                    if (isError(getTextResponse)) {
                        captcha.setError(errorCode(getTextResponse));
                        return false;
                    }

                    captcha.setResponse(getTextResponse);
                    captcha.setStatus(Captcha.Status.SOLVED);
                    return true;
                }

                try {
                    Thread.sleep(POLLING_PAUSE_MS);
                } catch (InterruptedException ex) {
                    break;
                }
            }

            captcha.setError(Captcha.Error.TIMEOUT);
            captcha.setStatus(Captcha.Status.ERROR);

        } catch (IOException ex) {
            LOG.error("io exception", ex);
            captcha.setError(EXCEPTION);
        } finally {
            captcha.setSolveDuration(System.currentTimeMillis() - started);
        }

        return false;
    }

    boolean isError(String response) {
        return response == null || response.contains("ERROR: ");
    }

    Captcha.Error errorCode(String response) {
        if (response == null || !response.contains("ERROR: ")) {
            return Captcha.Error.NETWORK_ERROR;
        }

        response = response.substring(7).toUpperCase();
        switch (response) {
            case "AUTHENTICATION_FAILED":
            case "INVALID_TOKEN":
                return Captcha.Error.INVALID_CREDENTIALS;

            case "NOT_DECODED":
            case "IMAGE_TIMED_OUT":
                return Captcha.Error.SERVICE_OVERLOADED;

            default:
                return Captcha.Error.NETWORK_ERROR;
        }

    }

    public boolean isRetryable(String response) {
        boolean retryable = response == null
            || response.toUpperCase().contains("UNKNOWN")
            || response.toUpperCase().contains("NOT_DECODED");
//        LOG.debug("isRetryable ? {} - {}", retryable, response);
        return retryable;
    }

    @Override
    public boolean reportIncorrect(Captcha captcha) {
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "anticaptcha";
    }

    @Override
    public boolean testLogin() {
        String balance = getRawBalance();
        if (balance == null || balance.isEmpty()) {
            return false;
        }
        try {
            Float.parseFloat(getRawBalance());
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    @Override
    public float getCredit() {
        try {
            return Float.parseFloat(getRawBalance());
        } catch (Exception ex) {
            return 0;
        }
    }

    protected String getRawBalance() {
        try(ScrapClient cli = new ScrapClient()){
            Map<String, Object> params = new HashMap<>();
            params.put("action", "REQUESTBALANCE");
            params.put("token", apiKey);
            int status = cli.post("http://captchatypers.com/Forms/RequestBalanceToken.ashx", params, ScrapClient.PostType.URL_ENCODED);
            if(status != 200){
                return "";
            }
            return cli.getContentAsString();
        }catch(Exception ex){
            LOG.error("exception", ex);
        }
        return "";
    }

    @Override
    public boolean hasCredit() {
        return getCredit() > 0.001f;

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
