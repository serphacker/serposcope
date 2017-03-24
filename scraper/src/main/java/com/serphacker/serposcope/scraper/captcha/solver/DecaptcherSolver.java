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


public class DecaptcherSolver implements CaptchaSolver {
    
    enum ResultCode {
        OK(0, "everything went OK"),
	ERR_GENERAL(-1,"general server internal error"),
	ERR_STATUS(-2,"status is not correct"),
	ERR_NET_ERROR(-3, "network data transfer error"),
	ERR_TEXT_SIZE(-4,"text is not of an appropriate size"),
	ERR_OVERLOAD(-5,"server's overloaded"),
	ERR_BALANCE(-6, "not enough funds to complete the request"),
	ERR_TIMEOUT(-7, "requiest timed out"),
	ERR_UNKNOWN(-200, "uknown error");
        
        int value;
        String msg;

        private ResultCode(int value, String msg) {
            this.value = value;
            this.msg = msg;
        }
        
        public static ResultCode fromValue(int value){
            for (ResultCode err : ResultCode.values()) {
                if(err.value == value){
                    return err;
                }
            }
            return ERR_UNKNOWN;
        }
    }
    
    static class Answer {
        public final ResultCode status;
        public final int majorId;
        public final int minorId;
        public final int type;
        public final int timeout;
        public final String text;

        public Answer(ResultCode status, int majorId, int minorId, int type, int timeout, String text) {
            this.status = status;
            this.majorId = majorId;
            this.minorId = minorId;
            this.type = type;
            this.timeout = timeout;
            this.text = text;
        }
        
        public static Answer fromResponse(String response){
            if(response == null){
                return null;
            }
            
            //ResultCode|MajorID|MinorID|Type|Timeout|Text
            //0|107|44685|0|0|n7hjks
            String[] split = response.split("\\|");
            try {
                if(split.length == 6){
                    return new Answer(
                        ResultCode.fromValue(Integer.parseInt(split[0])), 
                        Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]),
                        Integer.parseInt(split[3]),
                        Integer.parseInt(split[4]),
                        split[5] == null ? "" : split[5]
                    );
                } else if(split.length == 1){
                    return new Answer(ResultCode.fromValue(Integer.parseInt(split[0])), 0,0,0,0,"");
                }
            } catch(Exception ex){
            }
            
