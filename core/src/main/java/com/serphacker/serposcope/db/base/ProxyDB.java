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
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.querybuilder.QProxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.sql.Timestamp;

@Singleton
public class ProxyDB extends AbstractDB {
    
    QProxy t_proxy = QProxy.proxy;
    
    public long insert(Collection<Proxy> proxies){
        int inserted = 0;
        try(Connection con = ds.getConnection()){
            
            for (Proxy proxy : proxies) {
                SQLInsertClause insert = new SQLInsertClause(con, dbTplConf, t_proxy);
                insert.set(t_proxy.type, proxy.getType().ordinal());
                insert.set(t_proxy.ip, proxy.getIp());
                insert.set(t_proxy.port, proxy.getPort());
                insert.set(t_proxy.user, proxy.getUsername());
                insert.set(t_proxy.password, proxy.getPassword());
                insert.set(t_proxy.status, (byte)proxy.getStatus().ordinal());
                insert.set(t_proxy.lastCheck, proxy.getLastCheck() == null ? null : Timestamp.valueOf(proxy.getLastCheck()));
                insert.set(t_proxy.remoteIp, proxy.getRemoteip());
                Integer key = insert.executeWithKey(t_proxy.id);
                if(key != null){
                    ++inserted;
                    proxy.setId(key);
                }
            }
            
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return inserted;
    }
    
    public boolean update(Proxy proxy){
        boolean updated = false;
        try(Connection con = ds.getConnection()){
            
            SQLUpdateClause update = new SQLUpdateClause(con, dbTplConf, t_proxy);
            update.set(t_proxy.type, proxy.getType().ordinal());
            update.set(t_proxy.ip, proxy.getIp());
            update.set(t_proxy.port, proxy.getPort());
            update.set(t_proxy.user, proxy.getUsername());
            update.set(t_proxy.password, proxy.getPassword());
            update.set(t_proxy.status, (byte)proxy.getStatus().ordinal());
            update.set(t_proxy.lastCheck, proxy.getLastCheck() == null ? null : Timestamp.valueOf(proxy.getLastCheck()));
            update.set(t_proxy.remoteIp, proxy.getRemoteip());
            update.where(t_proxy.id.eq(proxy.getId()));
            updated = update.execute() == 1;
            
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return updated;
    }    
    
    public boolean updateStatus(Proxy.Status status, Collection<Integer> ids){
        boolean updated = false;
        try(Connection con = ds.getConnection()){
            
            SQLUpdateClause update = new SQLUpdateClause(con, dbTplConf, t_proxy);
            update.set(t_proxy.status, (byte)status.ordinal());
            update.where(t_proxy.id.in(ids));
            updated = update.execute() > 0;
            
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return updated;
    }        
    
    public List<Proxy> list(){
        List<Proxy> proxies = new ArrayList<>();
        
        try(Connection con = ds.getConnection()){
            List<Tuple> tuples = new SQLQuery<Void>(con, dbTplConf).select(t_proxy.all()).from(t_proxy).fetch();
            for (Tuple tuple : tuples) {
                proxies.add(fromTuple(tuple));
            }
        }catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return proxies;
    }
    
    public long delete(Collection<Integer> ids){
        long deleted = -1l;
        try(Connection con = ds.getConnection()){
            deleted = new SQLDeleteClause(con, dbTplConf, t_proxy).where(t_proxy.id.in(ids)).execute();
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return deleted;        
    }
    
    public long deleteByStatus(Proxy.Status status){
        long deleted = -1l;
        try(Connection con = ds.getConnection()){
            deleted = new SQLDeleteClause(con, dbTplConf, t_proxy)
                .where(t_proxy.status.eq((byte)status.ordinal()))
                .execute();
        } catch(Exception ex){
            LOG.error("SQL Error", ex);
        }
        return deleted;        
    }    
    
    protected Proxy fromTuple(Tuple tuple){
        if(tuple == null){
            return null;
        }
        Proxy.Status proxyStatus = Proxy.Status.UNCHECKED;
        if(tuple.get(t_proxy.status) != null && tuple.get(t_proxy.status) < Proxy.Status.values().length){
            proxyStatus = Proxy.Status.values()[tuple.get(t_proxy.status)];
        }
        
        return new Proxy(
            tuple.get(t_proxy.id),
            Proxy.Type.values()[tuple.get(t_proxy.type)],
            tuple.get(t_proxy.ip),
            tuple.get(t_proxy.port),
            tuple.get(t_proxy.user),
            tuple.get(t_proxy.password),
            tuple.get(t_proxy.remoteIp),
            tuple.get(t_proxy.lastCheck) == null ? null : tuple.get(t_proxy.lastCheck).toLocalDateTime(),
            proxyStatus
        );
    }
    
}
