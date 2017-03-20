/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package serposcope.lifecycle;

import conf.SerposcopeConf;
import java.io.File;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;

@Singleton
public class DBSizeUtils {

    @Inject
    SerposcopeConf conf;
    
    public long getDbUsage() {
        File file = getDbFile();
        if(file == null){
            return -1l;
        }
        return file.length();
    }
    
    public String getDbUsageFormatted() {
        long dbUsage = getDbUsage();
        if(dbUsage == -1){
            return null;
        }
        return FileUtils.byteCountToDisplaySize(dbUsage);
    }    
    
    public long getDiskFree() {
        File file = getDbFile();
        if(file == null){
            file = new File(System.getProperty("user.dir"));
            if(!file.exists()){
                return -1l;
            }
        }
        return file.getFreeSpace();
    }
    
    public String getDiskFreeFormatted() {
        long diskFree = getDiskFree();
        if(diskFree == -1){
            return null;
        }
        return FileUtils.byteCountToDisplaySize(diskFree);
    }       
    
    protected File getDbFile() {
        if(conf == null || conf.dbUrl == null){
            return null;
        }
        
        if(!conf.dbUrl.startsWith("jdbc:h2:")){
            return null;
        }
            
        String filename= conf.dbUrl.substring(8);
        int indexOfComma = filename.indexOf(';');
        if(indexOfComma != -1) {
            filename = filename.substring(0, indexOfComma);
        }
        
        File file = new File(filename + ".mv.db");
        if(!file.exists()){
            return null;
        }
        
        return file;
    }
    
}
