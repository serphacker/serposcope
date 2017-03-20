/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.db.base;

import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.querybuilder.QGoogleRank;
import com.serphacker.serposcope.querybuilder.QGoogleSerp;
import com.serphacker.serposcope.querybuilder.QGoogleTargetSummary;
import com.serphacker.serposcope.querybuilder.QRun;
import java.sql.Connection;
import java.util.List;


public class PruneDB extends AbstractDB {
    QRun t_run = QRun.run;
    QGoogleRank t_google_rank = QGoogleRank.googleRank;
    QGoogleSerp t_serp = QGoogleSerp.googleSerp;
    QGoogleTargetSummary t_target_summary = QGoogleTargetSummary.googleTargetSummary;
    
    public long prune(int maxRuns){
        
        if(maxRuns <= 0){
            return 0;
        }
        
        try(Connection con = ds.getConnection()){
            
            Long count = new SQLQuery<>(con, dbTplConf)
                .select(t_run.count())
                .from(t_run)
                .fetchFirst();
            
            if(count == null ){
                LOG.warn("count return null");
                return 0;
            }
            
            long limit = count - maxRuns;
            
            if(limit < 1){
                return 0;
            }
            
            List<Integer> runIds = new SQLQuery<>(con, dbTplConf)
                .select(t_run.id)
                .from(t_run)
                .orderBy(t_run.id.asc())
                .limit(limit)
                .fetch();
            
            new SQLDeleteClause(con, dbTplConf, t_google_rank).where(t_google_rank.runId.in(runIds)).execute();
            new SQLDeleteClause(con, dbTplConf, t_serp).where(t_serp.runId.in(runIds)).execute();
            new SQLDeleteClause(con, dbTplConf, t_target_summary).where(t_target_summary.runId.in(runIds)).execute();
            return new SQLDeleteClause(con, dbTplConf, t_run).where(t_run.id.in(runIds)).execute();
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return 0;
        
    }
}
