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
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import com.serphacker.serposcope.models.base.Event;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleSerpEntry;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class ExportDBIT {
    
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
        props.load(ClassLoader.class.getResourceAsStream("/testconfig.properties"));
        injectorH2 = Guice.createInjector(getModule("jdbc:h2:mem:test;MODE=MySQL"));
        injectorMySQL = Guice.createInjector(getModule(
            "jdbc:mysql://" + props.getProperty("mysql.host") + ":3306/" + props.getProperty("mysql.database") + "?user=" + props.getProperty("mysql.user") + "&password=" + props.getProperty("mysql.password") + "&allowMultiQueries=true"));
        wipe(injectorH2);
        wipe(injectorMySQL);
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
    
    public void injectMySQL(){
        baseDB = injectorMySQL.getInstance(BaseDB.class);
        exportDB = injectorMySQL.getInstance(ExportDB.class);
        googleDB = injectorMySQL.getInstance(GoogleDB.class);
    }    
    
    public void injectH2(){
        baseDB = injectorH2.getInstance(BaseDB.class);
        exportDB = injectorH2.getInstance(ExportDB.class);
        googleDB = injectorH2.getInstance(GoogleDB.class);
    }

    @Test
    public void testH2() throws Exception {
        injectH2();
        theTest("/tmp/serpo-test-dump-h2.sql");
    }
    
    @Test
    public void testMySQL() throws Exception {
        injectMySQL();
        theTest("/tmp/serpo-test-dump-mysq.sql");
    }
    
    protected void theTest(String path) throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2000,10,10,10,10,10);
        LocalDate ld = ldt.toLocalDate();
        
        baseDB.migration.recreateDb();
        baseDB.config.update("k'ey", "va'l");
        baseDB.config.update("k'ey", "va'l");
        
        User user = new User();
        user.setEmail("some'quotes\"and \n woops !");
        user.setPassword("hash me");
        baseDB.user.insert(user);
        User user2 = new User();
        user2.setPasswordHash(new byte[]{0,1,0});
        user2.setPasswordSalt(new byte[]{1,0,1});
        baseDB.user.insert(user2);
        
        // 
        Group grp = new Group(Group.Module.GOOGLE, "impo'ssible'name");
        baseDB.group.insert(grp);
        
        Event event = new Event(1, ld, "title with \" and ' xxx", "description with \" and ' xxx");
        baseDB.event.insert(event);
        
        baseDB.user.addPerm(user, grp);
        
        Run run = new Run(Run.Mode.CRON, Group.Module.GOOGLE, ldt);
        run.setCaptchas(3);
        run.setDay(ld);
        run.setErrors(3);
        run.setFinished(ldt.plusHours(1));
        run.setProgress(33);
        run.setStatus(Run.Status.RUNNING);
        baseDB.run.insert(run);
        
        
        Proxy proxy = new Proxy(0, Proxy.Type.HTTP, "ip'", 880, "username'", "password'", "remoteip", ldt, Proxy.Status.OK);
        baseDB.proxy.insert(Arrays.asList(proxy));
        
        GoogleSearch search1 = new GoogleSearch();
        search1.setCustomParameters("custom'");
        search1.setDatacenter("datacenter'");
        search1.setDevice(GoogleDevice.MOBILE);
        search1.setKeyword("keyword'");
        search1.setLocal("local'");
        search1.setTld("tld'");
        
        googleDB.search.insert(Arrays.asList(search1), grp.getId());
        
        
        GoogleSerp serp = new GoogleSerp(run.getId(), search1.getId(), ldt);
        GoogleSerpEntry gse = new GoogleSerpEntry("url'");
        gse.fillPreviousPosition(new HashMap<>());
        serp.addEntry(gse);
        googleDB.serp.insert(serp);
        
        GoogleTarget target = new GoogleTarget(grp.getId(), "target'name",GoogleTarget.PatternType.REGEX, "patern''x");
        googleDB.target.insert(Arrays.asList(target));
        
        assertTrue(exportDB.export(path));
    }
    
    @Test
    public void testSigned(){
        exportDB = new ExportDB();
        assertEquals("X'ff000a100f'", exportDB.blobToString(new byte[]{-1, 0, 10, 0x10, 0xf}));
    }
    
}
