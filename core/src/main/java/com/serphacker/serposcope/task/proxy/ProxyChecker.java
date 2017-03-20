/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.task.proxy;

import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.LoggerFactory;

public class ProxyChecker extends Thread {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProxyChecker.class);
    
    static Pattern PATTERN_IP = Pattern.compile("Your IP: ([0-9.:\\[\\]]+)");
    
    BaseDB db;
    int nThread = 50;
    int timeoutMS = 20000;
    String judgeUrl = "https://proxychecker.serphacker.com/";
    
    AtomicInteger checked = new AtomicInteger();
    volatile int totalProxies = 0;

    public ProxyChecker(BaseDB db, int threads, int timeoutMS){
        this.db = db;
        this.nThread = threads;
        this.timeoutMS = timeoutMS;
    }
    
    
    public ProxyChecker(BaseDB db) {
        this.db = db;
    }
    
    @Override
    public void run(){
        
        LOG.info("starting proxy checking task, threads = {}, timeout in MS = {}", nThread, timeoutMS);
        
        long start = System.currentTimeMillis();
        
        List<Proxy> proxies = db.proxy.list();
        if(proxies == null || proxies.isEmpty()){
            LOG.debug("no proxy to check");
            return;
        }
        
        totalProxies = proxies.size();
        
        ExecutorService executor = Executors.newFixedThreadPool(nThread);
        db.proxy.updateStatus(Proxy.Status.UNCHECKED, proxies.stream().map((t) -> t.getId()).collect(Collectors.toList()));
        
        for (Proxy proxy : proxies) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    ScrapClient cli = new ScrapClient();
                    
                    cli.setTimeout(timeoutMS);
                    ScrapProxy scrapProxy = proxy.toScrapProxy();
                    cli.setProxy(scrapProxy);
                    
                    LOG.info("checking {}", scrapProxy);
                    
                    Proxy.Status proxyStatus = Proxy.Status.ERROR;
                    
//                    try{Thread.sleep(30000l);}catch(Exception ex){}
                    
                    int httpStatus = cli.get(judgeUrl);
                    if(httpStatus == 200 && cli.getContentAsString() != null){
                        Matcher matcher = PATTERN_IP.matcher(cli.getContentAsString());
                        if(matcher.find()){
                            proxy.setRemoteip(matcher.group(1));
                            proxyStatus = Proxy.Status.OK;
                        }
                    }
                    
                    proxy.setStatus(proxyStatus);
                    proxy.setLastCheck(LocalDateTime.now());
                    db.proxy.update(proxy);
                    
                    checked.incrementAndGet();
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            executor.shutdownNow();
        }
        LOG.info("proxy checking finished in {}",DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-start));
    }
    
    public int getProgress(){
        return (int) (((float)checked.get()/(float)totalProxies)*100f);
    }
    
}