            return null;
        }
    }
    
    final static Logger LOG = LoggerFactory.getLogger(DecaptcherSolver.class);

    public final static int DEFAULT_TIMEOUT_MS = 30000;
    public final static int DEFAULT_MAX_RETRY_OVERLOAD = 5;

    private String apiUrl;
    private String login;
    private String password;
    private long timeoutMS;
    private int maxRetryOnOverload;    
    Random random = new Random();
    
    AtomicInteger captchaCount=new AtomicInteger();

    public DecaptcherSolver(String login, String password) {
        this(login, password, DEFAULT_TIMEOUT_MS);
    }
    
    public DecaptcherSolver(String login, String password, long timeoutMS) {
        this(login, password, timeoutMS, DEFAULT_MAX_RETRY_OVERLOAD);
    }    

    public DecaptcherSolver(String login, String password, long timeoutMS, int maxRetryOnOverload) {
        this("http://poster.de-captcher.com/", login, password, timeoutMS, maxRetryOnOverload);
    }

    public DecaptcherSolver(String apiUrl, String login, String password, long timeoutMS, int maxRetryOnOverload) {
        this.apiUrl = apiUrl;
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
        if(!(cap instanceof CaptchaImage)){
            cap.setError(Captcha.Error.UNSUPPORTED_TYPE);
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
        
        Map<String,Object> data = getMapWithCredentials();
        data.put("function", "picture2");
        data.put("pict",new ByteArrayBody(captcha.getImage(), ContentType.create(textMimeType), filename));
        data.put("pict_type","0");
        
        long started = System.currentTimeMillis();
        captcha.setStatus(Captcha.Status.SUBMITTED);
        try(ScrapClient http = new ScrapClient()){
            http.setTimeout((int)timeoutMS);
            int httpStatus = 0;
            Answer answer = null;
            int retry = 0;
            while(true){
                httpStatus = http.post(apiUrl, data, ScrapClient.PostType.MULTIPART);
                answer = Answer.fromResponse(http.getContentAsString());
                if(!isRetryable(httpStatus, answer)){
                    break;
                }
                
                if(++retry > maxRetryOnOverload){
                    break;
                }
                
                try {
                    Long sleep = 5000l*retry;
                    LOG.debug("server is overloaded, sleeping {} ms", sleep);
                    Thread.sleep(sleep);
                }catch(InterruptedException ex){
                    break;
                }
            }
            
            if(answer == null){
                if(httpStatus == 200){
                    captcha.setError(Captcha.Error.INVALID_CREDENTIALS);
                } else {
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                }
                captcha.setStatus(Captcha.Status.ERROR);
                
                return false;
            }
            
            captcha.setId(answer.majorId + "-" + answer.minorId);
            switch(answer.status){
                case OK:
                    captcha.setStatus(Captcha.Status.SOLVED);
                    captcha.setError(Captcha.Error.SUCCESS);
                    captcha.setResponse(answer.text);
                    return true;
                    
                case ERR_OVERLOAD:
                    captcha.setError(Captcha.Error.SERVICE_OVERLOADED);
                    break;
                    
                case ERR_BALANCE:
                    captcha.setError(Captcha.Error.OUT_OF_CREDITS);
                    break;
                    
                default:
                    captcha.setError(Captcha.Error.NETWORK_ERROR);
                    
            }
            
            captcha.setStatus(Captcha.Status.ERROR);
            
        } catch(IOException ex){
            LOG.error("io exception", ex);
            captcha.setError(EXCEPTION);
        } finally {
            captcha.setSolveDuration(System.currentTimeMillis()-started);
        }
        
        return false;
    }
    
    protected boolean isRetryable(int statusCode, Answer answer){
        if(answer == null){
            if(statusCode == 200){
                return false;
            } else {
                return true;
            }
        }
        return answer.status == ResultCode.ERR_OVERLOAD;
    }
    
    private final static Pattern pExtractId = Pattern.compile("([0-9]+)$");
    protected String extractId(String location){
        Matcher matcher = pExtractId.matcher(location);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
    
    @Override
    public boolean reportIncorrect(Captcha captcha) {
        if(captcha.getId() == null){
            return false;
        }
        
        String[] split = captcha.getId().split("-");
        if(split.length != 2){
            return false;
        }
        
        try(ScrapClient http = new ScrapClient()){
            Map<String, Object> data = getMapWithCredentials();
            data.put("function", "picture_bad2");
            data.put("major_id", split[0]);
            data.put("minor_id", split[1]);
            return http.post(apiUrl, data, ScrapClient.PostType.MULTIPART) == 200;
        }catch(Exception ex){
            LOG.warn("exception ", ex);
        }
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "decaptcher";
    }

    @Override
    public boolean testLogin() {
        String balanceRaw = getBalanceRaw();
        if(balanceRaw == null || balanceRaw.isEmpty()){
            return false;
        }
        return true;
    }    

    @Override
    public float getCredit() {
        try {
            return Float.parseFloat(getBalanceRaw());
        }catch(Exception ex){
            return 0;
        }
    }
    
    protected String getBalanceRaw() {
        try(ScrapClient cli = new ScrapClient()){
            Map<String, Object> map = getMapWithCredentials();
            map.put("function", "balance");
            int status = cli.post(apiUrl, map, ScrapClient.PostType.URL_ENCODED);
            if(status != 200){
                return null;
            }
            return cli.getContentAsString();
        }catch(Exception ex){
            LOG.error("exception", ex);
        }
        return null;
    }    

    @Override
    public boolean hasCredit() {
        return getCredit() > 0.002f;
        
    }

    @Override
    public void close() throws IOException {
    }
    
    
    protected Map<String,Object> getMapWithCredentials(){
        Map<String,Object> data = new HashMap<>();
        data.put("username", this.login);
        data.put("password",this.password);
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
