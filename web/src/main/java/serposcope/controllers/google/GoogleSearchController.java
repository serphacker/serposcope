/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import static com.serphacker.serposcope.db.base.RunDB.STATUSES_DONE;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Run;
import static com.serphacker.serposcope.models.google.GoogleRank.UNRANKED;
import com.serphacker.serposcope.models.google.GoogleSettings;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import ninja.Context;
import ninja.Router;
import ninja.i18n.Messages;
import ninja.params.Param;
import ninja.params.PathParam;
import org.apache.commons.lang3.StringEscapeUtils;


@Singleton
public class GoogleSearchController extends GoogleController {
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    Router router;
    
    @Inject
    Messages msg;
    
    @Inject
    ObjectMapper objectMapper;    
    
    public Result search(Context context, 
        @PathParam("searchId") Integer searchId,
        @Param("startDate") String startDateStr,
        @Param("endDate") String endDateStr
    ){
        GoogleSearch search = getSearch(context, searchId);
        Group group = context.getAttribute("group", Group.class);
        
        if(search == null){
            context.getFlashScope().error("error.invalidSearch");
            return Results.redirect(router.getReverseRoute(GoogleGroupController.class, "view", "groupId", group.getId()));
        }
        
        Run minRun = baseDB.run.findFirst(Module.GOOGLE, STATUSES_DONE, null);
        Run maxRun = baseDB.run.findLast(Module.GOOGLE, STATUSES_DONE, null);
        if(maxRun == null || minRun == null){
            return Results.ok()
                .render("search", search);
        }
        
        LocalDate minDay = minRun.getDay();
        LocalDate maxDay = maxRun.getDay();
        
        LocalDate startDate = null;
        if(startDateStr != null){
            try {startDate = LocalDate.parse(startDateStr);} catch(Exception ex){}
        }
        LocalDate endDate = null;
        if(endDateStr != null){
            try {endDate = LocalDate.parse(endDateStr);} catch(Exception ex){}
        }
        
        if(startDate == null || endDate == null || endDate.isBefore(startDate)){
            startDate = maxDay.minusDays(30);
            endDate = maxDay;
        }
        
        Run firstRun = baseDB.run.findFirst(Module.GOOGLE, STATUSES_DONE, startDate);
        Run lastRun = baseDB.run.findLast(Module.GOOGLE, STATUSES_DONE, endDate);
        
        if(firstRun == null || lastRun == null || firstRun.getDay().isAfter(lastRun.getDay())){
            return Results.ok()
                .render("f_warning", msg.get("error.noDataForThisPeriod", context, Optional.absent()).or(""))
                .render("startDate", startDate)
                .render("endDate", endDate)
                .render("minDate", minDay)
                .render("maxDate", maxDay)                        
                .render("search", search);            
        }
        
        startDate = firstRun.getDay();
        endDate = lastRun.getDay();
        
        
        String jsonEvents = null;
        try {
            jsonEvents = objectMapper.writeValueAsString(baseDB.event.list(group, startDate, endDate));
        } catch(JsonProcessingException ex){
            jsonEvents = "[]";
        }
        
        GoogleSerp lastSerp = googleDB.serp.get(lastRun.getId(), search.getId());
        
        List<GoogleTarget> targets = getTargets(context);
        
        String jsonRanks = getJsonRanks(group, targets, firstRun, lastRun, searchId);
        Config config = baseDB.config.getConfig();
        
        return Results.ok()
            .render("displayMode", config.getDisplayGoogleSearch())
            .render("events", jsonEvents)
            .render("targets", targets)
            .render("chart", jsonRanks)
            .render("search", search)
            .render("serp", lastSerp)
            .render("startDate", startDate)
            .render("endDate", endDate)
            .render("minDate", minDay)
            .render("maxDate", maxDay)            
            ;
    }

    
    protected String getJsonRanks(Group group, List<GoogleTarget> targets,Run firstRun, Run lastRun, int searchId){
        
        StringBuilder builder = new StringBuilder("{\"targets\":[");
        for (GoogleTarget target : targets) {
            builder.append("{\"id\":").append(target.getId())
            .append(",\"name\":\"").append(StringEscapeUtils.escapeJson(target.getName())).append("\"},");
        }
        if(builder.charAt(builder.length()-1) == ','){
            builder.setCharAt(builder.length()-1, ']');
        } else {
            builder.append(']');
        }
        builder.append(",\"ranks\":[");
        
        final int[] maxRank = new int[1];
        
        googleDB.serp.stream(firstRun.getId(), lastRun.getId(), searchId, (GoogleSerp serp) -> {
            
            builder.append('[').append(serp.getRunDay().toEpochSecond(ZoneOffset.UTC)*1000l).append(',');
            
            // calendar
            builder.append("null").append(",");
            
            for (GoogleTarget target : targets) {
                int position = UNRANKED;
                for (int i = 0; i < serp.getEntries().size(); i++) {
                    if(target.match(serp.getEntries().get(i).getUrl())){
                        position = i + 1;
                        break;
                    }
                }
                
                builder.append(position == UNRANKED ? "null" : position).append(',');
                if(position != UNRANKED && position > maxRank[0]){
                    maxRank[0] = position;
                }
            }
            
            if(builder.charAt(builder.length()-1) == ','){
                builder.setCharAt(builder.length()-1, ']');
            }
            builder.append(',');
        });
        if(builder.charAt(builder.length()-1) == ','){
            builder.setCharAt(builder.length()-1, ']');
        } else {
            builder.append(']');
        }
        
        builder.append(",\"maxRank\":").append(maxRank[0]);
        builder.append("}");
        
        return builder.toString();
    }
    
    public Result urlRanks(
        Context context,
        @PathParam("searchId") Integer searchId,
        @Param("url") String url,
        @Param("startDate") String startDateStr,
        @Param("endDate") String endDateStr        
    ){
        Group group = (Group)context.getAttribute("group");
        
        GoogleSearch search = getSearch(context, searchId);
        if(search == null){
            context.getFlashScope().error("error.invalidSearch");
            return Results.redirect(router.getReverseRoute(GoogleGroupController.class, "view", "groupId", group.getId()));
        }        
        
        LocalDate startDate = null;
        if(startDateStr != null){
            try {startDate = LocalDate.parse(startDateStr);} catch(Exception ex){}
        }
        LocalDate endDate = null;
        if(endDateStr != null){
            try {endDate = LocalDate.parse(endDateStr);} catch(Exception ex){}
        }        
        
        Run firstRun = baseDB.run.findFirst(Module.GOOGLE, STATUSES_DONE, startDate);
        Run lastRun = baseDB.run.findLast(Module.GOOGLE, STATUSES_DONE, endDate);
        
        if(url == null || firstRun == null || lastRun == null){
            return Results.badRequest().text();
        }
        
        StringBuilder builder = new StringBuilder("{");
        googleDB.serp.stream(firstRun.getId(), lastRun.getId(), search.getId(), (GoogleSerp t) -> {
            int position = 0;
            for (int i = 0; i < t.getEntries().size(); i++) {
                if(t.getEntries().get(i).getUrl().equals(url)){
                    position = i + 1;
                    break;
                }
            }
            
            builder
                .append("\"")
                .append(t.getRunDay().toEpochSecond(ZoneOffset.UTC)*1000l)
                .append("\":")
                .append(position)
                .append(",");
        });
        
        if(builder.charAt(builder.length()-1) == ','){
            builder.setCharAt(builder.length()-1, '}');
        } else {
            builder.append('}');
        }
        
        return Results.ok()
            .text()
            .render(builder.toString());
    }
    
    
}