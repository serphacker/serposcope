/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.google;

import com.serphacker.serposcope.scraper.google.scraper.FakeGScraper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.serphacker.serposcope.db.google.*;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.di.TaskFactory;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run.Mode;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleSerpEntry;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import com.serphacker.serposcope.scraper.google.scraper.RandomGScraper;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleTaskIT extends AbstractDBIT {

    public GoogleTaskIT() {
    }
    
    @Inject
    TaskFactory taskFactory;

    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;

    @Override
    protected List<Module> getModule() {
        List<Module> modules = super.getModule();
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(GoogleScraperFactory.class).toInstance(getGSF());
                install(new FactoryModuleBuilder().build(TaskFactory.class));
            }
        });
        return modules;
    }
    
    GoogleScraperFactory gsf = null;
    GoogleScraperFactory getGSF(){
        if(gsf != null){
            return gsf;
        }
        return (GoogleScraperFactory) (ScrapClient http, CaptchaSolver solver) -> new FakeGScraper(http, solver);
    }
    
    
    GoogleSettings options;
    public void initialize() {
        
        User user = new User();
        user.setAdmin(true);
        user.setEmail("x@x.com");
        try {
            user.setPassword("x@x.com");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(GoogleTaskIT.class.getName()).log(Level.SEVERE, null, ex);
        }
        baseDB.user.insert(user);
        
        options = googleDB.options.get();
        options.setPages(10);
        options.setResultPerPage(10);
        options.setMinPauseBetweenPageSec(0);
        options.setMaxPauseBetweenPageSec(0);
        googleDB.options.update(options);
        
        // fruits
        Group grpFruit = new Group(Group.Module.GOOGLE, "fruits");
        baseDB.group.insert(grpFruit);
        
        List<GoogleSearch> searchesFruit = new ArrayList<>();
        searchesFruit.add(new GoogleSearch("banane"));
        searchesFruit.add(new GoogleSearch("pomme"));
        searchesFruit.add(new GoogleSearch("peche"));
        
        googleDB.search.insert(searchesFruit, grpFruit.getId());
        
        // legumes
        Group grpLegume = new Group(Group.Module.GOOGLE, "legumes");
        baseDB.group.insert(grpLegume);        
        
        List<GoogleSearch> searchesLegume = new ArrayList<>();
        searchesLegume.add(new GoogleSearch("haricot"));
        searchesLegume.add(new GoogleSearch("épinard"));
        searchesLegume.add(new GoogleSearch("endive"));
        
        googleDB.search.insert(searchesLegume, grpLegume.getId());        
        
        // both
        Group grpBoth = new Group(Group.Module.GOOGLE, "both");
        baseDB.group.insert(grpBoth);    
        
        List<GoogleSearch> searchesBoth = new ArrayList<>();
        
        searchesBoth.add(new GoogleSearch(googleDB.search.getId(new GoogleSearch("haricot"))));
        searchesBoth.add(new GoogleSearch(googleDB.search.getId(new GoogleSearch("peche"))));
        
        googleDB.search.insert(searchesBoth, grpBoth.getId());
        
        
        googleDB.target.insert(Arrays.asList(
            new GoogleTarget(grpFruit.getId(), "www.site1.com", GoogleTarget.PatternType.REGEX, "^https?://www\\.site1\\.com/"),
            new GoogleTarget(grpFruit.getId(), "www.site20.com", GoogleTarget.PatternType.REGEX,"^https?://www\\.site20\\.com/"),
            new GoogleTarget(grpLegume.getId(), "www.site45.com", GoogleTarget.PatternType.REGEX, "^https?://www\\.site45\\.com/"),
            new GoogleTarget(grpLegume.getId(), "www.site1.com", GoogleTarget.PatternType.REGEX,"^https?://www\\.site1\\.com/")
        ));
    }
    
    @Test
    public void testSingleRun() throws Exception {
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
     
        List<GoogleSearch> grpFruit = googleDB.search.listByGroup(Arrays.asList(1));
        for (GoogleSearch search : grpFruit) {
            GoogleSerp serp = googleDB.serp.get(task.getRun().getId(), search.getId());
            assertEquals(options.getResultPerPage()*options.getPages(), serp.getEntries().size());
        }
    }    
    
    @Test
    public void testSingleRunScrapError() throws Exception {
        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        return new GoogleScrapResult(GoogleScrapResult.Status.ERROR_IP_BANNED, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }
    
    
    @Test
    public void testDeadLoop1() throws Exception {
        for (int i = 0; i < 100; i++) {
            baseDB.proxy.insert(Arrays.asList(new Proxy(new BindProxy("127.0.0." + i))));            
        }

        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    Random r= new Random();
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        GoogleScrapResult.Status status = r.nextBoolean() ? 
                            GoogleScrapResult.Status.ERROR_IP_BANNED : GoogleScrapResult.Status.OK;
                        return new GoogleScrapResult(status, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }    
    
    @Test
    public void testDeadLoop2() throws Exception {
        for (int i = 0; i < 1; i++) {
            baseDB.proxy.insert(Arrays.asList(new Proxy(new BindProxy("127.0.0." + i))));            
        }

        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    Random r= new Random();
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        GoogleScrapResult.Status status = r.nextBoolean() ? 
                            GoogleScrapResult.Status.ERROR_IP_BANNED : GoogleScrapResult.Status.OK;
                        return new GoogleScrapResult(status, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }    
    
    @Test
    public void testDeadLoop1_MT() throws Exception {
        initialize();
        for (int i = 0; i < 200; i++) {
            baseDB.proxy.insert(Arrays.asList(new Proxy(new BindProxy("127.0.0." + i))));            
        }
        
        GoogleSettings settings = googleDB.options.get();
        settings.setMaxThreads(10);
        googleDB.options.update(settings);
        
        
        for (int i = 0; i < 50; i++) {
            googleDB.search.insert(Arrays.asList(new GoogleSearch("search#" + i)), 1);
        }

        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    Random r= new Random();
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        GoogleScrapResult.Status status = r.nextBoolean() ? 
                            GoogleScrapResult.Status.ERROR_IP_BANNED : GoogleScrapResult.Status.OK;
                        return new GoogleScrapResult(status, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }
    
    @Test
    public void testDeadLoop2_MT() throws Exception {
        initialize();
        for (int i = 0; i < 50; i++) {
            baseDB.proxy.insert(Arrays.asList(new Proxy(new BindProxy("127.0.0." + i))));            
        }
        
        GoogleSettings settings = googleDB.options.get();
        settings.setMaxThreads(10);
        googleDB.options.update(settings);
        
        for (int i = 0; i < 50; i++) {
            googleDB.search.insert(Arrays.asList(new GoogleSearch("search#" + i)), 1);
        }

        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    Random r= new Random();
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        GoogleScrapResult.Status status = r.nextBoolean() ? 
                            GoogleScrapResult.Status.ERROR_IP_BANNED : GoogleScrapResult.Status.OK;
                        return new GoogleScrapResult(status, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }    
    
    @Test
    public void testSingleRunScrapErrorMultiProxy() throws Exception {
        baseDB.proxy.insert(Arrays.asList(
            new Proxy(new BindProxy("127.0.0.1")),
            new Proxy(new BindProxy("127.0.0.2")),
            new Proxy(new BindProxy("127.0.0.3")),
            new Proxy(new BindProxy("127.0.0.4")),
            new Proxy(new BindProxy("127.0.0.5"))
        ));
        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new GoogleScraper(http, solver){
                    @Override
                    public GoogleScrapResult scrap(GoogleScrapSearch config) throws InterruptedException {
                        return new GoogleScrapResult(GoogleScrapResult.Status.ERROR_IP_BANNED, new ArrayList<>());
                    }
                };
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.now().withNano(0));
        task.run();
    }    
    
    @Test
    public void testMultiRunMultiProxy() throws Exception {
        initialize();
        baseDB.proxy.insert(Arrays.asList(
            new Proxy(new BindProxy("127.0.0.1")),
            new Proxy(new BindProxy("127.0.0.2")),
            new Proxy(new BindProxy("127.0.0.3")),
            new Proxy(new BindProxy("127.0.0.4")),
            new Proxy(new BindProxy("127.0.0.5"))
        ));        
        
        int days = 10;
        
        GoogleTask task = null;
        LocalDateTime runDate = LocalDateTime.of(2010,10,10,10,10);
        for (int i = 0; i < days; i++) {
            task = taskFactory.create(Mode.CRON, runDate.plusDays(i));
            task.run();
        }
     
        // check the SERP
        List<GoogleSearch> grpFruit = googleDB.search.listByGroup(Arrays.asList(1));
        for (GoogleSearch search : grpFruit) {
            GoogleSerp serp = googleDB.serp.get(task.getRun().getId(), search.getId());
            System.out.println(search.getKeyword());
            for (int position = 0; position < serp.getEntries().size(); position++) {
                entryToString(serp.getEntries().get(position), position+1);
            }
        }
        
        // check the ranking
        List<GoogleTarget> targets = googleDB.target.list();
        for (GoogleTarget target : targets) {
            System.out.println(target.getName());
            List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(target.getGroupId()));
            for (GoogleSearch search : searches) {
                System.out.println("\t" + search.getKeyword() + "|" + 
                    googleDB.rank.get(task.getRun().getId(), target.getGroupId(), target.getId(), search.getId())
                );
            }
        }
        
    }    
    
//    protected String getDbUrl(){
//        return "jdbc:h2:/tmp/oxo;AUTO_SERVER=TRUE;MODE=MySQL";
//    }    
    
    @Test
    public void testPreviousRankings() throws Exception {
        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new RandomGScraper(http, solver);
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);        
        
        
        Map<Integer,Map<Integer, Integer>> mapOfPreviousRankings = new HashMap<>();
        
        
        initialize();
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 10, 0, 0).withNano(0));
        task.run();
        
        // check the ranking
        List<GoogleTarget> targets = googleDB.target.list();
        for (GoogleTarget target : targets) {
            Map<Integer, Integer> previousRanks = new HashMap<>();
            mapOfPreviousRankings.put(target.getId(), previousRanks);
            List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(target.getGroupId()));
            
            System.out.println(target.getName() + "|" + target.getGroupId());
            for (GoogleSearch search : searches) {
                GoogleRank fullRank = googleDB.rank.getFull(task.getRun().getId(), target.getGroupId(), target.getId(), search.getId());
                previousRanks.put(search.getId(), (int)fullRank.rank);
                
                System.out.println("\t" + search.getKeyword() + "|p:" + fullRank.previousRank + "|n:" + fullRank.rank);
            }
        }
        
        task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 11, 0, 0).withNano(0));
        task.run();        
        
        // check the ranking
        targets = googleDB.target.list(/*Arrays.asList(specificGroup)*/);
        for (GoogleTarget target : targets) {
            Map<Integer, Integer> previousRanks = mapOfPreviousRankings.get(target.getId());
            List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(target.getGroupId()));
            
            System.out.println(target.getName() + "|" + target.getGroupId());
            for (GoogleSearch search : searches) {
                GoogleRank fullRank = googleDB.rank.getFull(task.getRun().getId(), target.getGroupId(), target.getId(), search.getId());
                assertEquals((int)previousRanks.get(search.getId()), fullRank.previousRank);
                previousRanks.put(search.getId(), (int)fullRank.rank);
                
                System.out.println("\t" + search.getKeyword() + "|p:" + fullRank.previousRank + "|n:" + fullRank.rank);
            }
        }
        
        task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 12, 0, 0).withNano(0));
        task.run();
        
        // check the ranking
        targets = googleDB.target.list();
        for (GoogleTarget target : targets) {
            Map<Integer, Integer> previousRanks = mapOfPreviousRankings.get(target.getId());
            List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(target.getGroupId()));
            
            System.out.println(target.getName() + "|" + target.getGroupId());
            for (GoogleSearch search : searches) {
                GoogleRank fullRank = googleDB.rank.getFull(task.getRun().getId(), target.getGroupId(), target.getId(), search.getId());
                assertEquals((int)previousRanks.get(search.getId()), fullRank.previousRank);
                
                System.out.println("\t" + search.getKeyword() + "|p:" + fullRank.previousRank + "|n:" + fullRank.rank);
            }
        }
        
        
    } 


    @Test
    public void testRescan() throws Exception {
        gsf = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
                return new RandomGScraper(http, solver);
            }
        };
        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);     
        
        initialize();
