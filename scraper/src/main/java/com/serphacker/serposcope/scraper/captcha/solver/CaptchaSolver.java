/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha.solver;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import java.io.Closeable;

/**
 *
 * @author admin
 */
public interface CaptchaSolver extends Closeable {
    public boolean solve(Captcha captcha);
    public boolean reportIncorrect(Captcha captcha);
    public String getFriendlyName();
    public float getCredit();
    public boolean hasCredit();
    public boolean testLogin();
    public boolean init();
    public int getCaptchaCount();
    public void resetCaptchaCount();
}
