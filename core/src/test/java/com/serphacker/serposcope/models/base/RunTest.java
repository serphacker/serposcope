/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class RunTest {

    public RunTest() {
    }

    @Test
    public void testSomeMethod() {

        {
            Run r1 = new Run();
            r1.setStarted(LocalDateTime.now().withNano(0));
            r1.setProgress(50);
            assertEquals(1000, r1.getRemainingTimeMs(r1.getStarted().plusSeconds(1)));
        }

        {
            Run r1 = new Run();
            r1.setStarted(LocalDateTime.now().withNano(0));
            r1.setProgress(10);
            assertEquals(9000, r1.getRemainingTimeMs(r1.getStarted().plusSeconds(1)));
        }        
    }

}
