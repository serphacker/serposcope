/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 *
 * @author admin
 */
public class ProxyDBIT extends AbstractDBIT {
    
    public ProxyDBIT() {
    }
    
    @Inject
    ProxyDB db;

    @Test
    public void test() {
        List<ScrapProxy> proxies = Arrays.asList(
            new BindProxy("127.0.0.1"),
            new HttpProxy("127.0.0.2",8080),
            new BindProxy("127.0.0.3"),
            new HttpProxy("127.0.0.4",8080,"user","pass"),
            new BindProxy("127.0.0.5")
        );
        List<Proxy> dbproxies = proxies.stream().map((ScrapProxy t) -> new Proxy(t)).collect(Collectors.toList());
        
        assertEquals(proxies.size(), db.insert(dbproxies));
        List<Proxy> dbfetched = db.list();
        List<ScrapProxy> fetched = dbfetched.stream().map(Proxy::toScrapProxy).collect(Collectors.toList());
        assertEquals(new HashSet<>(proxies), new HashSet<>(fetched));
        
        db.delete(dbfetched.stream().map(Proxy::getId).collect(Collectors.toList()));
        assertEquals(0, db.list().size());
    }
    
    @Test
    public void update(){
        HttpProxy httpProxy = new HttpProxy("127.0.0.4",8080,"user","pass");
        
        Proxy proxy = new Proxy(httpProxy);
        
        assertEquals(1, db.insert(Arrays.asList(proxy)));
        assertEquals(1, proxy.getId());
        
        proxy.setLastCheck(LocalDateTime.of(2010, 10, 10, 10, 10, 10));
        proxy.setRemoteip("1.2.3.4");
        proxy.setStatus(Proxy.Status.ERROR);
        
        db.update(proxy);
        
        List<Proxy> fetched = db.list();
        assertEquals(fetched.get(0), proxy);
    }
    
}
