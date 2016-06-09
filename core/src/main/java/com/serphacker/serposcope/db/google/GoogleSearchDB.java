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
import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GoogleSearchDB extends AbstractDB {
    
    QGoogleSearch t_gs = QGoogleSearch.googleSearch;
    QGoogleSearchGroup t_gsg = QGoogleSearchGroup.googleSearchGroup;

    public int insert(Collection<GoogleSearch> searches, int groupId){
        int inserted = 0;
        
        try(Connection con = ds.getConnection()){
            
            for (GoogleSearch search : searches) {
                
                if(search.getId() == 0){
                    Integer key = new SQLInsertClause(con, dbTplConf, t_gs)
                        .set(t_gs.keyword, search.getKeyword())
                        .set(t_gs.tld, search.getTld())
                        .set(t_gs.datacenter, search.getDatacenter())
                        .set(t_gs.device, (byte)search.getDevice().ordinal())
                        .set(t_gs.local, search.getLocal())
                        .set(t_gs.customParameters, search.getCustomParameters())
                        .executeWithKey(t_gs.id);

                    if(key != null){
                        search.setId(key);
                    }
                }
                
                inserted += new SQLMergeClause(con, dbTplConf, t_gsg)
                    .set(t_gsg.groupId, groupId)
                    .set(t_gsg.googleSearchId, search.getId())
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
                .select(t_gs.id)
                .from(t_gs)
                .where(t_gs.keyword.eq(search.getKeyword()))
                .where(t_gs.device.eq((byte)search.getDevice().ordinal()));
            
            if(search.getTld() != null){
                query.where(t_gs.tld.eq(search.getTld()));
            } else {
                query.where(t_gs.tld.isNull());
            }
            
            if(search.getDatacenter() != null){
                query.where(t_gs.datacenter.eq(search.getDatacenter()));
            } else {
                query.where(t_gs.datacenter.isNull());
            }
            
            if(search.getLocal() != null){
                query.where(t_gs.local.eq(search.getLocal()));
            } else {
                query.where(t_gs.local.isNull());
            }
            
            if(search.getCustomParameters()!= null){
                query.where(t_gs.customParameters.eq(search.getCustomParameters()));
            } else {
                query.where(t_gs.customParameters.isNull());
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
                .select(t_gs.all())
                .from(t_gs)
                .where(t_gs.id.eq(id))
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
            
            deleted = new SQLDeleteClause(con, dbTplConf, t_gsg)
                .where(t_gsg.googleSearchId.eq(search.getId()))
                .where(t_gsg.groupId.eq(groupId))
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
                .from(t_gsg)
                .where(t_gsg.googleSearchId.eq(search.getId()))
                .fetchOne() != null;
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return hasGroup;
    }
    
    
    public boolean delete(GoogleSearch search){
        boolean deleted = false;
        
        try(Connection con = ds.getConnection()){
            
            deleted = new SQLDeleteClause(con, dbTplConf, t_gs)
                .where(t_gs.id.eq(search.getId()))
                .execute() == 1;
            
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return deleted;
    }       
    
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_gsg).execute();
            new SQLDeleteClause(con, dbTplConf, t_gs).execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
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
                .select(t_gs.all())
                .from(t_gs);
            
            if(groups != null){
                query.join(t_gsg).on(t_gs.id.eq(t_gsg.googleSearchId));
                query.where(t_gsg.groupId.in(groups));
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
    
    public Map<Integer, GoogleSearch> mapBySearchId(Collection<Integer> searchId){
        Map<Integer, GoogleSearch> searches = new HashMap<>();
        
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_gs.all())
                .from(t_gs)
                .where(t_gs.id.in(searchId));
            
            List<Tuple> tuples = query.fetch();
            
            if(tuples != null){
                for (Tuple tuple : tuples) {
                    searches.put(tuple.get(t_gs.id), fromTuple(tuple));
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
                .select(t_gsg.groupId)
                .from(t_gsg)
                .where(t_gsg.googleSearchId.eq(search.getId()))
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
        
        search.setId(tuple.get(t_gs.id));
        search.setKeyword(tuple.get(t_gs.keyword));
        search.setDatacenter(tuple.get(t_gs.datacenter));
        search.setDevice(GoogleDevice.values()[tuple.get(t_gs.device)]);
        search.setLocal(tuple.get(t_gs.local));
        search.setTld(tuple.get(t_gs.tld));
        search.setCustomParameters(tuple.get(t_gs.customParameters));
        
        return search;
    }
    
}
