/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BaseDB {

    @Inject
    public MigrationDB migration;

    @Inject
    public ConfigDB config;
    
    @Inject
    public GroupDB group;
    
    @Inject
    public RunDB run;
    
    @Inject
    public UserDB user;
    
    @Inject
    public EventDB event;
    
    @Inject
    public ProxyDB proxy;
    
}
