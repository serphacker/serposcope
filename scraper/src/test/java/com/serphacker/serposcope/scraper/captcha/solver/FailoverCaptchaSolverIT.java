/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.serphacker.serposcope.scraper.captcha.solver.solvers.TestingRandomCaptchaSolver;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.solver.solvers.TestingFixedCaptchaSolver;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class FailoverCaptchaSolverIT {
    
    private static final Logger LOG = LoggerFactory.getLogger(FailoverCaptchaSolverIT.class);
    
    
    @Test
    public void testSomeMethod() {
        
        CaptchaSolver solver1 = new TestingFixedCaptchaSolver("solver#1", 3);
        CaptchaSolver solver2 = new TestingFixedCaptchaSolver("solver#2", 2);
        
        RandomCaptchaSolver fcs = new RandomCaptchaSolver(Arrays.asList(solver1, solver2));
        
        CaptchaImage c = new CaptchaImage(null);
        assertTrue(fcs.solve(c));
        assertEquals(solver1, c.getLastSolver());
        assertTrue(fcs.solve(c));
        assertEquals(solver1, c.getLastSolver());
        assertTrue(fcs.solve(c));
        assertEquals(solver1, c.getLastSolver());
        
        assertTrue(fcs.solve(c));
        assertEquals(solver2, c.getLastSolver());
        assertTrue(fcs.solve(c));
        assertEquals(solver2, c.getLastSolver());
        
        assertFalse(fcs.solve(c));
        assertEquals(fcs, c.getLastSolver());
    }
    
    @Test
    public void testConcurrency() throws Exception {
        
        final RandomCaptchaSolver fcs = new RandomCaptchaSolver(Arrays.asList(
            new TestingRandomCaptchaSolver("fail-100-v1", 100, 50),
            new TestingRandomCaptchaSolver("fail-10", 10, 50),
            new TestingRandomCaptchaSolver("fail-never", 0, 0)
        ));
        
        
        Thread[] th = new Thread[50];
        for (int i = 0; i < th.length; i++) {
            th[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    CaptchaImage c = new CaptchaImage(null);
                    boolean result = fcs.solve(c);
                    LOG.info("captcha-{} solver={}, result={}, status={}", j, c.getLastSolver().getFriendlyName(), result, c.getStatus());
                }
            });
        }
        
        for (int i = 0; i < th.length; i++) {
            th[i].start();
        }
        
        for (int i = 0; i < th.length; i++) {
            th[i].join();
        }

    }    
    
}
