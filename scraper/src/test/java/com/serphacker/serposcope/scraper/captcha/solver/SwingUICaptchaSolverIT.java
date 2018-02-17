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
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class SwingUICaptchaSolverIT extends DeepIntegrationTest {

    public SwingUICaptchaSolverIT() {
    }

    @Test
    public void test() throws Exception {

        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
        solver.init();

        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    byte[] image = ByteStreams.toByteArray(ClassLoader.class.getResourceAsStream("/googlecaptcha-hyped.jpg"));
                    CaptchaImage captcha = new CaptchaImage(new byte[][]{image});
                    solver.solve(captcha);
                    System.out.println(captcha.getResponse() + "|" + captcha.getStatus() + "|" + captcha.getSolveDuration());
                } catch (IOException ex) {
                }
            });
            threads[i].start();
            Thread.sleep(500);
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("closing ui");
        solver.close();
        Thread.sleep(5000l);
        
    }
    
    
    @Test
    public void testXX() throws Exception {
        
        SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
        solver.init();
        Thread.sleep(3000l);
        System.out.println("closing ui");
        solver.close();
        Thread.sleep(5000l);
        
    }

}
