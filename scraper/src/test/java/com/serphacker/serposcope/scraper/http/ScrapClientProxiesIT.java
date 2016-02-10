/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;

import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import com.serphacker.serposcope.scraper.http.extensions.ScrapClientSocksAuthenticator;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import com.serphacker.serposcope.scraper.http.proxy.SocksProxy;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author admin
 */
public class ScrapClientProxiesIT extends DeepIntegrationTest {

    public ScrapClientProxiesIT() {
    }

    /* SQUID configuration
    
# sudo htpasswd -c /etc/squid3/passwords user
auth_param basic program /usr/lib/squid3/basic_ncsa_auth /etc/squid3/passwords
auth_param basic realm proxy
acl authenticated proxy_auth REQUIRED
http_access allow authenticated


http_port 3128

acl src1 localip 127.0.0.1/32
tcp_outgoing_address 127.0.0.1 src1

acl src2 localip 127.0.0.2/32
tcp_outgoing_address 127.0.0.2 src2

acl src3 localip 127.0.0.3/32
tcp_outgoing_address 127.0.0.3 src3
     */
 /*
        TODO : use jetty for unit tests
        <?php
        header('Content-Type: text/plain');
        echo $_SERVER['REMOTE_ADDR'];
        ?>
     */
    public final static String LOCAL_IP_WEBSERVICE = "http://localhost/tests/ip.php";

    @Test
    public void testInterfaces() throws Exception {
        String[] interfaces = new String[]{"127.0.0.2", "127.0.0.3"};
        try (ScrapClient cli = new ScrapClient()) {
            assertEquals("127.0.0.1", EntityUtils.toString(cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE)).getEntity()));

