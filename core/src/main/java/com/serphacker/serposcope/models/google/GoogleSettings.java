/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.util.Arrays;
import java.util.List;


public class GoogleSettings {
    
    int resultPerPage = 10;
    int pages = 5;
    int minPauseBetweenPageSec = 10;
    int maxPauseBetweenPageSec = 10;
    int maxThreads = 1;
    int fetchRetry = 3;    
    
    String defaultTld = "com";
    String defaultDatacenter = null;
    GoogleDevice defaultDevice = GoogleDevice.DESKTOP;
    String defaultLocal = null;
    String defaultCustomParameters = null;

    public int getResultPerPage() {
        return resultPerPage;
    }

    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getMinPauseBetweenPageSec() {
        return minPauseBetweenPageSec;
    }

    public void setMinPauseBetweenPageSec(int minPauseBetweenPageSec) {
        this.minPauseBetweenPageSec = minPauseBetweenPageSec;
    }

    public int getMaxPauseBetweenPageSec() {
        return maxPauseBetweenPageSec;
    }

    public void setMaxPauseBetweenPageSec(int maxPauseBetweenPageSec) {
        this.maxPauseBetweenPageSec = maxPauseBetweenPageSec;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getFetchRetry() {
        return fetchRetry;
    }

    public void setFetchRetry(int fetchRetry) {
        this.fetchRetry = fetchRetry;
    }
    
    // search
    public String getDefaultTld() {
        return defaultTld;
    }

    public void setDefaultTld(String defaultTld) {
        this.defaultTld = defaultTld;
    }

    public String getDefaultDatacenter() {
        return defaultDatacenter;
    }

    public void setDefaultDatacenter(String defaultDatacenter) {
        this.defaultDatacenter = defaultDatacenter;
    }

    public GoogleDevice getDefaultDevice() {
        return defaultDevice;
    }

    public void setDefaultDevice(GoogleDevice defaultDevice) {
        this.defaultDevice = defaultDevice;
    }
    
    public void setDefaultDevice(String deviceId){
        this.defaultDevice = GoogleDevice.DESKTOP;
        
        if(deviceId == null){
            return;
        }
        
        try {
            this.defaultDevice = GoogleDevice.values()[Integer.parseInt(deviceId)];
        } catch(Exception ex){
        }
    }
    
    public String getDefaultLocal() {
        return defaultLocal;
    }

    public void setDefaultLocal(String defaultLocal) {
        this.defaultLocal = defaultLocal;
    }

    public String getDefaultCustomParameters() {
        return defaultCustomParameters;
    }

    public void setDefaultCustomParameters(String defaultCustomParameters) {
        this.defaultCustomParameters = defaultCustomParameters;
    }
    
}
