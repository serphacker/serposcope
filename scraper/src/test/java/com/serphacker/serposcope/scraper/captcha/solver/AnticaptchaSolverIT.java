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
import com.serphacker.serposcope.scraper.captcha.CaptchaRecaptcha;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import java.io.IOException;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author admin
 */
public class AntiCaptchaSolverIT extends DeepIntegrationTest {
    
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
    
    @Test
    public void testGetUserData(){
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apikey0balance);
        assertNotNull(solver.getRawBalance());
    }
    
    @Test
    public void testGetCredit() {
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
        assertNotEquals(0, solver.getCredit());
        
        solver = new AntiCaptchaSolver(apikey0balance);
        assertEquals(0f, solver.getCredit(), 0);
    }
    
    @Test
    public void testHasCredit(){
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
        assertTrue(solver.hasCredit());
        
        solver = new AntiCaptchaSolver(apikey0balance);
        assertFalse(solver.hasCredit());
    }
    
    @Test
    public void testTestLogin(){
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
        assertTrue(solver.testLogin());
        
        solver = new AntiCaptchaSolver(apikey0balance);
        assertTrue(solver.testLogin());
        
        solver = new AntiCaptchaSolver(apiKey + "x");
        assertFalse(solver.testLogin());
    }
    
    
    @Test
    public void testSolveOutOfBalance() throws IOException {
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apikey0balance);
        byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
        CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
        assertFalse(solver.solve(captcha));
        assertEquals(Captcha.Status.ERROR, captcha.getStatus());
        assertEquals(Captcha.Error.OUT_OF_CREDITS, captcha.getError());
    }    
    
    
    @Test
    public void testInvalidCredential() throws IOException {
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apikey0balance + "x");
        byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
        CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
        assertFalse(solver.solve(captcha));
        assertEquals(Captcha.Status.ERROR, captcha.getStatus());
        assertEquals(Captcha.Error.INVALID_CREDENTIALS, captcha.getError());
    }    
    
    @Test
    public void testSuccessSolve() throws IOException {
        AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
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
            AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
            solver.solve(captcha);
            System.out.println("id : " + captcha.getId());
            System.out.println("response : " + captcha.getResponse());
            System.out.println("duration : " + captcha.getSolveDuration());
        }
    }
    
    @Test
    public void testSolverRecaptcha() throws Exception {
        
        try (AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey)) {
            CaptchaRecaptcha captcha = new CaptchaRecaptcha("6LfydQgUAAAAAMuh1gRreQdKjAop7eGmi6TrNIzp", "https://anti-captcha.com/recaptcha");
            solver.solve(captcha);
            System.out.println("id : " + captcha.getId());
            System.out.println("response : " + captcha.getResponse());
            System.out.println("duration : " + captcha.getSolveDuration());
            System.out.println("error : " + captcha.getError());
        }
        
    }    
    
//    @Test
//    public void testSolveRandomAndReportFalse() throws Exception {
//        try(ScrapClient cli = new ScrapClient()){
//            cli.get("https://sorry.google.com/sorry/image?id=15854950164179955873");
//            CaptchaImage captcha = new CaptchaImage(new byte[][]{cli.getContent()});
//            AntiCaptchaSolver solver = new AntiCaptchaSolver(apiKey);
//            solver.solve(captcha);
//            System.out.println("id : " + captcha.getId());
//            System.out.println("response : " + captcha.getResponse());
//            System.out.println("duration : " + captcha.getSolveDuration());
//            solver.reportIncorrect(captcha);
//        }
//    }        
}
