/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.google.inject.ImplementedBy;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.scraper.GoogleScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;

@ImplementedBy(GoogleScraperFactoryImpl.class)
public interface GoogleScraperFactory {
    public GoogleScraper get(ScrapClient http, CaptchaSolver solver);
}
