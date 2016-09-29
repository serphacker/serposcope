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
import com.google.common.base.Functions;
import com.google.inject.Inject;
import ninja.Result;
import ninja.Results;

import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.RunDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.models.base.Event;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleBest;
import com.serphacker.serposcope.models.google.GoogleRank;
import static com.serphacker.serposcope.models.google.GoogleRank.UNRANKED;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import static com.serphacker.serposcope.scraper.google.GoogleDevice.SMARTPHONE;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import ninja.Context;
import ninja.Router;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.utils.ResponseStreams;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.LoggerFactory;

@Singleton
public class GoogleTargetController extends GoogleController {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GoogleTargetController.class);

    @Inject
    BaseDB baseDB;

    @Inject
    GoogleDB googleDB;

    @Inject
    Router router;

    @Inject
    ObjectMapper objectMapper;

    private final static DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    public static class TargetVariation {

        public TargetVariation(GoogleSearch search, GoogleRank rank) {
            this.search = search;
            this.rank = rank;
        }

        public final GoogleSearch search;
        public final GoogleRank rank;

    }

    public static class TargetRank {

        public TargetRank(int now, int prev, String url) {
            this.now = now;
            this.prev = prev;
            this.url = url;
        }

        public final int now;
        public final int prev;
        public final String url;

        public String getRank() {
            if (now == UNRANKED) {
                return "-";
            }
            return Integer.toString(now);
        }

        public String getDiff() {
            if (prev == UNRANKED && now != UNRANKED) {
                return "in";
            }
            if (prev != UNRANKED && now == UNRANKED) {
                return "out";
            }
            int diff = prev - now;
            if (diff == 0) {
                return "=";
            }
            if (diff > 0) {
                return "+" + diff;
            }
            return Integer.toString(diff);
        }

        public String getDiffClass() {
            String diff = getDiff();
            switch (diff.charAt(0)) {
                case '+':
                case 'i':
                    return "plus";
                case '-':
                case 'o':
                    return "minus";
                default:
                    return "";
            }
        }

        public String getUrl() {
            return url;
        }

    }

    public Result target(Context context,
        @PathParam("targetId") Integer targetId,
        @Param("startDate") String startDateStr,
        @Param("endDate") String endDateStr
    ) {
        GoogleTarget target = getTarget(context, targetId);
        List<GoogleSearch> searches = context.getAttribute("searches", List.class);
        Group group = context.getAttribute("group", Group.class);
        Config config = baseDB.config.getConfig();

        String display = context.getParameter("display", config.getDisplayGoogleTarget());
        if (!Config.VALID_DISPLAY_GOOGLE_TARGET.contains(display) && !"export".equals(display)) {
            display = Config.DEFAULT_DISPLAY_GOOGLE_TARGET;
        }

        if (target == null) {
            context.getFlashScope().error("error.invalidTarget");
            return Results.redirect(router.getReverseRoute(GoogleGroupController.class, "view", "groupId", group.getId()));
        }

        Run minRun = baseDB.run.findFirst(group.getModule(), RunDB.STATUSES_DONE, null);
        Run maxRun = baseDB.run.findLast(group.getModule(), RunDB.STATUSES_DONE, null);

        if (maxRun == null || minRun == null || searches.isEmpty()) {
            String fallbackDisplay = "export".equals(display) ? "table" : display;
            return Results.ok()
                .template("/serposcope/views/google/GoogleTargetController/" + fallbackDisplay + ".ftl.html")
                .render("startDate", "")
                .render("endDate", "")
                .render("display", fallbackDisplay)
                .render("target", target);
        }

        LocalDate minDay = minRun.getDay();
        LocalDate maxDay = maxRun.getDay();

        LocalDate startDate = null;
        if (startDateStr != null) {
            try {
                startDate = LocalDate.parse(startDateStr);
            } catch (Exception ex) {
            }
        }
        LocalDate endDate = null;
        if (endDateStr != null) {
            try {
                endDate = LocalDate.parse(endDateStr);
            } catch (Exception ex) {
            }
        }

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            startDate = maxDay.minusDays(30);
            endDate = maxDay;
        }

        Run firstRun = baseDB.run.findFirst(group.getModule(), RunDB.STATUSES_DONE, startDate);
        Run lastRun = baseDB.run.findLast(group.getModule(), RunDB.STATUSES_DONE, endDate);

        List<Run> runs = baseDB.run.listDone(firstRun.getId(), lastRun.getId());

        startDate = firstRun.getDay();
        endDate = lastRun.getDay();

        switch (display) {
            case "table":
            case "variation":
                return Results.ok()
                    .template("/serposcope/views/google/GoogleTargetController/" + display + ".ftl.html")
                    .render("target", target)
                    .render("searches", searches)
                    .render("startDate", startDate)
                    .render("endDate", endDate)
                    .render("minDate", minDay)
                    .render("maxDate", maxDay)
                    .render("display", display); 
            case "chart":
                return renderChart(group, target, searches, runs, minDay, maxDay, startDate, endDate);
            case "export":
                return renderExport(group, target, searches, runs, minDay, maxDay, startDate, endDate);
            default:
                throw new IllegalStateException();
        }

    }

    public Result jsonVariation(
        Context context,
        @PathParam("targetId") Integer targetId,
        @Param("endDate") String endDateStr
    ) {
        GoogleTarget target = getTarget(context, targetId);
        List<GoogleSearch> searches = context.getAttribute("searches", List.class);
        Group group = context.getAttribute("group", Group.class);
        
        final LocalDate endDate;
        try {
            endDate = LocalDate.parse(endDateStr);
        } catch (Exception ex) {
            return Results.json().renderRaw("[[],[],[]]");
        }
        Run lastRun = baseDB.run.findLast(group.getModule(), RunDB.STATUSES_DONE, endDate);
        
        List<TargetVariation> ranksUp = new ArrayList<>();
        List<TargetVariation> ranksDown = new ArrayList<>();
        List<TargetVariation> ranksSame = new ArrayList<>();

        Map<Integer, GoogleSearch> searchesById = searches.stream()
            .collect(Collectors.toMap(GoogleSearch::getId, Function.identity()));

        List<GoogleRank> ranks = googleDB.rank.list(lastRun.getId(), group.getId(), target.getId());
        for (GoogleRank rank : ranks) {

            GoogleSearch search = searchesById.get(rank.googleSearchId);
            if (search == null) {
                continue;
            }

            if (rank.diff > 0) {
                ranksDown.add(new TargetVariation(search, rank));
            } else if (rank.diff < 0) {
                ranksUp.add(new TargetVariation(search, rank));
            } else {
                ranksSame.add(new TargetVariation(search, rank));
            }
        }

        Collections.sort(ranksUp, (TargetVariation o1, TargetVariation o2) -> Integer.compare(o1.rank.diff, o2.rank.diff));
        Collections.sort(ranksDown, (TargetVariation o1, TargetVariation o2) -> -Integer.compare(o1.rank.diff, o2.rank.diff));
        Collections.sort(ranksSame, (TargetVariation o1, TargetVariation o2) -> Integer.compare(o1.rank.rank, o2.rank.rank));

        return Results.ok()
            .json()
            .render((Context context0, Result result) -> {
                PrintWriter writer = null;
                OutputStream os = null;
                try {

                    String acceptEncoding = context0.getHeader("Accept-Encoding");
                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                        result.addHeader("Content-Encoding", "gzip");
                    }

                    ResponseStreams response = context0.finalizeHeaders(result);
                    os = response.getOutputStream();
                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                        os = new GZIPOutputStream(os);
                    }

                    writer = new PrintWriter(os);

                    writer.append("[");
                    int id = 0;
                    
                    writer.append("[");
                    for (int i = 0; i < ranksUp.size(); i++) {
                        TargetVariation var = ranksUp.get(i);
                        writer
                            .append("{")
                            .append("\"id\":").append(Integer.toString(id++))
                            .append(",\"search\":").append(searchToJson(var.search))
                            .append(",\"now\":").append(Integer.toString(var.rank.rank))
                            .append(",\"prv\":").append(Integer.toString(var.rank.previousRank))
                            .append(",\"diff\":").append(Integer.toString(var.rank.diff))
                            .append("}");
                        
                        if(i != ranksUp.size()-1){
                            writer.append(',');
                        }
                    }
                    
                    writer.append("],[");
                    
                    for (int i = 0; i < ranksDown.size(); i++) {
                        TargetVariation var = ranksDown.get(i);
                        writer
                            .append("{")
                            .append("\"id\":").append(Integer.toString(id++))
                            .append(",\"search\":").append(searchToJson(var.search))
                            .append(",\"now\":").append(Integer.toString(var.rank.rank))
                            .append(",\"prv\":").append(Integer.toString(var.rank.previousRank))
                            .append(",\"diff\":").append(Integer.toString(var.rank.diff))
                            .append("}");
                        
                        if(i != ranksDown.size()-1){
                            writer.append(',');
                        }
                    }
                    
                    writer.append("],[");
                    
                    for (int i = 0; i < ranksSame.size(); i++) {
                        TargetVariation var = ranksSame.get(i);
                        writer
                            .append("{")
                            .append("\"id\":").append(Integer.toString(id++))
                            .append(",\"search\":").append(searchToJson(var.search))
                            .append(",\"now\":").append(Integer.toString(var.rank.rank))
                            .append("}");
                        
                        if(i != ranksSame.size()-1){
                            writer.append(',');
                        }                        
                    }
                    writer.append("]]");

                } catch (Exception ex) {
                    LOG.warn("HTTP error", ex);
                } finally {
                    if (os != null) {
                        try {
                            writer.close();
                            os.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            });
    }

    protected StringBuilder searchToJson(GoogleSearch search) {
        StringBuilder searchesJson = new StringBuilder("{");
        searchesJson.append("\"id\":")
            .append(search.getId())
            .append(",");
        searchesJson.append("\"keyword\":\"")
            .append(StringEscapeUtils.escapeJson(search.getKeyword()))
            .append("\",");
        searchesJson.append("\"tld\":\"")
            .append(search.getTld() == null ? "" : StringEscapeUtils.escapeJson(search.getTld()))
            .append("\",");
        searchesJson.append("\"device\":\"")
            .append(SMARTPHONE.equals(search.getDevice()) ? 'M' : 'D')
            .append("\",");
        searchesJson.append("\"local\":\"")
            .append(search.getLocal() == null ? "" : StringEscapeUtils.escapeJson(search.getLocal()))
            .append("\",");
        searchesJson.append("\"datacenter\":\"")
            .append(search.getDatacenter() == null ? "" : StringEscapeUtils.escapeJson(search.getDatacenter()))
            .append("\",");
        searchesJson.append("\"custom\":\"")
            .append(search.getCustomParameters() == null ? "" : StringEscapeUtils.escapeJson(search.getCustomParameters()))
            .append("\"");
        searchesJson.append("}");
        return searchesJson;
    }

    protected Result renderChart(
        Group group,
        GoogleTarget target,
        List<GoogleSearch> searches,
        List<Run> runs,
        LocalDate minDay,
        LocalDate maxDay,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String display = "chart";
        StringBuilder builder = new StringBuilder("{\"searches\": [");
        for (GoogleSearch search : searches) {
            builder.append("\"").append(StringEscapeUtils.escapeJson(search.getKeyword())).append("\",");
        }
        builder.setCharAt(builder.length() - 1, ']');
        builder.append(",\"ranks\": [");

        int maxRank = 0;
        for (Run run : runs) {
            builder.append("\n\t[").append(run.getStarted().toEpochSecond(ZoneOffset.UTC) * 1000l).append(",");
            // calendar
            builder.append("null,");

            Map<Integer, GoogleRank> ranks = googleDB.rank.list(run.getId(), group.getId(), target.getId())
                .stream().collect(Collectors.toMap((g) -> g.googleSearchId, Function.identity()));
            
            for (GoogleSearch search : searches) {
                GoogleRank fullRank = ranks.get(search.getId());
//                GoogleRank fullRank = googleDB.rank.getFull(run.getId(), group.getId(), target.getId(), search.getId());
                if (fullRank != null && fullRank.rank != GoogleRank.UNRANKED && fullRank.rank > maxRank) {
                    maxRank = fullRank.rank;
                }
                builder.append(fullRank == null || fullRank.rank == GoogleRank.UNRANKED ? "null" : fullRank.rank).append(',');
            }

            builder.setCharAt(builder.length() - 1, ']');
            builder.append(",");
        }
        builder.setCharAt(builder.length() - 1, ']');
        builder.append(",\n\"maxRank\": ").append(maxRank).append("}");

        List<Event> events = baseDB.event.list(group, startDate, endDate);
        String jsonEvents = null;
        try {
            jsonEvents = objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException ex) {
            jsonEvents = "[]";
        }

        Map<Integer, GoogleBest> bestRanks = new HashMap<>();
        for (GoogleSearch search : searches) {
            bestRanks.put(search.getId(), googleDB.rank.getBest(target.getGroupId(), target.getId(), search.getId()));
        }

        return Results.ok()
            .template("/serposcope/views/google/GoogleTargetController/" + display + ".ftl.html")
            .render("target", target)
            .render("searches", searches)
            .render("startDate", startDate.toString())
            .render("endDate", endDate.toString())
            .render("minDate", minDay)
            .render("maxDate", maxDay)
            .render("display", display)
            .render("ranksJson", builder.toString())
            .render("eventsJson", jsonEvents);
    }

    protected Result renderExport(
        Group group,
        GoogleTarget target,
        List<GoogleSearch> searches,
        List<Run> runs,
        LocalDate minDay,
        LocalDate maxDay,
        LocalDate startDate,
        LocalDate endDate
    ) {

        return Results.ok()
            .text()
            .addHeader("Content-Disposition", "attachment; filename=\"export.csv\"")
            .render((Context context, Result result) -> {
                ResponseStreams stream = context.finalizeHeaders(result);
                try (Writer writer = stream.getWriter()) {
                    writer.append("date,rank,url,target,keyword,device,tld,datacenter,local,custom\n");
                    for (Run run : runs) {
                        String day = run.getDay().toString();
                        for (GoogleSearch search : searches) {
                            GoogleRank rank = googleDB.rank.getFull(run.getId(), group.getId(), target.getId(), search.getId());
                            writer.append(day).append(",");
                            if (rank != null) {
                                writer.append(Integer.toString(rank.rank)).append(",");
                                writer.append(rank.url).append(",");
                            } else {
                                writer.append(",").append(",");
                            }
                            writer.append(StringEscapeUtils.escapeCsv(target.getName())).append(",");
                            writer.append(StringEscapeUtils.escapeCsv(search.getKeyword())).append(",");
                            writer.append(search.getDevice() == GoogleDevice.DESKTOP ? "D" : "M").append(",");
                            writer.append(
                                search.getTld() != null
                                    ? StringEscapeUtils.escapeCsv(search.getTld())
                                    : ""
                            ).append(",");
                            writer.append(
                                search.getDatacenter() != null
                                    ? StringEscapeUtils.escapeCsv(search.getDatacenter())
                                    : ""
                            ).append(",");
                            writer.append(
                                search.getLocal() != null
                                    ? StringEscapeUtils.escapeCsv(search.getLocal())
                                    : ""
                            ).append(",");
                            writer.append(
                                search.getCustomParameters() != null
                                    ? StringEscapeUtils.escapeCsv(search.getCustomParameters())
                                    : ""
                            );
                            writer.append("\n");
                        }
                    }

                } catch (IOException ex) {
                    LOG.warn("error while exporting csv");
                }
            });

    }

    public Result jsonRanks(
        Context context,
        @PathParam("targetId") Integer targetId,
        @Param("startDate") String startDateStr,
        @Param("endDate") String endDateStr
    ) {
        final GoogleTarget target = getTarget(context, targetId);
        final List<GoogleSearch> searches = context.getAttribute("searches", List.class);
        final Group group = context.getAttribute("group", Group.class);
        final LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (Exception ex) {
            return Results.json().renderRaw("[]");
        }

        final Run firstRun = baseDB.run.findFirst(group.getModule(), RunDB.STATUSES_DONE, startDate);
        final Run lastRun = baseDB.run.findLast(group.getModule(), RunDB.STATUSES_DONE, endDate);
        final List<Run> runs = baseDB.run.listDone(firstRun.getId(), lastRun.getId());

        return Results.ok()
            .json()
            .render((Context context0, Result result) -> {
                PrintWriter writer = null;
                OutputStream os = null;
                try {

                    String acceptEncoding = context0.getHeader("Accept-Encoding");
                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                        result.addHeader("Content-Encoding", "gzip");
                    }

                    ResponseStreams response = context0.finalizeHeaders(result);
                    os = response.getOutputStream();
                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                        os = new GZIPOutputStream(os);
                    }

                    writer = new PrintWriter(os);
                    getTableJson(group, target, searches, runs, startDate, endDate, writer);

                } catch (Exception ex) {
                    LOG.warn("HTTP error", ex);
                } finally {
                    if (os != null) {
                        try {
                            writer.close();
                            os.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            });
    }

    protected void getTableJson(
        Group group,
        GoogleTarget target,
        List<GoogleSearch> searches,
        List<Run> runs,
        LocalDate startDate,
        LocalDate endDate,
        Writer writer
    ) throws IOException {
        writer.append("[[[-1, 0, 0, [");
        if (runs.isEmpty() || searches.isEmpty()) {
            writer.append("]]],[]]");
        }

        // events
        List<Event> events = baseDB.event.list(group, startDate, endDate);
        for (int i = 0; i < runs.size(); i++) {
            Run run = runs.get(i);
            Event event = null;

            for (Event candidat : events) {
                if (run.getDay().equals(candidat.getDay())) {
                    event = candidat;
                    break;
                }
            }

            if (event != null) {
                writer
                    .append("[\"").append(StringEscapeUtils.escapeJson(event.getTitle())).append("\",")
                    .append('"').append(StringEscapeUtils.escapeJson(event.getDescription())).append("\"]");
            } else {
                writer.append("0");
            }

            if (i != runs.size() - 1) {
                writer.append(",");
            }

        }
        writer.append("]],");

        Map<Integer, StringBuilder> builders = new HashMap<>();
        for (GoogleSearch search : searches) {
            StringBuilder builder;
            builders.put(search.getId(), builder = new StringBuilder("["));
            GoogleBest best = googleDB.rank.getBest(target.getGroupId(), target.getId(), search.getId());

            builder
                .append(search.getId())
                .append(",[\"").append(StringEscapeUtils.escapeJson(search.getKeyword()))
                .append("\",\"").append(search.getTld() == null ? "" : StringEscapeUtils.escapeJson(search.getTld()))
                .append("\",\"").append(SMARTPHONE.equals(search.getDevice()) ? 'M' : 'D')
                .append("\",\"").append(search.getLocal() == null ? "" : StringEscapeUtils.escapeJson(search.getLocal()))
                .append("\",\"").append(search.getDatacenter() == null ? "" : StringEscapeUtils.escapeJson(search.getDatacenter()))
                .append("\",\"").append(search.getCustomParameters() == null ? "" : StringEscapeUtils.escapeJson(search.getCustomParameters()))
                .append("\"],");

            if (best == null) {
                builder.append("0,");
            } else {
                builder
                    .append("[").append(best.getRank())
                    .append(",\"").append(best.getRunDay() != null ? best.getRunDay().toLocalDate().toString() : "?")
                    .append("\",\"").append(StringEscapeUtils.escapeJson(best.getUrl()))
                    .append("\"],");
            }
            builder.append("[");
        }

        for (int i = 0; i < runs.size(); i++) {
            Run run = runs.get(i);

            Map<Integer, GoogleRank> ranks = googleDB.rank.list0(run.getId(), group.getId(), target.getId())
                .stream().collect(Collectors.toMap((r) -> r.googleSearchId, Function.identity()));

            for (GoogleSearch search : searches) {
                StringBuilder builder = builders.get(search.getId());
                GoogleRank fullRank = ranks.get(search.getId());
                if (fullRank != null && fullRank.rank != GoogleRank.UNRANKED) {
                    builder.append("[").append(fullRank.rank)
                        .append(",").append(fullRank.previousRank)
                        .append(",\"").append(StringEscapeUtils.escapeJson(fullRank.url))
                        .append("\"],");
                } else {
                    builder.append("0,");
                }

                if (i == runs.size() - 1) {
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("]]");
                }
            }
        }

        List<StringBuilder> buildersArray = new ArrayList<>(builders.values());
        for (int i = 0; i < buildersArray.size(); i++) {
            writer.append(buildersArray.get(i));
            if (i != buildersArray.size() - 1) {
                writer.append(",");
            }
        }
        writer.append("],[");
        for (int i = 0; i < runs.size(); i++) {
            Run run = runs.get(i);
            writer.append("\"").append(run.getDay().toString()).append("\"");
            if (i != runs.size() - 1) {
                writer.append(",");
            }
        }
        writer.append("]]");
    }

    protected String getTableJsonData0(
        Group group,
        GoogleTarget target,
        List<GoogleSearch> searches,
        List<Run> runs,
        LocalDate startDate,
        LocalDate endDate
    ) {
        StringBuilder jsonData = new StringBuilder("[{\"id\": -1, \"best\": null, \"days\": [");
        if (runs.isEmpty() || searches.isEmpty()) {
            jsonData.append("]}]");
            return jsonData.toString();
        }

        // events
        List<Event> events = baseDB.event.list(group, startDate, endDate);
        for (Run run : runs) {
            Event event = null;

            for (Event candidat : events) {
                if (run.getDay().equals(candidat.getDay())) {
                    event = candidat;
                    break;
                }
            }

            if (event != null) {
                jsonData
                    .append("{\"title\":\"").append(StringEscapeUtils.escapeJson(event.getTitle()))
                    .append("\",\"description\":\"").append(StringEscapeUtils.escapeJson(event.getDescription()))
                    .append("\"},");
            } else {
                jsonData.append("null,");
            }
        }
        jsonData.deleteCharAt(jsonData.length() - 1);
        jsonData.append("]},");

        Map<Integer, StringBuilder> builders = new HashMap<>();

        for (GoogleSearch search : searches) {
            StringBuilder builder;
            builders.put(search.getId(), builder = new StringBuilder());
            builder.append("");
            GoogleBest best = googleDB.rank.getBest(target.getGroupId(), target.getId(), search.getId());

            builder.append("{\"id\":").append(search.getId())
                .append(",\"search\":{")
                .append("\"id\":").append(search.getId())
                .append(",\"k\":\"").append(StringEscapeUtils.escapeJson(search.getKeyword()))
                .append("\",\"t\":\"").append(search.getTld() == null ? "" : StringEscapeUtils.escapeJson(search.getTld()))
                .append("\",\"d\":\"").append(SMARTPHONE.equals(search.getDevice()) ? 'M' : 'D')
                .append("\",\"l\":\"").append(search.getLocal() == null ? "" : StringEscapeUtils.escapeJson(search.getLocal()))
                .append("\",\"dc\":\"").append(search.getDatacenter() == null ? "" : StringEscapeUtils.escapeJson(search.getDatacenter()))
                .append("\",\"c\":\"").append(search.getCustomParameters() == null ? "" : StringEscapeUtils.escapeJson(search.getCustomParameters()))
                .append("\"}, \"best\":");

            if (best == null) {
                builder.append("null,");
            } else {
                builder
                    .append("{\"rank\":").append(best.getRank())
                    .append(",\"date\":\"").append(best.getRunDay() != null ? best.getRunDay().toLocalDate().toString() : "?")
                    .append("\",\"url\":\"").append(StringEscapeUtils.escapeJson(best.getUrl()))
                    .append("\"},");
            }
            builder.append("\"days\": [");
        }

        for (int i = 0; i < runs.size(); i++) {
            Run run = runs.get(i);

            Map<Integer, GoogleRank> ranks = googleDB.rank.list0(run.getId(), group.getId(), target.getId())
                .stream().collect(Collectors.toMap((r) -> r.googleSearchId, Function.identity()));

            for (GoogleSearch search : searches) {
                StringBuilder builder = builders.get(search.getId());
                GoogleRank fullRank = ranks.get(search.getId());
                if (fullRank != null && fullRank.rank != GoogleRank.UNRANKED) {
                    builder.append("{\"r\":").append(fullRank.rank)
                        .append(",\"p\":").append(fullRank.previousRank)
                        .append(",\"u\":\"").append(StringEscapeUtils.escapeJson(fullRank.url))
                        .append("\"},");
                } else {
                    builder.append("{\"r\":32767,\"p\":null,\"u\":null},");
                }

                if (i == runs.size() - 1) {
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("]},");
                }
            }
        }

        for (StringBuilder value : builders.values()) {
            jsonData.append(value);
        }
        jsonData.deleteCharAt(jsonData.length() - 1);
        jsonData.append("]");

        return jsonData.toString();
    }

}
