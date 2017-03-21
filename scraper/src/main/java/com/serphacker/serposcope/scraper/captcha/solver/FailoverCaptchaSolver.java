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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FailoverCaptchaSolver implements CaptchaSolver {
    
    private static final Logger LOG = LoggerFactory.getLogger(FailoverCaptchaSolver.class);
    
    volatile CaptchaSolver currentSolver;
    
    List<CaptchaSolver> originalList;
    LinkedList<CaptchaSolver> solvers;
    
    AtomicInteger captchaCount=new AtomicInteger();

    public FailoverCaptchaSolver(Collection<CaptchaSolver> solvers) {
        this.originalList = new ArrayList<>(solvers);
        this.solvers = new LinkedList<>(solvers);
        this.currentSolver = this.solvers.poll();
    }

    @Override
    public boolean solve(Captcha captcha) {
        captchaCount.incrementAndGet();
        do {
            CaptchaSolver solver = currentSolver();
            if(solver == null){
                LOG.info("all captcha solver failed");
                captcha.setLastSolver(this);
                if(captcha.getError().equals(Captcha.Error.SUCCESS))
                	captcha.setError(Captcha.Error.SERVICE_OVERLOADED);
                return false;
            }
            
            if(solver.solve(captcha)){
                return true;
            }
            
            invalidSolver(solver, captcha);
        } while(true);
    }
    
    protected synchronized CaptchaSolver currentSolver() {
        return currentSolver;
    }
    
    protected synchronized void invalidSolver(CaptchaSolver solver, Captcha captcha) {
        if(solver == currentSolver){
            currentSolver = solvers.poll();
            LOG.info("{} failed with {}, {}, replacing by {}", 
                solver.getFriendlyName(), captcha.getError(), captcha.getErrorMsg(),
                currentSolver == null ? "nothing (no more captcha solver)" : currentSolver.getFriendlyName()
            );
        }
    }

    @Override
    public boolean init() {
        if(currentSolver == null){
            return false;
        }
        
        String failovers="none";
        if(!solvers.isEmpty()){
            failovers=solvers.stream().map(CaptchaSolver::getFriendlyName).collect(Collectors.joining(","));
        }
        
        LOG.info("default solver : {} | failover solvers : [{}]", currentSolver.getFriendlyName(), failovers);
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
        for (CaptchaSolver solver : originalList) {
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
