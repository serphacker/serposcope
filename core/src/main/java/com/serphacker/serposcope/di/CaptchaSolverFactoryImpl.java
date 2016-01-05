/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.captcha.solver.DeathByCaptchaSolver;
import com.serphacker.serposcope.scraper.captcha.solver.SwingUICaptchaSolver;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaptchaSolverFactoryImpl implements CaptchaSolverFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(CaptchaSolverFactoryImpl.class);

    @Override
    public CaptchaSolver get(Map<String, String> properties) {
        String service = properties.get("service");
        if(service != null){
            switch(service){
                case "swingui":
                    SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
                    return solver;
                
                case "deathbycaptcha":
                    String dbcUser = properties.getOrDefault("dbcuser","");
                    String dbcPass = properties.getOrDefault("dbcpass","");
                    if(!dbcUser.isEmpty() && !dbcPass.isEmpty()){
                        return new DeathByCaptchaSolver(dbcUser, dbcPass);
                    }
                    break;
                    
            }
        }
        return null;
    }

}
