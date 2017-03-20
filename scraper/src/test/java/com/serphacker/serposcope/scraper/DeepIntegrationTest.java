/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper;

import java.util.Properties;
import org.junit.Assume;
import org.junit.Before;


public abstract class DeepIntegrationTest {
    protected Properties props = new Properties();
    
    @Before
    public void before() throws Exception {
        props.load(ClassLoader.class.getResourceAsStream("/testconfig.properties"));
        Assume.assumeTrue("true".equals(props.getProperty("deepit")));
    }
}
