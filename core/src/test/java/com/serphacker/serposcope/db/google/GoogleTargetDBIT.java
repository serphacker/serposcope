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
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTarget.PatternType;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleTargetDBIT extends AbstractDBIT {
    
    @Inject
    BaseDB baseDB;

    @Inject
    GoogleDB googleDB;

    @Test
    public void test() {
        
        Group grp1 = new Group(Group.Module.GOOGLE, "google #1");
        Group grp2 = new Group(Group.Module.GOOGLE, "google #2");
        
        baseDB.group.insert(grp1);
        baseDB.group.insert(grp2);
        
        List<GoogleTarget> targets = Arrays.asList(
            new GoogleTarget(grp1.getId(), "t1-1", PatternType.REGEX, "^target1$"),
            new GoogleTarget(grp1.getId(), "t1-2", PatternType.REGEX, "^target1$"),
            new GoogleTarget(grp2.getId(), "t2-1", PatternType.REGEX, "^target2$")
        );
        
        assertEquals(3, googleDB.target.insert(targets));
        assertEquals(1, targets.get(0).getGroupId());
        assertEquals(1, targets.get(0).getId());
        
        assertEquals(1, targets.get(1).getGroupId());
        assertEquals(2, targets.get(1).getId());
        
        assertEquals(2, targets.get(2).getGroupId());
        assertEquals(3, targets.get(2).getId());
        
        
        assertEquals(3, googleDB.target.list().size());
        assertEquals(2, googleDB.target.list(Arrays.asList(grp1.getId())).size());
        assertEquals(1, googleDB.target.list(Arrays.asList(grp2.getId())).size());
        
        
        List<GoogleTarget> dlTargets = googleDB.target.list();
        
        ReflectionAssert.assertReflectionEquals(targets, dlTargets);
        
    }
    
    @Test
    public void testPatterns(){
        Group grp1 = new Group(Group.Module.GOOGLE, "google #1");
        
        baseDB.group.insert(grp1);
        
        GoogleTarget target = new GoogleTarget(1, "target", PatternType.DOMAIN, "example.com");
        googleDB.target.insert(Arrays.asList(target));
        target = googleDB.target.list(Arrays.asList(target.getGroupId())).get(0);

        assertTrue(target.match("https://example.com/"));
        assertTrue(target.match("https://example.com/abcd"));
        assertTrue(target.match("https://example.com"));
        assertTrue(target.match("http://example.com/"));
        assertTrue(target.match("http://example.com/abcd"));
        assertTrue(target.match("http://example.com"));
        assertFalse(target.match("https://domain.example.com"));
        assertFalse(target.match("https://domain.example.com/sdf"));
        assertFalse(target.match("https://pizza.com/http://example.com"));
        googleDB.target.wipe();
        
        target = new GoogleTarget(1, "target", PatternType.SUBDOMAIN, "example.com");
        googleDB.target.insert(Arrays.asList(target));
        target = googleDB.target.list(Arrays.asList(target.getGroupId())).get(0);
        assertTrue(target.match("https://example.com/"));
        assertTrue(target.match("https://example.com/abcd"));
        assertTrue(target.match("https://example.com"));
        assertTrue(target.match("http://example.com/"));
        assertTrue(target.match("http://example.com/abcd"));
        assertTrue(target.match("http://example.com"));
        assertTrue(target.match("https://domain.example.com"));
        assertTrue(target.match("https://domain.example.com/sdf"));
        assertTrue(target.match("http://domain.example.com"));
        assertTrue(target.match("http://domain.example.com/sdf"));
        assertFalse(target.match("https://pizza.com/abcd"));
        assertFalse(target.match("https://pizza.com/http://example.com"));
        assertFalse(target.match("https://pizza.com/http://xssd.example.com"));
        assertFalse(target.match("http://pizza.com/abcd"));
        assertFalse(target.match("http://pizza.com/http://example.com"));
        assertFalse(target.match("http://pizza.com/http://xssd.example.com"));        
        googleDB.target.wipe();        
        
        
        
    }
    
    @Test
    public void testHasTarget() {
        
        Group grp1 = new Group(Group.Module.GOOGLE, "google #1");
        Group grp2 = new Group(Group.Module.GOOGLE, "google #2");
        
        baseDB.group.insert(grp1);
        baseDB.group.insert(grp2);
        
        assertFalse(googleDB.target.hasTarget());
        
        List<GoogleTarget> targets = Arrays.asList(
            new GoogleTarget(grp1.getId(), "t1-1", PatternType.REGEX, "^target1$"),
            new GoogleTarget(grp1.getId(), "t1-2", PatternType.REGEX, "^target1$"),
            new GoogleTarget(grp2.getId(), "t2-1", PatternType.REGEX, "^target2$")
        );
        
        assertEquals(3, googleDB.target.insert(targets));
        assertTrue(googleDB.target.hasTarget());
    }
    
    
}
