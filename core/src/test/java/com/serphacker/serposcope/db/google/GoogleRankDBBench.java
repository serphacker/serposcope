/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTarget.PatternType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class GoogleRankDBBench {
    
    protected static Injector injector = null;

    @Inject
    BaseDB db;
    
    protected String getDbUrl(){
        return "jdbc:h2:" + System.getProperty("java.io.tmpdir") + "/serposcope_test;MODE=MySQL";
    }
    
    protected List<Module> getModule() {
        List<Module> lists = new ArrayList<>();
        lists.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataSource.class).toProvider(new DataSourceProvider(getDbUrl(), false)).in(Singleton.class);
                bind(Configuration.class).toProvider(new ConfigurationProvider(getDbUrl())).in(Singleton.class);
            }
        });
        return lists;
    }

    @Before
    public void before() throws Exception {
        if(injector == null){
            injector = Guice.createInjector(getModule());
        }
        injector.injectMembers(this);
        db.migration.recreateDb();
    }    
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    @Test
    public void testBenchmark() {
        
        Group grp = new Group(Group.Module.GOOGLE, "grp");
        baseDB.group.insert(grp);
        
        GoogleSearch search = new GoogleSearch("keyword");
        googleDB.search.insert(Arrays.asList(search), grp.getId());
        
        GoogleTarget target = new GoogleTarget(grp.getId(), "name", PatternType.REGEX, "pattern");
        googleDB.target.insert(Arrays.asList(target));
        

        long _time = System.currentTimeMillis();        
        for (int i = 0; i < 100000; i++) {
            Run run = new Run(Run.Mode.CRON, Group.Module.GOOGLE, LocalDateTime.now().withNano(0));
            baseDB.run.insert(run);

            GoogleRank rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 1, 2, 3, "url");
            googleDB.rank.insert(rank);

            rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 2, 3, 4, "url");
            googleDB.rank.insert(rank);
        }
        System.out.println(DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_time));
        
    }    
    
}
