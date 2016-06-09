/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Singleton;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.google.GoogleTarget;
import com.serphacker.serposcope.models.google.GoogleTarget.PatternType;
import com.serphacker.serposcope.querybuilder.QGoogleTarget;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class GoogleTargetDB extends AbstractDB {

    QGoogleTarget t_target = QGoogleTarget.googleTarget;

    public int insert(Collection<GoogleTarget> targets){
        int inserted = 0;
        
        try(Connection con = ds.getConnection()){
            
            for (GoogleTarget target : targets) {
                
                Integer key = new SQLInsertClause(con, dbTplConf, t_target)
                    .set(t_target.groupId, target.getGroupId())
                    .set(t_target.name,target.getName())
                    .set(t_target.patternType, (byte)target.getType().ordinal())
                    .set(t_target.pattern, target.getPattern())
                    .executeWithKey(t_target.id);
                
                if(key != null){
                    target.setId(key);
                    inserted++;
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return inserted;
    }
    
    public boolean delete(int targetId){
        boolean deleted = false;
        try(Connection con = ds.getConnection()){
            deleted = new SQLDeleteClause(con, dbTplConf, t_target)
                .where(t_target.id.eq(targetId))
                .execute() == 1;
        }catch(Exception ex){
            LOG.error("SQLError", ex);
        }
        return deleted;
    }
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_target).execute();
        }catch(Exception ex){
            LOG.error("SQLError", ex);
        }
    }    
    
    /**
     * list all target
     * @return 
     */
    public List<GoogleTarget> list(){
        return list(null);
    }
    
    public boolean hasTarget(){
        Integer hasOne=null;
        
        try(Connection con = ds.getConnection()){
            
            hasOne = new SQLQuery<Void>(con, dbTplConf)
                .select(Expressions.ONE)
                .from(t_target)
                .limit(1)
                .fetchFirst();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }        
        
        return hasOne != null;
    }
    
    /***
     * list target by group
     * @param groupId
     * @return 
     */
    public List<GoogleTarget> list(Collection<Integer> groups){
        List<GoogleTarget> targets = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_target.all())
                .from(t_target);
            
            if(groups != null){
                query.where(t_target.groupId.in(groups));
            }
            
            List<Tuple> tuples = query.fetch();
            
            if(tuples != null){
                for (Tuple tuple : tuples) {
                    targets.add(fromTuple(tuple));
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return targets;
    }
    
    public GoogleTarget get(int targetId){
        GoogleTarget target = null;
        
        try(Connection con = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<Void>(con, dbTplConf)
                .select(t_target.all())
                .from(t_target)
                .where(t_target.id.eq(targetId))
                .fetchOne();
            
            target = fromTuple(tuple);
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return target;
    }    
    
    GoogleTarget fromTuple(Tuple tuple) throws Exception{
        return new GoogleTarget(
            tuple.get(t_target.id),
            tuple.get(t_target.groupId),
            tuple.get(t_target.name),
            tuple.get(t_target.patternType) == null ? PatternType.REGEX : GoogleTarget.PatternType.values()[tuple.get(t_target.patternType)],
            tuple.get(t_target.pattern)
        );
    }
    
}
