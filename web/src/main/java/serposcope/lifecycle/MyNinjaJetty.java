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
import org.eclipse.jetty.server.ServerConnector;


public class MyNinjaJetty extends NinjaJetty {
    
    @Override
    protected void doConfigure() throws Exception {
        SerposcopeConf conf = new SerposcopeConf(System.getProperty("serposcope.conf"));
        conf.configure();
        conf.logEnv();
        conf.assertValid();
        
        super.doConfigure(); //To change body of generated methods, choose Tools | Templates.
        
        ServerConnector http = (ServerConnector)jetty.getConnectors()[0];
        http.setPort(conf.listenPort);
        if(conf.listenAddress != null){
            http.setHost(conf.listenAddress);
        }

        jetty.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", "2000000");
        jetty.setAttribute("org.eclipse.jetty.server.Request.maxFormKeys", "100000");
    }
    

}
