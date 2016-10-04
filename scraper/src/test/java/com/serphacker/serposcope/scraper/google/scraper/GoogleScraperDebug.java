/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google.scraper;

import com.google.common.io.ByteStreams;
import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import com.serphacker.serposcope.scraper.captcha.solver.DecaptcherSolver;
import com.serphacker.serposcope.scraper.captcha.solver.SwingUICaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.DirectNoProxy;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
@RunWith(MockitoJUnitRunner.class)
public class GoogleScraperDebug extends DeepIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleScraperDebug.class);
    
    @Test
    public void debugScrapSingle() throws IOException, InterruptedException {
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
//        DecaptcherSolver solver = new DecaptcherSolver(props.getProperty("decaptcher.login"), props.getProperty("decaptcher.password"));
        solver.init();
        
        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
        http.setProxy(new HttpProxy("127.0.0.1", 8181));
        GoogleScraper scraper = new GoogleScraper(http, solver);
        System.out.println(props.getProperty("decaptcher.login") + "|" + props.getProperty("decaptcher.password"));

        String[] keywords = new String[]{
            "forex"
        };
        
        for (String keyword : keywords) {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setKeyword(keyword);
            search.setPages(10);
            search.setResultPerPage(100);
//            search.setDatacenter("173.194.32.238");
//            search.setMinPauseBetweenPage(1000);
//            search.setMaxPauseBetweenPage(5000);
            search.setTld("fr");
            search.setCustomParameters("filter=0");

            GoogleScrapResult res = scraper.scrap(search);
            System.out.println(res.status);
            System.out.println(res.urls.size() + "|" + new HashSet<>(res.urls).size());
        }
    }    

    @Test
    public void debugScrap() throws IOException, InterruptedException {
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
//        DecaptcherSolver solver = new DecaptcherSolver(props.getProperty("decaptcher.login"), props.getProperty("decaptcher.password"));
        solver.init();
        
        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
        http.setProxy(new HttpProxy("127.0.0.1", 8080));
        GoogleScraper scraper = new GoogleScraper(http, solver);

        String[] keywords = new String[]{
            "kolozsvr",
            "leyden",
            "liberatory",
            "barcarole",
            "bedlamise",
            "unfreed",
            "unsacked",
            "criminative",
            "hippiatric",
            "banque",
            "banques",
            "banque en ligne",
            "bourse",
            "bourse en ligne",
            "trading",
            "forex"
        };
        
        for (String keyword : keywords) {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setKeyword(keyword);
            search.setPages(10);
            search.setResultPerPage(100);
//            search.setDatacenter("173.194.32.238");
//            search.setMinPauseBetweenPage(1000);
//            search.setMaxPauseBetweenPage(5000);
            search.setTld("fr");
            search.setCustomParameters("filter=0");

            GoogleScrapResult res = scraper.scrap(search);
            System.out.println(res.status);
            System.out.println(res.urls.size() + "|" + new HashSet<>(res.urls).size());
        }
    }

//    @Test
    public void debugParseSerp() throws IOException {

        String[] files = new String[]{
            "last-page-com-desktop",
            "last-page-com-mobile",
            "logo-com-desktop",
            "logo-com-mobile",
            "politique-com-desktop",
            "politique-com-mobile",
            "serposcope-com-desktop",
            "serposcope-com-mobile",
            "serposcope-fr-desktop",
            "serposcope-fr-mobile"
        };

        for (String file : files) {
            System.out.println(file);
            String content = new String(ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/serps/" + file)));

            ScrapClient http = mock(ScrapClient.class);
            when(http.getContentAsString()).thenReturn(content);

            GoogleScraper scraper = new GoogleScraper(http, null);
            scraper.parseSerp(new ArrayList<>());
            System.out.println("xxx|" + scraper.hasNextPage());
//            assertEquals(OK, http);
            System.out.println("---");
        }

    }

    @Test
    public void debugScrapSomeSerp() throws Exception {

        GoogleDevice[] devices = new GoogleDevice[]{GoogleDevice.DESKTOP, GoogleDevice.SMARTPHONE, GoogleDevice.MOBILE};
        String[] keywords = new String[]{
            "serphacker"
        };
        for (String keyword : keywords) {
            for (GoogleDevice device : devices) {
                GoogleScrapSearch search = new GoogleScrapSearch();
                search.setKeyword(keyword);
                search.setDevice(device);
                search.setTld("fr");

                ScrapClient http = new ScrapClient();
                GoogleScraper scraper = new GoogleScraper(http, null);

                scraper.prepareHttpClient(search);
                String url = scraper.buildRequestUrl(search, 0);

                assertEquals(OK, scraper.downloadSerp(url, null, search));
                Files.write(
                    new File(System.getProperty("java.io.tmpdir") + "/serps/" + keyword + "-"
                        + search.getTld() + "-"
                        + device.toString().toLowerCase() + ".html"
                    ).toPath(),
                    scraper.getHttp().getContent()
                );
                Thread.sleep(1000l);
            }
        }
    }
    
    @Test
    public void debugParseSomeSerp() throws Exception{
        ScrapClient http = mock(ScrapClient.class);
        GoogleScraper scraper = new GoogleScraper(http, null);
        
        //String[] splits = new String(ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/serps/actu"))).split("\n");
        String[] splits  = new String[]{
            "serphacker/serphacker-fr-desktop",
            "serphacker/serphacker-fr-mobile",
            "serphacker/serphacker-fr-smartphone"
        };
        for (String split : splits) {
            String content = new String(ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/serps/" + split)));
            when(http.getContentAsString()).thenReturn(content);
            List<String> urls = new ArrayList<>();
            
            System.out.println(split);
            scraper.parseSerp(urls);
            for (int i = 0; i < urls.size(); i++) {
                System.out.println(urls.get(i));
            }
            System.out.println("---");
            
        }
    }

//    @Test
    public void debugScrapLastPage() throws Exception {

        GoogleDevice[] devices = new GoogleDevice[]{GoogleDevice.DESKTOP, GoogleDevice.MOBILE};
        for (GoogleDevice device : devices) {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setKeyword("\"serposcope\" \"en ligne\" \"fort\"");
            search.setDevice(device);

            ScrapClient http = new ScrapClient();
            GoogleScraper scraper = new GoogleScraper(http, null);

            scraper.prepareHttpClient(search);
            String url = scraper.buildRequestUrl(search, 1);

            assertEquals(OK, scraper.downloadSerp(url, null, search));
            Files.write(new File(System.getProperty("java.io.tmpdir") + "/serps/last-page." + device.toString().toLowerCase()).toPath(), scraper.getHttp().getContent());
            Thread.sleep(1000l);
        }
    }

}
