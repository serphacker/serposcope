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


public class AntiCaptchaSolver implements CaptchaSolver {
    
    final static Logger LOG = LoggerFactory.getLogger(AntiCaptchaSolver.class);

    public final static long POLLING_PAUSE_MS = 2500l;
    public final static long DEFAULT_TIMEOUT_MS = 60000l;

    private String apiUrl = "http://anti-captcha.com/";
    private String apiKey;
    private long timeoutMS;
    private int maxRetryOnOverload;    
    Random random = new Random();
    
    AtomicInteger captchaCount=new AtomicInteger();

    public AntiCaptchaSolver(String apiKey) {
        this(apiKey, DEFAULT_TIMEOUT_MS);
    }
    
    public AntiCaptchaSolver(String apiKey, long timeoutMS) {
        this(apiKey, timeoutMS, 5);
    }    

    public AntiCaptchaSolver(String apiKey, long timeoutMS, int maxRetryOnOverload) {
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

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean solve(Captcha cap) {
        if(!(cap instanceof CaptchaImage)){
            return false;
        }
        
        captchaCount.incrementAndGet();
        
        CaptchaImage captcha = (CaptchaImage)cap;
        captcha.setLastSolver(this);
        captcha.setStatus(Captcha.Status.CREATED);
        
        String filename = null;
        String textMimeType = captcha.getMimes()[0];
        String[] mimes = null;        
        
        if(textMimeType != null)
            mimes = textMimeType.split("/");
        else
            textMimeType = "application/octet-stream";
        if(mimes != null && mimes.length == 2){
            if(isValidImageExtension(mimes[1])){
                filename = "image." + mimes[1];
            }
        } else {
            filename = "image.png";
        }
        
        Map<String,Object> data = new HashMap<>();
        data.put("method", "post");
        data.put("key", apiKey);
        data.put("file",new ByteArrayBody(captcha.getImage(), ContentType.create(textMimeType), filename));
        
        long started = System.currentTimeMillis();
        captcha.setStatus(Captcha.Status.SUBMITTED);
        try(ScrapClient http = new ScrapClient()){
            String response;
            int retry = 0;
            while(true){
                http.post(apiUrl + "in.php", data, ScrapClient.PostType.MULTIPART);
                response = http.getContentAsString();
                if(!isRetryable(response)){
                    break;
                }
                
                if(++retry > maxRetryOnOverload){
                    break;
                }
                
                try {
                    Long sleep = 5000l*retry;
                    LOG.debug("server is overloaded \"{}\", sleeping {} ms", response, sleep);
                    Thread.sleep(sleep);
                }catch(InterruptedException ex){
                    break;
                }
            }
            
            if(response == null){
                captcha.setError(Captcha.Error.NETWORK_ERROR);
                return false;
            }
            
            if(!response.startsWith("OK|") || response.length() < 4){
                switch(response){
                    case "ERROR_WRONG_USER_KEY":
                    case "ERROR_KEY_DOES_NOT_EXIST":
                        captcha.setError(Captcha.Error.INVALID_CREDENTIALS);
                        return false;
                        
                    case "ERROR_ZERO_BALANCE":
                        captcha.setError(Captcha.Error.OUT_OF_CREDITS);
                        return false;
                        
                    case "ERROR_NO_SLOT_AVAILABLE":
                        captcha.setError(Captcha.Error.SERVICE_OVERLOADED);
                        return false;
                        
                    default:
                        captcha.setError(Captcha.Error.NETWORK_ERROR);
                        return false;
                }
            }
            
            captcha.setId(response.substring(3));
            
            long timeLimit=System.currentTimeMillis() + timeoutMS;
            while(System.currentTimeMillis() < timeLimit){

                int status = http.get(apiUrl + "res.php?key=" + apiKey + 
                    "&action=get" + 
                    "&id=" + captcha.getId() + 
                    "&random=" + random.nextInt(Integer.MAX_VALUE));
                
                String res = http.getContentAsString();
                if(res == null){
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                    return false;
                }
                
                if(!"CAPCHA_NOT_READY".equals(apiKey)){
                    if(res.startsWith("OK|")){
                        captcha.setResponse(res.substring(3));
                        captcha.setStatus(Captcha.Status.SOLVED);
                        return true;
                    }
                    
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                    captcha.setStatus(Captcha.Status.ERROR);
                }

                try {
                    Thread.sleep(POLLING_PAUSE_MS);
                } catch (InterruptedException ex) {
                    break;
                }
            }
            
            captcha.setError(Captcha.Error.TIMEOUT);
            captcha.setStatus(Captcha.Status.ERROR);
            
        } catch(IOException ex){
            LOG.error("io exception", ex);
            captcha.setError(EXCEPTION);
        } finally {
            captcha.setSolveDuration(System.currentTimeMillis()-started);
        }
        
        return false;
    }
    
    public boolean isRetryable(String response){
        return response == null || "ERROR_NO_SLOT_AVAILABLE".equals(response);
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
        if(balance == null || balance.isEmpty()){
            return false;
        }
        try {
            Float.parseFloat(getRawBalance());
            return true;
        } catch(Exception ex){
        }
        return false;
    }    

    @Override
    public float getCredit() {
        try {
            return Float.parseFloat(getRawBalance());
        }catch(Exception ex){
            return 0;
        }
    }
    
    protected String getRawBalance() {
        try(ScrapClient cli = new ScrapClient()){
            int status = cli.get(apiUrl + "/res.php?key=" + apiKey + "&action=getbalance");
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
    
    protected Map<String,String> parseAnswer(String answer){
        Map<String,String> data = new HashMap<>();
        if(answer == null){
            return data;
        }
        String[] pairs = answer.split("&");
        for (String pair : pairs) {
            String[] keyvalue = pair.split("=");
            if(keyvalue.length == 1){
                data.put(keyvalue[0], "");
            } else if(keyvalue.length == 2){
                data.put(keyvalue[0], keyvalue[1]);
            }
        }
        return data;
    }
    
    public static boolean isValidImageExtension(String ext){
        if(
            ext.equalsIgnoreCase("jpg") ||
            ext.equalsIgnoreCase("gif") ||
            ext.equalsIgnoreCase("jpeg") ||
            ext.equalsIgnoreCase("png")
        )
            return true;
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
