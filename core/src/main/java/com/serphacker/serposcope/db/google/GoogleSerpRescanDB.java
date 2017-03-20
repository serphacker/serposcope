/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.db.google;

import com.serphacker.serposcope.db.base.RunDB;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleBest;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoogleSerpRescanDB {
    
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSerpRescanDB.class);
    
    @Inject
    GoogleSearchDB searchDB;
    
    @Inject
    GoogleSerpDB serpDB;
    
    @Inject
    GoogleTargetSummaryDB targetSummaryDB;
    
    @Inject
    GoogleRankDB rankDB;
    
    @Inject
    RunDB runDB;
    
    public void rescanNonBulk(Integer specificRunId, Collection<GoogleTarget> targets, Collection<GoogleSearch> searches,  boolean updateSummary) {
        LOG.debug("SERP rescan (non-bulk) : starting");
        long _start = System.currentTimeMillis();
        Run specPrevRun = null;
        Map<Integer, GoogleTargetSummary> specPrevRunSummaryByTarget = new HashMap<>();
        
        if(specificRunId != null){
            specPrevRun = runDB.findPrevious(specificRunId);
            if(specPrevRun != null){
                specPrevRunSummaryByTarget = targetSummaryDB.list(specPrevRun.getId()).stream()
                    .collect(Collectors.toMap(GoogleTargetSummary::getTargetId, Function.identity()));
            }
        }        
        
        for (GoogleTarget target : targets) {
            
            Map<Integer, GoogleTargetSummary> summaryByRunId = new HashMap<>();
            GoogleTargetSummary specificPreviousSummary = specPrevRunSummaryByTarget.get(target.getId());
            if(specificPreviousSummary != null){
                summaryByRunId.put(specPrevRun.getId(), specificPreviousSummary);
            }
            
            for (GoogleSearch search : searches) {
                final MutableInt previousRunId = new MutableInt(0);
                final MutableInt previousRank = new MutableInt(GoogleRank.UNRANKED);
                final MutableInt hits = new MutableInt(0);
                GoogleBest searchBest = new GoogleBest(target.getGroupId(), target.getId(), search.getId(), GoogleRank.UNRANKED, null, null);
                
                if(specPrevRun != null){
                    previousRunId.setValue(specPrevRun.getId());
                    previousRank.setValue(rankDB.get(specPrevRun.getId(), target.getGroupId(), target.getId(), search.getId()));
                    hits.setValue(rankDB.hits(specPrevRun.getId(), target.getGroupId(), target.getId(), search.getId()));
                    GoogleBest specificBest = rankDB.getBest(target.getGroupId(), target.getId(), search.getId());
                    if(specificBest != null){
                        searchBest = specificBest;
                    }
                }
                final GoogleBest best = searchBest;

                serpDB.stream(specificRunId, specificRunId, search.getId(), (GoogleSerp res) -> {
                    
                    int rank = GoogleRank.UNRANKED;
                    String rankedUrl = null;
                    for (int i = 0; i < res.getEntries().size(); i++) {
                        if (target.match(res.getEntries().get(i).getUrl())) {
                            rankedUrl = res.getEntries().get(i).getUrl();
                            rank = i + 1;
                            break;
                        }
                    }

                    // only update last run
                    GoogleRank gRank = new GoogleRank(res.getRunId(), target.getGroupId(), target.getId(), search.getId(),
                        rank, previousRank.shortValue(), hits.shortValue(), rankedUrl);
                    rankDB.insert(gRank);
                    
                    if(updateSummary){
                        GoogleTargetSummary summary = summaryByRunId.get(res.getRunId());
                        if (summary == null) {
                            summaryByRunId.put(res.getRunId(), summary = new GoogleTargetSummary(target.getGroupId(),
                                target.getId(), res.getRunId(), 0));
                        }
                        summary.addRankCandidat(gRank);
                    }                    

                    if (rank != GoogleRank.UNRANKED && rank <= best.getRank()) {
                        best.setRank((short) rank);
                        best.setUrl(rankedUrl);
                        best.setRunDay(res.getRunDay());
                    }

                    previousRunId.setValue(res.getRunId());
                    previousRank.setValue(rank);
                });

                if (best.getRank() != GoogleRank.UNRANKED) {
                    rankDB.insertBest(best);
                }
            }

            // fill previous summary score
            if(updateSummary){
                TreeMap<Integer, GoogleTargetSummary> summaries = new TreeMap<>(summaryByRunId);
                
                GoogleTargetSummary previousSummary = null;
                for (Map.Entry<Integer, GoogleTargetSummary> entry : summaries.entrySet()) {
                    if (previousSummary != null) {
                        entry.getValue().setPreviousScoreBP(previousSummary.getScoreBP());
                    }
                    previousSummary = entry.getValue();
                }
                
                if(specPrevRun != null){
                    summaries.remove(specPrevRun.getId());
                }
                
                if(!summaries.isEmpty()){
                    targetSummaryDB.insert(summaries.values());
                }
            }
        }
        LOG.debug("SERP rescan : done, duration = {}", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_start));
    }
    
    public void rescan(Integer specificRunId, Collection<GoogleTarget> targets, Collection<GoogleSearch> searches,  boolean updateSummary) {
        LOG.debug("SERP rescan (bulk) : starting");
        long _start = System.currentTimeMillis();
        Map<Integer, Integer> searchCountByGroup = searchDB.countByGroup();
        Run specPrevRun = null;
        Map<Integer, GoogleTargetSummary> specPrevRunSummaryByTarget = new HashMap<>();
        
        if(specificRunId != null){
            specPrevRun = runDB.findPrevious(specificRunId);
            if(specPrevRun != null){
                specPrevRunSummaryByTarget = targetSummaryDB.list(specPrevRun.getId()).stream()
                    .collect(Collectors.toMap(GoogleTargetSummary::getTargetId, Function.identity()));
            }
        }        
        
        List<GoogleRank> ranks = new ArrayList<>();
        for (GoogleTarget target : targets) {
            
            Map<Integer, GoogleTargetSummary> summaryByRunId = new HashMap<>();
            GoogleTargetSummary specificPreviousSummary = specPrevRunSummaryByTarget.get(target.getId());
            if(specificPreviousSummary != null){
                summaryByRunId.put(specPrevRun.getId(), specificPreviousSummary);
            }
            
            for (GoogleSearch search : searches) {
                final MutableInt previousRunId = new MutableInt(0);
                final MutableInt previousRank = new MutableInt(GoogleRank.UNRANKED);
                final MutableInt hits = new MutableInt(0);
                GoogleBest searchBest = new GoogleBest(target.getGroupId(), target.getId(), search.getId(), GoogleRank.UNRANKED, null, null);
                
                if(specPrevRun != null){
                    previousRunId.setValue(specPrevRun.getId());
                    previousRank.setValue(rankDB.get(specPrevRun.getId(), target.getGroupId(), target.getId(), search.getId()));
                    hits.setValue(rankDB.hits(specPrevRun.getId(), target.getGroupId(), target.getId(), search.getId()));
                    GoogleBest specificBest = rankDB.getBest(target.getGroupId(), target.getId(), search.getId());
                    if(specificBest != null){
                        searchBest = specificBest;
                    }
                }
                final GoogleBest best = searchBest;

                serpDB.stream(specificRunId, specificRunId, search.getId(), (GoogleSerp res) -> {
                    
                    int rank = GoogleRank.UNRANKED;
                    String rankedUrl = null;
                    for (int i = 0; i < res.getEntries().size(); i++) {
                        if (target.match(res.getEntries().get(i).getUrl())) {
                            rankedUrl = res.getEntries().get(i).getUrl();
                            rank = i + 1;
                            break;
                        }
                    }

                    // only update last run
                    GoogleRank gRank = new GoogleRank(res.getRunId(), target.getGroupId(), target.getId(), search.getId(),
                        rank, previousRank.shortValue(), hits.shortValue(), rankedUrl);
                    ranks.add(gRank);
                    if(ranks.size() > 2000){
                        rankDB.insert(ranks);
                        ranks.clear();
                    }
                    
                    if(updateSummary){
                        GoogleTargetSummary summary = summaryByRunId.get(res.getRunId());
                        if (summary == null) {
                            summaryByRunId.put(res.getRunId(), summary = new GoogleTargetSummary(target.getGroupId(),
                                target.getId(), res.getRunId(), 0));
                        }
                        summary.addRankCandidat(gRank);
                    }                    

                    if (rank != GoogleRank.UNRANKED && rank <= best.getRank()) {
                        best.setRank((short) rank);
                        best.setUrl(rankedUrl);
                        best.setRunDay(res.getRunDay());
                    }

                    previousRunId.setValue(res.getRunId());
                    previousRank.setValue(rank);
                });
                
                if (best.getRank() != GoogleRank.UNRANKED) {
                    rankDB.insertBest(best);
                }
            }
            
            // fill previous summary score
            if(updateSummary){
                TreeMap<Integer, GoogleTargetSummary> summaries = new TreeMap<>(summaryByRunId);
                
                GoogleTargetSummary previousSummary = null;
                for (Map.Entry<Integer, GoogleTargetSummary> entry : summaries.entrySet()) {
                    GoogleTargetSummary summary = entry.getValue();
                    summary.computeScoreBP(searchCountByGroup.getOrDefault(summary.getGroupId(), 0));
                    if (previousSummary != null) {
                        summary.setPreviousScoreBP(previousSummary.getScoreBP());
                    }
                    previousSummary = summary;
                }
                
                if(specPrevRun != null){
                    summaries.remove(specPrevRun.getId());
                }
                
                if(!summaries.isEmpty()){
                    targetSummaryDB.insert(summaries.values());
                }
            }
        }
        
        if(!ranks.isEmpty()){
            rankDB.insert(ranks);
            ranks.clear();
        }
        
        LOG.debug("SERP rescan : done, duration = {}", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_start));
    }    
    
    /*
    public void rescan(Integer specificRunId, List<GoogleSearch> searches, List<GoogleTarget> targets, boolean updateSummary) {
        Run specPrevRun = null;
        Map<Integer, GoogleTargetSummary> specPrevRunSummaryByTarget = new HashMap<>();
        
        if(specificRunId != null){
            specPrevRun = runDB.findPrevious(specificRunId);
            if(specPrevRun != null){
                specPrevRunSummaryByTarget = targetSummaryDB.list(specPrevRun.getId()).stream()
                    .collect(Collectors.toMap(GoogleTargetSummary::getTargetId, Function.identity()));
            }
        }        
        
        for (GoogleTarget target : targets) {
            
            Map<Integer, GoogleTargetSummary> summaryByRunId = new HashMap<>();
            GoogleTargetSummary specificPreviousSummary = specPrevRunSummaryByTarget.get(target.getId());
            if(specificPreviousSummary != null){
                summaryByRunId.put(specPrevRun.getId(), specificPreviousSummary);
            }
            
            for (GoogleSearch search : searches) {
                final MutableInt previousRunId = new MutableInt(0);
                final MutableInt previousRank = new MutableInt(GoogleRank.UNRANKED);
                GoogleBest searchBest = new GoogleBest(target.getGroupId(), target.getId(), search.getId(), GoogleRank.UNRANKED, null, null);
                
                if(specPrevRun != null){
                    previousRunId.setValue(specPrevRun.getId());
                    previousRank.setValue(rankDB.get(specPrevRun.getId(), target.getGroupId(), target.getId(), search.getId()));
                    GoogleBest specificBest = rankDB.getBest(target.getGroupId(), target.getId(), search.getId());
                    if(specificBest != null){
                        searchBest = specificBest;
                    }
                }
                final GoogleBest best = searchBest;

                serpDB.stream(specificRunId, specificRunId, search.getId(), (GoogleSerp res) -> {
                    
                    int rank = GoogleRank.UNRANKED;
                    String rankedUrl = null;
                    for (int i = 0; i < res.getEntries().size(); i++) {
                        if (target.match(res.getEntries().get(i).getUrl())) {
                            rankedUrl = res.getEntries().get(i).getUrl();
                            rank = i + 1;
                            break;
                        }
                    }

                    // only update last run
                    GoogleRank gRank = new GoogleRank(res.getRunId(), target.getGroupId(), target.getId(), search.getId(),
                        rank, previousRank.shortValue(), rankedUrl);
                    rankDB.insert(gRank);
                    
                    if(updateSummary){
                        GoogleTargetSummary summary = summaryByRunId.get(res.getRunId());
                        if (summary == null) {
                            summaryByRunId.put(res.getRunId(), summary = new GoogleTargetSummary(target.getGroupId(),
                                target.getId(), res.getRunId(), 0));
                        }
                        summary.addRankCandidat(gRank);
                    }                    

                    if (rank != GoogleRank.UNRANKED && rank <= best.getRank()) {
                        best.setRank((short) rank);
                        best.setUrl(rankedUrl);
                        best.setRunDay(res.getRunDay());
                    }

                    previousRunId.setValue(res.getRunId());
                    previousRank.setValue(rank);
                });

                if (best.getRank() != GoogleRank.UNRANKED) {
                    rankDB.insertBest(best);
                }
            }

            // fill previous summary score
            if(updateSummary){
                TreeMap<Integer, GoogleTargetSummary> summaries = new TreeMap<>(summaryByRunId);
                
                GoogleTargetSummary previousSummary = null;
                for (Map.Entry<Integer, GoogleTargetSummary> entry : summaries.entrySet()) {
                    if (previousSummary != null) {
                        entry.getValue().setPreviousScore(previousSummary.getScore());
                    }
                    previousSummary = entry.getValue();
                }
                
                if(specPrevRun != null){
                    summaries.remove(specPrevRun.getId());
                }
                
                if(!summaries.isEmpty()){
                    targetSummaryDB.insert(summaries.values());
                }
            }
        }
    }
    */
}
