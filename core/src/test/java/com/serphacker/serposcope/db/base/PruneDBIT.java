/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.di.TaskFactory;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.querybuilder.QGoogleRank;
import com.serphacker.serposcope.querybuilder.QGoogleSerp;
import com.serphacker.serposcope.querybuilder.QGoogleTargetSummary;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.scraper.FakeGScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.task.google.GoogleTask;
import com.serphacker.serposcope.task.google.GoogleTaskIT;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class PruneDBIT extends AbstractDBIT {
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    PruneDB pruneDB;
    
    @Inject
    TaskFactory taskFactory;
    
    @Inject
    protected DataSource ds;
    
    @Inject
    protected Configuration dbTplConf;    
    
    QGoogleRank t_google_rank = QGoogleRank.googleRank;
    QGoogleSerp t_serp = QGoogleSerp.googleSerp;
    QGoogleTargetSummary t_target_summary = QGoogleTargetSummary.googleTargetSummary;    
    
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
    
    @Test
    public void testPruneRuns() {
        
        List<Integer> insertedId = new ArrayList<>();
        
        LocalDateTime ldt = LocalDateTime.of(2010,10,10,10,10,10);
        for (int i = 0; i < 50; i++) {
            int id = baseDB.run.insert(new Run(Run.Mode.MANUAL, Group.Module.GOOGLE, ldt));
            insertedId.add(id);
            ldt = ldt.plusDays(1);
        }

        assertEquals(50, baseDB.run.listByStatus(null, null, null).size());
        
        pruneDB.prune(10);
        List<Run> runs = baseDB.run.listByStatus(null, null, null);
        assertEquals(10, runs.size());
        
        for (int i = 0; i < 10; i++) {
            assertEquals(50 - i, runs.get(i).getId());
        }
        
    }
    
    @Test
    public void testInTask() throws Exception {
        
        GoogleSettings options = googleDB.options.get();
        options.setPages(1);
        options.setResultPerPage(200);
        options.setMinPauseBetweenPageSec(0);
        options.setMaxPauseBetweenPageSec(0);
        googleDB.options.update(options);        

        Group group1 = new Group(Group.Module.GOOGLE, "group#1");
        baseDB.group.insert(group1);
        List<GoogleSearch> group1searches = new ArrayList<>();
        group1searches.add(new GoogleSearch("group#1#search1"));
        group1searches.add(new GoogleSearch("group#1#search2"));
        group1searches.add(new GoogleSearch("group#1#search3"));
        googleDB.search.insert(group1searches, group1.getId());
        
        Group group2 = new Group(Group.Module.GOOGLE, "group#2");
        baseDB.group.insert(group2);        
        List<GoogleSearch> group2searches = new ArrayList<>();
        group2searches.add(new GoogleSearch("group#2#search1"));
        googleDB.search.insert(group2searches, group2.getId());        
        
        
        List<GoogleTarget> group1targets = Arrays.asList(
            new GoogleTarget(group1.getId(), "www.site1.com", GoogleTarget.PatternType.REGEX, "^https?://www\\.site1\\.com/"),
            new GoogleTarget(group1.getId(), "www.site20.com", GoogleTarget.PatternType.REGEX,"^https?://www\\.site20\\.com/")
        );
        List<GoogleTarget> group2targets = Arrays.asList(
            new GoogleTarget(group2.getId(), "www.site45.com", GoogleTarget.PatternType.REGEX, "^https?://www\\.site45\\.com/"),
            new GoogleTarget(group2.getId(), "www.site1.com", GoogleTarget.PatternType.REGEX,"^https?://www\\.site1\\.com/")
        );
        googleDB.target.insert(group1targets);
        googleDB.target.insert(group2targets);
        
        int totalRuns = 100;
        
        GoogleTask task = null;
        LocalDateTime runDate = LocalDateTime.of(2010,10,10,10,10);
        for (int i = 0; i < totalRuns; i++) {
            task = taskFactory.create(Run.Mode.CRON, runDate.plusDays(i));
            task.run();
        }
        
        assertEquals(totalRuns, baseDB.run.listByStatus(null, null, null).size());
        try(Connection conn = ds.getConnection()){
            assertEquals(
                (long)totalRuns*(group1targets.size()+group2targets.size()), 
                (long)new SQLQuery<>(conn, dbTplConf).select(t_target_summary.count()).from(t_target_summary).fetchFirst()
            );
            assertEquals(
                (long)totalRuns*(group1searches.size()+group2searches.size()),
                (long)new SQLQuery<>(conn, dbTplConf).select(t_serp.count()).from(t_serp).fetchFirst()
            );
            
            assertEquals(
                (long)totalRuns*((group1targets.size()*group1searches.size())+(group2targets.size()*group2searches.size())),
                (long)new SQLQuery<>(conn, dbTplConf).select(t_google_rank.count()).from(t_google_rank).fetchFirst()
            );
        }
        
        
        totalRuns = 10;
        pruneDB.prune(totalRuns);
        
        assertEquals(totalRuns, baseDB.run.listByStatus(null, null, null).size());
        try(Connection conn = ds.getConnection()){
            assertEquals(
                (long)totalRuns*(group1targets.size()+group2targets.size()), 
                (long)new SQLQuery<>(conn, dbTplConf).select(t_target_summary.count()).from(t_target_summary).fetchFirst()
            );
            assertEquals(
                (long)totalRuns*(group1searches.size()+group2searches.size()),
                (long)new SQLQuery<>(conn, dbTplConf).select(t_serp.count()).from(t_serp).fetchFirst()
            );
            
            assertEquals(
                (long)totalRuns*((group1targets.size()*group1searches.size())+(group2targets.size()*group2searches.size())),
                (long)new SQLQuery<>(conn, dbTplConf).select(t_google_rank.count()).from(t_google_rank).fetchFirst()
            );
        }
        
    }
    
    
    
    
}
