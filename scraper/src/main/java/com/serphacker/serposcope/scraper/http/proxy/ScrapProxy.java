/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.proxy;


public interface ScrapProxy {
    
    public boolean hasAttr(String key);
    public void setAttr(String key, Object value);
    public  <T> T getAttr(String key, Class<T> clazz);
    public void removeAttr(String key);
    public void clearAttrs();
}
