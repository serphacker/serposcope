/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.util.Objects;


public class GoogleSearch {
    
    int id;
    String keyword;
    String tld;
    String datacenter;
    GoogleDevice device = GoogleDevice.DESKTOP;
    String local;
    String customParameters;

    public GoogleSearch() {
    }

    public GoogleSearch(int id) {
        this.id = id;
    }
    
    public GoogleSearch(String keyword) {
        this.keyword = keyword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTld() {
        return tld;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public GoogleDevice getDevice() {
        return device;
    }

    public void setDevice(GoogleDevice device) {
        if(device == null){
            device = GoogleDevice.DESKTOP;
            return;
        }
        this.device = device;
    }
    
    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.keyword);
        hash = 71 * hash + Objects.hashCode(this.tld);
        hash = 71 * hash + Objects.hashCode(this.datacenter);
        hash = 71 * hash + Objects.hashCode(this.device);
        hash = 71 * hash + Objects.hashCode(this.local);
        hash = 71 * hash + Objects.hashCode(this.customParameters);
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
        final GoogleSearch other = (GoogleSearch) obj;
        if (!Objects.equals(this.keyword, other.keyword)) {
            return false;
        }
        if (!Objects.equals(this.tld, other.tld)) {
            return false;
        }
        if (!Objects.equals(this.datacenter, other.datacenter)) {
            return false;
        }
        if (!Objects.equals(this.local, other.local)) {
            return false;
        }
        if (!Objects.equals(this.customParameters, other.customParameters)) {
            return false;
        }
        if (this.device != other.device) {
            return false;
        }
        return true;
    }
    
}
