/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers;

import com.google.inject.Inject;
import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.User;
import conf.SerposcopeConf;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import ninja.Context;
import ninja.Router;
import ninja.params.Param;
import ninja.session.FlashScope;
import ninja.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.helpers.Validator;

@Singleton
public class AuthController extends BaseController {
    public final static String PASSWORD_RESET_FILENAME = "password-reset.txt";
    
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    protected final static Long SESSION_NORMAL_LIFETIME = 2 * 60 * 60 * 1000L;    
    protected final static Long SESSION_REMEMBER_LIFETIME = 30 * 24 * 60 * 60 * 1000L;

    @Inject
    Router router;

    @Inject
    BaseDB baseDB;
    
    @Inject
    SerposcopeConf conf;

    public Result createAdmin(Context context) {
        if (!canCreateAdmin()) {
            context.getFlashScope().error("error.unauthorizedAccess");
            return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
        }
        return Results.ok()
            .render("passwordResetFileExists", passwordResetFileExists());
    }

    public synchronized Result doCreateAdmin(
        Context context,
        @Param("email") String email,
        @Param("email-confirm") String emailConfirm,
        @Param("password") String password,
        @Param("password-confirm") String passwordConfirm
    ) {
        FlashScope flash = context.getFlashScope();

        if (!canCreateAdmin()) {
            flash.error("error.unauthorizedAccess");
            return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
        }

        if (!Validator.isEmailAddress(email)) {
            flash.error("error.invalidEmail");
            return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
        }

        if (!email.equals(emailConfirm)) {
            flash.error("error.invalidEmailConfirm");
            return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
        }

        if (password == null || password.length() < 6) {
            flash.error("error.invalidPassword");
            return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
        }

        if (!password.equals(passwordConfirm)) {
            flash.error("error.invalidPasswordConfirm");
            return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
        }

        try {
            
            // password reset case
            User foundUser = baseDB.user.findByEmail(email);
            if(foundUser != null){
                
                foundUser.setPassword(password);
                foundUser.setAdmin(true);
                if(!baseDB.user.update(foundUser)){
                    LOG.error("can't update user in database");
                    flash.error("error.internalError");
                    return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
                } else {
                    flash.success("auth.createAdmin.passwordResetSuccess");
                    return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));                    
                }
            } else {
            
                User user = new User();
                user.setEmail(email);
                user.setPassword(password);
                user.setAdmin(true);
                if (baseDB.user.insert(user) == -1) {
                    LOG.error("can't insert user in database");
                    flash.error("error.internalError");
                    return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
                }
            }
        } catch (Exception ex) {
            LOG.error("internal error while saving admin user", ex);
            flash.error("error.internalError");
            return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
        }

        flash.success("auth.createAdmin.adminCreated");
        return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
    }

    protected boolean canCreateAdmin() {
        return !baseDB.user.hasAdmin() || passwordResetFileExists();
    }
    
    protected boolean passwordResetFileExists(){
        return new File(conf.datadir + "/" + PASSWORD_RESET_FILENAME).exists();
    }

    public Result login() {
        return Results.ok();
    }

    public Result doLogin(
        Context context,
        @Param("email") String email,
        @Param("password") String password,
        @Param("remember") Boolean rememberMe
    ) {
        FlashScope flash = context.getFlashScope();

        if (!Validator.isEmailAddress(email) || password == null) {
            flash.error("auth.login.invalidCredentials");
            return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
        }

        User user = baseDB.user.findByEmail(email);
        if (user == null) {
            flash.error("auth.login.invalidCredentials");
            return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
        }

        try {
            if (!user.verifyPassword(password)) {
                flash.error("auth.login.invalidCredentials");
                return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
            }
        } catch (Exception ex) {
            LOG.error("internal error on verifyPassword", ex);
            flash.error("msg.internalError");
            return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
        }

        Session sess = context.getSession();
        sess.put("to", Long.toString(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
        sess.put("id", Integer.toString(user.getId()));
        if (rememberMe != null && rememberMe) {
            sess.setExpiryTime(SESSION_REMEMBER_LIFETIME);
        } else {
            sess.setExpiryTime(SESSION_NORMAL_LIFETIME);
        }
        sess.getAuthenticityToken(); // generate token

        return Results.redirect(router.getReverseRoute(HomeController.class, "home"));
    }

    public Result logout(Context context) {
        User user = context.getAttribute("user", User.class);
        if(user != null){
            user.setLogout(LocalDateTime.now());
            baseDB.user.update(user);
        }
//        sess.clear();
        context.getFlashScope().success("auth.loggedOut");
        return Results.redirect(router.getReverseRoute(AuthController.class, "login"));        
    }

}
