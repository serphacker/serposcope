/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.scraper.captcha.solver;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RandomCaptchaSolver implements CaptchaSolver {
    
    private static final Logger LOG = LoggerFactory.getLogger(RandomCaptchaSolver.class);
    
    LinkedList<CaptchaSolver> solvers;
    
    AtomicInteger captchaCount=new AtomicInteger();

    public RandomCaptchaSolver(Collection<CaptchaSolver> solvers) {
        this.solvers = new LinkedList<>(solvers);
    }

    @Override
    public boolean solve(Captcha captcha) {
        captchaCount.incrementAndGet();
        
        ArrayList<CaptchaSolver> localSolvers = new ArrayList<>(solvers);
        Collections.shuffle(localSolvers, ThreadLocalRandom.current());
        
        for (CaptchaSolver solver : localSolvers) {
            captcha.setStatus(Captcha.Status.CREATED);
            captcha.setError(Captcha.Error.SUCCESS);
            LOG.info("trying {}", solver.getFriendlyName());
            if(solver.solve(captcha)){
                return true;
            }
            LOG.info("{} failed with {}", solver.getFriendlyName(), captcha.getError());
        }
        
        LOG.info("all captcha solver failed");
        return false;
    }
    
    @Override
    public boolean init() {
        if(solvers.isEmpty()){
            return false;
        }
        
        LOG.info("solvers : [{}]", solvers.stream().map(CaptchaSolver::getFriendlyName).collect(Collectors.joining(",")));
        return true;
    }    

    @Override
    public boolean reportIncorrect(Captcha captcha) {
        if(captcha.getLastSolver() == null){
            LOG.warn("captcha.getLastSolver() return null");
            return false;
        }
        
        return captcha.getLastSolver().reportIncorrect(captcha);
    }

    @Override
    public String getFriendlyName() {
        return "failover-captcha-solver";
    }

    @Override
    public int getCaptchaCount() {
        return captchaCount.get();
    }

    @Override
    public void resetCaptchaCount() {
        captchaCount.set(0);
    }

    @Override
    public void close() throws IOException {
        for (CaptchaSolver solver : solvers) {
            try {
                solver.close();
            } catch(IOException ex){
                LOG.warn("captcha solver {} .close() exception", solver.getFriendlyName(), ex);
            }
        }
    }
    
    @Override
    public boolean testLogin() {
        for (CaptchaSolver solver : solvers) {
            if(solver.testLogin()){
                return true;
            }
        }
        return false;
    }    
    
    @Override
    public float getCredit() {
//        return (float)solvers.stream().mapToDouble(CaptchaSolver::getCredit).sum();
        LOG.warn("getCredit() not implemented");
        return 0f;
    }

    @Override
    public boolean hasCredit() {
//        for (CaptchaSolver solver : solvers) {
//            if(solver.hasCredit()){
//                return true;
//            }
//        }
//        return false;
        
        LOG.warn("hasCredit() not implemented");
        return false;        
    }    

}
