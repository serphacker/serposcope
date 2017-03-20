/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Singleton;
import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.base.Run.Status;
import static com.serphacker.serposcope.models.base.Run.Status.ABORTING;
import static com.serphacker.serposcope.models.base.Run.Status.DONE_ABORTED;
import static com.serphacker.serposcope.models.base.Run.Status.DONE_CRASHED;
import static com.serphacker.serposcope.models.base.Run.Status.DONE_SUCCESS;
import static com.serphacker.serposcope.models.base.Run.Status.DONE_WITH_ERROR;
import static com.serphacker.serposcope.models.base.Run.Status.RUNNING;
import com.serphacker.serposcope.querybuilder.QRun;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RunDB extends AbstractDB {
    
    QRun t_run = QRun.run;
    
    public int insert(Run run) {
        int id = -1;
        try(Connection conn = ds.getConnection()){
            id = new SQLInsertClause(conn, dbTplConf, t_run)
                .set(t_run.moduleId, run.getModule().ordinal())
                .set(t_run.day, Date.valueOf(run.getDay() == null ? null : run.getDay()))
                .set(t_run.started, run.getStarted()== null ? null : Timestamp.valueOf(run.getStarted()))
                .set(t_run.finished, run.getFinished()== null ? null : Timestamp.valueOf(run.getFinished()))
                .set(t_run.captchas, run.getCaptchas())
                .set(t_run.progress, run.getProgress())
                .set(t_run.errors, run.getErrors())
                .set(t_run.status, run.getStatus().ordinal())
                .set(t_run.mode, run.getMode().ordinal())
                .executeWithKey(t_run.id);
            
            run.setId(id);
                
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return id;
    }
    
    public boolean updateStatus(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.status, run.getStatus().ordinal())
                .where(t_run.id.eq(run.getId()))
                .execute() == 1;
                
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;
    }
    
    public boolean updateStatusAborting(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.status, Status.ABORTING.ordinal())
                .where(t_run.id.eq(run.getId()))
                .where(t_run.status.eq(Status.RUNNING.ordinal()))
                .execute() == 1;
                
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;
    }

    public boolean updateStarted(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.started, run.getStarted()== null ? null : Timestamp.valueOf(run.getStarted()))
                .where(t_run.id.eq(run.getId()))
                .execute() == 1;
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;        
    }
    
    public boolean updateFinished(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.finished, run.getFinished() == null ? null : Timestamp.valueOf(run.getFinished()))
                .set(t_run.progress, run.getProgress())
                .set(t_run.captchas, run.getCaptchas())
                .set(t_run.errors, run.getErrors())
                .where(t_run.id.eq(run.getId()))
                .execute() == 1;
                
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;        
    }
    
    /**
     * update progress, captchas, errors
     * @param run
     * @return 
     */
    public boolean updateProgress(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.progress, run.getProgress())
                .where(t_run.id.eq(run.getId()))
                .execute() == 1;
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;        
    }
    
    public boolean updateCaptchas(Run run){
        boolean updated = false;
        try(Connection conn = ds.getConnection()){
            updated = new SQLUpdateClause(conn, dbTplConf, t_run)
                .set(t_run.captchas, run.getCaptchas())
                .where(t_run.id.eq(run.getId()))
                .execute() == 1;
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return updated;        
    }
    
    public void delete(int runId){
        try(Connection conn = ds.getConnection()){
            new SQLDeleteClause(conn, dbTplConf, t_run).where(t_run.id.eq(runId)).execute();
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }        
    }
    
    public List<Run> listDone(Integer firstId, Integer lastId){
        List<Run> runs = new ArrayList<>();
        try(Connection conn = ds.getConnection()){
            SQLQuery<Tuple> query = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run);
            
            if(firstId != null){
                query = query.where(t_run.id.goe(firstId));
            }
            
            if(lastId != null){
                query = query.where(t_run.id.loe(lastId));
            }
            
            List<Tuple> tuples = query
                .where(t_run.finished.isNotNull())
                .orderBy(t_run.id.asc())
                .fetch();
            
            for (Tuple tuple : tuples) {
                Run run = fromTuple(tuple);
                if(run != null){
                    runs.add(run);
                }
            }
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return runs;
    }    
    
    
    public long count(){
        Long count = -1l;
        try(Connection conn = ds.getConnection()){
            count = new SQLQuery<>(conn, dbTplConf).select(t_run.count()).from(t_run).fetchFirst();
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        if(count == null){
            count = -1l;
        }
        return count;
    }        
    
    /*
    public List<Run> listRunning(){
        List<Run> runs = new ArrayList<>();
        try(Connection conn = ds.getConnection()){
            
            List<Tuple> tuples = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.status.eq(Run.Status.RUNNING.ordinal()))
                .fetch();
                
            for (Tuple tuple : tuples) {
                runs.add(fromTuple(tuple));
            }
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return runs;        
    }
    */
    
    public final static Collection<Run.Status> STATUSES_RUNNING = Arrays.asList(RUNNING,ABORTING);
    public final static Collection<Run.Status> STATUSES_DONE = Arrays.asList(DONE_ABORTED,DONE_CRASHED,DONE_SUCCESS,DONE_WITH_ERROR);
    public List<Run> listByStatus(Collection<Run.Status> statuses, Long limit, Long offset){
        List<Integer> statusesVal = null;
        if(statuses != null && !statuses.isEmpty()){
            statusesVal = statuses.stream().map(Run.Status::ordinal).collect(Collectors.toList());
        }
        
        List<Run> runs = new ArrayList<>();
        try(Connection conn = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run);
            
            if(statusesVal != null){
                query.where(t_run.status.in(statusesVal));
            }
                
            query.orderBy(t_run.id.desc());
            
            if(limit != null){
                query.limit(limit);
            }
            
            if(offset != null){
                query.offset(offset);
            }
            
            List<Tuple> tuples = query.fetch();
                
            for (Tuple tuple : tuples) {
                Run run = fromTuple(tuple);
                if(run != null){
                    runs.add(run);
                }
            }
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return runs;        
    }
    
    public List<Run> findByDay(Module module, LocalDate day){
        List<Run> runs = new ArrayList<>();
        try(Connection conn = ds.getConnection()){
            
            List<Tuple> tuples = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.moduleId.eq(module.ordinal()))
                .where(t_run.day.eq(Date.valueOf(day)))
                .fetch();

            for (Tuple tuple : tuples) {
                Run run = fromTuple(tuple);
                if(run != null){
                    runs.add(run);
                }
            }
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return runs;          
    }    
    
    public Run find(int runId){
        Run run = null;
        try(Connection conn = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.id.eq(runId))
                .fetchFirst();
                
            run = fromTuple(tuple);
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return run;
    }
    
    public List<Run> findRunning(){
    	List<Run> runs = new ArrayList<>();
        try(Connection conn = ds.getConnection()){
            
        	List<Tuple> tuples = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.finished.isNull())
                .fetch();
        	
        	for (Tuple tuple : tuples) {
                Run run = fromTuple(tuple);
                if(run != null){
                    runs.add(run);
                }
            }

        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return runs;
    }
    
    public Run findPrevious(int runId){
        Run run = null;
        try(Connection conn = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<>(conn, dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.id.lt(runId))
                .orderBy(t_run.id.desc())
                .fetchFirst();
                
            run = fromTuple(tuple);
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return run;
    }    
    
    public Run findLast(Module module, Collection<Run.Status> statuses, LocalDate untilDate){
        Run run = null;
        try(Connection conn = ds.getConnection()){

            SQLQuery<Tuple> query = new SQLQuery<>(conn,dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.moduleId.eq(module.ordinal()));
            
            if( statuses != null){
                query.where(t_run.status.in(statuses.stream().map(Run.Status::ordinal).collect(Collectors.toList())));
            }
            
            if(untilDate != null){
                query.where(t_run.day.loe(Date.valueOf(untilDate)));
            }
        
            Tuple tuple = query
                .orderBy(t_run.id.desc())
                .fetchFirst();

            run = fromTuple(tuple);

        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return run;           
    }
    
    public Run findFirst(Module module, Collection<Run.Status> statuses, LocalDate fromDate){
        Run run = null;
        try(Connection conn = ds.getConnection()){

            SQLQuery<Tuple> query = new SQLQuery<>(conn,dbTplConf)
                .select(t_run.all())
                .from(t_run)
                .where(t_run.moduleId.eq(module.ordinal()));
            
            if( statuses != null){
                query.where(t_run.status.in(statuses.stream().map(Run.Status::ordinal).collect(Collectors.toList())));
            }
            
            if(fromDate != null){
                query.where(t_run.day.goe(Date.valueOf(fromDate)));
            }
        
            Tuple tuple = query
                .orderBy(t_run.id.asc())
                .fetchFirst();

            run = fromTuple(tuple);

        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return run;           
    }    
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_run).execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }
    
    protected Run fromTuple(Tuple tuple){
        
        if(tuple == null){
            return null;
        }
        
        Run run = new Run();
        
        run.setId(tuple.get(t_run.id));
        run.setModule(Module.values()[tuple.get(t_run.moduleId)]);
        run.setDay(tuple.get(t_run.day) == null ? null : tuple.get(t_run.day).toLocalDate());
        run.setStarted(tuple.get(t_run.started) == null ? null : tuple.get(t_run.started).toLocalDateTime());
        run.setFinished(tuple.get(t_run.finished) == null ? null : tuple.get(t_run.finished).toLocalDateTime());
        run.setStatus(Run.Status.values()[tuple.get(t_run.status)]);
        run.setProgress(tuple.get(t_run.progress));
        run.setErrors(tuple.get(t_run.errors));
        run.setCaptchas(tuple.get(t_run.captchas));
        run.setMode(Run.Mode.values()[tuple.get(t_run.mode)]);
        
        return run;
    }
    
}
