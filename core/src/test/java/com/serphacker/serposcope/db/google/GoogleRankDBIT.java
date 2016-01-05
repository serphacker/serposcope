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
import java.util.regex.Pattern;
import org.apache.commons.lang3.time.DurationFormatUtils;
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
        
        GoogleRank rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 1, 2, "url");
        googleDB.rank.insert(rank);
        
        rank = new GoogleRank(run.getId(), grp.getId(), target.getId(), search.getId(), 2, 3, "url");
        googleDB.rank.insert(rank);        
        
        
    }
    
}
