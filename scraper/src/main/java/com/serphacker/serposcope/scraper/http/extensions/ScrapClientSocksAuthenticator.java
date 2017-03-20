/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper.http.extensions;

import com.serphacker.serposcope.scraper.http.proxy.SocksProxy;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScrapClientSocksAuthenticator extends Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger(ScrapClientSocksAuthenticator.class);
    
    public final static ScrapClientSocksAuthenticator INSTANCE = new ScrapClientSocksAuthenticator();
    
    Map<String,PasswordAuthentication> credentials = Collections.synchronizedMap(new HashMap<>());
    
    public volatile boolean initialized = false;
    
    static  {
        ScrapClientSocksAuthenticator.set();
    }
    
    public static void set(){
        Authenticator.setDefault(INSTANCE);
        INSTANCE.initialized=true;
    }
    
    public boolean addProxy(SocksProxy proxy){
        if(proxy.getIp() == null || proxy.getUsername() == null || proxy.getPassword() == null){
            return false;
        }
        
        credentials.put(proxy.getIp() +":" + proxy.getPort(), 
            new PasswordAuthentication(proxy.getUsername(), proxy.getPassword().toCharArray()));
        
        return true;
    }
    
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        
        String protocol = getRequestingProtocol();
        if(protocol == null || !protocol.toLowerCase().startsWith("socks")){
            return super.getPasswordAuthentication();
        }
        
        String ip = getRequestingHost(); int port = getRequestingPort();
        if(ip == null || ip.isEmpty()){
            return super.getPasswordAuthentication();
        }
        
        PasswordAuthentication authInfo = credentials.get(ip + ":" + port);
        if(authInfo != null){
            return authInfo;
        }
        
        return super.getPasswordAuthentication();
    }
}
