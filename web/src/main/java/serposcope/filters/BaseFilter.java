/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.User;
import conf.SerposcopeConf;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.diagnostics.DiagnosticError;
import ninja.i18n.Lang;
import ninja.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.AuthController;
import static serposcope.controllers.AuthController.PASSWORD_RESET_FILENAME;

@Singleton
public class BaseFilter extends AbstractFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFilter.class);
    
    @Inject
    ObjectMapper objectMapper;

    @Inject
    BaseDB baseDB;

    @Inject
    Router router;
    
    @Inject
    SerposcopeConf conf;
    
    @Override
    public Result filter(FilterChain filterChain, Context context) {
//        LOG.trace("filter");
        Result redirect = redirectIfNoAdmin(context);
        if (redirect != null) {
            return redirect;
        }

        beforeBaseFilter(context);
        Result result = filterChain.next(context);
        afterBaseFilter(context, result);

        return result;
    }

    public Result redirectIfNoAdmin(Context context) {
        if (baseDB.user.hasAdmin() && ! new File(conf.datadir + "/" + PASSWORD_RESET_FILENAME).exists()) {
            return null;
        }
        
        if (AuthController.class.equals(context.getRoute().getControllerClass())
            && ("createAdmin".equals(context.getRoute().getControllerMethod().getName()))
            || "doCreateAdmin".equals(context.getRoute().getControllerMethod().getName())) {
            
            return null;
        }
        
        return Results.redirect(router.getReverseRoute(AuthController.class, "createAdmin"));
    }

    public void beforeBaseFilter(Context context) {
        User user = getAuthUser(context);
        if (user != null) {
            context.setAttribute("user", user);
            context.setAttribute("groups", baseDB.group.listForUser(user));
        }
    }

    public void afterBaseFilter(Context context, Result result) {
        
        if(result.getRenderable() instanceof DiagnosticError){
            return;
        }

        if (context.getAttribute("afterBaseFilter") != null) {
            LOG.warn("afterBaseFilter already called");
//            return;
        }
        context.setAttribute("afterBaseFilter", "called");

        result.supportedContentType(Result.TEXT_HTML);
        result.fallbackContentType(Result.TEXT_HTML);

        if (context.getAttribute("indexable") == null) {
            result.addHeader("X-Robots-Tag", "noindex, nofollow");
        }
        result.addHeader("X-Frame-Options", "deny"); // clickjacking
        result.addHeader("X-XSS-Protection", "1; mode=block"); // inurl xss
        result.addHeader("X-Content-Type-Options", "nosniff"); // prevent mime sniffing

        if (canRender(result)) {
            User user = context.getAttribute("user", User.class);
            if (user != null) {
                result.render("user", user);
            }
            List<Group> groups = context.getAttribute("groups", List.class);
            if(groups != null){
                try {
                    result.render("sidebarGroups", objectMapper.writeValueAsString(groups));
                }catch(JsonProcessingException ex){
                }
            }
        }
    }

    protected User getAuthUser(Context context) {
        String id = context.getSession().get("id");
        
        if (id == null || !isInt(id)) {
            return null;
        }

        User user = baseDB.user.findById(Integer.parseInt(id));

        if (user == null) {
            return null;
        }

        Long lastlogEpochSecond = null;
        try {
            lastlogEpochSecond = Long.parseLong(context.getSession().get("to"));
        } catch (Exception ex) {
        }

        if (lastlogEpochSecond == null || lastlogEpochSecond < 1l) {
            return null;
        }

        if (user.loggedOutAfter(lastlogEpochSecond)) {
            return null;
        }

        return user;
    }

    private boolean isInt(String id) {
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException | NullPointerException ex) {
            return false;
        }
    }

}
