/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper.captcha.solver.solvers;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TestingRandomCaptchaSolver implements CaptchaSolver {
    
    Random r = new Random();
    
    String name;
    int failRate;
    int sleepMS;

    public TestingRandomCaptchaSolver(String name, int failRate, int sleepMS) {
        this.name = name;
        this.failRate = failRate;
        this.sleepMS = sleepMS;
    }
    
    @Override
    public boolean solve(Captcha captcha) {
        
        if(sleepMS > 0){
            try {
                Thread.sleep(sleepMS);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestingRandomCaptchaSolver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        CaptchaImage c = (CaptchaImage)captcha;
        
        c.setLastSolver(this);
        if(r.nextInt(100) < failRate){
            c.setStatus(Captcha.Status.ERROR);
            c.setError(CaptchaImage.Error.SERVICE_OVERLOADED);
            return false;
        } else {
            c.setStatus(Captcha.Status.SOLVED);
            c.setResponse("response");
            return true;
        }
    }

    @Override
    public boolean reportIncorrect(Captcha captcha) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFriendlyName() {
        return name;
    }

    @Override
    public float getCredit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasCredit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean testLogin() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getCaptchaCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetCaptchaCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
