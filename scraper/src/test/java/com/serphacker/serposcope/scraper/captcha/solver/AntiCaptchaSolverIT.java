/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import org.junit.Before;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author admin
 */
public class AntiCaptchaSolverIT extends GenericSolverIT {
    
    public AntiCaptchaSolverIT() {
    }
    
    String apiKey;
    String apikey0balance;
    
    @Before
    public void readCredentials() throws Exception {
        apiKey = props.getProperty("antigate.api");
        apikey0balance = props.getProperty("antigate.api0balance");        
        assertNotNull(apiKey);
        assertNotNull(apikey0balance);
    }

    @Override
    protected CaptchaSolver getSolver() {
        return new AntiCaptchaSolver(apiKey);
    }

    @Override
    protected CaptchaSolver getSolverNoBalance() {
        return new AntiCaptchaSolver(apikey0balance);
    }

    @Override
    protected CaptchaSolver getSolverInvalidCredentials() {
        return new AntiCaptchaSolver(apiKey + "wrong-password");
    }
    
    
}
