/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.proxy;

import java.util.Objects;


public class BindProxy extends ScrapProxyWithContext {
    public final String ip;

    public BindProxy(String ip) {
        this.ip = ip;
    }
    
    @Override
    public String toString() {
        return "proxy:bind://"+ ip + "/";
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.ip);
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
        final BindProxy other = (BindProxy) obj;
        if (!Objects.equals(this.ip, other.ip)) {
            return false;
        }
        return true;
    }
    
}
