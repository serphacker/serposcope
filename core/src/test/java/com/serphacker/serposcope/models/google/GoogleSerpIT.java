/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleSerpIT {
    
    public GoogleSerpIT() {
    }
    
    ThreadLocalRandom r = ThreadLocalRandom.current();

    @Test
    public void testSerialization() throws IOException {
        GoogleSerp serp = new GoogleSerp(1, 2, null);
        
        for (int i = 0; i < r.nextInt(10, 20); i++) {
            GoogleSerpEntry entry = new GoogleSerpEntry("url-" + i);
            entry.map.put((short)r.nextInt(Short.MAX_VALUE), (short)r.nextInt(Short.MAX_VALUE));
            serp.addEntry(entry);
        }
        
        
        GoogleSerp serpUnserialized = new GoogleSerp(1, 2, null);
        serpUnserialized.setSerializedEntries(serp.getSerializedEntries());

        System.out.println(serpUnserialized.entries);
        
        
        ReflectionAssert.assertReflectionEquals(serp, serpUnserialized);
        
    }
    
}
