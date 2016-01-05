/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.google.inject.ImplementedBy;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import java.util.Map;

/**
 *
 * @author admin
 */
@ImplementedBy(CaptchaSolverFactoryImpl.class)
public interface CaptchaSolverFactory {
    public CaptchaSolver get(Map<String,String> properties);
}
