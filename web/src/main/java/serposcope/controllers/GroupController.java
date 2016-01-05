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
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    Router router;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;

    public Result groups(Context context) throws JsonProcessingException{
        
        return Results
            .ok()
            .render("groups", context.getAttribute("groups"))
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
