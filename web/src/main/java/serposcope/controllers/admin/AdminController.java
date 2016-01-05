/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.User;
import conf.SerposcopeConf;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import ninja.AuthenticityFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.Param;
import ninja.session.FlashScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.filters.AdminFilter;
import serposcope.filters.XSRFFilter;
import serposcope.helpers.Validator;

@FilterWith(AdminFilter.class)
@Singleton
public class AdminController extends BaseController {
    
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);
    
    @Inject
    SerposcopeConf conf;
    
    public Result admin(){
        
        return Results
            .ok()
            ;
    }
    
    public Result sysconfig(){
        
        StringBuilder builder = new StringBuilder(conf.dumpEnv());
        
        Properties props = System.getProperties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        
        return Results
            .ok()
            .text()
            .render(builder.toString())
            ;        
    }
    
}
