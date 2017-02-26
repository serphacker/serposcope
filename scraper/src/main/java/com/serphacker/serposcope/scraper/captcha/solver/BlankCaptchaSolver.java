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


public class BlankCaptchaSolver implements CaptchaSolver {

    @Override
    public boolean solve(Captcha captcha) {
        ((CaptchaImage)captcha).setStatus(Captcha.Status.SOLVED);
        ((CaptchaImage)captcha).setResponse("blank");
        return true;
    }

    @Override
    public boolean reportIncorrect(Captcha captcha) {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "blank-solver";
    }

    @Override
    public float getCredit() {
        return 1f;
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
        return 0;
    }

    @Override
    public void resetCaptchaCount() {
    }

    @Override
    public void close() throws IOException {
    }

}
