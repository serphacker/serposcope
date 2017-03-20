/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.User;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.HomeController;

@Singleton
public class CanReadFilter extends AbstractFilter {
    
    private static final Logger LOG = LoggerFactory.getLogger(CanReadFilter.class);
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    Router router;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
//        LOG.trace("filter");
        
        FlashScope flash = context.getFlashScope();
        
        User user = context.getAttribute("user", User.class);
        Group group = getGroup(context);
        
        if(group == null || (!user.isAdmin() && !user.canRead(group))){
            flash.error("error.unauthorizedAccess");
            return Results.redirect(router.getReverseRoute(HomeController.class, "home"));
        }
        context.setAttribute("group", group);
        
        Result result = filterChain.next(context);
        
        if(canRender(result)){
            result.render("group", group);
        }
        
        return result;
    }
    
    protected Group getGroup(Context context){
        Integer groupId = context.getPathParameterAsInteger("groupId");
        if(groupId == null){
            return null;
        }

        return baseDB.group.find(groupId);
    }    

}
