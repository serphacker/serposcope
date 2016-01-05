/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

package com.serphacker.serposcope.models.google;

import java.time.LocalDateTime;


public class GoogleBest {
    
    public final static GoogleBest NOBEST = new GoogleBest(0, 0, 0, (short)GoogleRank.UNRANKED, null, null);

    int groupId;
    int googleTargetId;
    int googleSearchId;
    short rank;
    LocalDateTime runDay;
    String url;

    public GoogleBest(int groupId, int googleTargetId, int googleSearchId, int rank, LocalDateTime runDay, String url) {
        this.groupId = groupId;
        this.googleTargetId = googleTargetId;
        this.googleSearchId = googleSearchId;
        this.rank = (short)rank;
        this.runDay = runDay;
        this.url = url;
    }
    
    public GoogleBest(){
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGoogleTargetId() {
        return googleTargetId;
    }

    public void setGoogleTargetId(int googleTargetId) {
        this.googleTargetId = googleTargetId;
    }

    public int getGoogleSearchId() {
        return googleSearchId;
    }

    public void setGoogleSearchId(int googleSearchId) {
        this.googleSearchId = googleSearchId;
    }

    public short getRank() {
        return rank;
    }

    public void setRank(short rank) {
        this.rank = rank;
    }

    public LocalDateTime getRunDay() {
        return runDay;
    }

    public void setRunDay(LocalDateTime runDay) {
        this.runDay = runDay;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    
    
}
