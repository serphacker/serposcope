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
public class ImageTyperzSolverIT extends GenericSolverIT {
    
    public ImageTyperzSolverIT() {
    }
    
    String apiKey;
    String apikey0balance;
    
    @Before
    public void readCredentials() throws Exception {
        apiKey = props.getProperty("imagetyperz.api");
        apikey0balance = props.getProperty("imagetyperz.api0balance");        
        assertNotNull(apiKey);
        assertNotNull(apikey0balance);
    }

    @Override
    protected CaptchaSolver getSolver() {
        return new ImageTyperzSolver(apiKey);
    }

    @Override
    protected CaptchaSolver getSolverNoBalance() {
        //return new ImageTyperzSolver(apikey0balance);
        return null;
    }

    @Override
    protected CaptchaSolver getSolverInvalidCredentials() {
        return new ImageTyperzSolver(apiKey + "wrong-password");
    }

//    @Test
//    public void testSpecific() throws Exception {
//        super.testGetCredit();//To change body of generated methods, choose Tools | Templates.
//    }
    
}
