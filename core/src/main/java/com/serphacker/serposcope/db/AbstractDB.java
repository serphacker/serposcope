/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db;

import com.querydsl.sql.Configuration;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractDB {
    
    protected final static Logger LOG = LoggerFactory.getLogger(AbstractDB.class);
    
    @Inject
    protected DataSource ds;
    
    @Inject
    protected Configuration dbTplConf;

}
