/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task.proxy;

import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ProxyCheckerTesting extends AbstractDBIT {
    
    public ProxyCheckerTesting() {
    }
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    protected Configuration dbTplConf;
    
    @Test
    public void testOOO(){
        System.out.println(dbTplConf.asLiteral("a'a"));
    }

    @Test
    public void testSomeMethod() throws InterruptedException {
        List<ScrapProxy> proxies = new ArrayList<>();
        for (int i = 2; i < 3; i++) {
            proxies.add(new BindProxy("127.0.0." + i));
        }
        List<Proxy> dbproxies = proxies.stream().map((ScrapProxy t) -> new Proxy(t)).collect(Collectors.toList());
        
        baseDB.proxy.insert(dbproxies);
        
        ProxyChecker checker = new ProxyChecker(baseDB, 1, 15000);
        checker.start();
        checker.join();
    }
    
    
    
}
