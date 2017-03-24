/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Singleton;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.querybuilder.QGoogleSerp;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.function.Consumer;
import javax.sql.rowset.serial.SerialBlob;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

@Singleton
public class GoogleSerpDB extends AbstractDB {
    
    QGoogleSerp t_serp = QGoogleSerp.googleSerp;
    
    LZ4Factory factory = LZ4Factory.fastestInstance();
    LZ4Compressor compressor = factory.fastCompressor();
    LZ4FastDecompressor decompressor = factory.fastDecompressor();    

    public boolean insert(GoogleSerp serp){
        boolean inserted = false;
        
        try(Connection con = ds.getConnection()){
            
            inserted = new SQLInsertClause(con, dbTplConf, t_serp)
                .set(t_serp.runId, serp.getRunId())
                .set(t_serp.googleSearchId, serp.getGoogleSearchId())
                .set(t_serp.runDay, Timestamp.valueOf(serp.getRunDay()))
                .set(t_serp.serp, new SerialBlob(compress(serp.getSerializedEntries())))
                .execute() == 1;

        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        
        return inserted;
    }
    
    public void deleteByRun(int runId){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_serp)
                .where(t_serp.runId.eq(runId))
                .execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }
    
    public void deleteBySearch(int searchId){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_serp)
                .where(t_serp.googleSearchId.eq(searchId))
                .execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }    
    
    public void wipe(){
        try(Connection con = ds.getConnection()){
            new SQLDeleteClause(con, dbTplConf, t_serp)
                .execute();
        } catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }
    
    public GoogleSerp get(int runId, int googleSearchId){
        GoogleSerp serp = null;
        try(Connection con = ds.getConnection()){
            
            Tuple tuple = new SQLQuery<Void>(con, dbTplConf)
                .select(t_serp.all())
                .from(t_serp)
                .where(t_serp.runId.eq(runId))
                .where(t_serp.googleSearchId.eq(googleSearchId))
                .fetchFirst();
            
            serp = fromTuple(tuple);
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
        return serp;
    }
    
//    public void stream(Collection<Integer> runs, int googleSearchId, Consumer<GoogleSerp> callback){
//        try(Connection con = ds.getConnection()){
//            
//            CloseableIterator<Tuple> iterate = new SQLQuery<Void>(con, dbTplConf)
//                .select(t_serp.all())
//                .from(t_serp)
//                .where(t_serp.runId.in(runs))
//                .where(t_serp.googleSearchId.eq(googleSearchId))
//                .orderBy(t_serp.runId.asc())
//                .iterate();
//            
//            while(iterate.hasNext()){
//                GoogleSerp serp = fromTuple(iterate.next());
//                callback.accept(serp);
//            }
//            
//        }catch(Exception ex){
//            LOG.error("SQL error", ex);
//        }
//    }    
    
    public void stream(Integer firstRun, Integer lastRun, int googleSearchId, Consumer<GoogleSerp> callback){
        try(Connection con = ds.getConnection()){
            
            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_serp.all())
                .from(t_serp);
            
            if(firstRun != null){
                query.where(t_serp.runId.goe(firstRun));
            }
            
            if(lastRun != null){
                query.where(t_serp.runId.loe(lastRun));
            }
            
            CloseableIterator<Tuple> iterate = query
                .where(t_serp.googleSearchId.eq(googleSearchId))
                .orderBy(t_serp.runId.asc())
                .iterate();
            
            while(iterate.hasNext()){
                GoogleSerp serp = fromTuple(iterate.next());
                callback.accept(serp);
            }
            
        }catch(Exception ex){
            LOG.error("SQL error", ex);
        }
    }
    
    protected GoogleSerp fromTuple(Tuple tuple) throws Exception{
        if(tuple == null){
            return null;
        }
        
        GoogleSerp serp = new GoogleSerp(tuple.get(t_serp.runId), tuple.get(t_serp.googleSearchId), tuple.get(t_serp.runDay).toLocalDateTime());
        Blob blob = tuple.get(t_serp.serp);
        if(blob != null){
            byte[] compressedData = blob.getBytes(1,(int)blob.length());
            serp.setSerializedEntries(decompress(compressedData));
        }
        
        return serp;
        
    }
    
    
    protected byte[] compress(byte[] data){
        if(data == null || data.length < 1){
            return null;
        }
        
        int decompressedLength = data.length;
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] tmp = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, decompressedLength, tmp, 0, maxCompressedLength);

        byte[] compressed = new byte[4 + compressedLength];
        ByteBuffer.wrap(compressed, 0, 4).putInt(decompressedLength);
        System.arraycopy(tmp, 0, compressed, 4, compressedLength);
        
        return compressed;
    }    
    
    protected byte[] decompress(byte[] compressed){
        if(compressed == null || compressed.length < 5){
            return null;
        }
        
        int decompressedLength = ByteBuffer.wrap(compressed, 0, 4).getInt();
        byte[] decompressed = new byte[decompressedLength];
        decompressor.decompress(compressed, 4, decompressed, 0, decompressedLength);
        
        return decompressed;
    }    
    
    
    
}