            for (String aInterface : interfaces) {
                cli.setProxy(new BindProxy(aInterface));
                assertEquals(aInterface, EntityUtils.toString(cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE)).getEntity()));
            }
            cli.setProxy(null);
            assertEquals("127.0.0.1", EntityUtils.toString(cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE)).getEntity()));
        }
    }

    @Test
    public void testHttpProxy() throws Exception {
        try (ScrapClient cli = new ScrapClient()) {
            cli.setProxy(new HttpProxy("127.0.0.1", 3128));
            try (CloseableHttpResponse response = cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE))) {
                assertEquals(407, response.getStatusLine().getStatusCode());
            }

            cli.setProxy(new HttpProxy("127.0.0.1", 3128, "userx", "passx"));
            try (CloseableHttpResponse response = cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE))) {
                assertEquals(407, response.getStatusLine().getStatusCode());
            }

            cli.setProxy(new HttpProxy("127.0.0.2", 3128, "user", "pass"));
            try (CloseableHttpResponse response = cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE))) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals("127.0.0.2", EntityUtils.toString(response.getEntity()));
            }

            cli.setProxy(null);
            try (CloseableHttpResponse response = cli.execute(new HttpGet(LOCAL_IP_WEBSERVICE))) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals("127.0.0.1", EntityUtils.toString(response.getEntity()));
            }
        }
    }

    @Test
    public void testHttpProxyAuthWithSiteAuth() throws Exception {

        String username = "uuu";
        String password = "ppp";
        String url = "https://httpbin.org/basic-auth/" + username + "/" + password;

        try (ScrapClient cli = new ScrapClient()) {
            cli.setCredentials(new AuthScope("httpbin.org", 443), new UsernamePasswordCredentials(username, password));
            cli.setProxy(new HttpProxy("127.0.0.1", 3128, "user", "pass"));

            {
                CloseableHttpResponse response = cli.execute(new HttpGet(url));
                assertEquals(200, response.getStatusLine().getStatusCode());
                EntityUtils.consumeQuietly(response.getEntity());
            }
            {
                CloseableHttpResponse response = cli.execute(new HttpGet(url));
                assertEquals(200, response.getStatusLine().getStatusCode());
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @Test
    public void testBadProxy1() throws Exception {
        ScrapClient dl = new ScrapClient();
        dl.setInsecureSSL(true);
        dl.setProxy(new HttpProxy("127.0.0.1", 8080));
        Map<String, Object> postdata = new HashMap<>();
        String remoteUrl = "https://proxychecker.serphacker.com";

        postdata.put("bla", "testing");
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
    }

    // socat TCP-LISTEN:1234,FORK -
    @Test
    public void testUnresponsiveHttpProxy() throws Exception {
        ScrapClient dl = new ScrapClient();
        dl.setProxy(new HttpProxy("127.0.0.1", 1234));
        String remoteUrl = "https://127.0.0.1:1235";

        int timeoutMS = 3333;
        dl.setTimeout(timeoutMS);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

        dl.setTimeout(timeoutMS = 1250);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);
    }
    
    @Test
    public void testUnresponsiveSocksProxy() throws Exception {
        ScrapClient dl = new ScrapClient();
        dl.setProxy(new SocksProxy("127.0.0.1", 1234));
        String remoteUrl = "https://127.0.0.1:1235";

        int timeoutMS = 3333;
        dl.setTimeout(timeoutMS);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

        dl.setTimeout(timeoutMS = 1250);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);
    }    
    
    @Test
    public void testUnresponsiveDirectToHttp() throws Exception {
        ScrapClient dl = new ScrapClient();
        String remoteUrl = "http://127.0.0.1:1234";

        int timeoutMS = 3333;
        dl.setTimeout(timeoutMS);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

        dl.setTimeout(timeoutMS = 1250);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);
    }        
    
    @Test
    public void testUnresponsiveDirectToSSL() throws Exception {
        ScrapClient dl = new ScrapClient();
        String remoteUrl = "https://127.0.0.1:1234";

        int timeoutMS = 3333;
        dl.setTimeout(timeoutMS);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

        dl.setTimeout(timeoutMS = 1250);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);
    }     

    @Test
    public void testUnresponsiveHttpServer() throws Exception {
        ScrapClient dl = new ScrapClient();
        int statusCode = 0;
//        dl.setProxy(new HttpProxy("127.0.0.1", 1234));

        String remoteUrl = "http://127.0.0.1:1234";

        int timeoutMS = 3333;
        dl.setTimeout(timeoutMS);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

        dl.setTimeout(timeoutMS = 1250);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue("Execution time : " + dl.getExecutionTimeMS(), dl.getExecutionTimeMS() >= timeoutMS);

    }
    
    @Test
    public void testSocksAuth() throws Exception {
        ScrapClient httpClient1 = new ScrapClient();
        SocksProxy socksProxy1 = new SocksProxy(
            props.getProperty("socks2.ip"), Integer.parseInt(props.getProperty("socks2.port")),
            props.getProperty("socks2.login"), props.getProperty("socks2.pass")
        );
        httpClient1.setProxy(socksProxy1);
        
        ScrapClient httpClient2 = new ScrapClient();
        SocksProxy socksProxy2 = new SocksProxy(
            props.getProperty("socks3.ip"), Integer.parseInt(props.getProperty("socks3.port")),
            props.getProperty("socks3.login"), props.getProperty("socks3.pass")
        );
        httpClient2.setProxy(socksProxy2);
        
        ScrapClient httpClient3 = new ScrapClient();
        SocksProxy socksProxy3 = new SocksProxy(
            props.getProperty("socks4.ip"), Integer.parseInt(props.getProperty("socks4.port")),
            props.getProperty("socks4.login"), props.getProperty("socks4.pass")
        );
        httpClient3.setProxy(socksProxy3);
        
        assertEquals(200, httpClient1.get("http://httpbin.org/ip"));
        assertTrue(httpClient1.getContentAsString().contains(socksProxy1.getIp()));
        
        assertEquals(200, httpClient2.get("http://httpbin.org/ip"));
        assertTrue(httpClient2.getContentAsString().contains(socksProxy2.getIp()));        
        
        assertEquals(200, httpClient3.get("http://httpbin.org/ip"));
        assertTrue(httpClient3.getContentAsString().contains(props.getProperty("socks4.eip")));
        
        assertEquals(200, httpClient1.get("http://httpbin.org/ip"));
        assertTrue(httpClient1.getContentAsString().contains(socksProxy1.getIp()));
        
        assertEquals(200, httpClient2.get("http://httpbin.org/ip"));
        assertTrue(httpClient2.getContentAsString().contains(socksProxy2.getIp()));       
        
        assertEquals(200, httpClient3.get("http://httpbin.org/ip"));
        assertTrue(httpClient3.getContentAsString().contains(props.getProperty("socks4.eip")));        
        
    }

    @Test
    public void testSocksMix() throws Exception {
        int statusCode;
        String html;
        ScrapClient httpClient = new ScrapClient();
        String remoteIp = "";
        SocksProxy socksProxy = new SocksProxy(
            props.getProperty("socks2.ip"), Integer.parseInt(props.getProperty("socks2.port")),
            props.getProperty("socks2.login"), props.getProperty("socks2.pass")
        );

        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));        
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));         

        
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("http://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));        
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(socksProxy);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertTrue(httpClient.getContentAsString().contains(socksProxy.getIp()));
        
        httpClient.setProxy(null);
        assertEquals(200, httpClient.get("https://httpbin.org/ip"));
        assertFalse(httpClient.getContentAsString().contains(socksProxy.getIp()));  
    }

}
