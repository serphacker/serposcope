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
import com.serphacker.serposcope.db.base.GroupDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.Run.Mode;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleSearchDBIT extends AbstractDBIT {

    public GoogleSearchDBIT() {
    }
    
    @Inject
    GoogleDB googleDB;
    
    @Inject
    BaseDB baseDB;

    @Inject
    GoogleSearchDB gsDB;
    
    @Inject
    GroupDB groupDb;
    
    @Test
    public void testFind() {
        
        int groupId = groupDb.insert(new Group(Group.Module.GOOGLE, "google"));
        assertEquals(1, groupId);
        
        GoogleSearch s1 = new GoogleSearch();
        s1.setKeyword("search #1");
        s1.setTld("fr");
        
        gsDB.insert(Arrays.asList(s1), groupId);
        
        assertEquals(1, s1.getId());
        ReflectionAssert.assertReflectionEquals(s1, gsDB.find(s1.getId()));
        
    }    
    
    @Test
    public void testGetId() {
        
        int groupId = groupDb.insert(new Group(Group.Module.GOOGLE, "google"));
        assertEquals(1, groupId);
        
        GoogleSearch s1 = new GoogleSearch();
        s1.setKeyword("search #1");
        s1.setTld("fr");
        
        gsDB.insert(Arrays.asList(s1), groupId);
        
        assertEquals(1, s1.getId());
        assertEquals(1, gsDB.getId(s1));
        
        s1.setTld("de");
        assertEquals(0, gsDB.getId(s1));
        
    }

    @Test
    public void test() {
        int grp1 = groupDb.insert(new Group(Group.Module.GOOGLE, "google #1"));
        assertEquals(1, grp1);        
        
        int grp2 = groupDb.insert(new Group(Group.Module.GOOGLE, "google #2"));
        assertEquals(2, grp2);                
        
        List<GoogleSearch> toInsert = new ArrayList<>();

        GoogleSearch s1 = new GoogleSearch();
        s1.setKeyword("search #1");
        toInsert.add(s1);

        GoogleSearch s2 = new GoogleSearch();
        s2.setKeyword("keyword");
        s2.setTld("tld");
        s2.setDatacenter("datacenter");
        s2.setDevice(GoogleDevice.DESKTOP);
        s2.setLocal("local");
        toInsert.add(s2);

        assertEquals(2, gsDB.insert(toInsert, grp1));
        
        
        GoogleSearch s3 = new GoogleSearch();
        s3.setKeyword("keyword #3");

        toInsert = Arrays.asList(s3);
        assertEquals(1, gsDB.insert(toInsert, grp2));
        
        List<GoogleSearch> inserted = gsDB.listByGroup(null);
        assertEquals(3, inserted.size());
        
        inserted = gsDB.listByGroup(Arrays.asList(grp1));
        assertEquals(2, inserted.size());
        
        assertEquals(1, inserted.get(0).getId());
        assertEquals(s1.getKeyword(), inserted.get(0).getKeyword());
        assertEquals(s1.getTld(), inserted.get(0).getTld());
        assertEquals(s1.getDatacenter(), inserted.get(0).getDatacenter());
        assertEquals(s1.getDevice(), inserted.get(0).getDevice());
        assertEquals(s1.getLocal(), inserted.get(0).getLocal());
        assertEquals(s1.getId(), inserted.get(0).getId());

        assertEquals(2, inserted.get(1).getId());
        assertEquals(s2.getKeyword(), inserted.get(1).getKeyword());
        assertEquals(s2.getTld(), inserted.get(1).getTld());
        assertEquals(s2.getDatacenter(), inserted.get(1).getDatacenter());
        assertEquals(s2.getDevice(), inserted.get(1).getDevice());
        assertEquals(s2.getLocal(), inserted.get(1).getLocal());
        assertEquals(s2.getId(), inserted.get(1).getId());
    }
    
    @Test
    public void delete(){
    int grp1 = groupDb.insert(new Group(Group.Module.GOOGLE, "google #1"));
        assertEquals(1, grp1);        
        
        int grp2 = groupDb.insert(new Group(Group.Module.GOOGLE, "google #2"));
        assertEquals(2, grp2);                
        
        List<GoogleSearch> toInsert = new ArrayList<>();

        GoogleSearch s1 = new GoogleSearch();
        s1.setKeyword("search #1");
        toInsert.add(s1);
        
        assertEquals(1, gsDB.insert(toInsert, grp1));
        assertEquals(1, gsDB.insert(toInsert, grp2));
        
        assertTrue(gsDB.deleteFromGroup(s1, grp1));
        assertTrue(gsDB.hasGroup(s1));
        assertFalse(gsDB.deleteFromGroup(s1, grp1));
        assertTrue(gsDB.hasGroup(s1));
        assertTrue(gsDB.deleteFromGroup(s1, grp2));
        assertFalse(gsDB.hasGroup(s1));        
        
    }
    
    @Test
    public void testListUnchecked() {
        
        int groupId = groupDb.insert(new Group(Group.Module.GOOGLE, "google"));
        assertEquals(1, groupId);
        
        GoogleSearch s1 = new GoogleSearch();
        s1.setKeyword("search #1");
        GoogleSearch s2 = new GoogleSearch();
        s2.setKeyword("search #2");
        gsDB.insert(Arrays.asList(s1, s2), groupId);
        
        Run run = new Run(Mode.MANUAL, Group.Module.GOOGLE, LocalDateTime.now());
        baseDB.run.insert(run);
        
        googleDB.serp.insert(new GoogleSerp(run.getId(), s1.getId(), LocalDateTime.now()));
        
        List<GoogleSearch> uncheckeds = googleDB.search.listUnchecked(run.getId());
        assertEquals(1, uncheckeds.size());
        assertEquals(s2.getId(), uncheckeds.get(0).getId());
        
        
    }
    
}
