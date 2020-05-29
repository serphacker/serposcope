/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper.captcha;


import java.util.LinkedHashMap;

public class CaptchaRecaptcha extends Captcha {
    
    String challenge;
    String dataS;
    String url;
    String response;
    LinkedHashMap<String, String> cookies;

    public CaptchaRecaptcha() {
    }

    public CaptchaRecaptcha(String challenge, String dataS, String url) {
        this.challenge = challenge;
        this.url = url;
        this.dataS = dataS;
    }
    
    public String getChallenge() {
        return challenge;
    }

    public String getDataS() {
        return dataS;
    }

    public LinkedHashMap<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(LinkedHashMap<String, String> cookies) {
        this.cookies = cookies;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
    
}
