/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.captcha.solver.DeathByCaptchaSolver;
import com.serphacker.serposcope.scraper.captcha.solver.DecaptcherSolver;
import com.serphacker.serposcope.scraper.captcha.solver.SwingUICaptchaSolver;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptchaSolverFactoryImpl implements CaptchaSolverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CaptchaSolverFactoryImpl.class);

    @Override
    public CaptchaSolver get(Config config) {
        if(config == null){
            return null;
        }
        
        switch (config.getCaptchaService()) {
            case SWINGUI:
                SwingUICaptchaSolver solver = new SwingUICaptchaSolver();
                return solver;

            case DEATHBYCAPTCHA:
                if (!StringUtils.isEmpty(config.getDbcUser()) && !StringUtils.isEmpty(config.getDbcPass())) {
                    return new DeathByCaptchaSolver(config.getDbcUser(), config.getDbcPass());
                }
                break;

            case DECAPTCHER:
                if (!StringUtils.isEmpty(config.getDbcUser()) && !StringUtils.isEmpty(config.getDbcPass())) {
                    return new DecaptcherSolver(config.getDbcUser(), config.getDbcPass());
                }
                break;
        }
        return null;

    }
}
