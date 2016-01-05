/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import freemarker.template.utility.StringUtil;
import java.util.Arrays;
import java.util.List;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.session.FlashScope;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.HomeController;

@Singleton
public class GoogleGroupFilter extends AbstractFilter {
    
    private static final Logger LOG = LoggerFactory.getLogger(GoogleGroupFilter.class);
    
    @Inject
    ObjectMapper objectMapper;
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    Router router;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
//        LOG.trace("filter");
        
        FlashScope flash = context.getFlashScope();
        
        Group group = context.getAttribute("group", Group.class);
        
        if(!Module.GOOGLE.equals(group.getModule())){
            flash.error("error.unauthorizedAccess");
            return Results.redirect(router.getReverseRoute(HomeController.class, "home"));
        }
        
        List<GoogleTarget> targets = googleDB.target.list(Arrays.asList(group.getId()));
        context.setAttribute("targets", targets);
        
        List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(group.getId()));
        context.setAttribute("searches", searches);
        
        Result result = filterChain.next(context);
        
        if(canRender(result)){
            result.render("sidebarGoogleSearches", serializeSearches(group, searches));
            result.render("sidebarGoogleTargets", serializeTargets(group, targets));
        }
        
        
        return result;
    }
    
    public String serializeTargets(Group group, List<GoogleTarget> targets){
        if(targets == null || targets.isEmpty()){
            return "[]";
        }
        
        StringBuilder builder = new StringBuilder("[");
        for (GoogleTarget target : targets) {
            builder.append("{\"id\":").append(target.getId()).append(",");
            builder.append("\"group\":").append(group.getId()).append(",");
            builder.append("\"name\":\"").append(StringEscapeUtils.escapeJson(target.getName())).append("\"},");
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }
    
    public String serializeSearches(Group group, List<GoogleSearch> searches){
        if(searches == null || searches.isEmpty()){
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (GoogleSearch search : searches) {
            builder.append("{\"id\":").append(search.getId()).append(",");
            builder.append("\"group\":").append(group.getId()).append(",");
            builder.append("\"name\":\"").append(StringEscapeUtils.escapeJson(search.getKeyword())).append("\"},");
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }    

}
