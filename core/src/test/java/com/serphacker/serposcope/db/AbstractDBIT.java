/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.Assume;
import org.junit.Before;

public class AbstractDBIT {
    
    protected Injector injector = null;

    @Inject
    BaseDB db;
    
    protected String getDbUrl(){
        return "jdbc:h2:mem:test;MODE=MySQL";
        // TODO improve integration test, should be done on a mysql backend too 
        // "jdbc:mysql://" + props.getProperty("mysql.host") + ":3306/" + props.getProperty("mysql.database") + "?user=" + props.getProperty("mysql.user") + "&password=" + props.getProperty("mysql.password") + "&allowMultiQueries=true"));
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
        injector = Guice.createInjector(getModule());
        injector.injectMembers(this);
        db.migration.recreateDb();
    }
    
}
