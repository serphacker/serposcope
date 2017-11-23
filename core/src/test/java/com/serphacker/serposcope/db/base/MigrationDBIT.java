/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import static com.serphacker.serposcope.db.base.MigrationDB.LAST_DB_VERSION;
import com.serphacker.serposcope.db.google.GoogleSearchDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.scraper.google.GoogleCountryCode;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author admin
 */
public class MigrationDBIT {
    
    String[] tables = new String[]{
        "CONFIG", "USER", "GROUP", "EVENT", "USER_GROUP", "RUN", "GROUP_RUN", "PROXY",
        "GOOGLE_SEARCH", "GOOGLE_SERP", "GOOGLE_SEARCH_GROUP", "GOOGLE_TARGET", "GOOGLE_RANK", "GOOGLE_RANK_BEST", "GOOGLE_TARGET_SUMMARY"
    };
    
    protected Injector injectorH2 = null;
    protected Injector injectorMySQL = null;

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
        props.load(ClassLoader.class.getResourceAsStream("/testconfig.properties"));
        injectorH2 = Guice.createInjector(getModule("jdbc:h2:mem:test;MODE=MySQL"));
        injectorMySQL = Guice.createInjector(getModule(
            "jdbc:mysql://" + props.getProperty("mysql.host") + ":3306/" + props.getProperty("mysql.database") + "?user=" + props.getProperty("mysql.user") + "&password=" + props.getProperty("mysql.password") + "&allowMultiQueries=true"));
        wipe(injectorH2);wipe(injectorMySQL);
    }
    
    protected void wipe(Injector injector) throws Exception {
        DataSource ds = injector.getInstance(DataSource.class);
        try(Connection con = ds.getConnection(); Statement stmt = con.createStatement()){
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=0;");
            for (String table : tables) {
                stmt.executeUpdate("DROP TABLE IF EXISTS `" + table + "`");
            }
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=1;");
        }
    }
    
    protected List<String> dumpDBDefinitions(Injector injector) throws Exception{
        List<String> defs = new ArrayList<>();
        
        DataSource ds = injector.getInstance(DataSource.class);
        
        String query = null;
        if(((HikariDataSource)ds).getJdbcUrl().contains("mysql://")){
            query = "select concat_ws('|',table_name,column_name,column_type) from information_schema.columns where table_schema = '" + props.get("mysql.database") +"'";
        } else {
            query = "select concat_ws('|',table_name,column_name,type_name) from information_schema.columns where table_schema != 'information_schema'";
        }
        
        try(Connection con = ds.getConnection(); Statement stmt = con.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                defs.add(rs.getString(1));
            }
        }
        
        Collections.sort(defs);
        
        return defs;
    }
    
    public void testDumpDBDef(Injector injector) throws Exception{
        MigrationDB mig = injector.getInstance(MigrationDB.class);
        assertTrue(dumpDBDefinitions(injector).isEmpty());
        mig.recreateDb();
        assertFalse(dumpDBDefinitions(injector).isEmpty());
    }
    
    @Test
    public void testDumpDBDef() throws Exception{
        testDumpDBDef(injectorH2);
        testDumpDBDef(injectorMySQL);
    }
    
    @Test
    public void testRecreateDB() throws Exception {
        testRecreateDB(injectorH2);
        testRecreateDB(injectorMySQL);
    }
    
    protected void testRecreateDB(Injector injector) throws Exception{
        MigrationDB mig = injector.getInstance(MigrationDB.class);
        assertFalse(mig.isDbCreated());
        mig.recreateDb();
        assertTrue(mig.isDbCreated());
    }
    
    @Test
    public void testMigrate() throws Exception {
        testMigrate(injectorH2);
        testMigrate(injectorMySQL);
    }
    
    protected void testMigrate(Injector injector) throws Exception {
        MigrationDB mig = injector.getInstance(MigrationDB.class);
        ConfigDB config = injector.getInstance(ConfigDB.class);
        
        mig.recreateDb();
        List<String> defs = dumpDBDefinitions(injector);
        assertFalse(defs.isEmpty());
        
        int version = config.getInt(ConfigDB.APP_DBVERSION, 0);
        assertEquals(mig.LAST_DB_VERSION, version);
        
        wipe(injector);
        assertTrue(dumpDBDefinitions(injector).isEmpty());
        
        mig.recreateDb(new String[]{
            "/db/v0/00-base.h2.sql",
            "/db/v0/01-google.h2.sql"
        });
        assertTrue(mig.isDbCreated());
        assertTrue(mig.LAST_DB_VERSION != config.getInt(ConfigDB.APP_DBVERSION, 0));
        
        mig.migrateIfNeeded();
        
        assertEquals(
            Sets.difference(new HashSet(dumpDBDefinitions(injector)), new HashSet<>(defs)).toString(), 
            defs, dumpDBDefinitions(injector));
        assertTrue(mig.LAST_DB_VERSION == config.getInt(ConfigDB.APP_DBVERSION, 0));
    }
    
    @Test
    public void testUpgradeTLD2Country() throws Exception {
        testUpgradeTLD2Country_(injectorH2);
        testUpgradeTLD2Country_(injectorMySQL);
    }    
    
    public static class TldCc {
        public final String tld;
        public final GoogleCountryCode country;

        public TldCc(String tld, GoogleCountryCode country) {
            this.tld = tld;
            this.country = country;
        }
    }
        
    public void testUpgradeTLD2Country_(Injector injector) throws Exception {
        MigrationDB mig = injector.getInstance(MigrationDB.class);
        DataSource ds = injector.getInstance(DataSource.class);
        GoogleSearchDB gsdb = injector.getInstance(GoogleSearchDB.class);
        
        mig.recreateDb(new String[]{
            "/db/v6/00-base.h2.sql",
            "/db/v6/01-google.h2.sql"
        });
        try(Connection con = ds.getConnection(); Statement stmt = con.createStatement();){
            stmt.executeUpdate("INSERT INTO `CONFIG` VALUES ('app.dbversion','6')");
        }
        assertTrue(mig.isDbCreated());
        
        TldCc[] tests = new TldCc[]{
            new TldCc("com", GoogleCountryCode.__),
            new TldCc(null, GoogleCountryCode.__),
            new TldCc("barbapapa", GoogleCountryCode.__),
            new TldCc("com.br", GoogleCountryCode.BR),
            new TldCc("co.uk", GoogleCountryCode.UK),
            new TldCc("fr", GoogleCountryCode.FR)
        };
        
        try(Connection con = ds.getConnection(); Statement stmt = con.createStatement();){
            for (int i = 0; i < tests.length; i++) {
                stmt.executeUpdate("INSERT INTO `GOOGLE_SEARCH`(`id`,`keyword`,`device`,`tld`) VALUES (" + (i+1) + ", '#" + i + "', 0, '" + tests[i].tld + "')");
            }
        }
        
        mig.migrateIfNeeded();
        
        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i].country, gsdb.find(i+1).getCountry());
        }
        
    }  
}
