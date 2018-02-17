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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

/**
 *
 * @author admin
 */
public abstract class GenericSolverIT extends DeepIntegrationTest {

    public GenericSolverIT() {
    }

    protected abstract CaptchaSolver getSolver();

    protected abstract CaptchaSolver getSolverInvalidCredentials();

    protected abstract CaptchaSolver getSolverNoBalance();

    @Test
    public void testGetCredit() throws IOException {
        try (CaptchaSolver solver = getSolver()) {
            assertNotEquals(0, solver.getCredit());
        }
        try (CaptchaSolver solver = getSolverNoBalance()) {
            if (solver == null) {
                return;
            }
            assertEquals(0f, solver.getCredit(), 0);
        }
    }

    @Test
    public void testHasCredit() throws IOException {
        try (CaptchaSolver solver = getSolver()) {
            assertTrue(solver.hasCredit());
        }
        try (CaptchaSolver solver = getSolverNoBalance()) {
            if (solver == null) {
                return;
            }
            assertFalse(solver.hasCredit());
        }

    }

    @Test
    public void testTestLogin() throws IOException {
        try (CaptchaSolver solver = getSolver()) {
            assertTrue(solver.testLogin());
        }

        try (CaptchaSolver solver = getSolverNoBalance()) {
            if(solver != null){
                assertTrue(solver.testLogin());
            }
        }

        try (CaptchaSolver solver = getSolverInvalidCredentials()) {
            assertFalse(solver.testLogin());
        }
    }

    @Test
    public void testSolveOutOfBalance() throws IOException {
        try (CaptchaSolver solver = getSolverNoBalance()) {
            if (solver == null) {
                return;
            }
            byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
            CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
            assertFalse(solver.solve(captcha));
            assertEquals(Captcha.Status.ERROR, captcha.getStatus());
            assertEquals(Captcha.Error.OUT_OF_CREDITS, captcha.getError());
        }
    }

    @Test
    public void testInvalidCredential() throws IOException {
        try (CaptchaSolver solver = getSolverInvalidCredentials()) {
            byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
            CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
            assertFalse(solver.solve(captcha));
            assertEquals(Captcha.Status.ERROR, captcha.getStatus());
            assertEquals(Captcha.Error.INVALID_CREDENTIALS, captcha.getError());
        }
    }

    @Test
    public void testSuccessSolve() throws IOException {
        try (CaptchaSolver solver = getSolver()) {
            byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
            CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
            assertTrue(solver.solve(captcha));
            assertEquals(Captcha.Status.SOLVED, captcha.getStatus());
            assertEquals(Captcha.Error.SUCCESS, captcha.getError());
            assertEquals("hyped", captcha.getResponse());
        }
    }

    @Test
    public void testSolverRecaptcha() throws Exception {

        try (CaptchaSolver solver = getSolver()) {
            CaptchaRecaptcha captcha = new CaptchaRecaptcha("6LeLLRkTAAAAAMWD6RpN9oBFamvaumbmjEPmiOxF", "https://spectrocoin.com/en/login.html");
            solver.solve(captcha);
            assertEquals(Captcha.Status.SOLVED, captcha.getStatus());
            assertEquals(Captcha.Error.SUCCESS, captcha.getError());
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
