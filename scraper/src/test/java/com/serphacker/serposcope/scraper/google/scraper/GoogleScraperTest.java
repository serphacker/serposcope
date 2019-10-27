/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 *
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google.scraper;

import com.serphacker.serposcope.scraper.ResourceHelper;
import com.serphacker.serposcope.scraper.google.GoogleCountryCode;

import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.ERROR_NETWORK;
import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;

import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.http.ScrapClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

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
 * @author admin
 */
@RunWith(MockitoJUnitRunner.class)
public class GoogleScraperTest {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleScraperTest.class);

    public GoogleScraperTest() {
    }

    @Test
    public void testBuildUrl() {
        GoogleScraper scraper = new GoogleScraper(null, null);

        GoogleScrapSearch search = null;
        String url = null;

        search = new GoogleScrapSearch();
        search.setKeyword("keyword");
        assertEquals("https://www.google.com/search?q=keyword", scraper.buildRequestUrl(search, 0));

        search = new GoogleScrapSearch();
        search.setKeyword("keyword");
        assertEquals("https://www.google.com/search?q=keyword&start=10", scraper.buildRequestUrl(search, 1));

        search = new GoogleScrapSearch();
        search.setKeyword("keyword");
        search.setDatacenter("10.0.0.1");
        assertEquals("https://www.google.com/search?q=keyword", scraper.buildRequestUrl(search, 0));
    }

    @Test
    public void testParseSerpEmpty() throws IOException {
        ScrapClient http = mock(ScrapClient.class);
        when(http.getContentAsString()).thenReturn("");

        GoogleScraper scraper = new GoogleScraper(http, null);
        assertEquals(ERROR_NETWORK, scraper.parseSerp(new ArrayList<>()));
    }

    @Test
    public void testDownloadNetworkError() throws Exception {
        ScrapClient http = mock(ScrapClient.class);
        when(http.get(any(), any())).thenReturn(-1);

        GoogleScrapSearch search = new GoogleScrapSearch();
        search.setKeyword("suivi de position");
        search.setCountry(GoogleCountryCode.FR);

        GoogleScraper scraper = new GoogleScraper(http, null);
        assertEquals(ERROR_NETWORK, scraper.scrap(search).status);
    }

    @Test
    public void testParseNetworkError() throws Exception {
        ScrapClient http = mock(ScrapClient.class);
        when(http.get(any(), any())).thenReturn(200);
        when(http.getContentAsString()).thenReturn("");

        GoogleScrapSearch search = new GoogleScrapSearch();
        search.setKeyword("suivi de position");
        search.setCountry(GoogleCountryCode.FR);

        GoogleScraper scraper = new GoogleScraper(http, null);
        assertEquals(ERROR_NETWORK, scraper.scrap(search).status);
    }


    @Test
    public void testBuildUule() {
        GoogleScraper scraper = new GoogleScraper(null, null);
        assertEquals(
            "w+CAIQICIpTW9udGV1eCxQcm92ZW5jZS1BbHBlcy1Db3RlIGQnQXp1cixGcmFuY2U",
            scraper.buildUule("Monteux,Provence-Alpes-Cote d'Azur,France").replaceAll("=+$", "")
        );
        assertEquals(
            "w+CAIQICIGRnJhbmNl",
            scraper.buildUule("France").replaceAll("=+$", "")
        );
        assertEquals(
            "w+CAIQICIlQ2VudHJlLVZpbGxlLENoYW1wYWduZS1BcmRlbm5lLEZyYW5jZQ",
            scraper.buildUule("Centre-Ville,Champagne-Ardenne,France").replaceAll("=+$", "")
        );
        assertEquals(
            "w+CAIQICIfTGlsbGUsTm9yZC1QYXMtZGUtQ2FsYWlzLEZyYW5jZQ",
            scraper.buildUule("Lille,Nord-Pas-de-Calais,France").replaceAll("=+$", "")
        );
    }

    @Test
    public void extractResults() {
        GoogleScraper scraper = new GoogleScraper(null, null);
        assertEquals(2490l, scraper.extractResultsNumber("Environ 2 490 résultats"));
//        assertEquals(25270000000l, scraper.extractResultsNumber("Page&nbsp;10 sur environ 25&nbsp;270&nbsp;000&nbsp;000&nbsp;résultats<nobr> (0,46&nbsp;secondes)&nbsp;</nobr>"));
//        assertEquals(25270000000l, scraper.extractResultsNumber("Page 10 of about 25,270,000,000 results<nobr> (0.42 seconds)&nbsp;</nobr>"));
        assertEquals(25270000000l, scraper.extractResultsNumber("About 25,270,000,000 results<nobr> (0.28 seconds)&nbsp;</nobr>"));
        assertEquals(225000l, scraper.extractResultsNumber("About 225,000 results<nobr> (0.87 seconds)&nbsp;</nobr>"));
//        assertEquals(225000l, scraper.extractResultsNumber("Page 5 of about 225,000 results (0.45 seconds) "));
    }

}
