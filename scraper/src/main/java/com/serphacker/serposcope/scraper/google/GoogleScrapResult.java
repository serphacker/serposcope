/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google;

import java.util.List;
import java.util.Objects;


public class GoogleScrapResult {
    
    public enum Status {
        OK,
        ERROR_NETWORK,
        ERROR_CAPTCHA_INCORRECT,
        ERROR_CAPTCHA_NO_SOLVER,
        ERROR_IP_BANNED
    };
    
    public Status status;
    public List<String> urls;
    public int captchas;
    public long googleResults;    

    public GoogleScrapResult() {
    }
    
    public GoogleScrapResult(Status status, List<String> urls) {
        this.status = status;
        this.urls = urls;
    }

    public GoogleScrapResult(Status status, List<String> urls, int captchas) {
        this.status = status;
        this.urls = urls;
        this.captchas = captchas;
    }

    public GoogleScrapResult(Status status, List<String> urls, int captchas, long googleResults) {
        this.status = status;
        this.urls = urls;
        this.captchas = captchas;
        this.googleResults = googleResults;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.status == null ? 0 : (this.status.ordinal()+1) );
        hash = 73 * hash + Objects.hashCode(this.urls);
        hash = 73 * hash + this.captchas;
        hash = 73 * hash + (int) (this.googleResults ^ (this.googleResults >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoogleScrapResult other = (GoogleScrapResult) obj;
        if (this.captchas != other.captchas) {
            return false;
        }
        if (this.googleResults != other.googleResults) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        if (!Objects.equals(this.urls, other.urls)) {
            return false;
        }
        return true;
    }
    
    
}
