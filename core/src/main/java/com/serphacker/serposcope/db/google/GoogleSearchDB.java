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
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.querybuilder.QGoogleSearch;
import com.serphacker.serposcope.querybuilder.QGoogleSearchGroup;
import com.serphacker.serposcope.querybuilder.QGoogleSerp;
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GoogleSearchDB extends AbstractDB {
    
    QGoogleSearch t_gsearch = QGoogleSearch.googleSearch;
    QGoogleSearchGroup t_ggroup = QGoogleSearchGroup.googleSearchGroup;
    QGoogleSerp t_gserp = QGoogleSerp.googleSerp;

    public int insert(Collection<GoogleSearch> searches, int groupId){
        int inserted = 0;
        
        try(Connection con = ds.getConnection()){
            
            for (GoogleSearch search : searches) {
                
                if(search.getId() == 0){
                    Integer key = new SQLInsertClause(con, dbTplConf, t_gsearch)
                        .set(t_gsearch.keyword, search.getKeyword())
                        .set(t_gsearch.tld, search.getTld())
                        .set(t_gsearch.datacenter, search.getDatacenter())
                        .set(t_gsearch.device, (byte)search.getDevice().ordinal())
                        .set(t_gsearch.local, search.getLocal())
                        .set(t_gsearch.customParameters, search.getCustomParameters())
                        .executeWithKey(t_gsearch.id);

                    if(key != null){
                        search.setId(key);
                    }
                }
                
                inserted += new SQLMergeClause(con, dbTplConf, t_ggroup)
                    .set(t_ggroup.groupId, groupId)
                    .set(t_ggroup.googleSearchId, search.getId())
                    .execute() == 1 ? 1 : 0;
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return inserted;
    }
    
    public int getId(GoogleSearch search){
        int id = 0;
        
        try(Connection con = ds.getConnection()){
            
            
            SQLQuery<Integer> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.id)
                .from(t_gsearch)
                .where(t_gsearch.keyword.eq(search.getKeyword()))
                .where(t_gsearch.device.eq((byte)search.getDevice().ordinal()));
            
            if(search.getTld() != null){
                query.where(t_gsearch.tld.eq(search.getTld()));
            } else {
                query.where(t_gsearch.tld.isNull());
            }
            
            if(search.getDatacenter() != null){
                query.where(t_gsearch.datacenter.eq(search.getDatacenter()));
            } else {
                query.where(t_gsearch.datacenter.isNull());
            }
            
            if(search.getLocal() != null){
                query.where(t_gsearch.local.eq(search.getLocal()));
            } else {
                query.where(t_gsearch.local.isNull());
            }
            
            if(search.getCustomParameters()!= null){
                query.where(t_gsearch.customParameters.eq(search.getCustomParameters()));
            } else {
                query.where(t_gsearch.customParameters.isNull());
            }
            
            Integer fetchedId = query.fetchFirst();
            
            if(fetchedId != null){
                id = fetchedId;
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return id;
    }
    
    public GoogleSearch find(Integer id){
        GoogleSearch search = null;
        
        try(Connection con = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.all())
                .from(t_gsearch)
                .where(t_gsearch.id.eq(id))
                .fetchFirst();
                
            search = fromTuple(tuple);
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return search;
    }    
    
    public boolean deleteFromGroup(GoogleSearch search, int groupId){
        boolean deleted = false;
        
        try(Connection con = ds.getConnection()){
            
            deleted = new SQLDeleteClause(con, dbTplConf, t_ggroup)
                .where(t_ggroup.googleSearchId.eq(search.getId()))
                .where(t_ggroup.groupId.eq(groupId))
                .execute() == 1;
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return deleted;
    }       
    
    public boolean hasGroup(GoogleSearch search){
        boolean hasGroup = false;
        
        try(Connection con = ds.getConnection()){
            
            hasGroup = new SQLQuery<Void>(con, dbTplConf)
                .select(Expressions.ONE)
                .from(t_ggroup)
                .where(t_ggroup.googleSearchId.eq(search.getId()))
                .fetchOne() != null;
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return hasGroup;
    }
    
    
    public boolean delete(GoogleSearch search){
        boolean deleted = false;
        
        try(Connection con = ds.getConnection()){
            
            deleted = new SQLDeleteClause(con, dbTplConf, t_gsearch)
                .where(t_gsearch.id.eq(search.getId()))
                .execute() == 1;
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return deleted;
    }       
    
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_ggroup).execute();
            new SQLDeleteClause(con, dbTplConf, t_gsearch).execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }
    
    public long count(){
        Long count = null;
        try(Connection con = ds.getConnection()){
            count =new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.count())
                .from(t_gsearch)
                .fetchFirst();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return count == null ? -1l : count;
    }
    
    public Map<Integer,Integer> countByGroup(){
        Map<Integer,Integer> map = new HashMap<>();
        try(Connection con = ds.getConnection()){
            List<Tuple> tuples = new SQLQuery<Void>(con, dbTplConf)
                .select(t_ggroup.groupId, t_ggroup.count())
                .from(t_ggroup)
                .groupBy(t_ggroup.groupId)
                .fetch();
            for (Tuple tuple : tuples) {
                map.put(tuple.get(t_ggroup.groupId), tuple.get(1, Long.class).intValue());
            }
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return map;
    }    
    
    /**
     * list all google search
     * @return 
     */
    public List<GoogleSearch> list(){
        return listByGroup(null);
    }
    
    /***
     * list google searches belonging to a specific group
     * @param groupId
     * @return 
     */
    public List<GoogleSearch> listByGroup(Collection<Integer> groups){
        List<GoogleSearch> searches = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.all())
                .from(t_gsearch);
            
            if(groups != null){
                query.join(t_ggroup).on(t_gsearch.id.eq(t_ggroup.googleSearchId));
                query.where(t_ggroup.groupId.in(groups));
            }
            
            List<Tuple> tuples = query.fetch();
            
            if(tuples != null){
                for (Tuple tuple : tuples) {
                    searches.add(fromTuple(tuple));
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return searches;
    }
    
    public List<GoogleSearch> listUnchecked(int runId){
        List<GoogleSearch> searches = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.all())
                .from(t_gsearch)
                .where(t_gsearch.id.notIn(
                    new SQLQuery<Void>(con, dbTplConf)
                        .select(t_gserp.googleSearchId)
                        .from(t_gserp)
                        .where(t_gserp.runId.eq(runId))
                ));
            
            List<Tuple> tuples = query.fetch();
            
            if(tuples != null){
                for (Tuple tuple : tuples) {
                    searches.add(fromTuple(tuple));
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return searches;
    }    
    
    public Map<Integer, GoogleSearch> mapBySearchId(Collection<Integer> searchId){
        Map<Integer, GoogleSearch> searches = new HashMap<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gsearch.all())
                .from(t_gsearch)
                .where(t_gsearch.id.in(searchId));
            
            List<Tuple> tuples = query.fetch();
            
            if(tuples != null){
                for (Tuple tuple : tuples) {
                    searches.put(tuple.get(t_gsearch.id), fromTuple(tuple));
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return searches;
    }
    
    public List<Integer> listGroups(GoogleSearch search){
        List<Integer> groups = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            
            List<Integer> ids = new SQLQuery<Void>(con, dbTplConf)
                .select(t_ggroup.groupId)
                .from(t_ggroup)
                .where(t_ggroup.googleSearchId.eq(search.getId()))
                .fetch();
            
            if(ids != null){
                groups.addAll(ids);
            }
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return groups;        
    }
    
    GoogleSearch fromTuple(Tuple tuple){
        if(tuple == null){
            return null;
        }
        
        GoogleSearch search = new GoogleSearch();
        
        search.setId(tuple.get(t_gsearch.id));
        search.setKeyword(tuple.get(t_gsearch.keyword));
        search.setDatacenter(tuple.get(t_gsearch.datacenter));
        search.setDevice(GoogleDevice.values()[tuple.get(t_gsearch.device)]);
        search.setLocal(tuple.get(t_gsearch.local));
        search.setTld(tuple.get(t_gsearch.tld));
        search.setCustomParameters(tuple.get(t_gsearch.customParameters));
        
        return search;
    }
    
}
