/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GoogleDB {
    
    @Inject
    public GoogleOptionsDB options;
    
    @Inject
    public GoogleSearchDB search;
    
    @Inject
    public GoogleSerpDB serp;
    
    @Inject
    public GoogleTargetDB target;
    
    @Inject
    public GoogleRankDB rank;
    
    @Inject
    public GoogleTargetSummaryDB targetSummary;
    
    @Inject
    public GoogleSerpRescanDB serpRescan;
    
}
