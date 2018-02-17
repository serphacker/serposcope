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
public class DecaptcherSolverIT extends GenericSolverIT {

    public DecaptcherSolverIT() {
    }

    String login;
    String password;
    
    String login0balance;
    String password0balance;
    
    @Before
    public void readCredentials() throws Exception {
        assertNotNull(login = props.getProperty("decaptcher.login"));
        assertNotNull(password = props.getProperty("decaptcher.password"));
        assertNotNull(login0balance = props.getProperty("decaptcher.login0balance"));
        assertNotNull(password0balance = props.getProperty("decaptcher.password0balance"));
    }
    
    @Override
    protected CaptchaSolver getSolver() {
        return new DecaptcherSolver(login, password);
    }

    @Override
    protected CaptchaSolver getSolverNoBalance() {
        return new DecaptcherSolver(login0balance, password0balance);
    }

    @Override
    protected CaptchaSolver getSolverInvalidCredentials() {
        return new DecaptcherSolver("wrong-login", "wrong-password");
    }

    @Override
    public void testSolverRecaptcha() throws Exception {
        // not supported
    }

}
