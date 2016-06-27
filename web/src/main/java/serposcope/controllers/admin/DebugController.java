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
import com.p6spy.engine.spy.P6DataSource;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTarget.PatternType;
import com.serphacker.serposcope.task.TaskManager;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.i18n.Messages;
import ninja.params.Param;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.AuthController;
import serposcope.controllers.BaseController;
import serposcope.controllers.admin.DebugController.DebugFilter;
import serposcope.filters.AdminFilter;
import serposcope.filters.XSRFFilter;

@FilterWith({
    AdminFilter.class,
    DebugFilter.class
})
@Singleton
public class DebugController extends BaseController {
    
    public static class DebugFilter implements Filter{
        
        @Inject
        NinjaProperties props;
        
        @Inject
        Router router;

        @Override
        public Result filter(FilterChain filterChain, Context context) {

            if(props.isProd()){
                context.getFlashScope().error("error.unauthorizedAccess");
                return Results.redirect(router.getReverseRoute(AuthController.class, "login"));
            }

            return filterChain.next(context);
        }
        
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    Router router;
    
    @Inject
    TaskManager taskManager;
    
    @Inject
    Messages msg;
    
    public Result debug(Context context){
        
        return Results
            .ok()
//            .render("f_warning",msg.get("test.translate", context, Optional.absent(),12l).or(""))
            ;
    }
    
    @FilterWith(XSRFFilter.class)
    public Result wipeRankings(Context context){
        FlashScope flash = context.getFlashScope();
        
        googleDB.targetSummary.wipe();
        googleDB.rank.wipe();
        googleDB.serp.wipe();
        baseDB.run.wipe();
        
        flash.put("warning","admin.debug.rankingsWiped");
        return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
    }
    
    @FilterWith(XSRFFilter.class)
    public Result wipeGroups(Context context){
        FlashScope flash = context.getFlashScope();
        
        
        googleDB.targetSummary.wipe();
        googleDB.rank.wipe();
        googleDB.serp.wipe();
        baseDB.run.wipe();
        
        googleDB.target.wipe();
        googleDB.search.wipe();
        baseDB.event.wipe();
        baseDB.group.wipe();
        
        flash.put("warning","admin.debug.groupsWiped");
        return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
    }
    
    @FilterWith(XSRFFilter.class)
    public Result generate(
        Context context,
        @Param("groups") Integer groups,
        @Param("searchPerGroup") Integer searchPerGroup,
        @Param("targetPerGroup") Integer targetPerGroup
    ){
        FlashScope flash = context.getFlashScope();
        
        if(groups == null || groups < 1 || searchPerGroup == null || searchPerGroup < 1 || targetPerGroup == null || targetPerGroup < 1){
            flash.put("error","error.invalidParameters");
            return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));            
        }
        
        for (int i = 0; i < groups; i++) {
            Group group = new Group(Group.Module.GOOGLE, "group#" + i);
            baseDB.group.insert(group);
            
            List<GoogleSearch> searches = new ArrayList<>();
            for (int j = 0; j < searchPerGroup; j++) {
                GoogleSearch search = new GoogleSearch("search#" + j + "#" + group.getName());
                search.setTld("com");
                searches.add(search);
            }
            googleDB.search.insert(searches, group.getId());

            List<GoogleTarget> targets = new ArrayList<>();
            for (int j = 0; j < targetPerGroup; j++) {
                int targetFakeId = j+1;
                GoogleTarget target = new GoogleTarget(group.getId(), "target#" + targetFakeId + "#" + group.getName(), 
                    PatternType.REGEX, "^https?://www.site" + targetFakeId + ".com.+");
                targets.add(target);
            }
            googleDB.target.insert(targets);
            
        }
        
        
        flash.put("warning","admin.debug.generated");
        return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
    }    
    
    
    @FilterWith(XSRFFilter.class)
    public Result dryRun(Context context,
        @Param("startDate") String start,
        @Param("endDate") String end
    ){
        long _start = System.currentTimeMillis();
        FlashScope flash = context.getFlashScope();

        LocalDate startDate = null;
        LocalDate endDate = null;
        
        try {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        } catch(Exception ex){
        }
        
        if(startDate == null || endDate == null || startDate.isAfter(endDate)){
            flash.error("error.invalidDate");
            return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
        }
        
        Run lastRun = baseDB.run.findLast(Module.GOOGLE, null, null);
        if(lastRun != null && lastRun.getDay().isAfter(startDate)){
            flash.error("error.invalidDate");
            return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));            
        }
        
        LocalDate date = LocalDate.from(startDate);
        
        GoogleSettings ggOptions = googleDB.options.get();
        
        int minPauseBetweenPageSec = ggOptions.getMinPauseBetweenPageSec();
        int maxPauseBetweenPageSec = ggOptions.getMaxPauseBetweenPageSec();
        ggOptions.setMinPauseBetweenPageSec(0);
        ggOptions.setMaxPauseBetweenPageSec(0);
        googleDB.options.update(ggOptions);
        
        try {
            while(date.isBefore(endDate)){
                LOG.debug("dry run {}", date);
                if(!taskManager.startGoogleTask(new Run(Run.Mode.MANUAL, Module.GOOGLE, date.atTime(13, 37, 00)))){
                    LOG.error("can't startGoogleTask");
                    flash.error("can't startGoogleTask");
                    return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
                }
                taskManager.joinGoogleTask();
                date = date.plusDays(1);
            }            
        } catch(Exception ex){
            LOG.error("an error occured", ex);
            flash.error("an error occured");
            return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
        } finally {
            ggOptions.setMinPauseBetweenPageSec(minPauseBetweenPageSec);
            ggOptions.setMaxPauseBetweenPageSec(maxPauseBetweenPageSec);        
            googleDB.options.update(ggOptions);            
        }

        LOG.debug("dry run timing : {}", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_start));
        flash.success("ok");
        return Results.redirect(router.getReverseRoute(DebugController.class, "debug"));
    }    
    
    @Inject
    DataSource ds;
    
    public Result test(@Param("query") String query) throws SQLException{
        long _start = System.currentTimeMillis();
        
        HikariDataSource hds= null;
        if(ds instanceof HikariDataSource){
            hds = (HikariDataSource)ds;
        } else {
            hds = ((P6DataSource)ds).unwrap(HikariDataSource.class);
        }
        
        if(query != null){
//            try(Connection con = DriverManager.getConnection(hds.getJdbcUrl()); Statement stmt = con.createStatement() ){
            try(Connection con = ds.getConnection(); Statement stmt = con.createStatement() ){
                stmt.execute(query);
            }
        }

        String duration = DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_start);
        
        LOG.info(duration);
        
        return Results.ok()
            .text()
            .render(duration);
    }
    
    public Result shutdown(){
        System.exit(0);
        return Results.ok();
    }
    
    @FilterWith(XSRFFilter.class)
    public Result dummyPost(){
        return Results.ok().text().render("dummyPost");
    }
    
    
}
