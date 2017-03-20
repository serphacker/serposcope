/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.google.GoogleDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleTarget;
import conf.SerposcopeConf;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.Param;
import ninja.session.FlashScope;
import ninja.utils.ResponseStreams;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.HttpOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.filters.AdminFilter;
import serposcope.filters.XSRFFilter;

@FilterWith(AdminFilter.class)
@Singleton
public class LogController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(LogController.class);

    @Inject
    Router router;

    @Inject
    SerposcopeConf conf;
    
    @Inject
    BaseDB basedb;
    
    @Inject
    GoogleDB googledb;

    public Result logs() {
        String[] logs = getLogFiles();
        return Results.ok().render("logs", logs);
    }

    public Result viewLog(Context context, @Param("log") String log, @Param("ano") String ano) {
        String[] logs = getLogFiles();

        if (log == null || !Arrays.asList(logs).contains(log)) {
            context.getFlashScope().error("admin.log.invalidLogFile");
            return Results.redirect(router.getReverseRoute(LogController.class, "logs"));
        }

        String path = conf.logdir + "/" + log;

        if ("1".equals(ano)) {
            
            Map<String,String> map = getObfuscateMap();
            
            return Results
                .text()
                .render((ctx, res) -> {
                    ResponseStreams responseStreams = context.finalizeHeaders(res);
                    try (
                        Writer writer = responseStreams.getWriter();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
                    ) {
                        writer.append("############################################################################\n");
                        writer.append("# WARNING : log anonymization will not obfuscate deleted keywords and data #\n");
                        writer.append("############################################################################\n");
                        writer.append("\n");
                        reader.lines().forEach((String line) -> {
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                if(!StringUtils.isEmpty(entry.getKey())){
                                    line = line.replace(entry.getKey(), entry.getValue());
                                }
                            }
                            try {writer.append(line).append("\n");} catch (IOException ex) {}
                        });
                    } catch (IOException ex) {
                        LOG.error("view log", ex);
                    }
                });
        } else {
            return Results
                .text()
                .render((ctx, res) -> {
                    ResponseStreams responseStreams = context.finalizeHeaders(res);
                    try (OutputStream os = responseStreams.getOutputStream()) {
                        Files.copy(new File(path).toPath(), os);
                    } catch (IOException ex) {
                        LOG.error("view log", ex);
                    }
                });
        }
    }
    
    protected String[] getLogFiles() {
        File logDir = new File(conf.logdir);

        String[] logs = logDir.list();
        if (logs == null) {
            logs = new String[0];
        } else {
            Arrays.sort(logs, Collections.reverseOrder());
        }

        return logs;
    }
    
    protected Map<String, String> getObfuscateMap(){
        Map<String,String> obfuscate = new HashMap<>();
        
        List<Proxy> proxies = basedb.proxy.list();
        for (Proxy proxy : proxies) {
            obfuscate.put(proxy.getIp(), "#OBF#proxy-ip#");
            obfuscate.put(proxy.getUsername(), "#OBF#proxy-user#");
            obfuscate.put(proxy.getPassword(), "#OBF#proxy-pass#");
        }
        
        List<Group> groups = basedb.group.list();
        for (Group group : groups) {
            obfuscate.put(group.getName(), "#OBF#group-" + group.getId() + "#");
        }
        
        List<GoogleSearch> searches = googledb.search.list();
        for (GoogleSearch search : searches) {
            obfuscate.put(search.getKeyword(), "#OBF#search-" + search.getId() + "#");
            try {
                obfuscate.put(URLEncoder.encode(search.getKeyword(), "UTF-8"), "#OBF#search-" + search.getId() + "#");
            } catch (UnsupportedEncodingException ex) {
            }
        }
        
        List<GoogleTarget> targets  = googledb.target.list();
        for (GoogleTarget target : targets) {
            obfuscate.put(target.getName(), "#OBF#target-" + target.getId() + "#");
        }

        return getSortedMapByReverseLength(obfuscate);
    }
    
    protected Map<String,String> getSortedMapByReverseLength(Map<String,String> map){
        Map<String,String> sortedMap = new TreeMap<>((String o1, String o2) -> {
            if(o2 == null && o1 != null){
                return -1;
            }
            if(o2 != null && o1 == null){
                return 1;
            }
            if(o1 == null && o2 == null){
                return 0;
            }            
            int lenDiff = o2.length() - o1.length();
            if(lenDiff == 0){
                return o1.compareTo(o2);
            }
            return lenDiff;
        });
        sortedMap.putAll(map);
        return sortedMap;
    }

}
