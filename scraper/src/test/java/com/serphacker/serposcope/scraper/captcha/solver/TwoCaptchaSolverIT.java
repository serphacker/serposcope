/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import java.io.IOException;
import org.junit.Before;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class TwoCaptchaSolverIT extends GenericSolverIT {
    
    public TwoCaptchaSolverIT() {
    }
    
    String apiKey;
    String apikey0balance;
    
    @Before
    public void readCredentials() throws Exception {
        apiKey = props.getProperty("2captcha.api");
        apikey0balance = props.getProperty("2captcha.api0balance");        
        assertNotNull(apiKey);
        assertNotNull(apikey0balance);
    }

    @Override
    protected CaptchaSolver getSolver() {
        return new TwoCaptchaSolver(apiKey);
    }

    @Override
    protected CaptchaSolver getSolverNoBalance() {
        return new TwoCaptchaSolver(apikey0balance);
    }

    @Override
    protected CaptchaSolver getSolverInvalidCredentials() {
        return new TwoCaptchaSolver(apiKey + "wrong-password");
    }
    
}
