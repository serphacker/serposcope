/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTargetSummary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleTargetSummaryDBIT extends AbstractDBIT {
    
    @Inject
    BaseDB base;
    
    @Inject
    GoogleDB google;
    
    @Test
    public void test() {
        
        Random r = new Random();
        
        Group group = new Group(Group.Module.GOOGLE, "group");
        base.group.insert(group);
        
        GoogleTarget target = new GoogleTarget(group.getId(), "target#0", GoogleTarget.PatternType.REGEX, "");
        google.target.insert(Arrays.asList(target));
        
        List<GoogleSearch> searches = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            searches.add(new GoogleSearch("kw-" + i));
        }
        google.search.insert(searches, group.getId());
        
        Run run = new Run(Run.Mode.CRON, Group.Module.GOOGLE, LocalDateTime.of(2015,10,10,10,10));
        base.run.insert(run);
        
        
        GoogleTargetSummary summary = new GoogleTargetSummary(group.getId(), target.getId(), run.getId(), 0);
        
        List<GoogleRank> ranks = new ArrayList<>();
        for (GoogleSearch search : searches) {
            GoogleRank rank = new GoogleRank(run.getId(), group.getId(), target.getId(), search.getId(), r.nextInt(100)+1, r.nextInt(100)+1, r.nextInt(100)+1, "url-" + search.getId());
            google.rank.insert(rank);
            ranks.add(rank);
            summary.addRankCandidat(rank);
        }
        
        google.targetSummary.insert(Arrays.asList(summary));
        
        
        ReflectionAssert.assertLenientEquals(summary, google.targetSummary.list(run.getId()).get(0));
        
        
    }
    
    @Test
    public void testSerialize(){
        GoogleTargetSummary targetSum = new GoogleTargetSummary();
        
        List<GoogleRank> ranks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targetSum.addRankCandidat(new GoogleRank(0, 0, 0, i, i, 0, 0, "void"));
        }
        
        System.out.println(google.targetSummary.unserializeIds(targetSum.getTopRanksSerialized()));
        
        
    }
    
}
