/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di.db;

import com.google.inject.Provider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 *
 * @author admin
 */
public class ConfigurationProvider implements Provider<Configuration> {
    
    String dbUrl;

    public ConfigurationProvider(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    
    @Override
    public Configuration get() {
        SQLTemplates template = null;
        if(dbUrl.startsWith("jdbc:h2:")){
            template = H2Templates.builder().quote().build();
        } else {
            template = MySQLTemplates.builder().quote().build();
        }
        Configuration conf = new Configuration(template);
        return conf;
    }
    
}
