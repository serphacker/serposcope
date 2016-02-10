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
import com.serphacker.serposcope.models.base.User;
import conf.SerposcopeConf;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import ninja.AuthenticityFilter;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.params.Param;
import ninja.session.FlashScope;
import ninja.utils.ResponseStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.filters.AdminFilter;
import serposcope.filters.XSRFFilter;
import serposcope.helpers.Validator;

@FilterWith(AdminFilter.class)
@Singleton
public class AdminController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Inject
    SerposcopeConf conf;

    public Result admin() {
        return Results
            .ok();
    }

    public Result sysconfig() {

        StringBuilder builder = new StringBuilder(conf.dumpEnv());

        Properties props = System.getProperties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        return Results
            .ok()
            .text()
            .render(builder.toString());
    }

    public Result stackdump(Context context) {

        return Results
            .contentType("text/plain")
            .render((ctx, res) -> {
                ResponseStreams responseStreams = context.finalizeHeaders(res);
                try (
                    PrintWriter writer = new PrintWriter(responseStreams.getOutputStream());) {
                    final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                    final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
                    for (ThreadInfo threadInfo : threadInfos) {
                        writer.append('"');
                        writer.append(threadInfo.getThreadName());
                        writer.append("\" ");
                        final Thread.State state = threadInfo.getThreadState();
                        writer.append("\n   java.lang.Thread.State: ");
                        writer.append(state.toString());
                        final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
                        for (final StackTraceElement stackTraceElement : stackTraceElements) {
                            writer.append("\n        at ");
                            writer.append(stackTraceElement.toString());
                        }
                        writer.println("\n");
                    }
                } catch (IOException ex) {
                    LOG.error("stackdump", ex);
                }
            });
    }

}
