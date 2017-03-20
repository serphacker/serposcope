/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google;

import java.util.List;


public class GoogleScrapResult {
    
    public enum Status {
        OK,
        ERROR_NETWORK,
        ERROR_CAPTCHA_INCORRECT,
        ERROR_CAPTCHA_NO_SOLVER,
        ERROR_IP_BANNED
    };

    public GoogleScrapResult(Status status, List<String> urls) {
        this.status = status;
        this.urls = urls;
    }

    public GoogleScrapResult(Status status, List<String> urls, int captchas, int hits) {
        this.status = status;
        this.urls = urls;
        this.captchas = captchas;
        this.hits = hits;
    }
    
    public Status status;
    public List<String> urls;
    public int captchas;
    public int hits;
}
