/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Singleton;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.base.Config;
import static com.serphacker.serposcope.models.base.Config.DEFAULT_PRUNE_RUNS;
import com.serphacker.serposcope.querybuilder.QConfig;
import java.sql.Connection;

@Singleton
public class ConfigDB extends AbstractDB {
    
    public final static String APP_DBVERSION = "app.dbversion";
    public final static String APP_SECRET = "app.secret";
    public final static String APP_INSTALLID = "app.installid";
    
//    public final static String APP_CRON_ENABLED = "app.cronenabled";
    public final static String APP_CRON_TIME = "app.crontime";
    
//    public final static String APP_DBC_USER = "app.dbc.user";
//    public final static String APP_DBC_PASS = "app.dbc.pass";
//    
//    public final static String APP_DECAPTCHER_USER = "app.decaptcher.user";
//    public final static String APP_DECAPTCHER_PASS = "app.decaptcher.pass";
    
    public final static String APP_ANTICAPTCHA_KEY = "app.anticaptcha.key";
    
    public final static String APP_DISPLAY_HOME = "app.display.home";
    public final static String APP_DISPLAY_GOOGLE_TARGET = "app.display.google.target";
    public final static String APP_DISPLAY_GOOGLE_SEARCH = "app.display.google.search";
    
    public final static String APP_PRUNE_RUNS = "app.prune.runs";
    
    public final static String APP_NOTI_TASK = "app.noti.task";
    
    QConfig t_cfg = QConfig.config;
    
    public void update(String name, String value){
        boolean updated=false;
        try(Connection con = ds.getConnection()){
            
            if(value == null){
                updated = new SQLDeleteClause(con, dbTplConf, t_cfg)
                    .where(t_cfg.name.eq(name))
                    .execute() == 1;
            } else {
                updated = new SQLInsertClause(con, dbTplConf, t_cfg)
                    .set(t_cfg.name, name)
                    .set(t_cfg.value, value)
                    .addFlag(Position.END, 
                        " on duplicate key update value = " + dbTplConf.asLiteral(value)
                    )
                    .execute() == 1;
            }
                
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
//        return updated;
    }
    
    public String get(String name, String _default){
        
        String value = null;
        try(Connection con = ds.getConnection()){
            
            value = new SQLQuery<Void>(con, dbTplConf)
                .select(t_cfg.value)
                .from(t_cfg)
                .where(t_cfg.name.eq(name))
                .fetchOne();
            
            if(value == null){
                value = _default;
            }
                
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        
        return value;
    }
    
    
    public void updateInt(String name, Integer value){
        update(name, value == null ? null : Integer.toString(value));
    }    
    
    public int getInt(String name, int _default){
        String value = get(name, null);
        if(value == null){
            return _default;
        }
        
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException ex){
            return _default;
        }
    }
    
    public void updateBoolean(String name, Boolean value){
        update(name, value == null ? null : Boolean.toString(value));
    }    
    
    public boolean getBoolean(String name, boolean _default){
        String value = get(name, null);
        if(value == null){
            return _default;
        }
        
        try {
            return Boolean.parseBoolean(value);
        } catch(NumberFormatException ex){
            return _default;
        }
    }    
    
    public Config getConfig(){
        Config config = new Config();

        config.setCronTime(get(APP_CRON_TIME, null));
        
//        config.setDbcUser(get(APP_DBC_USER,null));
//        config.setDbcPass(get(APP_DBC_PASS,null));
//        
//        config.setDecaptcherUser(get(APP_DECAPTCHER_USER,null));
//        config.setDecaptcherPass(get(APP_DECAPTCHER_PASS,null));
        
        config.setAnticaptchaKey(get(APP_ANTICAPTCHA_KEY, null));
        
        config.setTaskNotificationUrl(get(APP_NOTI_TASK, null));
        
        config.setDisplayHome(get(APP_DISPLAY_HOME, Config.DEFAULT_DISPLAY_HOME));
        config.setDisplayGoogleSearch(get(APP_DISPLAY_GOOGLE_SEARCH, Config.DEFAULT_DISPLAY_GOOGLE_SEARCH));
        config.setDisplayGoogleTarget(get(APP_DISPLAY_GOOGLE_TARGET, Config.DEFAULT_DISPLAY_GOOGLE_TARGET));
        
        config.setPruneRuns(getInt(APP_PRUNE_RUNS, Config.DEFAULT_PRUNE_RUNS));
        
        
        return config;
    }
    
    public void updateConfig(Config config){
        update(APP_CRON_TIME, config.getCronTime() == null ? null : config.getCronTime().toString());
        
//        update(APP_DBC_USER, config.getDbcUser());
//        update(APP_DBC_PASS, config.getDbcPass());
//
//        update(APP_DECAPTCHER_USER, config.getDecaptcherUser());
//        update(APP_DECAPTCHER_PASS, config.getDecaptcherPass());
        
        update(APP_ANTICAPTCHA_KEY, config.getAnticaptchaKey());
        
        update(APP_NOTI_TASK, config.getTaskNotificationUrl());
        
        update(APP_DISPLAY_HOME, config.getDisplayHome());
        update(APP_DISPLAY_GOOGLE_SEARCH, config.getDisplayGoogleSearch());
        update(APP_DISPLAY_GOOGLE_TARGET, config.getDisplayGoogleTarget());
        
        updateInt(APP_PRUNE_RUNS, config.getPruneRuns());
    }
    
}
