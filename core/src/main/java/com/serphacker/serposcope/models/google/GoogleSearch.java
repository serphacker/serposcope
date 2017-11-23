/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import com.serphacker.serposcope.scraper.google.GoogleCountryCode;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.util.Objects;


public class GoogleSearch {
    
    int id;
    String keyword;
    GoogleCountryCode country = GoogleCountryCode.__;
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

    public GoogleCountryCode getCountry() {
        return country;
    }

    public void setCountry(GoogleCountryCode country) {
        if(country == null){
            country = GoogleCountryCode.__;
        }
        this.country = country;
    }
    
    public void setCountry(String country){
        if(country != null && !country.isEmpty()){
            try {
                this.country = GoogleCountryCode.valueOf(country.toUpperCase());
                return;
            } catch(Exception ex){
            }
        }
        
        this.country = GoogleCountryCode.__;
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
        hash = 71 * hash + Objects.hashCode(this.country);
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
        if (!Objects.equals(this.country, other.country)) {
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
