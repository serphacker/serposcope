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
import com.serphacker.serposcope.db.base.RunDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import static com.serphacker.serposcope.models.base.Group.Module.GOOGLE;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.time.LocalDateTime;
import java.util.List;
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
import com.serphacker.serposcope.task.TaskManager;
import java.util.Arrays;
import ninja.params.PathParam;
import serposcope.controllers.HomeController;

@FilterWith(AdminFilter.class)
@Singleton
public class TaskController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    @Inject
    TaskManager taskManager;

    @Inject
    GoogleDB googleDB;

    @Inject
    BaseDB baseDB;

    @Inject
    Router router;

    public Result debug() {

        return Results
            .ok();
    }

    public Result tasks(Context context,
        @Param("page") Integer page
    ) {

        List<Run> running = taskManager.listRunningTasks();

        if (page == null) {
            page = 0;
        }

        long limit = 50;
        long offset = page * limit;

        List<Run> done = baseDB.run.listByStatus(RunDB.STATUSES_DONE, limit, offset);

        Integer previousPage = page > 0 ? (page - 1) : null;
        Integer nextPage = done.size() == limit ? (page + 1) : null;

        return Results.ok()
            .render("previousPage", previousPage)
            .render("nextPage", nextPage)
            .render("running", running)
            .render("done", done);
    }

    @FilterWith(XSRFFilter.class)
    public Result startTask(
        Context context,
        @Param("module") Integer moduleId,
        @Param("update") Boolean update
    ) {
        FlashScope flash = context.getFlashScope();
//        Module module = Module.getByOrdinal(moduleId);
//        if (module == null || module != Module.GOOGLE) {
//            flash.error("error.invalidModule");
//            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
//        }        

        Run run = null;
        if(Boolean.TRUE.equals(update)){
            run = baseDB.run.findLast(GOOGLE, null, null);
        }
        
        if(run == null){
            run = new Run(Run.Mode.MANUAL, Group.Module.GOOGLE, LocalDateTime.now());
        } else {
            run.setStatus(Run.Status.RUNNING);
            run.setStarted(LocalDateTime.now());            
        }
        
        if (!taskManager.startGoogleTask(run)) {
            flash.error("admin.task.errGoogleAlreadyRunning");
            return Results.redirect(router.getReverseRoute(HomeController.class, "home"));
        }
        flash.success("admin.task.tasksStarted");
        return Results.redirect(router.getReverseRoute(HomeController.class, "home"));
    }

    @FilterWith(XSRFFilter.class)
    public Result abortTask(
        Context context,
        @Param("id") Integer runId
    ) {
        FlashScope flash = context.getFlashScope();
        if (runId == null) {
            flash.error("error.invalidId");
            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        Run run = baseDB.run.find(runId);
        if (run == null) {
            flash.error("error.invalidRun");
            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        if (run.getStatus() != Run.Status.RUNNING) {
            flash.error("error.invalidRun");
            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        switch (run.getModule()) {
            case GOOGLE:
                if (taskManager.abortGoogleTask(true)) {
                    flash.success("admin.task.abortingTask");
                } else {
                    flash.error("admin.task.failAbort");
                }
                break;

            default:
                flash.error("error.invalidModule");

        }

//        run.sets
        return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
    }

    @FilterWith(XSRFFilter.class)
    public Result deleteRun(
        Context context,
        @PathParam("runId") Integer runId
    ) {
        FlashScope flash = context.getFlashScope();

        Run run = baseDB.run.find(runId);
        if (run == null || run.getFinished() == null) {
            flash.error("error.invalidId");
            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        switch (run.getModule()) {
            case GOOGLE:
                googleDB.targetSummary.deleteByRun(run.getId());
                googleDB.rank.deleteByRunId(run.getId());
                googleDB.serp.deleteByRun(run.getId());
                baseDB.run.delete(run.getId());
                flash.put("warning", "admin.task.googleRunDeleted");
                break;

            default:
                flash.error("error.notImplemented");
                return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
    }

    @FilterWith(XSRFFilter.class)
    public Result rescanSerp(
        Context context,
        @PathParam("runId") Integer runId
    ) {
        FlashScope flash = context.getFlashScope();

        Run run = baseDB.run.find(runId);
        if (run == null || run.getFinished() == null) {
            flash.error("error.invalidId");
            return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        switch (run.getModule()) {
            case GOOGLE:
                // delete google ranks
                googleDB.targetSummary.deleteByRun(run.getId());
                googleDB.rank.deleteByRunId(run.getId());
                
                List<Group> groups = baseDB.group.list();
                for (Group group : groups) {
                    List<GoogleTarget> targets = googleDB.target.list(Arrays.asList(group.getId()));
                    List<GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(group.getId()));
                    googleDB.serpRescan.rescan(run.getId(), targets, searches, true);
                }
                
                /*
                Map<Integer, Integer> previousSummary = new HashMap<>();
                
                Run previousRun = baseDB.run.findPrevious(run.getId());
                if(previousRun != null){
                    previousSummary = googleDB.targetSummary.getPreviousScoreBP(previousRun.getId());
                } 

                Map<Integer,GoogleTargetSummary> summariesByTarget = new HashMap<>();
                Map<Integer,List<GoogleTarget>> targetsByGroup = new HashMap<>();                
                List<GoogleTarget> targets = googleDB.target.list();
                for (GoogleTarget target : targets) {
                    targetsByGroup.putIfAbsent(target.getGroupId(), new ArrayList<>());
                    targetsByGroup.get(target.getGroupId()).add(target);
                    summariesByTarget.put(target.getId(), new GoogleTargetSummary(target.getGroupId(), target.getId(), run.getId(), 
                        previousSummary.getOrDefault(target.getId(), 0)));
                }

                List<GoogleSearch> searches = googleDB.search.list();
                for (GoogleSearch search : searches) {
                    GoogleSerp res = googleDB.serp.get(run.getId(), search.getId());
                    if(res == null){
                        continue;
                    }
                    
                    List<Integer> searchGroupsIds = googleDB.search.listGroups(search);
                    
                    List<GoogleTarget> searchTargets = new ArrayList<>();
                    for (Integer groupId : searchGroupsIds) {
                        List<GoogleTarget> groupTargets = targetsByGroup.get(groupId);
                        if(groupTargets != null && !groupTargets.isEmpty()){
                            searchTargets.addAll(groupTargets);
                        }
                    }
                    
                    for (GoogleTarget target : searchTargets) {
                        GoogleBest best = googleDB.rank.getBest(target.getGroupId(), target.getId(), search.getId());
                        int rank = GoogleRank.UNRANKED;
                        String rankedUrl = null;
                        for (int i = 0; i < res.getEntries().size(); i++) {
                            if (target.match(res.getEntries().get(i).getUrl())) {
                                rankedUrl = res.getEntries().get(i).getUrl();
                                rank = i + 1;
                                break;
                            }
                        }

                        int previousRank = GoogleRank.UNRANKED;
                        if(previousRun != null){
                            previousRank = googleDB.rank.get(previousRun.getId(), target.getGroupId(), target.getId(), search.getId());
                        }
                        
                        GoogleRank googleRank = new GoogleRank(res.getRunId(), target.getGroupId(), target.getId(), 
                            search.getId(), rank, previousRank,rankedUrl);
                        
                        googleDB.rank.insert(googleRank);
                        
                        summariesByTarget.get(target.getId()).addRankCandidat(googleRank);

                        if(rank != GoogleRank.UNRANKED && rank <= best.getRank()){
                            best.setRank((short)rank);
                            best.setUrl(rankedUrl);
                            best.setRunDay(res.getRunDay());
                            googleDB.rank.insertBest(best);
                        }
                    }
                }
                if(!summariesByTarget.isEmpty()){
                    googleDB.targetSummary.insert(summariesByTarget.values());
                }
                */
                
                flash.success("admin.task.serpRescanDone");
                break;

            default:
                flash.error("error.notImplemented");
                return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
        }

        return Results.redirect(router.getReverseRoute(TaskController.class, "tasks"));
    }

//    public String getGroupRoute(Group group){
//        if(group != null){
//            switch(group.getModule()){
//                case GOOGLE:
//                    return router.getReverseRoute(GoogleGroupController.class, "view", "groupId", group.getId());
//            }
//        }
//        
//        return router.getReverseRoute(HomeController.class, "home");
//    }
}
