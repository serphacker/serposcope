package com.serphacker.serposcope.scraper.google.scraper;

import com.serphacker.serposcope.scraper.ResourceHelper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoogleScraperParserTest {

    public final static Logger LOG = LoggerFactory.getLogger(GoogleScraperParserTest.class);

    private final static List<String> DIRECTORIES = Arrays.asList(
        "/google/201804",
        "/google/201810",
        "/google/201910",
        "/google/202111"
    );

    @Test
    public void lastPage() throws IOException {

        List<String> dirs = DIRECTORIES.stream().map(d -> d + "/last-page").collect(Collectors.toList());
        List<String> files = ResourceHelper.listResourceDir(dirs);

        for (String file : files) {
            LOG.debug("checking " + file);
            String content = ResourceHelper.readResourceAsString(file);
            ScrapClient http = mock(ScrapClient.class);
            when(http.getContentAsString()).thenReturn(content);
            GoogleScraper scraper = new GoogleScraper(http, null);
            assertEquals(OK, scraper.parseSerp(new ArrayList<>()));
            assertFalse(scraper.hasNextPage());
        }

    }

    @Test
    public void top10() throws Exception {

        List<String> dirs = DIRECTORIES.stream().map(d -> d + "/top-10").collect(Collectors.toList());
        List<String> files = ResourceHelper.listResourceDir(dirs);

        for (String file : files) {

            if (file.endsWith(".res")) {
                continue;
            }

            LOG.info("checking {}", file);

            String serpHtml = ResourceHelper.readResourceAsString(file);
            List<String> expectedUrls = Arrays.asList(ResourceHelper.readResourceAsString(file + ".res").split("\n"));

            assertFalse(serpHtml.isEmpty());
            assertFalse(expectedUrls.isEmpty());

            ScrapClient http = mock(ScrapClient.class);
            when(http.getContentAsString()).thenReturn(serpHtml);
            GoogleScraper scraper = new GoogleScraper(http, null);
            List<String> urls = new ArrayList<>();
            assertEquals(OK, scraper.parseSerp(urls));
            assertTrue(scraper.hasNextPage());

            assertEquals(expectedUrls, urls.subList(0, Math.min(urls.size(), expectedUrls.size())));
        }

    }

}
