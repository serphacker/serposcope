/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.proxy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;


/**
 * thread safe
 * @author admin
 */
public class ProxyRotator {

    final Queue<ScrapProxy> proxies = new ArrayDeque<>();

    public ProxyRotator(Collection<ScrapProxy> proxies) {
        this.proxies.addAll(proxies);
    }
    
    public boolean addAll(Collection<ScrapProxy> proxies){
        synchronized(proxies){
            return this.proxies.addAll(proxies);
        }
    }
    
    public boolean add(ScrapProxy proxy){
        synchronized(proxy){
            return proxies.add(proxy);
        }
    }
    
    public ScrapProxy poll(){
        return rotate(null);
    }
    
    public ScrapProxy rotate(ScrapProxy previousProxy){
        synchronized(proxies){
            if(previousProxy != null){
                proxies.add(previousProxy);
            }
            return proxies.poll();
        }
    }
    
    public int remaining(){
        synchronized(proxies){
            return proxies.size();
        }
    }
    
    public List<ScrapProxy> list(){
        synchronized(proxies){
            return new ArrayList<>(proxies);
        }
    } 
    
    
}
