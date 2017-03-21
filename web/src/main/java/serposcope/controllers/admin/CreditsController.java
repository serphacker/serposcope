package serposcope.controllers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import conf.SerposcopeConf;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import serposcope.controllers.BaseController;
import serposcope.filters.AdminFilter;

@FilterWith(AdminFilter.class)
@Singleton
public class CreditsController extends BaseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CreditsController.class);

    @Inject
    SerposcopeConf conf;
    
    @Inject
    Router router;
    
    public Result credits(Context context) {
    	
    	return Results
    			.ok();
    }

}
