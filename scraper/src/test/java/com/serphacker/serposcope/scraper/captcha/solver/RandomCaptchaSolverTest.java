/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class RandomCaptchaSolverTest {
    
    static class DummyCaptchaSolver implements CaptchaSolver {
        
        final int id;
        final boolean success;

        public DummyCaptchaSolver(int id, boolean success) {
            this.id = id;
            this.success = success;
        }

        @Override
        public boolean solve(Captcha captcha) {
            if(success){
                captcha.setStatus(Captcha.Status.SOLVED);
                return success;
            }
            captcha.setStatus(Captcha.Status.ERROR);
            captcha.setError(Captcha.Error.SERVICE_OVERLOADED);
            return success;
        }

        @Override
        public boolean reportIncorrect(Captcha captcha) {
            return true;
        }

        @Override
        public String getFriendlyName() {
            return (success ? "success" : "fail") + "-" + id;
        }

        @Override
        public float getCredit() {
            return 10f;
        }

        @Override
        public boolean hasCredit() {
            return true;
        }

        @Override
        public boolean testLogin() {
            return true;
        }

        @Override
        public boolean init() {
            return true;
        }

        @Override
        public int getCaptchaCount() {
            return 1;
        }

        @Override
        public void resetCaptchaCount() {
        }

        @Override
        public void close() throws IOException {
        }
        
    }
    
    public RandomCaptchaSolverTest() {
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(RandomCaptchaSolverTest.class);

    @Test
    public void testSomeMethod() {
        
        RandomCaptchaSolver solver = new RandomCaptchaSolver(Arrays.asList(
            new DummyCaptchaSolver(1, false),
            new DummyCaptchaSolver(2, false),
            new DummyCaptchaSolver(3, true)
        ));
        
        for (int i = 0; i < 10; i++) {
            LOG.info("==== captcha ====");
            solver.solve(new CaptchaImage(new byte[][]{}));
        }
    }
    
}
