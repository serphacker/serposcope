/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class Config {
    
    public final static Pattern PATTERN_CRONTIME = Pattern.compile("^([0-9]+):([0-9]+)$");
    
//    boolean cronEnabled;
    LocalTime cronTime;
    
    String dbcUser;
    String dbcPass;
    
    String decaptcherUser;
    String decaptcherPass;
    
    String anticaptchaKey;
    
    String taskNotificationUrl;
    
    public final static String DEFAULT_DISPLAY_HOME = "summary";
    public final static List<String> VALID_DISPLAY_HOME = Arrays.asList("summary","table");
    public final static String DEFAULT_DISPLAY_GOOGLE_TARGET = "table";
    public final static List<String> VALID_DISPLAY_GOOGLE_TARGET = Arrays.asList("variation","chart","table");
    public final static String DEFAULT_DISPLAY_GOOGLE_SEARCH = "split";
    public final static List<String> VALID_DISPLAY_GOOGLE_SEARCH = Arrays.asList("split","chart","table");    
    
    String displayHome=DEFAULT_DISPLAY_HOME;
    String displayGoogleTarget=DEFAULT_DISPLAY_GOOGLE_TARGET;
    String displayGoogleSearch=DEFAULT_DISPLAY_GOOGLE_SEARCH;
    
    public final static int DEFAULT_PRUNE_RUNS = 365;
    int pruneRuns = DEFAULT_PRUNE_RUNS;

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

    public String getDecaptcherUser() {
        return decaptcherUser;
    }

    public void setDecaptcherUser(String decaptcherUser) {
        this.decaptcherUser = decaptcherUser;
    }

    public String getDecaptcherPass() {
        return decaptcherPass;
    }

    public void setDecaptcherPass(String decaptcherPass) {
        this.decaptcherPass = decaptcherPass;
    }

    public String getAnticaptchaKey() {
        return anticaptchaKey;
    }

    public void setAnticaptchaKey(String anticaptchaKey) {
        this.anticaptchaKey = anticaptchaKey;
    }
    
    public String getTaskNotificationUrl() {
    	return taskNotificationUrl;
    }
    
    public void setTaskNotificationUrl(String taskNotificationUrl) {
    	this.taskNotificationUrl = taskNotificationUrl;
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

    public int getPruneRuns() {
        return pruneRuns;
    }

    public void setPruneRuns(int pruneRuns) {
        this.pruneRuns = pruneRuns;
    }
    
}
