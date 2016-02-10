/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.google;

import com.google.inject.Inject;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import java.util.List;
import org.apache.http.cookie.Cookie;

public class GoogleTaskRunnable implements Runnable {

    protected static final Logger LOG = LoggerFactory.getLogger(GoogleTaskRunnable.class);
//    public final static int MAX_FETCH_TRY = 3;

    GoogleTask controller;
    
    GoogleScraper scraper;

    public GoogleTaskRunnable(GoogleTask controller) {
        this.controller = controller;
        scraper = controller.genScraper();
    }
    
    boolean cookiesStickToProxy = true;

    @Override
    public void run() {
        GoogleSearch search = null;
        ScrapProxy proxy = null;
        int searchTry = 0;
        
        LOG.info("google thread started");
        try {
            
            while (!controller.shouldStop()) {

                if (Thread.currentThread().interrupted()) {
                    LOG.error("interrupted, aborting the thread");
                    break;
                }
                
                if(cookiesStickToProxy && proxy != null){
                    List<Cookie> cookies = scraper.getHttp().getCookies();
                    if(cookies != null){
                        proxy.setAttr("cookies", cookies);
                    }
                }

                proxy = controller.rotator.rotate(proxy);
                if (proxy == null) {
                    LOG.warn("no more proxy, stopping the thread");
                    break;
                }
                scraper.getHttp().setProxy(proxy);
                
                if(cookiesStickToProxy){
                    scraper.getHttp().clearCookies();
                    List<Cookie> cookies = proxy.getAttr("cookies", List.class);
                    if(cookies != null){
                        scraper.getHttp().addCookies(proxy.getAttr("cookies", List.class));
                    }
                }

                if(search == null){
                    try {
                        search = controller.searches.poll(1, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        LOG.error("interrupted while polling, aborting the thread");
                        break;
                    }
                    searchTry = 0;
                }

                if (search == null) {
//                    LOG.trace("no search to do, waiting for termination");
                    continue;
                }

                ++searchTry;
                GoogleScrapResult res = null;
                LOG.info("search \"{}\" | try {} | total search done : {}/{}",
                    new Object[]{search.getKeyword(), searchTry, controller.searchDone.get(), controller.totalSearch});

                try {
                    res = scraper.scrap(getScrapConfig(controller.googleOptions, search));
                } catch (InterruptedException ex) {
                    LOG.error("interrupted while scraping, aborting the thread");
                    break;
                }
                
                if( res.captchas > 0 ){
                    controller.incCaptchaCount(res.captchas);
                }

                if (res.status != OK) {
                    LOG.warn("scrap failed for {} because of {}", search.getKeyword(), res.status);
                    proxy = null;
                    continue;
                }

                controller.onSearchDone(search, res);
                search = null;
            }
            
        } catch (Exception ex) {
            LOG.error("unhandled exception, aborting the thread", ex);
            ex.printStackTrace();
        } finally {
            if (proxy != null){
                controller.rotator.add(proxy);
            }
            if (search != null) {
                controller.searches.add(search);
            }
        }
        LOG.info("google thread stopped");
    }

    protected GoogleScrapSearch getScrapConfig(GoogleSettings options, GoogleSearch search) {
        GoogleScrapSearch scrapSearch = new GoogleScrapSearch();
        
        // options.getFetchRetry(); // TODO
        scrapSearch.setPagePauseMS(options.getMinPauseBetweenPageSec()*1000l, options.getMaxPauseBetweenPageSec()*1000l);
        scrapSearch.setPages(options.getPages());
        scrapSearch.setResultPerPage(options.getResultPerPage());
        
        scrapSearch.setCustomParameters(search.getCustomParameters());
        scrapSearch.setDatacenter(search.getDatacenter());
        scrapSearch.setDevice(search.getDevice());
        scrapSearch.setKeyword(search.getKeyword());
        scrapSearch.setTld(search.getTld());
        scrapSearch.setLocal(search.getLocal());
        
        return scrapSearch;
    }
    public static final long serialVersionUID = 0L;

}
