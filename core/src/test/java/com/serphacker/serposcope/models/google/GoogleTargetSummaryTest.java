/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleTargetSummaryTest {
    
    public GoogleTargetSummaryTest() {
    }
    
    Random r = new Random();

    @Test
    public void testTop() {
        List<GoogleRank> ranks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ranks.add(new GoogleRank(0, 0, 0, i, i, 0, null));
        }
        Collections.shuffle(ranks);
        
        GoogleTargetSummary summary = new GoogleTargetSummary();
        
        for (GoogleRank rank : ranks) {
            summary.topRanks.add(rank);
        }
        
        assertEquals(5, summary.topRanks.size());
        ReflectionAssert.assertLenientEquals(new HashSet<>(summary.topRanks), 
            new HashSet<>(Arrays.asList(
                new GoogleRank(0, 0, 0, 1, 1, 0, null),
                new GoogleRank(0, 0, 0, 2, 2, 0, null),
                new GoogleRank(0, 0, 0, 3, 3, 0, null),
                new GoogleRank(0, 0, 0, 4, 4, 0, null),
                new GoogleRank(0, 0, 0, 5, 5, 0, null)
            ))
        );
    }
    
    @Test
    public void test(){
        List<GoogleRank> ranks = new ArrayList<>();
        GoogleTargetSummary summary = new GoogleTargetSummary();        
        
        for (int i = 0; i < 100; i++) {
            ranks.add(new GoogleRank(0, 0, 0, 0, r.nextInt(100), r.nextInt(100), null));
        }
        Collections.shuffle(ranks);
                
        for (GoogleRank rank : ranks) {
            summary.addRankCandidat(rank);
        }
        
        System.out.println("TOP-RANKS");
        for (GoogleRank rank : summary.getTopRanks()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }        
        
        System.out.println("TOP-IMPROVEMENTS");
        for (GoogleRank rank : summary.getTopImprovements()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }
        
        System.out.println("TOP-LOSTS");
        for (GoogleRank rank : summary.getTopLosts()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }        
    }
    
    
    @Test
    public void testMT() throws Exception{
        final List<GoogleRank> ranks = new ArrayList<>();
        final GoogleTargetSummary summary = new GoogleTargetSummary();        
        
        for (int i = 0; i < 100; i++) {
            ranks.add(new GoogleRank(0, 0, 0, 0, r.nextInt(100), r.nextInt(100), null));
        }
        Collections.shuffle(ranks);
                
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threads.length; i++) {
            final int j = i;
            threads[i] = new Thread(() -> {summary.addRankCandidat(ranks.get(j));}, "th-" + j);
        }
        
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }        
        
        System.out.println("TOP-RANKS");
        for (GoogleRank rank : summary.getTopRanks()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }        
        
        System.out.println("TOP-IMPROVEMENTS");
        for (GoogleRank rank : summary.getTopImprovements()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }
        
        System.out.println("TOP-LOSTS");
        for (GoogleRank rank : summary.getTopLosts()) {
            System.out.println("PREV:" + rank.previousRank + " => NOW:" + rank.rank + "|DIFF:" + rank.diff);
        }        
    }    
    
}
