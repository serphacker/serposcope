/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;

import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ScrapClientSSLIT extends DeepIntegrationTest {

    public ScrapClientSSLIT() {
    }
    
    @Test
    public void testSslWithInvalidHostnameFail() throws Exception {
        ScrapClient client = new ScrapClient();
        client.get("https://54.175.219.8");
        assertTrue(client.getException() instanceof SSLPeerUnverifiedException);
    }
    
    @Test
    public void testSslWithInvalidHostname() throws Exception {
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        assertEquals(200, client.get("https://54.175.219.8"));
    }    
    
//    @Test(expected = SSLHandshakeException.class)
//    public void testSslWithSelfSignedFail() throws Exception {
//        ScrapClient client = new ScrapClient(false);
//        CloseableHttpResponse response = client.execute(new HttpGet("https://selfsigned.indahax.com"));
//    }
    
//    @Test
//    public void testSslWithSelfSigned() throws Exception {
//        ScrapClient client = new ScrapClient(true);
//        CloseableHttpResponse response = client.execute(new HttpGet("https://selfsigned.indahax.com"));
//        assertEquals(200, response.getStatusLine().getStatusCode());
//    }    

    
    @Test
    public void testMitmSslProxyWithSelfSignedCertificateFail() {
        ScrapClient client = new ScrapClient();
        client.setProxy(new HttpProxy("127.0.0.1", 8080));
        client.get("https://httpbin.org");
        assertTrue(client.getException() instanceof SSLHandshakeException);
    }

    @Test
    public void testMitmSslProxyWithSelfSignedCertificateSuccess() {
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        client.setProxy(new HttpProxy("127.0.0.1", 8080));
        assertEquals(200, client.get("https://httpbin.org"));
    }
    
    @Test
    public void testInsecureSwitching() throws Exception {
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        client.setProxy(new HttpProxy("127.0.0.1", 8080));
        assertEquals(200, client.get("https://httpbin.org"));
        
        boolean occured=false;
        client.setInsecureSSL(false);
        assertEquals(-1, client.get("https://httpbin.org"));
    }    
    
    
}
