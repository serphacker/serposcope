/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.extensions;

import com.serphacker.serposcope.scraper.http.proxy.SocksProxy;
import java.net.PasswordAuthentication;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ScrapClientSocksAuthenticatorTest {
    
    public ScrapClientSocksAuthenticatorTest() {
    }
    
    ScrapClientSocksAuthenticator INSTANCE = ScrapClientSocksAuthenticator.INSTANCE;

    @Test
    public void testSomeMethod() {
        assertFalse(INSTANCE.addProxy(new SocksProxy("127.0.0.1", 1010)));
        assertTrue(INSTANCE.addProxy(new SocksProxy("127.0.0.1", 1010, "login", "pass")));
        PasswordAuthentication cred = INSTANCE.credentials.get("127.0.0.1:1010");
        assertEquals("login", cred.getUserName());
        assertArrayEquals("pass".toCharArray(), cred.getPassword());        
        assertTrue(INSTANCE.addProxy(new SocksProxy("127.0.0.1", 1010, "login", "pass")));
        assertEquals(1, INSTANCE.credentials.size());
        assertTrue(INSTANCE.addProxy(new SocksProxy("127.0.0.1", 1010, "loginx", "passx")));
        assertEquals(1, INSTANCE.credentials.size());
        cred = INSTANCE.credentials.get("127.0.0.1:1010");
        assertEquals("loginx", cred.getUserName());
        assertArrayEquals("passx".toCharArray(), cred.getPassword());
    }
    
}
