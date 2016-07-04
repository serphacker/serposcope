/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.google;

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
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.Run.Status;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author admin
 */
public class GoogleTaskUpdateIT extends AbstractDBIT {

    public GoogleTaskUpdateIT() {
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

    volatile GoogleScraperFactory currentFactory;
    GoogleScraperFactory getGSF() {
        return (GoogleScraperFactory) (ScrapClient http, CaptchaSolver solver) -> {
            return currentFactory.get(http, solver);
        };
    }

    GoogleScraperFactory successFactory = new GoogleScraperFactory() {
        @Override
        public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
            return new GoogleScraper(http, solver) {
                @Override
                public GoogleScrapResult scrap(GoogleScrapSearch search) throws InterruptedException {
                    switch (search.getKeyword()) {
                        case "group1#search1":
                            return buildResult(1, 2);
                        case "group1#search2":
                            return buildResult(5, 6);
                        case "group1#search3":
                            return buildResult(50, 51);
                        case "group1#search4":
                            return buildResult(-1, -1);
                        case "group1#search5":
                            return buildResult(-1, -1);
                        case "group1#search6":
                            return buildResult(1, -1);
                        case "group1#search7":
                            return buildResult(5, -1);
                        case "group1#search8":
                            return buildResult(50, -1);
                        case "group1#search9":
                            return buildResult(1, -1);
                        default:
                            throw new IllegalStateException();
                    }
                }
            };
        }
    };

    GoogleScraperFactory failFactory = new GoogleScraperFactory() {
            @Override
            public GoogleScraper get(ScrapClient http, CaptchaSolver solver) {
            return new GoogleScraper(http, solver) {
                @Override
                public GoogleScrapResult scrap(GoogleScrapSearch search) throws InterruptedException {
                    switch (search.getKeyword()) {
                        case "group1#search1":
                            return buildResult(1, 2);
                        case "group1#search2":
                            return buildResult(5, 6);
                        case "group1#search3":
                            return buildResult(50, 51);
                        case "group1#search4":
                            return buildResult(-1, -1);
                        case "group1#search5":
                            return buildResult(-1, -1);
                        default:
                            return new GoogleScrapResult(GoogleScrapResult.Status.ERROR_NETWORK, null);
                    }
                }
            };
        }
    };

//    GoogleScraper run3Scraper(ScrapClient http, CaptchaSolver solver) {
//        return new GoogleScraper(http, solver) {
//            @Override
//            public GoogleScrapResult scrap(GoogleScrapSearch search) throws InterruptedException {
//                switch (search.getKeyword()) {
//                    case "group1#search1":
//                        return buildResult(1, 2);
//                    case "group1#search2":
//                        return buildResult(5, 6);
//                    case "group1#search3":
//                        return buildResult(50, 51);
//                    case "group1#search4":
//                        return buildResult(-1, -1);
//                    case "group1#search5":
//                        return buildResult(-1, -1);
//                    default:
//                        return new GoogleScrapResult(GoogleScrapResult.Status.ERROR_NETWORK, null);
//                }
//            }
//        };
//    }

    protected GoogleScrapResult buildResult(int t1, int t2) {
        if (t1 != -1 && t1 == t2) {
            throw new IllegalArgumentException("target1 and target2 have same rankings");
        }
        GoogleScrapResult res = new GoogleScrapResult(GoogleScrapResult.Status.OK, new ArrayList<>());
        for (int i = 0; i < Math.max(t1, t2); i++) {
            res.urls.add("http://www.example.com");
        }
        if (t1 > 0) {
            res.urls.set(t1 - 1, "http://" + tagert1.getPattern());
        }
        if (t2 > 0) {
            res.urls.set(t2 - 1, "http://" + tagert2.getPattern());
        }
        return res;
    }

    List<GoogleSearch> searchesGroup1 = new ArrayList<>();
    GoogleTarget tagert1;
    GoogleTarget tagert2;

    public void initialize() {
        Group group1 = new Group(Group.Module.GOOGLE, "group1");
        baseDB.group.insert(group1);

        searchesGroup1.clear();
        searchesGroup1.add(new GoogleSearch("group1#search1"));
        searchesGroup1.add(new GoogleSearch("group1#search2"));
        searchesGroup1.add(new GoogleSearch("group1#search3"));
        searchesGroup1.add(new GoogleSearch("group1#search4"));
        searchesGroup1.add(new GoogleSearch("group1#search5"));
        searchesGroup1.add(new GoogleSearch("group1#search6"));
        searchesGroup1.add(new GoogleSearch("group1#search7"));
        searchesGroup1.add(new GoogleSearch("group1#search8"));
        searchesGroup1.add(new GoogleSearch("group1#search9"));

        googleDB.search.insert(searchesGroup1, group1.getId());

        googleDB.target.insert(Arrays.asList(
            tagert1 = new GoogleTarget(group1.getId(), "www.site1.com", GoogleTarget.PatternType.DOMAIN, "www.site1.com"),
            tagert2 = new GoogleTarget(group1.getId(), "www.site2.com", GoogleTarget.PatternType.DOMAIN, "www.site2.com")
        ));

    }

