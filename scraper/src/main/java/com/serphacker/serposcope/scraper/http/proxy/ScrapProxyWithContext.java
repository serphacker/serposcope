/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper.http.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ScrapProxyWithContext implements ScrapProxy{
    Map<String,Object> context = new ConcurrentHashMap<>();
    
    @Override
    public boolean hasAttr(String key){
        return context.containsKey(key);
    }
    
    @Override
    public void setAttr(String key, Object value){
        context.put(key, value);
    }
    
    @Override
    public <T> T getAttr(String key, Class<T> clazz){
        return (T) context.get(key);
    }
    
    @Override
    public void removeAttr(String key){
        context.remove(key);
    }
    
    @Override
    public void clearAttrs(){
        context.clear();
    }
}
