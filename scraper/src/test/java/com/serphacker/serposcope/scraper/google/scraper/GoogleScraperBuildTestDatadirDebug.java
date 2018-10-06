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
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status.OK;
import static org.junit.Assert.assertEquals;

/**
 * @author admin
 */
public class GoogleScraperBuildTestDatadirDebug {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleScraperBuildTestDatadirDebug.class);

    String basedatadir = System.getProperty("java.io.tmpdir") + "/serposcope/google";

    @Test
    public void debugScrapSomeSerp() throws Exception {

        String datadir = basedatadir + "/top-10";

        GoogleDevice[] devices = new GoogleDevice[]{
            GoogleDevice.DESKTOP,
            GoogleDevice.SMARTPHONE
        };

        GoogleCountryCode[] countries = new GoogleCountryCode[]{
//            GoogleCountryCode.__,
            GoogleCountryCode.FR
        };

        String[] keywords = new String[]{
            "image lion",
            "politique",
            "location voiture",
            "reine des neiges"
        };

        ScrapClient http = new ScrapClient();
        http.setInsecureSSL(true);
        //                http.setProxy(new HttpProxy("127.0.0.1", 8080));
        GoogleScraper scraper = new GoogleScraper(http, null);

        for (String keyword : keywords) {
            for (GoogleDevice device : devices) {
                for (GoogleCountryCode country : countries) {

                    GoogleScrapSearch search = new GoogleScrapSearch();
                    search.setKeyword(keyword);
                    search.setDevice(device);
                    search.setCountry(country);
                    search.setResultPerPage(10);

                    scraper.prepareHttpClient(search);
                    String url = scraper.buildRequestUrl(search, 0);
                    LOG.info("url = {}", url);

                    assertEquals(OK, scraper.downloadSerp(url, null, search, 0));
                    saveSerp(datadir, search, scraper.getHttp().getContent());

                    Thread.sleep(1000l);
                }
            }
        }
    }

    @Test
    public void downloadSerpWithLastPage() throws Exception {
        String datadir = basedatadir + "/last-page";

        ScrapClient http = new ScrapClient();
        GoogleScraper scraper = new GoogleScraper(http, null);

        GoogleDevice[] devices = new GoogleDevice[]{
            GoogleDevice.DESKTOP,
            GoogleDevice.SMARTPHONE
        };

        GoogleCountryCode[] countries = new GoogleCountryCode[]{
            GoogleCountryCode.__,
            GoogleCountryCode.FR
        };

        for (GoogleDevice device : devices) {
            for (GoogleCountryCode country : countries) {
                GoogleScrapSearch search = new GoogleScrapSearch();
                search.setKeyword("serposcope 1239");
                search.setDevice(device);
                search.setCountry(country);

                scraper.prepareHttpClient(search);
                String url = scraper.buildRequestUrl(search, 1);

                assertEquals(OK, scraper.downloadSerp(url, null, search, 0));
                saveSerp(datadir, search, scraper.getHttp().getContent());
            }
        }

    }

    public void saveSerp(String datadir, GoogleScrapSearch search, byte[] data) throws IOException {
        Files.createDirectories(Paths.get(datadir));

        String filename = datadir + "/";
        filename += search.getKeyword() + "-";
        filename += search.getCountry() + "-";
        filename += search.getDevice();

        Files.write(Paths.get(filename + ".html"), data);
        Files.write(Paths.get(filename + ".txt"), data);
    }

    @Test
    public void translateFileUrlInSerps() throws IOException, URISyntaxException {
        final List<String> resources = ResourceHelper.listResourceInDirectories(new String[]{"/google/201810/top-10"})
            .stream()
            .filter(f -> f.endsWith("SMARTPHONE.txt.res"))
            .collect(Collectors.toList());

        for (String resource : resources) {
            System.out.println(resource);
            final String[] urls = ResourceHelper.toString(resource).split("\n");
            for (String url : urls) {
                if(url.startsWith("file:///")) {
                    List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));
                    url = params.stream().filter(p -> "q".equals(p.getName())).map(NameValuePair::getValue).findFirst().orElse(null);
                }
                System.out.println(url);
            }
            System.out.println("---");
        }

    }

}
