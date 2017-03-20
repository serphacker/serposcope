/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers;

import com.google.inject.Inject;
import com.serphacker.serposcope.models.base.Group;
import ninja.FilterWith;
import ninja.Router;
import serposcope.controllers.google.GoogleGroupController;
import serposcope.filters.BaseFilter;

@FilterWith(BaseFilter.class)
public abstract class BaseController {

}
