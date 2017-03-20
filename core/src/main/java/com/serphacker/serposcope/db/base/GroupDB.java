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
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Group.Module;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.querybuilder.QGroup;
import com.serphacker.serposcope.querybuilder.QUserGroup;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GroupDB extends AbstractDB {
    
    QGroup t_group = QGroup.group;
    QUserGroup t_user_group = QUserGroup.userGroup;

    public int insert(Group group){
        int id = 0;
        
        try(Connection con = ds.getConnection()){
            
            Integer key = new SQLInsertClause(con, dbTplConf, t_group)
                .set(t_group.moduleId, group.getModule().ordinal())
                .set(t_group.name, group.getName())
                .executeWithKey(t_group.id);
            
            if(key != null){
                id = key;
                group.setId(id);
            }
            
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }
        
        return id;
    }
    
    public boolean delete(Group group){
        boolean deleted = false;
        
        try(Connection con = ds.getConnection()){
            
            deleted = new SQLDeleteClause(con, dbTplConf, t_group)
                .where(t_group.id.eq(group.getId()))
                .execute() == 1;
            
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }
        
        return deleted;
    }
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_user_group).execute();
            new SQLDeleteClause(con, dbTplConf, t_group).execute();
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }
    }    
    
    public boolean update(Group group){
        boolean updated = false;
        
        try(Connection con = ds.getConnection()){
            updated = new SQLUpdateClause(con, dbTplConf, t_group)
                .set(t_group.name, group.getName())
                .where(t_group.id.eq(group.getId()))
                .execute() == 1;
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }
        
        return updated;
    }    
    
    public List<Group> list(){
        return list(null);
    }
    
    public List<Group> list(Module module){
        List<Group> groups = new ArrayList<>();
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_group.all())
                .from(t_group);
            
            if(module != null){
                query.where(t_group.moduleId.eq(module.ordinal()));
            }
            
            List<Tuple> tuples = query.fetch();

            for (Tuple tuple : tuples) {
                groups.add(fromTuple(tuple));
            }
            
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }        
        
        return groups;
    }
    
    public List<Group> listForUser(User user){
        List<Group> groups = new ArrayList<>();
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_group.all())
                .from(t_group);
            
            if(user != null && !user.isAdmin()){
                query.join(t_user_group).on(t_user_group.groupId.eq((t_group.id)));
                query.where(t_user_group.userId.eq(user.getId()));
            }
            
            List<Tuple> tuples = query.fetch();

            for (Tuple tuple : tuples) {
                groups.add(fromTuple(tuple));
            }
            
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }        
        
        return groups;
    }    
    
    public Group find(int groupId){
        Group group = null;
        try(Connection con = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<Void>(con, dbTplConf)
                .select(t_group.all())
                .from(t_group)
                .where(t_group.id.eq(groupId))
                .fetchFirst();

            group = fromTuple(tuple);
            
        }catch(Exception ex){
            LOG.error("SQLError ex", ex);
        }        
        
        return group;
    }    
    
    
    Group fromTuple(Tuple tuple){
        if(tuple == null){
            return null;
        }
        
        return new Group(
            tuple.get(t_group.id), 
            Group.Module.values()[tuple.get(t_group.moduleId)], 
            tuple.get(t_group.name)
        );
    }
    
}
