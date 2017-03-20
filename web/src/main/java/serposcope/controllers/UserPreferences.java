/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.User;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.params.Param;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import serposcope.filters.AuthFilter;
import serposcope.filters.XSRFFilter;
import serposcope.helpers.Validator;

@Singleton
@FilterWith(AuthFilter.class)
public class UserPreferences extends BaseController {
    
    @Inject
    NinjaProperties props;
    
    @Inject
    Router router;

    @Inject
    BaseDB baseDB;
    
    @Inject
    Lang lang;

    public Result preferences(Context context) {
        return Results.ok()
            .render("lang", lang.getLanguage(context, Optional.absent()));
    }

    @FilterWith(XSRFFilter.class)
    public Result update(
        @Param("password") String password,
        @Param("passwordConfirm") String passwordConfirm,
        @Param("passwordCurrent") String passwordCurrent,
        @Param("lang") String chooseLang,
        Context context
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        FlashScope flash = context.getFlashScope();
        User user = context.getAttribute("user", User.class);

        if (!Validator.isEmpty(password)) {

            if (password == null || password.length() < 6) {
                flash.error("error.invalidPassword");
                return Results.redirect(router.getReverseRoute(UserPreferences.class, "preferences"));
            }

            if (!password.equals(passwordConfirm)) {
                flash.error("error.invalidPasswordConfirm");
                return Results.redirect(router.getReverseRoute(UserPreferences.class, "preferences"));
            }
            
            if(passwordCurrent==null || !user.verifyPassword(passwordCurrent)){
                flash.error("error.invalidCurrentPassword");
                return Results.redirect(router.getReverseRoute(UserPreferences.class, "preferences"));                
            }
            
            user.setPassword(password);
        }

        baseDB.user.update(user);
        
        flash.success("preferences.preferencesUpdated");
        Result result = Results.redirect(router.getReverseRoute(UserPreferences.class, "preferences"));
        
        if(lang.isLanguageDirectlySupportedByThisApplication(chooseLang)){
            lang.setLanguage(chooseLang, result);
        } else {
            lang.clearLanguage(result);
        }
        
        return result;
    }

}
