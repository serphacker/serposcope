/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class GoogleSerpEntryTest {
    
    public GoogleSerpEntryTest() {
    }

    @Test
    public void testGetUnicodeUrl() throws Exception {
        assertEquals("http://bücher.ch", new GoogleSerpEntry("http://bücher.ch").getUnicodeUrl());
        assertEquals("http://bücher.ch/", new GoogleSerpEntry("http://bücher.ch/").getUnicodeUrl());
        assertEquals("http://bücher.ch/kpi", new GoogleSerpEntry("http://bücher.ch/kpi").getUnicodeUrl());
        
        assertEquals("http://bücher.ch", new GoogleSerpEntry("http://xn--bcher-kva.ch").getUnicodeUrl());
        assertEquals("http://bücher.ch/", new GoogleSerpEntry("http://xn--bcher-kva.ch/").getUnicodeUrl());
        assertEquals("http://bücher.ch/kpi", new GoogleSerpEntry("http://xn--bcher-kva.ch/kpi").getUnicodeUrl());
    }
    
}
