/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.lifecycle;

import conf.SerposcopeConf;
import ninja.standalone.NinjaJetty;


public class MyNinjaJetty extends NinjaJetty {

    @Override
    protected void doConfigure() throws Exception {
        SerposcopeConf conf = new SerposcopeConf(System.getProperty("serposcope.conf"));
        conf.configure();
        conf.logEnv();
        conf.assertValid();
        
        port = conf.listenPort;
        host = conf.listenAddress;
        super.doConfigure(); //To change body of generated methods, choose Tools | Templates.
        
    }
    
    
    
    

}
