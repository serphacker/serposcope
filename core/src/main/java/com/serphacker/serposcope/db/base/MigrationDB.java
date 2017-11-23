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
import com.serphacker.serposcope.scraper.google.GoogleCountryCode;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class MigrationDB extends AbstractDB {
    
    public final static int LAST_DB_VERSION = 7;
    
    public final static String[] DB_SCHEMA_FILES = new String[]{
        "/db/00-base.h2.sql",
        "/db/01-google.h2.sql"
    };
    
    public final static String[] TABLES = new String[]{
        "CONFIG", "USER", "GROUP", "EVENT", "USER_GROUP", "RUN", "PROXY",
        "GOOGLE_SEARCH", "GOOGLE_SERP", "GOOGLE_SEARCH_GROUP", "GOOGLE_TARGET", "GOOGLE_RANK", "GOOGLE_RANK_BEST", "GOOGLE_TARGET_SUMMARY"
    };    
    
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
        recreateDb(DB_SCHEMA_FILES);
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
                    
                    // LEGACY MIGRATION, TO BE REMOVED
                    if(fromDBVersion == 3 && isMySQL()){
                        stmt.executeUpdate("alter table `RUN` drop foreign key RUN_ibfk_1;");
                    }
                    InputStream stream = MigrationDB.class.getResourceAsStream(file);
                    if(stream != null){
                        stmt.executeUpdate(new String(ByteStreams.toByteArray(stream)));
                    }
                    
                    switch(fromDBVersion){
                        case 4:
                            upgradeFromV4(stmt);
                            break;
                        case 5:
                            upgradeFromV5(stmt);
                            break;
                        case 6:
                            upgradeFromV6(stmt);
                            break;
                    }
                }catch(Exception ex){
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }
    
    protected void upgradeFromV4(Statement stmt) throws Exception {
        // GOOGLE_TARGET_SUMMARY 
        stmt.executeUpdate("alter table `GOOGLE_TARGET_SUMMARY` drop column `previous_score`;");
        stmt.executeUpdate("alter table `GOOGLE_TARGET_SUMMARY` drop column `score`;");
        stmt.executeUpdate("alter table `GOOGLE_TARGET_SUMMARY` add column `score_raw` int default 0;");
        stmt.executeUpdate("alter table `GOOGLE_TARGET_SUMMARY` add column `score_basis_point` int default 0;");
        stmt.executeUpdate("alter table `GOOGLE_TARGET_SUMMARY` add column `previous_score_basis_point` int default 0;");
        
        // update previous captcha parameters
        String captchaService = config.get("app.captchaservice", "").toLowerCase();
        switch(captchaService){
            case "deathbycaptcha":
                config.update(ConfigDB.APP_DBC_USER, config.get("app.dbcuser", ""));
                config.update(ConfigDB.APP_DBC_PASS, config.get("app.dbcpass", ""));
                break;
            case "decaptcher":
                config.update(ConfigDB.APP_DECAPTCHER_USER, config.get("app.dbcuser", ""));
                config.update(ConfigDB.APP_DECAPTCHER_PASS, config.get("app.dbcpass", ""));
                break;
            case "anticaptcha":
                config.update(ConfigDB.APP_ANTICAPTCHA_KEY, config.get("app.dbcapi", ""));
                break;
        }
        
        stmt.executeUpdate("insert into `CONFIG` values ('app.dbversion','5') on duplicate key update `value` = '5';");
    }
    
    protected void upgradeFromV5(Statement stmt) throws Exception {
        if(isMySQL()){
            stmt.executeUpdate("ALTER TABLE `CONFIG` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `CONFIG` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `USER` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `USER` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GROUP` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GROUP` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `EVENT` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `EVENT` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `USER_GROUP` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `USER_GROUP` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `RUN` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `RUN` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `PROXY` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `PROXY` CHARACTER SET utf8 COLLATE utf8_bin;");
            
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SEARCH` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SEARCH` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SERP` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SERP` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SEARCH_GROUP` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_SEARCH_GROUP` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_TARGET` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_TARGET` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_RANK` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_RANK` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_RANK_BEST` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_RANK_BEST` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_TARGET_SUMMARY` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `GOOGLE_TARGET_SUMMARY` CHARACTER SET utf8 COLLATE utf8_bin;");
        }
        stmt.executeUpdate("insert into `CONFIG` values ('app.dbversion','6') on duplicate key update `value` = '6';");
    }
    
    protected void upgradeFromV6(Statement stmt) throws Exception {
        stmt.executeUpdate("alter table `GOOGLE_SEARCH` add column `country` varchar(2) after tld;");
        
        try(Connection con = ds.getConnection(); Statement stmtRO = con.createStatement()){
            ResultSet rs = stmtRO.executeQuery("select * from `GOOGLE_SEARCH`;");
            while(rs.next()){
                GoogleCountryCode country = MigrationTLDtoCountry.fromTld(rs.getString("tld"));
                stmt.executeUpdate("update `GOOGLE_SEARCH` set `country` = '" + country.name() + "' WHERE `id` = " + rs.getInt("id") + ";");
            }            
        }

        stmt.executeUpdate("alter table `GOOGLE_SEARCH` drop column `tld`;");
        stmt.executeUpdate("insert into `CONFIG` values ('app.dbversion','7') on duplicate key update `value` = '7';");
    }    
    
}
