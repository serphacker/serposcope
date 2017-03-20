/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.models.google.GoogleSettings;

@Singleton
public class GoogleOptionsDB {
    
    private final static String PAGES = "google.pages";
    private final static String RESULT_PER_PAGE = "google.result_per_page";
    private final static String MIN_PAUSE_BETWEEN_PAGE_SEC = "google.min_pause_between_page_sec";
    private final static String MAX_PAUSE_BETWEEN_PAGE_SEC = "google.max_pause_between_page_sec";    
    private final static String MAX_THREADS = "google.maxThreads";
    private final static String FETCH_RETRY = "google.fetchRetry";    
    
    private final static String DEFAULT_DATACENTER = "google.default_datacenter";
    private final static String DEFAULT_DEVICE = "google.default.device";
    private final static String DEFAULT_LOCAL = "google.default.local";
    private final static String DEFAULT_TLD = "google.default.tld";
    private final static String DEFAULT_CUSTOM_PARAMETERS = "google.default.custom";
    
    @Inject
    ConfigDB configDB;
    
    public GoogleSettings get(){
        GoogleSettings options = new GoogleSettings();
        
        options.setPages(configDB.getInt(PAGES, options.getPages()));
        options.setResultPerPage(configDB.getInt(RESULT_PER_PAGE, options.getResultPerPage()));
        options.setMinPauseBetweenPageSec(configDB.getInt(MIN_PAUSE_BETWEEN_PAGE_SEC, options.getMinPauseBetweenPageSec()));
        options.setMaxPauseBetweenPageSec(configDB.getInt(MAX_PAUSE_BETWEEN_PAGE_SEC, options.getMaxPauseBetweenPageSec()));        
        options.setMaxThreads(configDB.getInt(MAX_THREADS, options.getMaxThreads()));
        options.setFetchRetry(configDB.getInt(FETCH_RETRY, options.getFetchRetry()));
        
        options.setDefaultDatacenter(configDB.get(DEFAULT_DATACENTER, options.getDefaultDatacenter()));
        options.setDefaultDevice(configDB.get(DEFAULT_DEVICE, null));
        options.setDefaultLocal(configDB.get(DEFAULT_LOCAL, options.getDefaultLocal()));
        options.setDefaultTld(configDB.get(DEFAULT_TLD, options.getDefaultTld()));
        options.setDefaultCustomParameters(configDB.get(DEFAULT_CUSTOM_PARAMETERS, options.getDefaultCustomParameters()));
        
        return options;
    }
    
    public void update(GoogleSettings opts){
        
        GoogleSettings def = new GoogleSettings();

        // scraping
        configDB.updateInt(PAGES, nullIfDefault(opts.getPages(), def.getPages()));
        configDB.updateInt(RESULT_PER_PAGE, nullIfDefault(opts.getResultPerPage(), def.getResultPerPage()));
        configDB.updateInt(MIN_PAUSE_BETWEEN_PAGE_SEC, nullIfDefault(opts.getMinPauseBetweenPageSec(), def.getMinPauseBetweenPageSec()));
        configDB.updateInt(MAX_PAUSE_BETWEEN_PAGE_SEC, nullIfDefault(opts.getMaxPauseBetweenPageSec(), def.getMaxPauseBetweenPageSec()));
        configDB.updateInt(MAX_THREADS, nullIfDefault(opts.getMaxThreads(), def.getMaxThreads()));
        configDB.updateInt(FETCH_RETRY, nullIfDefault(opts.getFetchRetry(), def.getFetchRetry()));

        // search
        configDB.update(DEFAULT_DATACENTER, nullIfDefault(opts.getDefaultDatacenter(), def.getDefaultDatacenter()));
        configDB.updateInt(DEFAULT_DEVICE, nullIfDefault(opts.getDefaultDevice().ordinal(), def.getDefaultDevice().ordinal()));
        configDB.update(DEFAULT_LOCAL, nullIfDefault(opts.getDefaultLocal(), def.getDefaultLocal()));
        configDB.update(DEFAULT_TLD, nullIfDefault(opts.getDefaultTld(), def.getDefaultTld()));
        configDB.update(DEFAULT_CUSTOM_PARAMETERS, nullIfDefault(opts.getDefaultCustomParameters(),def.getDefaultCustomParameters()));
        
    }
    
    protected Integer nullIfDefault(Integer value, Integer def){
        return (Integer)nullIfDefaultObject(value, def);
    }
    
    protected String nullIfDefault(String value, String def){
        return (String)nullIfDefaultObject(value, def);
    }
    
    protected Object nullIfDefaultObject(Object value, Object def){
        if(def == null && value != null){
            return value;
        }
        
        if(value == null){
            return null;
        }
        
        if(def.equals(value)){
            return null;
        }
        return value;
    }
    
}
