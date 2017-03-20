/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.serphacker.serposcope.scraper.http.ScrapClient;

/**
 *
 * @author admin
 */

public class ScrapClientFactoryImpl implements ScrapClientFactory {

    @Override
    public ScrapClient get(String defaultUserAgent, int timeoutMS) {
        ScrapClient client = new ScrapClient();
        client.setUseragent(defaultUserAgent);
        client.setTimeout(timeoutMS);
        
        return client;
    }
}
