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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author admin
 */
public class DeathByCaptchaSolverIT extends DeepIntegrationTest {
    
    public DeathByCaptchaSolverIT() {
    }
    
    String dbcLogin;
    String dbcPassword;
    
    @Before
    public void readCredentials() throws Exception {
        dbcLogin = props.getProperty("dbclogin");
        dbcPassword = props.getProperty("dbcpassword");        
        assertNotNull(dbcLogin);
        assertNotNull(dbcPassword);
    }
    
    @Test
    public void testGetUserData(){
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
        System.out.println(solver.getUserData());
    }

    @Test
    public void testGetCredit() {
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
        assertNotEquals(0, solver.getCredit());
    }
    
    @Test
    public void testHasCredit(){
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
        assertTrue(solver.hasCredit());
    }
    
    @Test
    public void testTestLogin(){
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
        assertTrue(solver.testLogin());        
    }
    
    @Test
    public void testSuccessSolve() throws IOException {
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
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
            DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
            solver.solve(captcha);
            System.out.println("id : " + captcha.getId());
            System.out.println("response : " + captcha.getResponse());
            System.out.println("duration : " + captcha.getSolveDuration());
        }
    }
    
    @Test
    public void testExtractId() throws Exception {
        DeathByCaptchaSolver solver = new DeathByCaptchaSolver(dbcLogin, dbcPassword);
        assertEquals("1477215", solver.extractId("http://api.dbcapi.me/api/captcha/1477215"));
    }
    
    
}
