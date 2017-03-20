/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.DeepIntegrationTest;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class ExportHugeDBIT extends DeepIntegrationTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExportHugeDBIT.class);
    
    String[] tables = new String[]{
        "CONFIG", "USER", "GROUP", "EVENT", "USER_GROUP", "RUN", "PROXY",
        "GOOGLE_SEARCH", "GOOGLE_SERP", "GOOGLE_SEARCH_GROUP", "GOOGLE_TARGET", "GOOGLE_RANK", "GOOGLE_RANK_BEST", "GOOGLE_TARGET_SUMMARY"
    };
    
    protected Injector injectorH2 = null;
    protected Injector injectorMySQL = null;

    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    ExportDB exportDB;
    
    
    protected List<Module> getModule(String dburl) {
        List<Module> lists = new ArrayList<>();
        lists.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataSource.class).toProvider(new DataSourceProvider(dburl, false)).in(Singleton.class);
                bind(Configuration.class).toProvider(new ConfigurationProvider(dburl)).in(Singleton.class);
            }
        });
        return lists;
    }

    Properties props = new Properties();
    
    @Before
    public void before() throws Exception {
        super.before();
        props.load(ClassLoader.class.getResourceAsStream("/testconfig.properties"));
        assertTrue(new File(props.getProperty("h2.huge3gpath") + ".mv.db").exists());
        injectorH2 = Guice.createInjector(getModule("jdbc:h2:" + props.getProperty("h2.huge3gpath") + ";MODE=MySQL"));
        baseDB = injectorH2.getInstance(BaseDB.class);
        exportDB = injectorH2.getInstance(ExportDB.class);
        googleDB = injectorH2.getInstance(GoogleDB.class);        
        System.out.println("DB initialized");
    }
    
    @Test
    public void testExport3GDatabase() throws Exception {
        System.out.println("starting test");
        // medium-3G.mv.db
        exportDB.export("/var/tmp/export.sql.gz");
    }
    
}
