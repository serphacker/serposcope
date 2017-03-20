/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.google;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
//import com.serphacker.serposcope.di.ScraperFactory;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import com.serphacker.serposcope.scraper.http.proxy.ProxyRotator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import java.util.ArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author admin
 */
@RunWith(MockitoJUnitRunner.class)
public class GoogleTaskRunnableTest {

    GoogleTask taskController;
    GoogleTaskRunnable runnable;
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    Appender mockAppender;

    protected void reconfigureLogger() {
        mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.detachAppender("MOCK");
        root.addAppender(mockAppender);
    }
    
    protected void assertLogged(String msg){
        assertLogged(msg, 1);
    }
    
    protected void assertLogged(String msg, int times){
        verify(mockAppender, times(times)).doAppend(argThat(new ArgumentMatcher() {
            @Override
            public boolean matches(final Object argument) {
                String logged = ((LoggingEvent) argument).getFormattedMessage();
                return logged.equals(msg);
            }
        }));        
    }    

    @Before
    public void before() {
        reconfigureLogger();
        taskController = mock(GoogleTask.class);
        taskController.rotator = new ProxyRotator(Collections.EMPTY_LIST);
//        taskController.searchDone = new AtomicInteger();
        taskController.totalSearch = 0;        
        taskController.googleOptions = new GoogleSettings();
        
        runnable = new GoogleTaskRunnable(taskController);
        runnable.scraper = mock(GoogleScraper.class);
        when(runnable.scraper.getHttp()).thenReturn(mock(ScrapClient.class));
    }

    @Test
    public void testInterrupted() {
        Thread.currentThread().interrupt();
        runnable.run();
        assertTrue(!Thread.currentThread().isInterrupted());
        assertLogged("interrupted, aborting the thread");
    }

    @Test
    public void testNoProxy() {
        runnable.run();
        assertLogged("no more proxy, stopping the thread");
    }

    @Test
    public void testInterruptPolling() throws Exception{
        taskController.rotator.add(new BindProxy("127.0.0.1"));
        taskController.searches = new LinkedBlockingQueue<>();
        
        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(50);
        thread.interrupt();
        thread.join();
        assertLogged("interrupted while polling, aborting the thread");
    }
    
//    @Test
//    public void testShouldRun() throws Exception{
//        taskController.rotator.add(new BindProxy("127.0.0.1"));
//        taskController.searches = mock(LinkedBlockingQueue.class);
//        when(taskController.shouldStop()).thenReturn(false, false, true);
//        runnable.run();
//        
//        assertLogged("no search to do, waiting for termination", 2);
//    }
    
    @Test
    public void testUnhandledExceptionInScrap() throws Exception{
        List<ScrapProxy> proxies = Arrays.asList(new BindProxy("127.0.0.1"),new BindProxy("127.0.0.2"));
        taskController.rotator.addAll(proxies);
        taskController.searches = new LinkedBlockingQueue<>();
        taskController.searches.add(new GoogleSearch("keyword"));

        when(taskController.shouldStop()).thenReturn(false, true);
        when(runnable.scraper.scrap(any())).thenThrow(new UnsupportedOperationException("lolex"));
        
        runnable.run();
        assertLogged("unhandled exception, aborting the thread");
        
        // proxy should sent back to proxyrotator
        Assert.assertEquals(new HashSet<>(proxies), new HashSet<>(taskController.rotator.list()));
    }
    
    @Test
    public void testSucessfullSearch() throws Exception{
        List<ScrapProxy> proxies = Arrays.asList(new BindProxy("127.0.0.1"),new BindProxy("127.0.0.2"));
        taskController.rotator.addAll(proxies);
        taskController.searches = new LinkedBlockingQueue<>();
        GoogleSearch theSearch = new GoogleSearch("keyword");
        taskController.searches.add(theSearch);

        when(taskController.shouldStop()).thenReturn(false, true);
//        GoogleScraper scraper = mock(GoogleScraper.class);
        
        GoogleScrapResult scrapResult = new GoogleScrapResult(GoogleScrapResult.Status.OK, new ArrayList<>());
        
        when(runnable.scraper.scrap(any())).thenReturn(scrapResult);
        
//        taskController.scaperFactory = mock(ScraperFactory.class);
//        when(taskController.scaperFactory.getGoogleScraper(any())).thenReturn(scraper);
        
        runnable.run();
        verify(taskController, times(1)).onSearchDone(theSearch, scrapResult);
        Assert.assertEquals(new HashSet<>(proxies), new HashSet<>(taskController.rotator.list()));
        assertTrue(taskController.searches.isEmpty());
    }
    
