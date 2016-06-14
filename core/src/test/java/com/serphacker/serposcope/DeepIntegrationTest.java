/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope;

import java.util.Properties;
import org.junit.Assume;
import org.junit.Before;


public abstract class DeepIntegrationTest {
    @Before
    public void before() throws Exception {
        System.out.println("props : " + System.getProperty("deepit"));
        System.out.println("props : " + System.getProperty("deepit"));
        System.out.println("props : " + System.getProperty("deepit"));
        System.out.println("props : " + System.getProperty("deepit"));
        Assume.assumeTrue("true".equals(System.getProperty("deepit")));
    }
}