//        googleDB.target.delete(2);
//        googleDB.target.delete(3);
//        googleDB.target.delete(4);        

        List<Group> allGroups = baseDB.group.list(Group.Module.GOOGLE);
        List<GoogleTarget> allTargets = googleDB.target.list();
        
        GoogleTask task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 10, 0, 0).withNano(0));
        task.run();
        task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 11, 0, 0).withNano(0));
        task.run();        
        task = taskFactory.create(Mode.CRON, LocalDateTime.of(2010, 10, 12, 0, 0).withNano(0));
        task.run();
        
        List<GoogleTargetSummary> originalSummaries = googleDB.targetSummary.list(task.getRun().getId());
        for (GoogleTargetSummary originalSummary : originalSummaries) {
            System.out.println(originalSummary);
        }
        
        for (GoogleTarget target : allTargets) {
            googleDB.targetSummary.deleteByTarget(target.getId());
        }        
        System.out.println("rescaning #1");
        for (Group group : allGroups) {
            List<GoogleSearch> searchRescan = googleDB.search.listByGroup(Arrays.asList(group.getId()));
            List<GoogleTarget> targetRescan = googleDB.target.list(Arrays.asList(group.getId()));
            googleDB.serpRescan.rescan(null, targetRescan, searchRescan, true);
        }
        ReflectionAssert.assertReflectionEquals(originalSummaries, googleDB.targetSummary.list(task.getRun().getId()));
        
        
        
        for (GoogleTarget target : allTargets) {
            googleDB.targetSummary.deleteByTarget(target.getId());
        }   
        System.out.println("rescaning #2");
        List<Run> runs = baseDB.run.listDone(null, null);
        for (Run run : runs) {
            for (Group group : allGroups) {
                List<GoogleSearch> searchRescan = googleDB.search.listByGroup(Arrays.asList(group.getId()));
                List<GoogleTarget> targetRescan = googleDB.target.list(Arrays.asList(group.getId()));
                googleDB.serpRescan.rescan(run.getId(), targetRescan, searchRescan, true);
            }
        }
        ReflectionAssert.assertReflectionEquals(originalSummaries, googleDB.targetSummary.list(task.getRun().getId()));
        
        
    }

    
    protected void entryToString(GoogleSerpEntry entry, int position){
        System.out.println(
            position + "|" + 
            entry.getUrl() + "|" + 
            entry.getMap().get((short)1) + "|" +
            entry.getMap().get((short)7) + "|" +
            entry.getMap().get((short)30) + "|" +
            entry.getMap().get((short)90)
        );
    }
    
}
