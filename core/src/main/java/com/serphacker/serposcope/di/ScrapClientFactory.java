/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.google.inject.ImplementedBy;
import com.serphacker.serposcope.scraper.http.ScrapClient;

/**
 *
 * @author admin
 */

@ImplementedBy(ScrapClientFactoryImpl.class)
public interface ScrapClientFactory {
    public ScrapClient get(String defaultUserAgent, int timeoutMS);
}
