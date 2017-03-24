/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google.scraper;

import com.serphacker.serposcope.scraper.captcha.solver.SwingUICaptchaSolver;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
@RunWith(MockitoJUnitRunner.class)
public class GoogleScraperIT {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleScraperIT.class);

    public GoogleScraperIT() {
    }


//    @Test
    public void testHandleCaptcha() throws Exception {
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
        solver.init();

        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
//        http.setProxy(new HttpProxy("127.0.0.1", 8080));
        GoogleScraper scraper = new GoogleScraper(http, solver);
        assertEquals(OK, scraper.handleCaptchaRedirect("http://www.google.fr/search?q=100", null, "https://ipv4.google.com/sorry/index?continue=https://www.google.fr/"));
    }
    
    @Test
    public void testUule() throws Exception {
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
        solver.init();
        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
        String[] places = new String[]{"Paris", "Lille"};

        for (String place : places) {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setLocal(place);
            search.setKeyword("restaurant");
            search.setTld("fr");
            search.setPages(1);
            search.setResultPerPage(10);

            GoogleScraper scraper = new GoogleScraper(http, solver);
            GoogleScrapResult result = scraper.scrap(search);
            assertEquals(OK, result.status);
            boolean success = false;
            for (String url : result.urls) {
                System.out.println(url);
                if (url.toLowerCase().contains(place.toLowerCase())) {
                    success = true;
                    break;
                }
            }
            assertTrue(success);
        }
    }

//    @Test
    public void testDatacenter() throws Exception {
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
        solver.init();

        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
        GoogleScraper scraper = new GoogleScraper(http, solver);

        {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setKeyword("restaurant");
            search.setTld("fr");
            search.setDatacenter("173.194.32.248");
            search.setPages(3);
            search.setResultPerPage(10);
            assertEquals(OK, scraper.scrap(search).status);
        }
        
        {
            GoogleScrapSearch search = new GoogleScrapSearch();
            search.setKeyword("restaurant");
            search.setDatacenter("173.194.32.224");
            search.setPages(3);
            search.setResultPerPage(10);
            assertEquals(OK, scraper.scrap(search).status);
        }        
        
    }
    
    @Test
    public void testDebugDump(){
        GoogleScraper scraper = new GoogleScraper(null, null);
        scraper.debugDump("filename", "test data 2");
    }

    
}
