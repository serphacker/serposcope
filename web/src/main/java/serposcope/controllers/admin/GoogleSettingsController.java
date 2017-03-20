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
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
public class GoogleSettingsController extends BaseController {
    
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSettingsController.class);
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    Router router;
    
    public Result settings(){
        return Results
            .ok()
            .render("options", googleDB.options.get());
    }
    
    @FilterWith(XSRFFilter.class)
    public Result update(
        Context context,
        @Param("targetDisplayMode") String targetDisplayMode, 
        @Param("searchDisplayMode") String searchDispayMode,
        @Param("pages") Integer pages, @Param("result-per-page") Integer resultPerPage,
        @Param("min-pause") Integer minPause, @Param("max-pause") Integer maxPause,
        @Param("maxThreads") Integer maxThreads, @Param("fetchRetry") Integer fetchRetry,
        @Param("tld") String tld, @Param("datacenter") String datacenter,
        @Param("device") Integer device,
        @Param("local") String local, @Param("custom") String custom
    ){
        FlashScope flash = context.getFlashScope();
        
        GoogleSettings defaultOptions = new GoogleSettings();
        GoogleSettings options = googleDB.options.get();
        
        if(pages != null && resultPerPage != null){
            if(pages*resultPerPage > 1000 || pages*resultPerPage < 1){
                flash.error("admin.google.invalidPages");
                return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));
            }
            options.setPages(pages);
            options.setResultPerPage(resultPerPage);
        }
        
        if(minPause != null && maxPause != null){
            if(minPause > maxPause){
                flash.error("admin.google.invalidPauseRange");
                return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));                
            }
            options.setMinPauseBetweenPageSec(minPause);
            options.setMaxPauseBetweenPageSec(maxPause);
        }
        
        if(fetchRetry != null){
            options.setFetchRetry(fetchRetry);
        }
        
        if(maxThreads != null){
            options.setMaxThreads(maxThreads);
        }
        
        if(!Validator.isEmpty(tld)){
            if(!Validator.isGoogleTLD(tld)){
                flash.error("admin.google.invalidTLD");
                return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));    
            }            
            options.setDefaultTld(tld);
        } else {
            options.setDefaultTld(defaultOptions.getDefaultTld());
        }
        
        if(!Validator.isEmpty(datacenter)){
            if( !Validator.isIPv4(datacenter)){
                flash.error("error.invalidIP");
                return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));
            }
            options.setDefaultDatacenter(datacenter);            
        } else {
            options.setDefaultDatacenter(defaultOptions.getDefaultDatacenter());
        }
        
        
        if(device != null && device == 1){
            options.setDefaultDevice(GoogleDevice.MOBILE);
        } else {
            options.setDefaultDevice(GoogleDevice.DESKTOP);
        }
        
        if(Validator.isNotEmpty(local)){
            options.setDefaultLocal(local);
        } else {
            options.setDefaultLocal(defaultOptions.getDefaultLocal());
        }
        
        if(Validator.isNotEmpty(custom)){
            options.setDefaultCustomParameters(custom);
        } else {
            options.setDefaultCustomParameters(defaultOptions.getDefaultCustomParameters());
        }        
        
        
        googleDB.options.update(options);
        
        flash.success("label.settingsUpdated");
        return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));
    }
    
    @FilterWith(XSRFFilter.class)
    public Result reset(
        Context context
    ){
        GoogleSettings options = new GoogleSettings();
        googleDB.options.update(options);
        context.getFlashScope().success("label.settingsUpdated");
        return Results.redirect(router.getReverseRoute(GoogleSettingsController.class, "settings"));
    }    
}
