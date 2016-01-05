/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.services;

import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.scheduler.Schedule;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.Version;

public class Scheduler {
    
    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
    
    @Inject
    NinjaProperties props;
    
    @Inject
    BaseDB baseDB;
    
    Version lastVersion = null;
        
    
    @Schedule(delay = 6, initialDelay = 0, timeUnit = TimeUnit.HOURS)
    public void checkForUpdate() throws UnsupportedEncodingException{
        if(!props.isProd()){
            return;
        }
        
        String url = "https://serposcope.serphacker.com/update/check.php" +
            "?uid=" + baseDB.config.get(ConfigDB.APP_INSTALLID, "") + 
            "&version=" + Version.CURRENT + 
            "&os=" + URLEncoder.encode(osVersion(), "utf-8");
        
        ScrapClient httpClient = new ScrapClient(true);
        httpClient.get(url);
        try  {
            lastVersion = new Version(httpClient.getContentAsString().replaceAll("(^\\s+)|(\\s+$)", ""));
            LOG.info("last version {} | current version {}", lastVersion, Version.CURRENT);
        }catch(Exception ex){
            LOG.warn("can't fetch last version from serphacker.com");
        }
        
    }
    
    protected String osVersion(){
        return System.getProperty("os.name") + "|" + System.getProperty("os.arch")  + "|" + System.getProperty("java.version");
    }
    
    public boolean hasNewVersion(){
        return lastVersion != null && lastVersion.compareTo(Version.CURRENT) > 0;
    }
    
}
