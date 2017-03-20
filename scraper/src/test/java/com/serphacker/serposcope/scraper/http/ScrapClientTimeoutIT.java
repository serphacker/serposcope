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
import com.serphacker.serposcope.scraper.http.proxy.SocksProxy;
import java.io.InterruptedIOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class ScrapClientTimeoutIT extends DeepIntegrationTest {
        
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
    public void testTimeouts() throws Exception{
        int status = 0;
        ScrapClient cli = new ScrapClient();
        
        status = cli.get("https://httpbin.org/delay/2");
        assertEquals(200, status);
        
        cli.setTimeout(100);
        
        status = cli.get("https://httpbin.org/delay/2");
        assertEquals(-1, status);
        assertTrue(cli.getException() instanceof InterruptedIOException);
        
        
        cli.setTimeout(null);
        
        status = cli.get("https://httpbin.org/delay/2");
        assertEquals(200, status);
    }
}
