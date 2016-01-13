/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.google.common.io.ByteStreams;
import com.serphacker.serposcope.scraper.DeepIntegrationTest;
import com.serphacker.serposcope.scraper.captcha.Captcha;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author admin
 */
public class DecaptcherSolverIT extends DeepIntegrationTest {
    
    public DecaptcherSolverIT() {
    }

    String login;
    String password;
    
    String login0balance;
    String password0balance;
    
    @Before
    public void readCredentials() throws Exception {
        login = props.getProperty("decaptcher.login");
        password = props.getProperty("decaptcher.password");
        login0balance = props.getProperty("decaptcher.login0balance");
        password0balance = props.getProperty("decaptcher.password0balance");
        assertNotNull(login);
        assertNotNull(password);
    }
    
    @Test
    public void testGetUserData(){
        DecaptcherSolver solver = new DecaptcherSolver(login, password);
        assertNotNull(solver.getBalanceRaw());
    }

    @Test
    public void testGetCredit() {
        DecaptcherSolver solver = new DecaptcherSolver(login, password);
        assertNotEquals(0, solver.getCredit());
        
        solver = new DecaptcherSolver(login0balance, password0balance);
        assertEquals(0f, solver.getCredit(), 0);
    }
    
    @Test
    public void testHasCredit(){
        DecaptcherSolver solver = new DecaptcherSolver(login, password);
        assertTrue(solver.hasCredit());
        
        solver = new DecaptcherSolver(login0balance, password0balance);
        assertFalse(solver.hasCredit());
    }
    
    @Test
    public void testTestLogin(){
        DecaptcherSolver solver = new DecaptcherSolver(login, password);
        assertTrue(solver.testLogin());
        
        solver = new DecaptcherSolver(login0balance, password0balance);
        assertTrue(solver.testLogin());
        
        solver = new DecaptcherSolver(login, password + "x");
        assertFalse(solver.testLogin());
    }
    
    @Test
    public void testSolveOutOfBalance() throws IOException {
        DecaptcherSolver solver = new DecaptcherSolver(login0balance, password0balance);
        byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
        CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
        assertFalse(solver.solve(captcha));
        assertEquals(Captcha.Status.ERROR, captcha.getStatus());
        assertEquals(Captcha.Error.OUT_OF_CREDITS, captcha.getError());
    }    
    
    @Test
    public void testInvalidCredential() throws IOException {
        DecaptcherSolver solver = new DecaptcherSolver(login0balance, password0balance + "x");
        byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
        CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
        assertFalse(solver.solve(captcha));
        assertEquals(Captcha.Status.ERROR, captcha.getStatus());
        assertEquals(Captcha.Error.INVALID_CREDENTIALS, captcha.getError());
    }    
    
    @Test
    public void testSuccessSolve() throws IOException {
        DecaptcherSolver solver = new DecaptcherSolver(login, password);
        byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
        CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
        assertTrue(solver.solve(captcha));
        assertEquals(Captcha.Status.SOLVED, captcha.getStatus());
        assertEquals(Captcha.Error.SUCCESS, captcha.getError());
        assertEquals("hyped", captcha.getResponse());
    }
    
    @Test
    public void testSolveRandom() throws Exception {
        try(ScrapClient cli = new ScrapClient()){
            cli.get("https://sorry.google.com/sorry/image?id=15854950164179955873");
            CaptchaImage captcha = new CaptchaImage(new byte[][]{cli.getContent()});
            DecaptcherSolver solver = new DecaptcherSolver(login, password);
            solver.solve(captcha);
            System.out.println("id : " + captcha.getId());
            System.out.println("response : " + captcha.getResponse());
            System.out.println("duration : " + captcha.getSolveDuration());
        }
    }
    
//    @Test
//    public void testSolveRandomAndReportFalse() throws Exception {
//        try(ScrapClient cli = new ScrapClient()){
//            cli.get("https://sorry.google.com/sorry/image?id=15854950164179955873");
//            CaptchaImage captcha = new CaptchaImage(new byte[][]{cli.getContent()});
//            DecaptcherSolver solver = new DecaptcherSolver(login, password);
//            solver.solve(captcha);
//            System.out.println("id : " + captcha.getId());
//            System.out.println("response : " + captcha.getResponse());
//            System.out.println("duration : " + captcha.getSolveDuration());
//            solver.reportIncorrect(captcha);
//        }
//    }    
    
    
}
