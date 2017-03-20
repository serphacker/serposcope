/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.filters;

import com.google.inject.Singleton;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.session.Session;
import ninja.utils.NinjaConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class XSRFFilter extends AbstractFilter {

    public static final String XSRF_INPUT_NAME = "_xsrf";

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        
        if (!validXsrf(context)) {
            return Results
                .badRequest()
                .template("serposcope/views/system/400xsrf.ftl");
        }
        
        return filterChain.next(context);
        
    }
    
    public static boolean validXsrf(Context context){
        Session session = context.getSession();
        String authenticityToken = context.getParameter(NinjaConstant.AUTHENTICITY_TOKEN);
        if(authenticityToken == null){
            authenticityToken = context.getParameter(XSRF_INPUT_NAME);
        }
        
        if(session.getAuthenticityToken().equals(authenticityToken)){
//            session.remove("___AT"); // regenerate a new xsrf token
            return true;
        }
        
        return false;
    }
    
}

