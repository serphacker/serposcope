/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import static com.serphacker.serposcope.db.base.RunDB.STATUSES_DONE;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.Run.Mode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class RunDBIT extends AbstractDBIT {

    public RunDBIT() {
    }

    @Inject
    RunDB runDB;
    
    @Inject
    GroupDB groupDB;

    @Test
    public void test() {

        Run run = new Run(Run.Mode.MANUAL, Module.TWITTER, LocalDateTime.of(2010,10,10,10,10,10));

        int id = runDB.insert(run);

        assertTrue(id > 0);
        assertEquals(id, run.getId());

        Run fetchedRun = runDB.find(id);
        ReflectionAssert.assertReflectionEquals(run, fetchedRun);
        
        run.setFinished(LocalDateTime.now().withNano(0));
        run.setStatus(Run.Status.DONE_SUCCESS);
        runDB.updateStatus(run);
        runDB.updateFinished(run);
        
        fetchedRun = runDB.find(id);
        ReflectionAssert.assertReflectionEquals(run, fetchedRun);
    }
    
    @Test
    public void findFirstLast(){
        
        Group group = new Group(Module.GOOGLE, "google");
        groupDB.insert(group);
        
        List<Integer> groups = Arrays.asList(1);
        
        Run run1 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 9, 10, 10));
        run1.setStatus(Run.Status.RUNNING);
        runDB.insert(run1);
        assertEquals(1, run1.getId());
        
        Run run2 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        run2.setStatus(Run.Status.DONE_SUCCESS);
        run2.setFinished(run2.getStarted().plusHours(1));
        runDB.insert(run2);
        assertEquals(2, run2.getId());
        
        Run run3 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 11, 10, 10));
        run3.setStatus(Run.Status.DONE_SUCCESS);
        run3.setFinished(run3.getStarted().plusHours(1));
        runDB.insert(run3);
        
        Run run4 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 12, 10, 10));
        run4.setStatus(Run.Status.DONE_SUCCESS);
        run4.setFinished(run4.getStarted().plusHours(1));
        runDB.insert(run4);
        
        Run run5 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 13, 10, 10));
        run5.setStatus(Run.Status.DONE_SUCCESS);
        run5.setFinished(run5.getStarted().plusHours(1));
        runDB.insert(run5);
        
        Run run6 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 14, 10, 10));
        run6.setStatus(Run.Status.DONE_SUCCESS);
        run6.setFinished(run6.getStarted().plusHours(1));
        runDB.insert(run6);
        
        Run run7 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 15, 10, 10));
        run7.setStatus(Run.Status.RUNNING);
        runDB.insert(run7);        
        
        assertNull(runDB.findFirst(group.getModule(), null, LocalDate.of(2050, 10, 15)));
        assertEquals(run1.getId(), runDB.findFirst(group.getModule(), null, null).getId());
        assertEquals(run2.getId(), runDB.findFirst(group.getModule(), STATUSES_DONE, null).getId());
        assertEquals(run3.getId(), runDB.findFirst(group.getModule(), STATUSES_DONE, run3.getStarted().toLocalDate()).getId());
        
        
        assertNull(runDB.findLast(group.getModule(), null, LocalDate.of(2000, 10, 15)));
        assertEquals(run7.getId(), runDB.findLast(group.getModule(), null, null).getId());
        assertEquals(run6.getId(), runDB.findLast(group.getModule(), STATUSES_DONE, null).getId());
        assertEquals(run5.getId(), runDB.findLast(group.getModule(), STATUSES_DONE, run5.getStarted().toLocalDate()).getId());        
    }
    
    @Test
    public void testList(){
        Group group = new Group(Module.GOOGLE, "google");
        groupDB.insert(group);
        
        int runId = -1;
        
        List<Integer> groups = Arrays.asList(1);
        int nRun = 50;
        
        for (int i = 0; i < nRun; i++) {
            Run run = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
            run.setStatus(Run.Status.DONE_SUCCESS);
            runId = runDB.insert(run);
        }
        
        assertEquals(nRun, runDB.listByStatus(STATUSES_DONE, null, null).size());
        
        for (int i = 0; i < nRun; i++) {
            assertEquals(nRun-i, runDB.listByStatus(STATUSES_DONE, 1l, (long)i).get(0).getId());
        }
    }
    
    @Test
    public void findByStatus(){
        Group group = new Group(Module.GOOGLE, "google");
        groupDB.insert(group);
        
        int runId = -1;
        
        List<Integer> groups = Arrays.asList(1);

        Run run = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        run.setStatus(Run.Status.DONE_SUCCESS);
        runId = runDB.insert(run);
        
        run = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        run.setStatus(Run.Status.RUNNING);
        runId = runDB.insert(run);
        
        run = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        run.setStatus(Run.Status.ABORTING);
        runId = runDB.insert(run);
        
        run = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        run.setStatus(Run.Status.ABORTING);
        runId = runDB.insert(run);
        
        List<Run> runs = runDB.listByStatus(RunDB.STATUSES_RUNNING,null,null);
        assertEquals(3, runs.size());
        
    }
    
    @Test
    public void findPrevious(){
        Group group = new Group(Module.GOOGLE, "google");
        groupDB.insert(group);
        
        Run run1 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        runDB.insert(run1);
        
        Run run2 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        runDB.insert(run2);
        
        assertEquals(run1.getId(), runDB.findPrevious(run2.getId()).getId());
        
        Run run3 = new Run(Mode.MANUAL, group.getModule(), LocalDateTime.of(2010, 10, 10, 10, 10));
        runDB.insert(run3);
        assertEquals(run2.getId(), runDB.findPrevious(run3.getId()).getId());
        
    }
    
}



