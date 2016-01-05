/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ConfigDBIT extends AbstractDBIT {
    
    @Inject
    ConfigDB config;

    @Test
    public void testConfig() {
        assertEquals("defaultXXX", config.get("test", "defaultXXX"));
        
        config.update("test", "valu'e1");
        assertEquals("valu'e1", config.get("test", "defaultXXX"));
        
        config.update("test", "valu'e2");
        assertEquals("valu'e2", config.get("test", "defaultXXX"));
        
        
        assertEquals("defaultXXX", config.get("test2", "defaultXXX"));
        
        config.update("test2", "value1");
        assertEquals("value1", config.get("test2", "defaultXXX"));
        assertEquals("valu'e2", config.get("test", "defaultXXX"));
        
        config.update("test2", "value2");
        assertEquals("value2", config.get("test2", "defaultXXX"));
        assertEquals("valu'e2", config.get("test", "defaultXXX"));
    }
    
}
