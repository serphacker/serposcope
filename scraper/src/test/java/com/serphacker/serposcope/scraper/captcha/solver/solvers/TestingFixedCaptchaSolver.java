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


public class TestingFixedCaptchaSolver implements CaptchaSolver {
    
    Random r = new Random();
    
    String name;
    int countValid;
    int solved;

    public TestingFixedCaptchaSolver(String name, int countValid) {
        this.name = name;
        this.countValid = countValid;
    }
    
    @Override
    public boolean solve(Captcha captcha) {
        CaptchaImage c = (CaptchaImage)captcha;
        c.setLastSolver(this);
        
        if(solved++ < countValid){
            c.setStatus(Captcha.Status.SOLVED);
            c.setResponse("response");
            return true;
        }
        
        c.setStatus(Captcha.Status.ERROR);
        c.setError(CaptchaImage.Error.SERVICE_OVERLOADED);
        return false;
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
