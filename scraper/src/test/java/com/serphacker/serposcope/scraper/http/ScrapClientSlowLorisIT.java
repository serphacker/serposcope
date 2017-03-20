/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;

import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import org.junit.Test;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author admin
 */
public class ScrapClientSlowLorisIT extends DeepIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScrapClientSlowLorisIT.class);

    static String HTTP_RESPONSE
        = "HTTP/1.1 200 OK\r\n"
        + "Server: nginx/1.4.6 (Ubuntu)\r\n"
        + "Date: Wed, 10 Feb 2016 10:40:16 GMT\r\n"
        + "Content-Type: text/html\r\n"
        + "Connection: Close\r\n\r\n";

    int port;
    ServerSocket serverSocket;
    Thread serverThread;

    @Before
    public void before() throws Exception {
        serverThread = new Thread() {
            public void run() {
                try {
                    port = ThreadLocalRandom.current().nextInt(10000, 20000);

                    LOG.info("listening on {}", port);
                    serverSocket = new ServerSocket(port, 10);
//        serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
                    while (true) {
                        Socket sck = serverSocket.accept();

                        byte[] buffer = new byte[4096];

                        int read = sck.getInputStream().read(buffer);
                        System.err.println("read : " + read);
                        System.err.println(new String(buffer, 0, read));

                        for (int i = 0; i < HTTP_RESPONSE.length(); i++) {
                            char charC = HTTP_RESPONSE.charAt(i);
                            sck.sendUrgentData(charC);
                            Thread.sleep(1000);
                            System.err.print(charC);
                        }

                    }
                } catch (Exception ex) {
                    LOG.error("EX : ", ex);
                }
            }

        };
        serverThread.start();

    }

    @After
    public void after() {
        try {
            serverSocket.close();
        } catch (Exception ex) {
        }
        try {
            serverThread.join();
        } catch (Exception ex) {
        }
    }

    @Test
    public void testSlowLorisClientSide() throws Exception {
        Thread.sleep(2500);
        ScrapClient dl = new ScrapClient();
        String remoteUrl = "http://127.0.0.1:" + port;
        
        dl.setTimeout(5000);
        assertEquals(-1, dl.get(remoteUrl));
        assertTrue(dl.getExecutionTimeMS() < dl.getTimeout()+1000);
        LOG.info("test done");
    }
}