    @Test
    public void testRunUpdate() throws Exception {

        taskFactory = Guice.createInjector(getModule()).getInstance(TaskFactory.class);
        initialize();

        GoogleTask task = taskFactory.create(new Run(Mode.CRON, Group.Module.GOOGLE, LocalDateTime.now().withNano(0)));
        currentFactory = successFactory;
        task.shuffle = false;
        task.run();
        Run lastRun = baseDB.run.findLast(Group.Module.GOOGLE, null, null);
        assertEquals(1, lastRun.getId());
        assertEquals(Status.DONE_SUCCESS, lastRun.getStatus());

        List<GoogleTargetSummary> summaries = googleDB.targetSummary.list(lastRun.getId());
        GoogleTargetSummary t1sum = summaries.get(0);
        GoogleTargetSummary t2sum = summaries.get(1);

        assertEquals(tagert1.getId(), t1sum.getTargetId());
        assertEquals(480, t1sum.getScoreRaw());
        assertEquals(5333, t1sum.getScoreBP());
        assertEquals(3, t1sum.getTotalTop3());
        assertEquals(2, t1sum.getTotalTop10());
        assertEquals(2, t1sum.getTotalTop100());
        assertEquals(2, t1sum.getTotalOut());

        assertEquals(tagert2.getId(), t2sum.getTargetId());
        assertEquals(160, t2sum.getScoreRaw());
        assertEquals(1777, t2sum.getScoreBP());
        

        // run2 with failure
        task = taskFactory.create(new Run(Mode.CRON, Group.Module.GOOGLE, LocalDateTime.now().withNano(0)));
        currentFactory = failFactory;
        task.shuffle = false;
        task.run();
        Run failRun = baseDB.run.findLast(Group.Module.GOOGLE, null, null);
        assertEquals(2, failRun.getId());
        assertEquals(Status.DONE_WITH_ERROR, failRun.getStatus());

        summaries = googleDB.targetSummary.list(failRun.getId());
        GoogleTargetSummary t1sumR2 = summaries.get(0);
        GoogleTargetSummary t2sumR2 = summaries.get(1);

        assertEquals(tagert1.getId(), t1sumR2.getTargetId());
        assertEquals(190, t1sumR2.getScoreRaw());
        assertEquals(2111, t1sumR2.getScoreBP());
        assertEquals(1, t1sumR2.getTotalTop3());
        assertEquals(1, t1sumR2.getTotalTop10());
        assertEquals(1, t1sumR2.getTotalTop100());
        assertEquals(2, t1sumR2.getTotalOut());

        assertEquals(t2sum.getTargetId(), t2sumR2.getTargetId());
        assertEquals(t2sum.getScoreRaw(), t2sumR2.getScoreRaw());
        assertEquals(t2sum.getScoreBP(), t2sumR2.getScoreBP());
        assertEquals(t2sum.getTotalTop10(), t2sumR2.getTotalTop10());
        assertEquals(t2sum.getTotalTop100(), t2sumR2.getTotalTop100());
        assertEquals(2, t2sumR2.getTotalOut());

        
        // run2 update (with success)
        failRun.setStarted(LocalDateTime.now().withNano(0));
        task = taskFactory.create(failRun);
        currentFactory = successFactory;
        task.shuffle = false;
        task.run();
        
        Run updatedRun = baseDB.run.findLast(Group.Module.GOOGLE, null, null);
        assertEquals(failRun.getId(), updatedRun.getId());
        assertEquals(Status.DONE_SUCCESS, updatedRun.getStatus());

        summaries = googleDB.targetSummary.list(failRun.getId());
        GoogleTargetSummary t1sumR3 = summaries.get(0);
        GoogleTargetSummary t2sumR3 = summaries.get(1);

        assertEquals(t1sum.getTargetId(), t1sumR3.getTargetId());
        assertEquals(t1sum.getScoreRaw(), t1sumR3.getScoreRaw());
        assertEquals(t1sum.getScoreBP(), t1sumR3.getScoreBP());
        assertEquals(t1sum.getTotalTop3(), t1sumR3.getTotalTop3());
        assertEquals(t1sum.getTotalTop10(), t1sumR3.getTotalTop10());
        assertEquals(t1sum.getTotalTop100(), t1sumR3.getTotalTop100());
        assertEquals(t1sum.getTotalOut(), t1sumR3.getTotalOut());

        assertEquals(t2sum.getTargetId(), t2sumR3.getTargetId());
        assertEquals(t2sum.getScoreRaw(), t2sumR3.getScoreRaw());
        assertEquals(t2sum.getScoreBP(), t2sumR3.getScoreBP());
        assertEquals(t2sum.getTotalTop3(), t2sumR3.getTotalTop3());
        assertEquals(t2sum.getTotalTop10(), t2sumR3.getTotalTop10());
        assertEquals(t2sum.getTotalTop100(), t2sumR3.getTotalTop100());
        assertEquals(t2sum.getTotalOut(), t2sumR3.getTotalOut());     
    }

}
