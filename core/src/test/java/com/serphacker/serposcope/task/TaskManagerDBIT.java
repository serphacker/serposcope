/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task;

import com.serphacker.serposcope.scraper.google.scraper.FakeGScraper;
import com.google.inject.AbstractModule;
import com.serphacker.serposcope.db.google.*;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.di.TaskFactory;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.Run.Mode;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleSerpEntry;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTarget.PatternType;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.google.scraper.RandomGScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class TaskManagerDBIT extends AbstractDBIT {

    public TaskManagerDBIT() {
    }
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    TaskManager taskManager;
    
    @Inject
    TaskFactory gtFactory;

    @Override
    protected List<Module> getModule() {
        List<Module> modules = super.getModule();
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
//                bind(ScraperFactory.class).toInstance((GoogleScrapConfig config) -> new FakeGScraper(config));
//                bind(TaskManager.class).toInstance(new TaskManager());
//                bind(GoogleScraper.class).to(RandomGScraper.class);
                bind(GoogleScraperFactory.class).toInstance((GoogleScraperFactory) (ScrapClient http, CaptchaSolver solver) -> new FakeGScraper(http, solver));
                install(new FactoryModuleBuilder().build(TaskFactory.class));
            }
        });
        return modules;
    }
    
    
    public void initialize() {
        
        User user = new User();
        user.setAdmin(true);
        user.setEmail("x@x.com");
        try {
            user.setPassword("x@x.com");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(TaskManagerDBIT.class.getName()).log(Level.SEVERE, null, ex);
        }
        baseDB.user.insert(user);
        
        GoogleSettings options = googleDB.options.get();
        options.setPages(10);
        options.setResultPerPage(10);
        options.setMinPauseBetweenPageSec(1);
        options.setMaxPauseBetweenPageSec(1);
        googleDB.options.update(options);
        
        // fruits
        Group grpFruit = new Group(Group.Module.GOOGLE, "group #1");
        baseDB.group.insert(grpFruit);
        List<GoogleSearch> searchesFruit = new ArrayList<>();
        searchesFruit.add(new GoogleSearch("search #1"));
        searchesFruit.add(new GoogleSearch("search #2"));
        searchesFruit.add(new GoogleSearch("search #3"));
        googleDB.search.insert(searchesFruit, grpFruit.getId());
        
        googleDB.target.insert(Arrays.asList(
            new GoogleTarget(grpFruit.getId(), "www.site1.com", PatternType.REGEX, "^https?://www\\.site1\\.com/"),
            new GoogleTarget(grpFruit.getId(), "www.site20.com", PatternType.REGEX, "^https?://www\\.site20\\.com/")
        ));
    }

    @Test
    public void testSingleRun() throws Exception {
        initialize();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        
        taskManager.startGoogleTask(Mode.CRON, now);
        Thread.sleep(2000l);
        taskManager.abortGoogleTask(true);
        taskManager.joinGoogleTask();
//        // check the SERP
//        List<GoogleSearch> grpFruit = googleDB.search.list(Arrays.asList(1l));
//        
//        for (GoogleSearch search : grpFruit) {
//            GoogleSerp serp = googleDB.serp.get(task.run.getId(), search.getId());
//            System.out.println(search.getKeyword());
//            for (int position = 0; position < serp.getEntries().size(); position++) {
//                entryToString(serp.getEntries().get(position), position+1);
//            }
//        }
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
