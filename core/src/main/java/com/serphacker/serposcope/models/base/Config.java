/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import static com.serphacker.serposcope.models.base.Config.CaptchaService.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class Config {
    
    public final static Pattern PATTERN_CRONTIME = Pattern.compile("^([0-9]+):([0-9]+)$");
    
    public enum CaptchaService {
        DISABLE,
        DEATHBYCAPTCHA;
        
        public static CaptchaService fromString(String name){
            if(name != null){
                if(DEATHBYCAPTCHA.toString().equals(name.toUpperCase())){
                    return DEATHBYCAPTCHA;
                }
            }
            return DISABLE;
        }
    };
    
//    boolean cronEnabled;
    LocalTime cronTime;
    CaptchaService captchaService = DISABLE;
    String dbcUser;
    String dbcPass;
    
    public final static String DEFAULT_DISPLAY_HOME = "summary";
    public final static List<String> VALID_DISPLAY_HOME = Arrays.asList("summary","table");
    public final static String DEFAULT_DISPLAY_GOOGLE_TARGET = "table";
    public final static List<String> VALID_DISPLAY_GOOGLE_TARGET = Arrays.asList("variation","chart","table");
    public final static String DEFAULT_DISPLAY_GOOGLE_SEARCH = "split";
    public final static List<String> VALID_DISPLAY_GOOGLE_SEARCH = Arrays.asList("split","chart","table");    
    
    String displayHome=DEFAULT_DISPLAY_HOME;
    String displayGoogleTarget=DEFAULT_DISPLAY_GOOGLE_TARGET;
    String displayGoogleSearch=DEFAULT_DISPLAY_GOOGLE_SEARCH;

    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    public String getDbcUser() {
        return dbcUser;
    }

    public void setDbcUser(String dbcUser) {
        this.dbcUser = dbcUser;
    }

    public String getDbcPass() {
        return dbcPass;
    }

    public void setDbcPass(String dbcPass) {
        this.dbcPass = dbcPass;
    }

    public LocalTime getCronTime() {
        return cronTime;
    }

    public void setCronTime(LocalTime cronTime) {
        this.cronTime = cronTime;
    }
    
    public void setCronTime(String dateTime){
        if(dateTime == null || dateTime.isEmpty()){
            this.cronTime = null;
        }
        try{
            this.cronTime = LocalTime.parse(dateTime);
        } catch(Exception ex){
            this.cronTime = null;
        }
    }

    public String getDisplayHome() {
        return displayHome;
    }

    public void setDisplayHome(String displayHome) {
        this.displayHome = displayHome;
    }

    public String getDisplayGoogleTarget() {
        return displayGoogleTarget;
    }

    public void setDisplayGoogleTarget(String displayGoogleTarget) {
        this.displayGoogleTarget = displayGoogleTarget;
    }

    public String getDisplayGoogleSearch() {
        return displayGoogleSearch;
    }

    public void setDisplayGoogleSearch(String displayGoogleSearch) {
        this.displayGoogleSearch = displayGoogleSearch;
    }
    
}
