/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import conf.SerposcopeConf;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.Param;
import ninja.session.FlashScope;
import serposcope.controllers.google.GoogleGroupController;
import serposcope.filters.AdminFilter;
import serposcope.filters.AuthFilter;
import serposcope.filters.XSRFFilter;

@Singleton
@FilterWith(AuthFilter.class)
public class GroupController extends BaseController {
    
    @Inject
    SerposcopeConf conf;
    
    @Inject
    Router router;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;

    public Result groups(Context context) throws JsonProcessingException{
        long count = googleDB.search.count();
        return Results
            .ok()
            .render("groups", context.getAttribute("groups"))
            .render("search_count", googleDB.search.count())
            .render("h2warning", count > 2000 && conf.dbUrl != null && conf.dbUrl.contains(":h2:"))
            ;
    }
    
    @FilterWith({
        AdminFilter.class,
        XSRFFilter.class
    })
    public Result create(
        Context context,
        @Param("name") String name,
        @Param("module") Integer moduleNum
    ){
        FlashScope flash = context.getFlashScope();
        Module module = null; 
        
        if(name == null || name.isEmpty()){
            flash.error("error.invalidName");
            return Results.redirect(router.getReverseRoute(GroupController.class, "home"));
        }
        
        try {
            module = Module.values()[moduleNum];
        }catch(Exception ex){
            flash.error("error.invalidModule");
            return Results.redirect(router.getReverseRoute(GroupController.class, "home"));            
        }
        
        Group group = new Group(module, name);
        baseDB.group.insert(group);
        
        flash.success("home.groupCreated");
        switch(group.getModule()){
            case GOOGLE:
                return Results.redirect(router.getReverseRoute(GoogleGroupController.class, "view", "groupId", group.getId()));  
            default:
                return Results.redirect(router.getReverseRoute(GroupController.class, "home"));
        }
        
        
        
    }
    
}
