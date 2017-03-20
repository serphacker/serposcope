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
import com.serphacker.serposcope.querybuilder.QConfig;
import com.serphacker.serposcope.querybuilder.QEvent;
import com.serphacker.serposcope.querybuilder.QGoogleRank;
import com.serphacker.serposcope.querybuilder.QGoogleRankBest;
import com.serphacker.serposcope.querybuilder.QGoogleSearch;
import com.serphacker.serposcope.querybuilder.QGoogleSearchGroup;
import com.serphacker.serposcope.querybuilder.QGoogleSerp;
import com.serphacker.serposcope.querybuilder.QGoogleTarget;
import com.serphacker.serposcope.querybuilder.QGoogleTargetSummary;
import com.serphacker.serposcope.querybuilder.QGroup;
import com.serphacker.serposcope.querybuilder.QProxy;
import com.serphacker.serposcope.querybuilder.QRun;
import com.serphacker.serposcope.querybuilder.QUser;
import com.serphacker.serposcope.querybuilder.QUserGroup;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class MigrationDB extends AbstractDB {
    
    public final static int LAST_DB_VERSION = 6;
    
    public final static String[] DB_SCHEMA_FILES = new String[]{
        "/db/00-base.h2.sql",
        "/db/01-google.h2.sql"
    };
    
    public final static String[] TABLES = new String[]{
        QConfig.TABLE_NAME, QUser.TABLE_NAME, QGroup.TABLE_NAME, QEvent.TABLE_NAME, QUserGroup.TABLE_NAME, QRun.TABLE_NAME, QProxy.TABLE_NAME,
        QGoogleSearch.TABLE_NAME, QGoogleSerp.TABLE_NAME, QGoogleSearchGroup.TABLE_NAME, QGoogleTarget.TABLE_NAME, QGoogleRank.TABLE_NAME, QGoogleRankBest.TABLE_NAME, QGoogleTargetSummary.TABLE_NAME
    };    
    
    @Inject
    ConfigDB config;
    
    private static final Logger LOG = LoggerFactory.getLogger(MigrationDB.class);
    
    public boolean isDbCreated() throws Exception {
        boolean created = false;
        try(
            Connection con = ds.getConnection();
        ){
            created = con.getMetaData().getTables(null, null, QGoogleSearch.TABLE_NAME, null).first();
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
            stmt.executeUpdate("INSERT INTO `"+QConfig.TABLE_NAME+"` VALUES ('app.dbversion','" + LAST_DB_VERSION + "')");
        }
    }
    
    protected void recreateDb(String[] resources) throws Exception {
        try(
            Connection con = ds.getConnection();
            Statement stmt = con.createStatement()
        ){
            for (String resource : resources) {
            	String s = new String(ByteStreams.toByteArray(MigrationDB.class.getResourceAsStream(resource)));
            	s = s.replaceAll("`CONFIG`", QConfig.TABLE_NAME);
            	s = s.replaceAll("`EVENT`", QEvent.TABLE_NAME);
            	s = s.replaceAll("`USER`", QUser.TABLE_NAME);
            	s = s.replaceAll("`GROUP`", QGroup.TABLE_NAME);
            	s = s.replaceAll("`USER_GROUP`", QUserGroup.TABLE_NAME);
            	s = s.replaceAll("`RUN`", QRun.TABLE_NAME);
            	s = s.replaceAll("`PROXY`", QProxy.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_SEARCH`", QGoogleSearch.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_SERP`", QGoogleSerp.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_SEARCH_GROUP`", QGoogleSearchGroup.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_TARGET`", QGoogleTarget.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_RANK`", QGoogleRank.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_RANK_BEST`", QGoogleRankBest.TABLE_NAME);
            	s = s.replaceAll("`GOOGLE_TARGET_SUMMARY`", QGoogleTargetSummary.TABLE_NAME);
                stmt.executeUpdate(s);
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
                        stmt.executeUpdate("alter table `"+QRun.TABLE_NAME+"` drop foreign key RUN_ibfk_1;");
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
        stmt.executeUpdate("alter table `"+QGoogleTargetSummary.TABLE_NAME+"` drop column `previous_score`;");
        stmt.executeUpdate("alter table `"+QGoogleTargetSummary.TABLE_NAME+"` drop column `score`;");
        stmt.executeUpdate("alter table `"+QGoogleTargetSummary.TABLE_NAME+"` add column `score_raw` int default 0;");
        stmt.executeUpdate("alter table `"+QGoogleTargetSummary.TABLE_NAME+"` add column `score_basis_point` int default 0;");
        stmt.executeUpdate("alter table `"+QGoogleTargetSummary.TABLE_NAME+"` add column `previous_score_basis_point` int default 0;");
        
        // update previous captcha parameters
        String captchaService = config.get("app.captchaservice", "").toLowerCase();
        switch(captchaService){
//            case "deathbycaptcha":
//                config.update(ConfigDB.APP_DBC_USER, config.get("app.dbcuser", ""));
//                config.update(ConfigDB.APP_DBC_PASS, config.get("app.dbcpass", ""));
//                break;
//            case "decaptcher":
//                config.update(ConfigDB.APP_DECAPTCHER_USER, config.get("app.dbcuser", ""));
//                config.update(ConfigDB.APP_DECAPTCHER_PASS, config.get("app.dbcpass", ""));
//                break;
            case "anticaptcha":
                config.update(ConfigDB.APP_ANTICAPTCHA_KEY, config.get("app.dbcapi", ""));
                break;
        }
        
        stmt.executeUpdate("insert into `"+QConfig.TABLE_NAME+"` values ('app.dbversion','5') on duplicate key update `value` = '5';");
    }
    
    protected void upgradeFromV5(Statement stmt) throws Exception {
        if(isMySQL()){
            stmt.executeUpdate("ALTER TABLE `"+QConfig.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QConfig.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QUser.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QUser.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGroup.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGroup.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QEvent.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QEvent.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QUserGroup.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QUserGroup.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QRun.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QRun.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QProxy.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QProxy.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSearch.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSearch.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSerp.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSerp.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSearchGroup.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleSearchGroup.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleTarget.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleTarget.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleRank.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleRank.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleRankBest.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleRankBest.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleTargetSummary.TABLE_NAME+"` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;");
            stmt.executeUpdate("ALTER TABLE `"+QGoogleTargetSummary.TABLE_NAME+"` CHARACTER SET utf8 COLLATE utf8_bin;");
        }
        stmt.executeUpdate("insert into `"+QConfig.TABLE_NAME+"` values ('app.dbversion','6') on duplicate key update `value` = '6';");
    }
    
}
