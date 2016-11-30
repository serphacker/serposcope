/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.google;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import ninja.NinjaTest;
import ninja.Result;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import serposcope.controllers.google.GoogleTargetController;

/**
 *
 * @author admin
 */
public class GoogleTargetControllerIT {

    @Inject
    BaseDB baseDB;

    @Inject
    GoogleDB googleDB;

    GoogleTargetController gtc;

    protected String getDbUrl() {
        return "jdbc:h2:/var/tmp/serposcope/db;MODE=MySQL";
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
        if (baseDB == null) {
            Guice.createInjector(getModule()).injectMembers(this);
        }
        gtc = new GoogleTargetController();
        gtc.baseDB = baseDB;
        gtc.googleDB = googleDB;
    }

//    @Test
    public void testSomeMethod() throws Exception {
        Group group = baseDB.group.list().get(0);
        GoogleTarget target = googleDB.target.list().get(0);
        List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(group.getId()));
        List<Run> runs = baseDB.run.listDone(null, null);

        Run firstRun = runs.get(0);
        Run lastRun = runs.get(runs.size() - 1);

        for (int i = 0; i < 3; i++) {
            {
                long _start = System.currentTimeMillis();
                StringWriter strWriter=  new StringWriter();
                try(PrintWriter writer = new PrintWriter(strWriter)){
                    gtc.getTableJson(group, target, searches, runs, firstRun.getDay(), lastRun.getDay(), writer);
                }
                String jsonData = strWriter.toString();
                long _stop = System.currentTimeMillis();
                System.out.println("NEW : " + DurationFormatUtils.formatDurationHMS(_stop - _start));
                System.out.println("data " + jsonData.length());
            }
            {
                long _start = System.currentTimeMillis();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try(PrintWriter writer = new PrintWriter(new GZIPOutputStream(baos))){
                    gtc.getTableJson(group, target, searches, runs, firstRun.getDay(), lastRun.getDay(), writer);
                }
                long _stop = System.currentTimeMillis();
                System.out.println("NEW+GZ : " + DurationFormatUtils.formatDurationHMS(_stop - _start));
                System.out.println("data " + baos.size());
            }            
            {
                long _start = System.currentTimeMillis();
                String jsonData = gtc.getTableJsonData0(group, target, searches, runs, firstRun.getDay(), lastRun.getDay());
                long _stop = System.currentTimeMillis();
                System.out.println("NEW2 : " + DurationFormatUtils.formatDurationHMS(_stop - _start));
                System.out.println("data " + jsonData.length());
            }            
//            {
//                long _start = System.currentTimeMillis();
//                String jsonData = gtc.getTableJsonDataOld(group, target, searches, runs, firstRun.getDay(), lastRun.getDay());
//                long _stop = System.currentTimeMillis();
//                System.out.println("OLD : " + DurationFormatUtils.formatDurationHMS(_stop - _start));
//                System.out.println("data " + jsonData.length());
//            }
        }

    }

}
