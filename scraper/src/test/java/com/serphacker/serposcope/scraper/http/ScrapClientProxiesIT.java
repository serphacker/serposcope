/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;

import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import static org.junit.Assert.assertEquals;
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
        Map<String,Object> postdata = new HashMap<>();
        String remoteUrl = "https://proxychecker.serphacker.com";
        
        postdata.put("bla", "testing");
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
        assertEquals(200, dl.post(remoteUrl, postdata, ScrapClient.PostType.URL_ENCODED));
    }

    @Test
    public void testUnresponsiveProxy() throws Exception {
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
    
}
