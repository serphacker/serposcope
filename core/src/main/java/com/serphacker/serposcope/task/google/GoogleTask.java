/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.google;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.di.CaptchaSolverFactory;
import com.serphacker.serposcope.di.ScrapClientFactory;
//import com.serphacker.serposcope.di.ScraperFactory;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleSerpEntry;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.DirectNoProxy;
import com.serphacker.serposcope.scraper.http.proxy.ProxyRotator;
import com.serphacker.serposcope.task.AbstractTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;

import java.util.stream.Collectors;

import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.models.google.GoogleBest;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class GoogleTask extends AbstractTask {

    protected static final Logger LOG = LoggerFactory.getLogger(GoogleTask.class);
    
    GoogleScraperFactory googleScraperFactory;
    CaptchaSolverFactory captchaSolverFactory;
    ScrapClientFactory scrapClientFactory;
    
    GoogleDB googleDB;
    ProxyRotator rotator;

    Run previousRun;
    final Map<Short,Integer> previousRunsByDay = new ConcurrentHashMap<>();
    final Map<Integer,List<GoogleTarget>> targetsByGroup = new ConcurrentHashMap<>();
    final Map<Integer,GoogleTargetSummary> summariesByTarget = new ConcurrentHashMap<>();
    
    LinkedBlockingQueue<GoogleSearch> searches;
    GoogleSettings googleOptions;
    protected final AtomicInteger searchDone = new AtomicInteger();
    final AtomicInteger captchaCount = new AtomicInteger();
    
    Thread[] threads;
    volatile int totalSearch;
    volatile boolean interrupted;
    
    CaptchaSolver solver;
    String httpUserAgent;
    int httpTimeoutMS;
    boolean updateRun;
    boolean shuffle = true;
    
    @Inject
    public GoogleTask(
        GoogleScraperFactory googleScraperFactory,
        CaptchaSolverFactory captchaSolverFactory,
        ScrapClientFactory scrapClientFactory,
        GoogleDB googleDB,
        @Assisted Run run
    ){
        super(run);
        this.googleScraperFactory = googleScraperFactory;
        this.captchaSolverFactory = captchaSolverFactory;
        this.scrapClientFactory = scrapClientFactory;
        this.googleDB = googleDB;
        this.updateRun = run.getId() == 0 ? false : true;
        
        httpUserAgent = ScrapClient.DEFAULT_USER_AGENT;
        httpTimeoutMS = ScrapClient.DEFAULT_TIMEOUT_MS;
    }    
    

    @Override
    public Run.Status doRun() {
        solver = initializeCaptchaSolver();
        googleOptions = googleDB.options.get();

        initializeSearches();
        initializePreviousRuns();
        initializeTargets();
        
        
        int nThread = googleOptions.getMaxThreads();
        List<ScrapProxy> proxies = baseDB.proxy.list().stream().map(Proxy::toScrapProxy).collect(Collectors.toList());
        
        if(proxies.isEmpty()){
            LOG.warn("no proxy configured, using direct connection");
            proxies.add(new DirectNoProxy());
        }
        
        if( proxies.size() < nThread ){
            LOG.info("less proxy ({}) than max thread ({}), setting thread number to {}", 
                new Object[]{proxies.size(), nThread, nThread});
            nThread = proxies.size();
        }
        
        rotator = new ProxyRotator(proxies);
        totalSearch = searches.size();
        
        startThreads(nThread);
        waitForThreads();
        
        finalizeSummaries();
        
        if(solver != null){
            try {solver.close();} catch (IOException ex) {}
        }
        
        LOG.warn("{} proxies failed during the task", proxies.size() - rotator.list().size());
        
        int remainingSearch = totalSearch - searchDone.get();
        if(remainingSearch > 0){
            run.setErrors(remainingSearch);
            LOG.warn("{} searches have not been checked", remainingSearch);
            return Run.Status.DONE_WITH_ERROR;
        }
        
        return Run.Status.DONE_SUCCESS;
    }
    
    protected void startThreads(int nThread){
        threads = new Thread[nThread];
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread(new GoogleTaskRunnable(this), "google-" + iThread);
            threads[iThread].start();
        }        
    }
    
    protected void waitForThreads(){
        while(true){
            try {
                for (Thread thread : threads) {
                    thread.join();
                }
                return;
            }catch(InterruptedException ex){
                interruptThreads();
            }
        }
    }
    
    protected void interruptThreads(){
        interrupted = true;
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }
    
    protected boolean shouldStop(){
        if(searchDone.get() == totalSearch){
            return true;
        }
        
        if(interrupted){
            return true;
        }
        
        return false;
    }
    
    protected void incCaptchaCount(int captchas){
        run.setCaptchas(captchaCount.addAndGet(captchas));
        baseDB.run.updateCaptchas(run);
    }
    
    protected void onSearchDone(GoogleSearch search, GoogleScrapResult res){
        insertSearchResult(search, res);
        incSearchDone();
    }
    
    protected void incSearchDone(){
        run.setProgress((int) (((float)searchDone.incrementAndGet()/(float)totalSearch)*100f) );
        baseDB.run.updateProgress(run);
    }
    
    protected void insertSearchResult(GoogleSearch search, GoogleScrapResult res) {
        Map<Short, GoogleSerp> history = getHistory(search);

        GoogleSerp serp = new GoogleSerp(run.getId(), search.getId(), run.getStarted());
        for (String url : res.urls) {
            GoogleSerpEntry entry = new GoogleSerpEntry(url);
            entry.fillPreviousPosition(history);
            serp.addEntry(entry);
        }
        googleDB.serp.insert(serp);

        List<Integer> groups = googleDB.search.listGroups(search);
        for (Integer group : groups) {
            List<GoogleTarget> targets = targetsByGroup.get(group);
            if (targets == null) {
                continue;
            }
            for (GoogleTarget target : targets) {
                int best = googleDB.rank.getBest(group, target.getId(), search.getId()).getRank();
                int rank = GoogleRank.UNRANKED;
                String rankedUrl = null;
                for (int i = 0; i < res.urls.size(); i++) {
                    if (target.match(res.urls.get(i))) {
                        rankedUrl = res.urls.get(i);
                        rank = i + 1;
                        break;
                    }
                }
                
                int previousRank = GoogleRank.UNRANKED;
                if (previousRun != null) {
                    previousRank = googleDB.rank.get(previousRun.getId(), group, target.getId(), search.getId());
                }
                
                GoogleRank gRank = new GoogleRank(run.getId(), group, target.getId(), search.getId(), rank, previousRank, res.hits, rankedUrl);
                googleDB.rank.insert(gRank);
                
                GoogleTargetSummary summary = summariesByTarget.get(target.getId());
                summary.addRankCandidat(gRank);
                
                if(rank != GoogleRank.UNRANKED && rank <= best){
                    googleDB.rank.insertBest(new GoogleBest(group, target.getId(), search.getId(), rank, run.getStarted(), rankedUrl));
                }
                
            	String url = baseDB.config.getConfig().getTaskNotificationUrl();
                
                //LOG.info("serposcope-agent sending notification to: "+url);
                
                if(url != null && !url.isEmpty())
                {
                	try{
	                	LOG.info("serposcope-agent sending notification to: "+url);
	                	CloseableHttpClient httpNotifiyClient = HttpClientBuilder.create().build();
	                	HttpPost post = new HttpPost(url);
		
	                	// add header
	                	post.setHeader("User-Agent", "serposcope-agent");
		
	                	List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
	                	urlParameters.add(new BasicNameValuePair("keyword", search.getKeyword()));
	                	urlParameters.add(new BasicNameValuePair("rank", rank+""));
	                	urlParameters.add(new BasicNameValuePair("best", best+""));
	                	urlParameters.add(new BasicNameValuePair("hits", res.hits+""));
	                	urlParameters.add(new BasicNameValuePair("url", rankedUrl));
	                	urlParameters.add(new BasicNameValuePair("previousRank", previousRank+""));
	                	urlParameters.add(new BasicNameValuePair("target", target.getPattern()+""));
	                	//urlParameters.add(new BasicNameValuePair("try", searchTry+""));
	                	//urlParameters.add(new BasicNameValuePair("done", controller.getSearchDone()+""));
	                	//urlParameters.add(new BasicNameValuePair("total", controller.totalSearch+""));
		
	                	post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
		
	                	HttpResponse response = httpNotifiyClient.execute(post);
	                	LOG.info("serposcope-agent response code : "
		                                + response.getStatusLine().getStatusCode());
		
	                	BufferedReader rd = new BufferedReader(
		                        new InputStreamReader(response.getEntity().getContent()));
		
	                	StringBuffer result = new StringBuffer();
	                	String line = "";
		                while ((line = rd.readLine()) != null) {
		                	result.append(line);
		                }
                	}
                	catch (Exception e)
                	{
                		LOG.error("serposcope-agent unhandled exception", e);
                	}
                }
                
            }
        }
    }    
    
    protected void initializeSearches() {
        List<GoogleSearch> searchList;
        if(updateRun){
            searchList = googleDB.search.listUnchecked(run.getId());
        } else {
            searchList = googleDB.search.list();
        }
        if(shuffle){
            Collections.shuffle(searchList);
        }
        searches = new LinkedBlockingQueue<>(searchList);
        LOG.info("{} searches to do", searches.size());
    }
    
    protected void initializeTargets() {
        Map<Integer, Integer> previousScorePercent = new HashMap<>();
        
        if(previousRun != null){
            previousScorePercent = googleDB.targetSummary.getPreviousScore(previousRun.getId());
        } 
        
        List<GoogleTarget> targets = googleDB.target.list();
        for (GoogleTarget target : targets) {
            targetsByGroup.putIfAbsent(target.getGroupId(), new ArrayList<>());
            targetsByGroup.get(target.getGroupId()).add(target);
            summariesByTarget.put(
                target.getId(), 
                new GoogleTargetSummary(target.getGroupId(), target.getId(), run.getId(), previousScorePercent.getOrDefault(target.getId(), 0))
            );
        }
        
        if(updateRun){
            List<GoogleTargetSummary> summaries = googleDB.targetSummary.list(run.getId());
            for (GoogleTargetSummary summary : summaries) {
                summariesByTarget.put(summary.getTargetId(), summary);
            }
        }
    }
    
    protected void initializePreviousRuns(){
        previousRun = baseDB.run.findPrevious(run.getId());
        if(previousRun == null){
            return;
        }
        
        short[] days = new short[]{1,7,30,90};
        
        for (short day : days) {
            List<Run> pastRuns = baseDB.run.findByDay(run.getModule(), run.getDay().minusDays(day));
            if(!pastRuns.isEmpty()){
                previousRunsByDay.put(day, pastRuns.get(0).getId());
            }
        }
    }
    
    protected Map<Short,GoogleSerp> getHistory(GoogleSearch search){
        Map<Short,GoogleSerp> history = new HashMap<>();
        
        for (Map.Entry<Short, Integer> entry : previousRunsByDay.entrySet()) {
            GoogleSerp serp = googleDB.serp.get(entry.getValue(), search.getId());
            if(serp != null){
                history.put(entry.getKey(), serp);
            }
        }
        return history;
    }    
    
    protected void finalizeSummaries(){
        Map<Integer, Integer> searchCountByGroup = googleDB.search.countByGroup();
        for (GoogleTargetSummary summary : summariesByTarget.values()) {
            summary.computeScoreBP(searchCountByGroup.getOrDefault(summary.getGroupId(), 0));
        }
        googleDB.targetSummary.insert(summariesByTarget.values());
    }
    
    protected GoogleScraper genScraper(){
        return googleScraperFactory.get(
            scrapClientFactory.get(httpUserAgent, httpTimeoutMS),
            solver
        );
    }

    @Override
    protected void onCrash(Exception ex) {
        
    }
    
    protected final CaptchaSolver initializeCaptchaSolver(){
    	LOG.info("baseDB.config.getConfig() - {}", baseDB.config.getConfig().getDbcUser());
        solver = captchaSolverFactory.get(baseDB.config.getConfig());
        if(solver != null){
            if(!solver.init()){
                LOG.info("failed to init captcha solver {}", solver.getFriendlyName());
                return null;
            }
            return solver;
        } else {
            LOG.info("no captcha service configured");
            return null;
        }
        
    }
    
    int getSearchDone(){
        return searchDone != null ? searchDone.get() : 0;
    }

}
