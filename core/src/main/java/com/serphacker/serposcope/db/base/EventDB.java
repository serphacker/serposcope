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
import com.serphacker.serposcope.models.base.Event;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.querybuilder.QEvent;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EventDB extends AbstractDB {
    
    
    QEvent t_event = QEvent.event;
    
    public boolean insert(Event event){
        boolean inserted = false;
        
        try(Connection con = ds.getConnection()){
            
            inserted = new SQLInsertClause(con, dbTplConf, t_event)
                .set(t_event.groupId, event.getGroupId())
                .set(t_event.day, Date.valueOf(event.getDay()))
                .set(t_event.title, event.getTitle())
                .set(t_event.description, event.getDescription())
                .execute() == 1;
            
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return inserted;
    }
    
    public boolean update(Event event){
        boolean updated = false;
        
        try(Connection con = ds.getConnection()){
            
            updated = new SQLUpdateClause(con, dbTplConf, t_event)
                .set(t_event.title, event.getTitle())
                .set(t_event.description, event.getDescription())
                .where(t_event.groupId.eq(event.getGroupId()))
                .where(t_event.day.eq(Date.valueOf(event.getDay())))
                .execute() == 1;
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return updated;
    }
    
    public boolean delete(Event event){
        boolean updated = false;
        
        try(Connection con = ds.getConnection()){
            
            updated = new SQLDeleteClause(con, dbTplConf, t_event)
                .where(t_event.groupId.eq(event.getGroupId()))
                .where(t_event.day.eq(Date.valueOf(event.getDay())))
                .execute() == 1;
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return updated;
    }    
    
    public boolean delete(Group group){
        boolean updated = false;
        
        try(Connection con = ds.getConnection()){
            
            updated = new SQLDeleteClause(con, dbTplConf, t_event)
                .where(t_event.groupId.eq(group.getId()))
                .execute() == 1;
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return updated;
    }    
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_event).execute();
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }    
    
    public Event find(Group group, LocalDate day){
        Event event = null;
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_event.all())
                .from(t_event)
                .where(t_event.groupId.eq(group.getId()))
                .where(t_event.day.eq(Date.valueOf(day)));
            event = fromTuple(query.fetchOne());
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return event;
    }    
    
    public List<Event> list(Group group, LocalDate startDate, LocalDate endDate){
        List<Event> events = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_event.all())
                .from(t_event)
                .where(t_event.groupId.eq(group.getId()));
            
            if(startDate != null){
                query.where(t_event.day.goe(Date.valueOf(startDate)));
            }
                
            if(endDate != null){
                query.where(t_event.day.loe(Date.valueOf(endDate)));
            }
            
            query.orderBy(t_event.day.desc());
            
            List<Tuple> tuples = query.fetch();
            for (Tuple tuple : tuples) {
                events.add(fromTuple(tuple));
            }
            
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return events;
    }
    
    protected Event fromTuple(Tuple tuple){
        if(tuple == null){
            return null;
        }        
        
        Event event = new Event();
        
        event.setGroupId(tuple.get(t_event.groupId));
        event.setDay(tuple.get(t_event.day).toLocalDate());
        event.setTitle(tuple.get(t_event.title));
        event.setDescription(tuple.get(t_event.description));
        
        return event;
    }
    
}
