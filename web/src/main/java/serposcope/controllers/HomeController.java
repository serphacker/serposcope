/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.RunDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import serposcope.filters.AuthFilter;

@Singleton
@FilterWith(AuthFilter.class)
public class HomeController extends BaseController {
    
    @Inject
    Router router;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    public static class TargetHomeEntry {

        public TargetHomeEntry(String groupName, GoogleTarget target, GoogleTargetSummary summary, List<Integer> scoreHistory) {
            this.groupName = groupName;
            this.target = target;
            this.summary = summary;
            this.scoreHistory = scoreHistory;
        }
        public final String groupName;
        public final GoogleTarget target;
        public final GoogleTargetSummary summary;
        public final List<Integer> scoreHistory;
    }

    
    public Result home(Context context) {
        
        String display = context.getParameter("display" , baseDB.config.getConfig().getDisplayHome());
        if(!Config.VALID_DISPLAY_HOME.contains(display)){
            display = Config.DEFAULT_DISPLAY_HOME;
        }
        
        List<Group> groups = (List<Group>) context.getAttribute("groups");
        Run currentRun = baseDB.run.findLast(Module.GOOGLE, RunDB.STATUSES_RUNNING, null);
        Run lastRun = baseDB.run.findLast(Module.GOOGLE, RunDB.STATUSES_DONE, null);
        if(lastRun == null){
            return Results
                .ok()
                .template("/serposcope/views/HomeController/" + display + ".ftl.html")
                .render("display", display)
                .render("summaries", Collections.EMPTY_LIST)
                .render("currentRun", currentRun)
                .render("lastRun", lastRun)
                .render("groups", groups)
                .render("lastlog", LocalDate.now().toString() + ".log")
                ;
        }
        
        List<TargetHomeEntry> summaries = new ArrayList<>();
        
        Map<Integer, GoogleTargetSummary> summariesByTarget = googleDB.targetSummary.list(
            lastRun.getId(), "table".equals(display)).stream().collect(
                Collectors.toMap(GoogleTargetSummary::getTargetId,Function.identity())
            );
        
        List<GoogleTarget> targets = googleDB.target.list(groups.stream().map(Group::getId).collect(Collectors.toList()));
        
        Map<Integer, Group> groupById = groups.stream().collect(Collectors.toMap(Group::getId, Function.identity()));
        
        
        for (GoogleTarget target : targets) {
            GoogleTargetSummary summary = summariesByTarget.get(target.getId());
            if(summary != null){
                List<Integer> scoreHistory = googleDB.targetSummary.listScoreHistory(target.getGroupId(), target.getId(), 30);
                int missingScore = 30 - scoreHistory.size();
                for (int i = 0; i < missingScore; i++) {
                    scoreHistory.add(0, 0);
                }
                summaries.add(new TargetHomeEntry(groupById.get(target.getGroupId()).getName(), target, summary, scoreHistory));
            }
        }
        
        Set<Integer> searchIds = new HashSet<>();
        for (TargetHomeEntry homeEntry : summaries) {
            homeEntry.summary.visitReferencedSearchId(searchIds);
        }
        
        return Results
            .ok()
            .template("/serposcope/views/HomeController/" + display + ".ftl.html")
            .render("display", display)
            .render("groups", context.getAttribute("groups"))
            .render("currentRun", currentRun)
            .render("lastRun", lastRun)
            .render("lastRuns", baseDB.run.listByStatus(null, 7l, 0l))
            .render("hasTarget", googleDB.target.hasTarget())
            .render("summaries", summaries)
            .render("searches", googleDB.search.mapBySearchId(searchIds))
            .render("lastlog", LocalDate.now().toString() + ".log")
            ;
    }

//    public static class GoogleHomeEntry {
//
//        public GoogleHomeEntry(GoogleTarget target, GoogleSearch search, GoogleRank rank) {
//            this.target = target;
//            this.search = search;
//            this.rank = rank;
//        }
//                
//        public GoogleTarget target;
//        public GoogleSearch search;
//        public GoogleRank rank;
//
//        public GoogleTarget getTarget() {
//            return target;
//        }
//
//        public GoogleSearch getSearch() {
//            return search;
//        }
//
//        public GoogleRank getRank() {
//            return rank;
//        }
//        
//    }
//    public Result home(Context context) throws JsonProcessingException{
//        
//        List<GoogleHomeEntry> googleRanksUp = new ArrayList<>();
//        List<GoogleHomeEntry> googleRanksDown = new ArrayList<>();
//        List<GoogleHomeEntry> googleRanksSame = new ArrayList<>();
//        
//        Run lastRun = baseDB.run.findLastDone();
//        if(lastRun == null){
//            return Results
//                .ok()
//                .render("groups", context.getAttribute("groups"));
//        }
//        
//        
//        List<Group> groups = context.getAttribute("groups", List.class);
//        for (Group group : groups) {
//            Run run = baseDB.run.findLastDone(group.getId());
//            if(run == null){
//                continue;
//            }
//            
//            List<GoogleRank> ranks = googleDB.rank.list(run.getId(), group.getId());
//            
//            Map<Integer, GoogleTarget> targets = googleDB.target.list(Arrays.asList(group.getId()))
//                .stream().collect(Collectors.toMap(GoogleTarget::getId, Function.identity()));
//            
//            Map<Integer, GoogleSearch> searches = googleDB.search.listByGroup(Arrays.asList(group.getId()))
//                .stream().collect(Collectors.toMap(GoogleSearch::getId, Function.identity()));            
//            
//            for (GoogleRank rank : ranks) {
//                
//                GoogleTarget target = targets.get(rank.googleTargetId);
//                GoogleSearch search = searches.get(rank.googleSearchId);
//                
//                if(target == null || search == null){
//                    continue;
//                }
//                
//                GoogleHomeEntry entry = new GoogleHomeEntry(target,search, rank);
//                if(rank.diff > 0){
//                    googleRanksDown.add(entry);
//                } else if(rank.diff < 0){
//                    googleRanksUp.add(entry);
//                } else {
//                    googleRanksSame.add(entry);
//                }
//            }
//        }
//        
//        Collections.sort(googleRanksUp, (GoogleHomeEntry o1, GoogleHomeEntry o2) -> 
//            Integer.compare(o1.getRank().diff, o2.getRank().diff));
//        Collections.sort(googleRanksDown, (GoogleHomeEntry o1, GoogleHomeEntry o2) -> 
//            -Integer.compare(o1.getRank().diff, o2.getRank().diff));        
//        Collections.sort(googleRanksSame, (GoogleHomeEntry o1, GoogleHomeEntry o2) -> 
//            Integer.compare(o1.getRank().rank, o2.getRank().rank));        
//        
//        Run currentRun = new Run();
//        currentRun.setProgress(14);
//        currentRun.setStarted(LocalDateTime.now().minusMinutes(30));
//        currentRun.setStatus(Run.Status.RUNNING); 
//        
//        return Results
//            .ok()
//            .render("groups", context.getAttribute("groups"))
//            .render("lastRun", lastRun)
//            .render("task", currentRun)
//            .render("googleRanksUp", googleRanksUp)
//            .render("googleRanksDown", googleRanksDown)
//            .render("googleRanksSame", googleRanksSame)
//            ;
//    }
    
}
