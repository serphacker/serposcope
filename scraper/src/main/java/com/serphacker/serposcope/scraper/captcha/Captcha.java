/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha;

import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import static com.serphacker.serposcope.scraper.captcha.Captcha.Error.SUCCESS;
import static com.serphacker.serposcope.scraper.captcha.Captcha.Status.CREATED;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author admin
 */
public abstract class Captcha {
    
    public enum Status {
        CREATED,
        SUBMITTED,
        SOLVED,
        ERROR
    };
    
    public enum Error {
        SUCCESS,
        SERVICE_OVERLOADED,
        INVALID_CREDENTIALS,
        OUT_OF_CREDITS,
        NETWORK_ERROR,
        EXCEPTION
    };
    
    String id;
    Status status = CREATED;
    Error error = SUCCESS;
    long solveDuration;
    Map<String,String> context = new HashMap<>();
    CaptchaSolver lastSolver;
    
    
    
//    private int subType;
//    private String response;        // the response of the captcha solving

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
        if(error != SUCCESS){
            setStatus(Status.ERROR);
        }
    }

    public long getSolveDuration() {
        return solveDuration;
    }

    public void setSolveDuration(long solveDuration) {
        this.solveDuration = solveDuration;
    }

    public CaptchaSolver getLastSolver() {
        return lastSolver;
    }

    public void setLastSolver(CaptchaSolver lastSolver) {
        this.lastSolver = lastSolver;
    }
}
