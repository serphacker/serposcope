/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;


public class GoogleRank {
    
    public final static int UNRANKED = Short.MAX_VALUE;
    
    public final int runId;
    public final int groupId;
    public final int googleTargetId;
    public final int googleSearchId;
    public final short rank;
    public final short previousRank;
    public final short hits;
    public final short diff;
    public final String url;
    
    public GoogleRank(int runId, int groupId, int googleTargetId, int googleSearchId, int rank, int previousRank, long hits, String url) {
        if(previousRank == 0){
            previousRank = GoogleRank.UNRANKED;
        }
        if(rank == 0){
            rank = GoogleRank.UNRANKED;
        }
        this.runId = runId;
        this.groupId = groupId;
        this.googleTargetId = googleTargetId;
        this.googleSearchId = googleSearchId;
        this.rank = (short)rank;
        this.previousRank = (short)previousRank;
        this.hits = (short)hits;
        this.diff = (short)(rank - previousRank);
        if(url != null && url.length() >= 256){
            url = url.substring(0, 256);
        }
        this.url = url;
    }
    
    public String getDisplayDiff(){
        if(previousRank == UNRANKED && rank != UNRANKED){
            return "in";
        }
        if(previousRank != UNRANKED && rank == UNRANKED){
            return "out";
        }
        int diff = previousRank - rank;
        if(diff == 0){
            return "=";
        }
        if(diff > 0){
            return "+" + diff;
        }
        return Integer.toString(diff);
    }    
    
}
