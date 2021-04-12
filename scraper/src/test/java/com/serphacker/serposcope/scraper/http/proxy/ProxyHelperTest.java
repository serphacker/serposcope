/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.proxy;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ProxyHelperTest {
    
    public ProxyHelperTest() {
    }
    
    ProxyHelper helper = new ProxyHelper();

    @Test
    public void testParseHttp() {
        HttpProxy httpProxy = (HttpProxy) helper.parse("http://login:pass@127.0.0.1:1234/");
        assertEquals("127.0.0.1", httpProxy.ip);
        assertEquals(1234, httpProxy.port);
        assertEquals("login", httpProxy.username);
        assertEquals("pass", httpProxy.password);
        
        httpProxy = (HttpProxy) helper.parse("http://127.0.0.1:1234/");
        assertEquals("127.0.0.1", httpProxy.ip);
        assertEquals(1234, httpProxy.port);
        assertNull(httpProxy.username);
        assertNull(httpProxy.password);

        httpProxy = (HttpProxy) helper.parse("http://login:pass@localhost:1234/");
        assertEquals("localhost", httpProxy.ip);
        assertEquals(1234, httpProxy.port);
        assertEquals("login", httpProxy.username);
        assertEquals("pass", httpProxy.password);

        httpProxy = (HttpProxy) helper.parse("http://localhost:1234/");
        assertEquals("localhost", httpProxy.ip);
        assertEquals(1234, httpProxy.port);
        assertNull(httpProxy.username);
        assertNull(httpProxy.password);
    }
    
    @Test
    public void testParseSocks(){
        
        SocksProxy socksProxy = (SocksProxy) helper.parse("socks://login:pass@127.0.0.1:1234/");
        assertEquals("127.0.0.1", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertEquals("login", socksProxy.username);
        assertEquals("pass", socksProxy.password);
        
        socksProxy = (SocksProxy) helper.parse("socks://127.0.0.1:1234/");
        assertEquals("127.0.0.1", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertNull(socksProxy.username);
        assertNull(socksProxy.password);
        
        socksProxy = (SocksProxy) helper.parse("socks://login:pass@127.0.0.1:1234");
        assertEquals("127.0.0.1", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertEquals("login", socksProxy.username);
        assertEquals("pass", socksProxy.password);
        
        socksProxy = (SocksProxy) helper.parse("socks://127.0.0.1:1234");
        assertEquals("127.0.0.1", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertNull(socksProxy.username);
        assertNull(socksProxy.password);

        socksProxy = (SocksProxy) helper.parse("socks://login:pass@localhost:1234/");
        assertEquals("localhost", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertEquals("login", socksProxy.username);
        assertEquals("pass", socksProxy.password);

        socksProxy = (SocksProxy) helper.parse("socks://localhost:1234/");
        assertEquals("localhost", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertNull(socksProxy.username);
        assertNull(socksProxy.password);

        socksProxy = (SocksProxy) helper.parse("socks://login:pass@localhost:1234");
        assertEquals("localhost", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertEquals("login", socksProxy.username);
        assertEquals("pass", socksProxy.password);

        socksProxy = (SocksProxy) helper.parse("socks://localhost:1234");
        assertEquals("localhost", socksProxy.ip);
        assertEquals(1234, socksProxy.port);
        assertNull(socksProxy.username);
        assertNull(socksProxy.password);
    }
    
    @Test
    public void testParseBind(){
        
        BindProxy bindProxy = (BindProxy) helper.parse("bind://127.0.0.1/");
        assertEquals("127.0.0.1", bindProxy.ip);
        
        bindProxy = (BindProxy) helper.parse("bind://127.0.0.1");
        assertEquals("127.0.0.1", bindProxy.ip);        
        
    }
    
    @Test
    public void testExtractCredentials(){
        ProxyHelper.Credentials creds;
        assertNull(helper.parseCredentials("@"));
        assertNull(helper.parseCredentials("aaa@"));
        
        creds = helper.parseCredentials(":@");
        assertEquals("", creds.login);
        assertEquals("", creds.password);
        
        creds = helper.parseCredentials("a:@");
        assertEquals("a", creds.login);
        assertEquals("", creds.password);        
        
        creds = helper.parseCredentials(":a@");
        assertEquals("", creds.login);
        assertEquals("a", creds.password);    
        
        creds = helper.parseCredentials("aaa:bbb@");
        assertEquals("aaa", creds.login);
        assertEquals("bbb", creds.password);        
        
    }
    
}
