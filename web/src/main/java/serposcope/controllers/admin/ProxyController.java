/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.admin;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Proxy;
import com.serphacker.serposcope.task.proxy.ProxyChecker;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.controllers.BaseController;
import serposcope.filters.AdminFilter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import ninja.Context;
import ninja.i18n.Messages;
import ninja.params.Param;
import ninja.params.Params;
import ninja.session.FlashScope;
import serposcope.filters.XSRFFilter;
import serposcope.helpers.Validator;

@FilterWith(AdminFilter.class)
@Singleton
public class ProxyController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    @Inject
    BaseDB baseDB;

    @Inject
    Router router;
    
    
    final Object lockProxyChecker = new Object();
    ProxyChecker currentProxyChecker = null;
    

    public Result proxies() {
        
        boolean running = false;
        int progress = 0;
        synchronized(lockProxyChecker){
            if(currentProxyChecker != null){
                running = currentProxyChecker.isAlive();
                progress = currentProxyChecker.getProgress();
            }
        }
        
        return Results.ok()
            .render("running", running)
            .render("progress", progress)
            .render("proxies", baseDB.proxy.list());
    }
    
    @Inject
    Messages msg;
    
    @FilterWith(XSRFFilter.class)
    public Result add(
        Context context,
        @Param("proxies") String proxies
    ){
        FlashScope flash = context.getFlashScope();
        
        if(Validator.isEmpty(proxies)){
            flash.error("error.invalidParameters");
            return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
        }
        
        List<Proxy> dbproxies = new ArrayList<>();
        String[] lines = proxies.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("(^\\s+)|(\\s+$)", "");
            
            if(line.isEmpty()){
                continue;
            }
            
            Proxy proxy = new Proxy();
            
            String[] split = line.split("\\#");
            if(split.length < 2){
                flash.error(msg.get("admin.proxy.error.invalidFormat", context, Optional.absent(), line).or(""));
                return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
            }
            
            if(!Validator.isIPv4(split[1])){
                flash.error(msg.get("admin.proxy.error.invalidIPv4", context, Optional.absent(), line).or(""));
                return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));                
            }
            
            proxy.setIp(split[1]);
            
            switch(split[0]){
                
                case "socks":
                case "http":
                    proxy.setType("http".equals(split[0]) ? Proxy.Type.HTTP : Proxy.Type.SOCKS);
                    try {
                        proxy.setPort(Integer.parseInt(split[2]));
                    } catch(Exception ex){
                        flash.error(msg.get("admin.proxy.error.invalidPort", context, Optional.absent(), line).or(""));
                        return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
                    }
                    if(split.length > 3 && !split[3].isEmpty()){
                        proxy.setUsername(split[3]);
                    }
                    if(split.length > 4 && !split[4].isEmpty()){
                        proxy.setPassword(split[4]);
                    }
                    break;
                    
                case "bind":
                    proxy.setType(Proxy.Type.BIND);
                    break;
                    
                default:
                    flash.error(msg.get("admin.proxy.error.invalidType", context, Optional.absent(), line).or(""));
                    return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
                    
            }
            
            dbproxies.add(proxy);
        }
        
        baseDB.proxy.insert(dbproxies);

        flash.success("admin.proxy.proxyAdded");
        return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
    }
    
    @FilterWith(XSRFFilter.class)
    public Result delete(
        Context context,
        @Params("id[]") String[] ids
    ){
        FlashScope flash = context.getFlashScope();
        List<Integer> keys = null;
        try {
            keys = Arrays.stream(ids).map(Integer::parseInt).collect(Collectors.toList());
        }catch(Exception ex){
            flash.error("error.invalidParameters");
            return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));            
        }
        
        if(keys != null){
            baseDB.proxy.delete(keys);
            flash.put("warning","admin.proxy.proxyDeleted");
        }
        return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
    } 
    
    @FilterWith(XSRFFilter.class)
    public Result deleteInvalid(Context context){
        FlashScope flash = context.getFlashScope();
        baseDB.proxy.deleteByStatus(Proxy.Status.ERROR);
        flash.put("warning","admin.proxy.proxyDeleted");
        return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
    }        
    
    @FilterWith(XSRFFilter.class)
    public Result abortCheck(Context context){
        synchronized(lockProxyChecker){
            if(currentProxyChecker != null && currentProxyChecker.isAlive()){
                currentProxyChecker.interrupt();
            }
        }
        
        context.getFlashScope().put("warning","admin.proxy.checkTaskAborted");
        return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));              
    }
    
    @FilterWith(XSRFFilter.class)
    public Result startCheck(Context context){
        
        FlashScope flash = context.getFlashScope();
        
        synchronized(lockProxyChecker){
            
            if(currentProxyChecker != null && currentProxyChecker.isAlive()){
                flash.error("admin.proxy.checkTaskAlreadyRunning");
                return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));
            }
            
            currentProxyChecker = new ProxyChecker(baseDB);
            currentProxyChecker.start();

            flash.success("admin.proxy.checkTaskStarted");
            return Results.redirect(router.getReverseRoute(ProxyController.class, "proxies"));            
        }
    }

}
