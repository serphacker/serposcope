/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.task;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractTask extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTask.class);
    
    volatile boolean abort = false;
    
    @Inject
    protected BaseDB baseDB;
    
    protected long startMilliseconds;
    protected final Run run;
    
    public AbstractTask(Run run) {
        this.run = run;
    }
    
    @Override
    public void run() {
        startMilliseconds = System.currentTimeMillis();
        LOG.info(
            "task started for module {} of day {} ({})", 
            new Object[]{run.getModule(), run.getDay(), (run.getId() == 0 ? "new task": "recheck")}
        );
        if(run.getId() == 0){
            baseDB.run.insert(run);
        } else {
            run.setStatus(Run.Status.RUNNING);
            baseDB.run.updateStatus(run);
            baseDB.run.updateStarted(run);
        }
        
        List<Integer> groupsIds = baseDB.group.list(run.getModule())
                .stream().map((Group g) -> g.getId()).collect(Collectors.toList());
        
        if(groupsIds.isEmpty()){
            LOG.warn("no group to analyze");
            endRun(Run.Status.DONE_SUCCESS);
            return;
        }
        
        try {
            Run.Status status = doRun();
            endRun(status);
        }catch(Exception ex){
            onCrash(ex);
            LOG.error("Task crashed", ex);
            endRun(Run.Status.DONE_CRASHED);
        }
        LOG.info("task done for module {}", run.getModule());
    }
    
    protected abstract Run.Status doRun() throws Exception;
    protected abstract void onCrash(Exception ex);
    
    protected void endRun(Run.Status status) {
        run.setFinished(run.getStarted().plusSeconds((System.currentTimeMillis() - startMilliseconds) / 1000l));
        baseDB.run.updateFinished(run);
        
        if(abort){
            run.setStatus(Run.Status.DONE_ABORTED);
        } else {
            run.setStatus(status);
        }        
        baseDB.run.updateStatus(run);
    }

    public Run getRun() {
        return run;
    }
    
    public void abort() {
        abort = true;
    }
    
}
