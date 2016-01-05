/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di.db;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceProvider implements Provider<DataSource> {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);

    public final String url;
    public final boolean debug;
    
    public DataSourceProvider(String url, boolean debug) {
        this.url = url;
        this.debug = debug;
    }
    
    @Override
    public DataSource get() {
        try {
            
            // try to create connection with native driver first,
            // because HikariCP don't fail fast on wrong password
            try (Connection  connection = DriverManager.getConnection(url)){
            }
            
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(url);
            config.setConnectionTestQuery("select 1 from dual;");
            config.setConnectionTimeout(60000);
            config.setValidationTimeout(60000);

            DataSource ds = new HikariDataSource(config);
            if(debug){
                ds = new P6DataSource(ds);
            }
            ds.getConnection().close();
            
            return ds;
        } catch (Exception ex) {
            LOG.error("Can't establish connection to database",ex);
            System.err.println("Can't establish connection to database");
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    
}
