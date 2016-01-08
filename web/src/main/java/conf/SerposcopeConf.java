/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package conf;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Random;
import javax.inject.Singleton;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaModeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.Version;

@Singleton
public class SerposcopeConf {
    
    private static final Logger LOG = LoggerFactory.getLogger(SerposcopeConf.class);
    
    Random r = new Random();
    public String datadir;
    public String logdir;
    public String dbUrl;
    public boolean dbDebug;
    public String listenAddress;
    public int listenPort;

    Properties props;

    public SerposcopeConf(String filename) {
        props = new Properties();
        props.putAll(System.getProperties());
        
        if(filename == null || filename.isEmpty()){
            return;
        }
        
        File file = new File(filename);
        if(!file.exists() || !file.canRead()){
            return;
        }
        
        try {
            props.load(new FileInputStream(file));
        } catch(Exception ex){
            LOG.error("can't read property file {}", filename, ex);
            return;
        }
        
        props.putAll(System.getProperties());
    }
    
    public static boolean isTesting(){
        String currentTest = System.getProperty("test");
        if(currentTest != null && !currentTest.isEmpty()){
            return true;
        }
        if(NinjaModeHelper.determineModeFromSystemPropertiesOrProdIfNotSet().equals(NinjaMode.test)){
            return true;
        }
        return false;
    }
    
    public void configure(){
        String currentTest = System.getProperty("test");
        if(isTesting()){
            LOG.warn("configure env for test mode (running test {})", currentTest);
            configureTestEnv();
        } else {
            configureProdEnv();
        }

    }
    
    protected void configureProdEnv(){
        datadir = props.getProperty("serposcope.datadir", 
            isWindows() ?
            (System.getenv("APPDATA") + "/serposcope") :
            (System.getProperty("user.home") + "/serposcope")
        );
        logdir = props.getProperty("serposcope.logdir", datadir + "/logs");
        dbUrl = props.getProperty("serposcope.db.url", "jdbc:h2:" + datadir + "/db");
        if(dbUrl.startsWith("jdbc:h2")){
            String dbOptions = props.getProperty("serposcope.db.options", "");
            if(!dbOptions.toLowerCase().contains(";mode=mysql")){
                dbOptions += ";MODE=MySQL";
            }
            dbUrl += dbOptions;
        }
        dbDebug = "true".equals(props.getProperty("serposcope.db.debug", "false"));
        listenAddress = props.getProperty("serposcope.listenAddress", "0.0.0.0");
        try {
            listenPort = Integer.parseInt(props.getProperty("serposcope.listenPort", "7134"));
        } catch(Exception ex){
            listenPort = 7134;
        }
    }
    
    protected void configureTestEnv(){
        datadir = props.getProperty("serposcope.datadir", 
            System.getProperty("java.io.tmpdir") + "/serposcope/" + (100000 +r.nextInt(100000))
        );        
        logdir = props.getProperty("serposcope.logdir", datadir + "/logs");
        dbUrl = props.getProperty("serposcope.db.url", "jdbc:h2:mem:integrationtest");
        if(dbUrl.startsWith("jdbc:h2")){
            String dbOptions = props.getProperty("serposcope.db.options", "");
            if(!dbOptions.toLowerCase().contains(";mode=mysql")){
                dbOptions += ";MODE=MySQL";
            }
            dbUrl += dbOptions;
        }
        dbDebug = "true".equals(props.getProperty("serposcope.db.debug", "false"));
        listenAddress = props.getProperty("serposcope.listenAddress", "0.0.0.0");
        try {
            listenPort = Integer.parseInt(props.getProperty("serposcope.listenPort", "1024"));
        } catch(Exception ex){
            listenPort = 1024;
        }
    }
    
    protected boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("win");
    }    
    
    public void logEnv(){
        LOG.info("serposcope.version          : " + Version.CURRENT);
        LOG.info("serposcope.datadir          : " + datadir);
        LOG.info("serposcope.logdir           : " + logdir);
        LOG.info("serposcope.db.url           : " + dbUrl);
        LOG.info("serposcope.db.debug         : " + dbDebug);
        LOG.info("serposcope.listenAddress    : " + listenAddress);
        LOG.info("serposcope.listenPort       : " + listenPort);
    }
    
    public String dumpEnv(){
        return 
            "serposcope.version          : " + Version.CURRENT + "\n" + 
            "serposcope.datadir          : " + datadir + "\n" + 
            "serposcope.logdir           : " + logdir + "\n" + 
            "serposcope.db.url           : " + dbUrl + "\n" + 
            "serposcope.db.debug         : " + dbDebug + "\n" + 
            "serposcope.listenAddress    : " + listenAddress + "\n" + 
            "serposcope.listenPort       : " + listenPort;
    }
    
    public void assertValid(){
        File fDatadir = new File(datadir);
        if(!fDatadir.exists() && !fDatadir.mkdirs()){
            LOG.error("can't create or white in data directory \"" + datadir + "\"");
            LOG.error("please specify -Dserposcope.datadir=/full/path/to/datadir options");
            System.exit(1);
        }
        
        File fLogDir = new File(logdir);
        if(!fLogDir.exists() && !fLogDir.mkdirs()){
            LOG.error("can't create or white in log directory \"" + logdir + "\"");
            LOG.error("please specify -Dserposcope.logdir=/full/path/to/logdir options");
            System.exit(1);
        }
        
        File fConfFile = new File(datadir + "/serposcope.conf");
        if(!fConfFile.exists()){
            try {
                Files.copy(ClassLoader.class.getResourceAsStream("/serposcope.conf"), fConfFile.toPath());
            }catch(Exception ex){
            }
        }
    }
}
