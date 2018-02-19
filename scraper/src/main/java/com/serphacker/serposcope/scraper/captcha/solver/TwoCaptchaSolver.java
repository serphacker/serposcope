/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.serphacker.serposcope.scraper.captcha.Captcha;
import static com.serphacker.serposcope.scraper.captcha.Captcha.Error.EXCEPTION;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.CaptchaRecaptcha;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TwoCaptchaSolver implements CaptchaSolver {
    
    final static int SOFT_ID = 2083;
    
    final static Logger LOG = LoggerFactory.getLogger(TwoCaptchaSolver.class);
    
    final static Configuration JSONPATH_CONF = Configuration.defaultConfiguration()
        .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    
    public final static long POLLING_PAUSE_MS = 2500l;
    public final static long DEFAULT_TIMEOUT_MS = 180000l;

    private String apiUrl = "http://2captcha.com/";
    private String apiKey;
    private long timeoutMS;
    private int maxRetryOnOverload;    
    Random random = new Random();
    
    AtomicInteger captchaCount=new AtomicInteger();

    public TwoCaptchaSolver(String apiKey) {
        this(apiKey, DEFAULT_TIMEOUT_MS);
    }
    
    public TwoCaptchaSolver(String apiKey, long timeoutMS) {
        this(apiKey, timeoutMS, 5);
    }    

    public TwoCaptchaSolver(String apiKey, long timeoutMS, int maxRetryOnOverload) {
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
    public boolean solve(Captcha captcha) {
        if(!(captcha instanceof CaptchaImage || captcha instanceof CaptchaRecaptcha)){
            return false;
        }
        
        captchaCount.incrementAndGet();
        
        captcha.setLastSolver(this);
        captcha.setStatus(Captcha.Status.CREATED);
        
        Map<String,Object> createTaskMap = new HashMap<>();
        createTaskMap.put("key", apiKey);
        createTaskMap.put("soft_id", "" + SOFT_ID);
        
        if(captcha instanceof CaptchaImage){
            createTaskMap.put("method", "base64");
            createTaskMap.put("body",Base64.encode(((CaptchaImage)captcha).getImage()));
        }        
        
        if(captcha instanceof CaptchaRecaptcha){
            createTaskMap.put("method", "userrecaptcha");
            createTaskMap.put("googlekey", ((CaptchaRecaptcha)captcha).getChallenge());
            createTaskMap.put("pageurl", ((CaptchaRecaptcha)captcha).getUrl());
        }
        

        
        long started = System.currentTimeMillis();
        captcha.setStatus(Captcha.Status.SUBMITTED);
        try(ScrapClient http = new ScrapClient()){
//            http.setInsecureSSL(true);
//            http.setProxy(new HttpProxy("127.0.0.1", 8080));
            String response;
            int retry = 0;
            while(true){
                http.post(apiUrl + "/in.php", createTaskMap, ScrapClient.PostType.URL_ENCODED);
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
                
                if(!"CAPCHA_NOT_READY".equals(res)){
                    if(res.startsWith("OK|") && !res.substring(3).isEmpty()){
                        
                        if(captcha instanceof CaptchaRecaptcha){
                            ((CaptchaRecaptcha)captcha).setResponse(res.substring(3));
                        }
                        
                        if(captcha instanceof CaptchaImage){
                            ((CaptchaImage)captcha).setResponse(res.substring(3));
                        }
                        captcha.setStatus(Captcha.Status.SOLVED);
                        return true;
                    }
                    
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                    captcha.setStatus(Captcha.Status.ERROR);
                    return false;
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
        boolean retryable = response == null || response.toUpperCase().contains("ERROR_NO_SLOT_AVAILABLE");
//        LOG.debug("isRetryable ? {} - {}", retryable, response);
        return retryable;
    }
    
    @Override
    public boolean reportIncorrect(Captcha captcha) {
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "2captcha";
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

