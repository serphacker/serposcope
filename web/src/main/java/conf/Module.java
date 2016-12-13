/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package conf;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.querydsl.sql.Configuration;
import com.serphacker.serposcope.di.CaptchaSolverFactory;
import com.serphacker.serposcope.di.GoogleScraperFactory;
import com.serphacker.serposcope.di.db.ConfigurationProvider;
import com.serphacker.serposcope.di.db.DataSourceProvider;
import com.serphacker.serposcope.di.TaskFactory;
import com.serphacker.serposcope.models.base.Config;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import com.serphacker.serposcope.scraper.google.scraper.RandomGScraper;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.task.TaskManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import javax.sql.DataSource;
import ninja.template.TemplateEngineFreemarkerReverseRouteHelper;
import ninja.utils.Crypto;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaModeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.helpers.CryptoOverride;
import serposcope.helpers.TemplateEngineFreemarkerReverseRouteHelperSerposcope;
import ninja.conf.FrameworkModule;
import ninja.conf.NinjaClassicModule;
import serposcope.services.CronService;
//import serposcope.services.CronSrv;
import ninja.utils.NinjaProperties;



@Singleton
public class Module extends FrameworkModule {
    
    private static final Logger LOG = LoggerFactory.getLogger(Module.class);
    
    SerposcopeConf conf;
    
    private final NinjaProperties ninjaProperties;

    public Module(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }
    
    @Override
    protected void configure() {
        beforeModuleConfigure();
        
        install(
            new NinjaClassicModule(ninjaProperties)
                .freemarker(true)
                .json(true)
                .xml(false)
                .postoffice(false)
                .cache(false)
                .migrations(false)
                .jpa(false)
        );
        
        bind(SerposcopeConf.class).toInstance(conf);
//        bind(Scheduler.class);
        bind(CronService.class);
        bind(Crypto.class).to(CryptoOverride.class);
        bind(TemplateEngineFreemarkerReverseRouteHelper.class).to(TemplateEngineFreemarkerReverseRouteHelperSerposcope.class);
        bind(Configuration.class).toProvider(new ConfigurationProvider(conf.dbUrl)).in(Singleton.class);
        bind(DataSource.class).toProvider(new DataSourceProvider(conf.dbUrl,conf.dbDebug)).in(Singleton.class);
        bind(TaskManager.class).in(Singleton.class);
        install(new FactoryModuleBuilder().build(TaskFactory.class));
        
        // debugging
        if(NinjaModeHelper.determineModeFromSystemPropertiesOrProdIfNotSet().equals(NinjaMode.dev)){
            bind(CaptchaSolverFactory.class).toInstance((CaptchaSolverFactory) (Config config) -> null);
            bind(GoogleScraperFactory.class).toInstance((GoogleScraperFactory) (ScrapClient http, CaptchaSolver solver) -> new RandomGScraper(http, solver));
        }
    }
    
    protected void beforeModuleConfigure(){
        conf = new SerposcopeConf(System.getProperty("serposcope.conf"));
        conf.configure();
        LOG.info("now logging to " + conf.logdir + "/" + LocalDate.now() + ".log");
        configureLogback();
        conf.logEnv();
    }
    
    protected void configureLogback(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(context);
        appender.setName("FILE");

        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setMaxHistory(10);
        rollingPolicy.setFileNamePattern(conf.logdir + "/%d.log");
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.setPattern("[%d] [%t] %-5p %c{5} - %m%n");
        encoder.start();
        appender.setEncoder(encoder);
        
        appender.start();
        
        Appender<ILoggingEvent> stdoutAppender = context.getLogger("root").getAppender("STDOUT");
        if(stdoutAppender != null){
            context.getLogger("root").detachAppender("STDOUT");
            stdoutAppender.stop();
        }
        context.getLogger("root").addAppender(appender);
    }
    

}
