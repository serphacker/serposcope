/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package db;

/*
 * MIT License (MIT) - Copyright (c) 2015 SERP Hacker https://serphacker.com/
 * 
 * @link https://serposcope.serphacker.com/ Serposcope rank checker
 * @author Pierre Nogues <pierre@serphacker.com>
 * @license https://opensource.org/licenses/MIT
 */


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.Before;

public abstract class DBIntegrationTest {

    @Inject
    BaseDB baseDB;
    
    private List<Module> getModule() {
        List<Module> lists = new ArrayList<>();
        lists.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataSource.class).toProvider(new DataSourceProvider("jdbc:h2:mem:test;MODE=MySQL", false)).in(Singleton.class);
                bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Singleton.class);
            }
        });
        return lists;
    }

    @Before
    public void before() throws Exception {
        if(baseDB == null){
            Guice.createInjector(getModule()).injectMembers(this);
        }
        baseDB.migration.recreateDb();
    }
    
}
