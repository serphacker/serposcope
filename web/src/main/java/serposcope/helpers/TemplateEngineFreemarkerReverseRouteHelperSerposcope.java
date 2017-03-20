/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;


import java.util.ArrayList;
import java.util.List;

import ninja.Router;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import ninja.template.TemplateEngineFreemarkerReverseRouteHelper;

@Singleton
public class TemplateEngineFreemarkerReverseRouteHelperSerposcope 
    extends TemplateEngineFreemarkerReverseRouteHelper
{
    
    Router router;

    @Inject
    public TemplateEngineFreemarkerReverseRouteHelperSerposcope(Router router) {
        super(router);
        this.router = router;
    }

    @Override
    public TemplateModel computeReverseRoute(List args) throws TemplateModelException {

        if (args.size() < 2) {

            throw new TemplateModelException(
                    "Please specify at least classname and controller (2 parameters).");

        } else {

            List<String> strings = new ArrayList<>(args.size());

            for (Object o : args) {

                // We currently allow only numbers and strings as arguments
                if (o instanceof String) {
                    strings.add((String) o);
                } if (o instanceof SimpleScalar) {
                    strings.add(((SimpleScalar) o).getAsString());
                } else if (o instanceof SimpleNumber) {
                    strings.add(((SimpleNumber) o).toString());
                }

            }

            String className = "serposcope.controllers." + strings.get(0);
            try {
                
                Class<?> clazz = Class.forName(className);
                
                Object [] parameterMap = strings.subList(2, strings.size()).toArray();

                String reverseRoute = router.getReverseRoute(
                        clazz,
                        strings.get(1),
                        parameterMap);

                return new SimpleScalar(reverseRoute);
            } catch (ClassNotFoundException ex) {
                throw new TemplateModelException("Error. Cannot find class for String: " + className);
            }
        }

    }
}