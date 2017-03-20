/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class GoogleScrapSearchTest {
    
    public GoogleScrapSearchTest() {
    }

    @Test
    public void testSomeMethod() {
        
        long minPauseMS = 25000;
        long maxPauseMS = 50000;
        GoogleScrapSearch search = new GoogleScrapSearch();
        search.setPagePauseMS(minPauseMS, maxPauseMS);
        
        for (int i = 0; i < 1000; i++) {
            long pauseMS = search.getRandomPagePauseMS();
            assertTrue(pauseMS >= minPauseMS);
            assertTrue(pauseMS <= maxPauseMS);
        }
        
        
    }
    
}
