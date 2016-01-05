/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.google;

import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.util.List;
import ninja.Context;
import ninja.FilterWith;
import serposcope.controllers.BaseController;
import serposcope.filters.AuthFilter;
import serposcope.filters.CanReadFilter;
import serposcope.filters.GoogleGroupFilter;

@FilterWith({
    AuthFilter.class,
    CanReadFilter.class,
    GoogleGroupFilter.class
})
public abstract class GoogleController extends BaseController {
    
    protected List<GoogleTarget> getTargets(Context context){
        return context.getAttribute("targets", List.class);
    }
    
    protected List<GoogleSearch> getSearches(Context context){
        return context.getAttribute("searches", List.class);
    }    

    protected GoogleTarget getTarget(Context context, Integer targetId){
        if(targetId == null){
            return null;
        }
        List<GoogleTarget> targets = context.getAttribute("targets", List.class);
        for (GoogleTarget target : targets) {
            if(target.getId() == targetId){
                return target;
            }
        }
        
        return null;
    }
    
    protected GoogleSearch getSearch(Context context, Integer searchId){
        if(searchId == null){
            return null;
        }
        List<GoogleSearch> searches = context.getAttribute("searches", List.class);
        for (GoogleSearch search : searches) {
            if(search.getId() == searchId){
                return search;
            }
        }
        
        return null;
    }
    
}
