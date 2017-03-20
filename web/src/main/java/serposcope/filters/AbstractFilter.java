/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.filters;

import ninja.Filter;
import ninja.Result;
import ninja.diagnostics.DiagnosticError;


public abstract class AbstractFilter implements Filter {
    public boolean canRender(Result result){
        
        if ((result.getContentType() == null || result.getContentType().contains("text/html"))
            && (result.getStatusCode() < 300 || result.getStatusCode() > 399)
            && !(result.getRenderable() instanceof DiagnosticError)
        ) {
            return true;
        }        
        
        return false;
    }
}
