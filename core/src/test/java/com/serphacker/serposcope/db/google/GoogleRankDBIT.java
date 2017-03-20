/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleRank;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.time.DurationFormatUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class GoogleRankDBIT extends AbstractDBIT {
    
    public GoogleRankDBIT() {
    }
    
    @Inject
    BaseDB baseDB;
    
    @Inject
    GoogleDB googleDB;
    
    @Test
    public void testSomeMethod() {
        
        Group grp = new Group(Group.Module.GOOGLE, "grp");
        baseDB.group.insert(grp);
        
        GoogleSearch search = new GoogleSearch("keyword");
        googleDB.search.insert(Arrays.asList(search), grp.getId());
        
        GoogleTarget target = new GoogleTarget(grp.getId(), "name", GoogleTarget.PatternType.REGEX, "pattern");
        googleDB.target.insert(Arrays.asList(target));
        
        Run run = new Run(Run.Mode.CRON, Group.Module.GOOGLE, LocalDateTime.now().withNano(0));
        baseDB.run.insert(run);
        
        GoogleRank rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 1, 2, 3, "url");
        googleDB.rank.insert(rank);
        
        rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 2, 3, 4, "url");
        googleDB.rank.insert(rank);        
        
        
    }
    
    
    @Test
    public void testInsertBatch() {
        
        Group grp = new Group(Group.Module.GOOGLE, "grp");
        baseDB.group.insert(grp);
        
        GoogleSearch search1 = new GoogleSearch("search1");
        GoogleSearch search2 = new GoogleSearch("search2");
        googleDB.search.insert(Arrays.asList(search1, search2), grp.getId());
        
        GoogleTarget target = new GoogleTarget(grp.getId(), "name", GoogleTarget.PatternType.REGEX, "pattern");
        googleDB.target.insert(Arrays.asList(target));
        
        Run run = new Run(Run.Mode.CRON, Group.Module.GOOGLE, LocalDateTime.now().withNano(0));
        baseDB.run.insert(run);
        
        GoogleRank rank1 = new GoogleRank(run.getId(), grp.getId(), target.getId(), search1.getId(), 1, 2, 3, "url-1");
        GoogleRank rank2 = new GoogleRank(run.getId(), grp.getId(), target.getId(), search2.getId(), 2, 3, 4, "url-2");
        
        assertTrue(googleDB.rank.insert(Arrays.asList(rank1, rank2)));
        List<GoogleRank> ranks = googleDB.rank.list(run.getId(), grp.getId(), target.getId());
        assertEquals(2, ranks.size());
        assertEquals("url-1", ranks.get(0).url);
        assertEquals("url-2", ranks.get(1).url); 
        
        rank1 = new GoogleRank(run.getId(), grp.getId(), target.getId(), search1.getId(), 10, 20, 30, "url-xxx-1");
        rank2 = new GoogleRank(run.getId(), grp.getId(), target.getId(), search2.getId(), 20, 30, 40, "url-xxx-2");        
        
        assertTrue(googleDB.rank.insert(Arrays.asList(rank1, rank2)));
        ranks = googleDB.rank.list(run.getId(), grp.getId(), target.getId());
        assertEquals(2, ranks.size());
        assertEquals("url-xxx-1", ranks.get(0).url);
        assertEquals("url-xxx-2", ranks.get(1).url); 
        
    }    
    
}
