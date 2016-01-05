/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.AbstractDB;
import java.sql.Connection;
import java.sql.Statement;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class MigrationDB extends AbstractDB {
    
    public final static int LAST_DB_VERSION = 4;
    
    @Inject
    ConfigDB config;
    
    private static final Logger LOG = LoggerFactory.getLogger(MigrationDB.class);
    
    public boolean isDbCreated() throws Exception {
        boolean created = false;
        try(
            Connection con = ds.getConnection();
        ){
            created = con.getMetaData().getTables(null, null, "google_search", null).first();
        } catch(Exception ex){
            LOG.error("isDbCreated", ex);
        }
        return created;
    }
    
    
    public void recreateDb() throws Exception {
        recreateDb(new String[]{
            "/db/00-base.h2.sql",
            "/db/01-google.h2.sql"
        });
        try(
            Connection con = ds.getConnection();
            Statement stmt = con.createStatement()
        ){
            stmt.executeUpdate("INSERT INTO `CONFIG` VALUES ('app.dbversion','" + LAST_DB_VERSION + "')");
        }
    }
    
    protected void recreateDb(String[] resources) throws Exception {
        try(
            Connection con = ds.getConnection();
            Statement stmt = con.createStatement()
        ){
            for (String resource : resources) {
                stmt.executeUpdate(new String(ByteStreams.toByteArray(MigrationDB.class.getResourceAsStream(resource))));
            }
        }
    }
    
    public void migrateIfNeeded() throws Exception {
        int dbVersion = config.getInt(ConfigDB.APP_DBVERSION, 0);
        if(dbVersion >= LAST_DB_VERSION){
            LOG.info("database up to date");
            return;
        }
        
        for (int fromDBVersion = dbVersion; fromDBVersion < LAST_DB_VERSION; fromDBVersion++) {
            LOG.info("migrating from db version {} to db version {}", fromDBVersion, fromDBVersion+1);
            String file = "/db/patch/" + fromDBVersion + "-" + (fromDBVersion+1) + ".sql";
            
            try(Connection con = ds.getConnection();){
                try(Statement stmt = con.createStatement()){
                    con.setAutoCommit(false);
                    if(fromDBVersion == 3 && !dbTplConf.getTemplates().isNativeMerge()){
                        stmt.executeUpdate("alter table `RUN` drop foreign key RUN_ibfk_1;");
                    }
                    stmt.executeUpdate(new String(ByteStreams.toByteArray(MigrationDB.class.getResourceAsStream(file))));
                }catch(Exception ex){
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }
    
}
