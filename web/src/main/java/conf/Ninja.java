/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package conf;

import com.fasterxml.uuid.Generators;
import com.google.inject.Inject;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.db.base.RunDB;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.task.TaskManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import ninja.NinjaDefault;
import ninja.template.TemplateEngineFreemarker;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;
import ninja.utils.SecretGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.Version;
import serposcope.helpers.CookieEncryptionOverride;
import serposcope.helpers.CryptoOverride;
import serposcope.services.Scheduler;

public class Ninja extends NinjaDefault {
    
    private static final Logger LOG = LoggerFactory.getLogger(Ninja.class);

    @Inject
    TemplateEngineFreemarker freemarker;
    
    @Inject
    BaseDB db;
    
    @Inject
    NinjaProperties props;
    
    @Inject
    CryptoOverride  crypto;

    @Inject
    CookieEncryptionOverride cookieEncryption;
    
    @Inject
    Scheduler update;
    
    @Inject
    TaskManager taskManager;

    @Override
    public void onFrameworkStart() {
        try {
            freemarker.getConfiguration().setAPIBuiltinEnabled(true);
            Map<String,Object> global = new HashMap<>();
            global.put("ldtf", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            global.put("version", Version.CURRENT);
            global.put("prod", props.isProd());
            global.put("update", update);
            global.put("lastlog", LocalDate.now().toString() + ".log");
            freemarker.getConfiguration().setSharedVariable("global", global);
            freemarker.getConfiguration().addAutoImport("fu","/serposcope/views/helpers/functions.ftl.html");
            
            if(!db.migration.isDbCreated()){
                LOG.warn("initial database creation");
                
                db.migration.recreateDb();
                
                if(!db.migration.isDbCreated()){
                    throw new IllegalStateException("can't recreate database");
                }
            } else {
                
                try {
                    db.migration.migrateIfNeeded();
                }catch(Exception ex){
                    LOG.error("database migration failed", ex);
                    System.exit(1);
                }
                
            }
            
            
            String installID = db.config.get(ConfigDB.APP_INSTALLID, null);
            if(installID == null){
                UUID randomUUID = Generators.timeBasedGenerator().generate();
                db.config.update(ConfigDB.APP_INSTALLID, randomUUID.toString());
            }
            
            String appSecret = db.config.get(ConfigDB.APP_SECRET, null);
            if(appSecret == null){
                LOG.warn("Generating application secret (should only happen on first launch)");
                appSecret = SecretGenerator.generateSecret();
                db.config.update(ConfigDB.APP_SECRET, appSecret);
                if(!appSecret.equals(db.config.get(ConfigDB.APP_SECRET, null))){
                    LOG.error("Can't save generated application secret (is database write permission ok ?)");
                    System.exit(1);
                }
            }
            
            ((NinjaPropertiesImpl)props).setProperty(NinjaConstant.applicationSecret, appSecret);
            crypto.update(props);
            cookieEncryption.update(props);
            
            List<Run> runs = db.run.listByStatus(RunDB.STATUSES_RUNNING, null, null);
            if(!runs.isEmpty()){
                LOG.warn("Mark crashed {} running task", runs.size());
                for (Run run : runs) {
                    run.setStatus(Run.Status.DONE_CRASHED);
                    run.setFinished(LocalDateTime.now());
                    db.run.updateStatus(run);
                    db.run.updateFinished(run);
                }
            }
            super.onFrameworkStart();
        }catch(Exception ex){
            LOG.error("fatal error", ex);
            System.exit(1);
        }
    }

    @Override
    public void onFrameworkShutdown() {
        try {if(taskManager.abortGoogleTask(true)){Thread.sleep(1000);}}catch(Exception ex){}
        super.onFrameworkShutdown();
    }
    
    private static long makeEpoch() {
        // UUID v1 timestamp must be in 100-nanoseconds interval since 00:00:00.000 15 Oct 1582.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
        c.set(Calendar.YEAR, 1582);
        c.set(Calendar.MONTH, Calendar.OCTOBER);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }    
    
    
}