    /*
    @Test
    public void testSucessfullSearchMultiTry() throws Exception{
        List<ScrapProxy> proxies = Arrays.asList(new BindProxy("127.0.0.1"),new BindProxy("127.0.0.2"));
        taskController.rotator.addAll(proxies);
        taskController.searches = new LinkedBlockingQueue<>();
        GoogleSearch theSearch = new GoogleSearch("keyword");
        taskController.searches.add(theSearch);

        when(taskController.shouldStop()).thenReturn(false, true);
//        GoogleScraper scraper = mock(GoogleScraper.class);
        
        GoogleScrapResult networkError = new GoogleScrapResult(GoogleScrapResult.Status.ERROR_NETWORK, new ArrayList<>());
        
        GoogleScrapResult okResult = new GoogleScrapResult(GoogleScrapResult.Status.OK, new ArrayList<>());
        
        when(runnable.scraper.scrap(any())).thenReturn(networkError, networkError, okResult);
        
//        taskController.scaperFactory = mock(ScraperFactory.class);
//        when(taskController.scaperFactory.getGoogleScraper(any())).thenReturn(scraper);
        
        runnable.run();
        assertLogged("search \"keyword\" | try : 3/3 | total search done : 0/0");
        verify(taskController, times(1)).onSearchDone(theSearch, okResult);
        Assert.assertEquals(new HashSet<>(proxies), new HashSet<>(taskController.rotator.list()));
        assertTrue(taskController.searches.isEmpty());
    }
    
    @Test
    public void testErrorMultiTry() throws Exception{
        taskController.rotator.add(new BindProxy("127.0.0.1"));
        taskController.searches = new LinkedBlockingQueue<>();
        GoogleSearch theSearch = new GoogleSearch("keyword");
        taskController.searches.add(theSearch);

        when(taskController.shouldStop()).thenReturn(false, true);
//        GoogleScraper scraper = mock(GoogleScraper.class);
        
        GoogleScrapResult networkError = new GoogleScrapResult(GoogleScrapResult.Status.ERROR_NETWORK, new ArrayList<>());
        
        
        when(runnable.scraper.scrap(any())).thenReturn(networkError);
        
//        taskController.scaperFactory = mock(ScraperFactory.class);
//        when(taskController.scaperFactory.getGoogleScraper(any())).thenReturn(scraper);
        
        runnable.run();
        assertLogged("search \"keyword\" | try : 3/3 | total search done : 0/0");
        assertLogged("scrap failed after 3 try, removing proxy:bind://127.0.0.1/");
        verify(taskController, never()).onSearchDone(any(),any());
        assertFalse(taskController.searches.isEmpty());
    }
    */
    
    @Test
    public void testProxyEviction() throws Exception{
        ScrapProxy evictableProxy = new BindProxy("127.0.0.1");
        List<ScrapProxy> proxies = Arrays.asList(evictableProxy, new BindProxy("127.0.0.2"), new BindProxy("127.0.0.3"));
        taskController.rotator.addAll(proxies);
        taskController.searches = new LinkedBlockingQueue<>();
        GoogleSearch theSearch = new GoogleSearch("keyword");
        taskController.searches.add(theSearch);

        when(taskController.shouldStop()).thenReturn(false, true);
//        GoogleScraper scraper = mock(GoogleScraper.class);
        
        GoogleScrapResult networkError = new GoogleScrapResult(GoogleScrapResult.Status.ERROR_NETWORK, new ArrayList<>());
        
        when(runnable.scraper.scrap(any())).thenReturn(networkError);
        
//        taskController.scaperFactory = mock(ScraperFactory.class);
//        when(taskController.scaperFactory.getGoogleScraper(any())).thenReturn(scraper);
        
        runnable.run();
        assertLogged("search \"keyword\" | try 1 | total search done : 0/0");
        assertLogged("scrap failed for keyword because of ERROR_NETWORK");
        verify(taskController, never()).onSearchDone(any(), any());
        assertFalse(taskController.rotator.list().contains(evictableProxy));
        assertEquals(proxies.size()-1, taskController.rotator.list().size());
        assertFalse(taskController.searches.isEmpty());
    }    
    
}
