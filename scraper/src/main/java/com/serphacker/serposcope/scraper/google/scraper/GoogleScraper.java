/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google.scraper;

import com.serphacker.serposcope.scraper.captcha.Captcha;
import com.serphacker.serposcope.scraper.captcha.CaptchaImage;
import com.serphacker.serposcope.scraper.captcha.solver.CaptchaSolver;
import static com.serphacker.serposcope.scraper.google.GoogleDevice.MOBILE;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult;
import com.serphacker.serposcope.scraper.google.GoogleScrapResult.Status;
import com.serphacker.serposcope.scraper.google.GoogleScrapSearch;
import com.serphacker.serposcope.scraper.http.ScrapClient;
import com.serphacker.serposcope.scraper.http.proxy.DirectNoProxy;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * not thread safe
 * @author admin
 */
public class GoogleScraper {
    
    public final static int MAX_RETRY = 3;
    
    final static BasicClientCookie NCR_COOKIE = new BasicClientCookie("PREF", "ID=1111111111111111:CR=2");
    static {
        NCR_COOKIE.setDomain("google.com");
        NCR_COOKIE.setPath("/");
        NCR_COOKIE.setAttribute(ClientCookie.PATH_ATTR, "/");
        NCR_COOKIE.setAttribute(ClientCookie.DOMAIN_ATTR, ".google.com");
    }
    
    public final static String DEFAULT_DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0";
    public final static String DEFAULT_MOBILE_UA = "Mozilla/5.0 (Android; Mobile; rv:37.0) Gecko/37.0 Firefox/37.0";
    
    private static final Logger LOG = LoggerFactory.getLogger(GoogleScraper.class);

    protected ScrapClient http;
    protected CaptchaSolver solver;
    Random random = new Random();
    
    Document lastSerpHtml = null;
    int captchas=0;

    public GoogleScraper(ScrapClient client, CaptchaSolver solver) {
//        this.search = search;
        this.http = client;
        this.solver = solver;
    }
    
    public GoogleScrapResult scrap(GoogleScrapSearch search) throws InterruptedException {
        lastSerpHtml = null;
        captchas = 0;
        List<String> urls = new ArrayList<>();
        prepareHttpClient(search);
        
        String referrer = "https://" + buildHost(search) + "/";
        for (int page = 0; page < search.getPages(); page++) {
            
            if(Thread.interrupted()){
                throw new InterruptedException();
            }
            
            String url = buildRequestUrl(search, page);
            
            Status status = null;
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                
                LOG.debug("GET {} via {} try {}", url, http.getProxy() == null ? new DirectNoProxy() : http.getProxy(), retry+1);
                
                status = downloadSerp(url, referrer, search);
                if(status == Status.OK){
                    status = parseSerp(urls);
                    if(status == Status.OK){
                        break;
                    }
                }
                
                if(!isRetryableStatus(status)){
                    break;
                }
            }
            
            if(status != Status.OK){
                return new GoogleScrapResult(status, urls, captchas);
            }
            
            if(!hasNextPage()){
                break;
            }
            
            long pause = search.getRandomPagePauseMS();
            if(pause > 0){
                try {
                    LOG.trace("sleeping {} milliseconds", pause);
                    Thread.sleep(pause);
                } catch(InterruptedException ex){
                    throw ex;
                }                
            }
        }
        return new GoogleScrapResult(Status.OK, urls, captchas);
    }
    
    protected void prepareHttpClient(GoogleScrapSearch search){
        if(MOBILE.equals(search.getDevice())){
            http.setUseragent(DEFAULT_MOBILE_UA);
        } else {
            http.setUseragent(DEFAULT_DESKTOP_UA);
        }
        
        if("com".equals(search.getTld())){
            http.addCookie(NCR_COOKIE);
        }

        String hostname = "www.google.com";
        if(search.getTld() != null && !search.getTld().isEmpty()) {
            hostname = "www.google." + search.getTld();
        }
        
        http.removeRoutes();
        if(search.getDatacenter() != null && !search.getDatacenter().isEmpty()){
            http.setRoute(new HttpHost(hostname, -1, "https"), new HttpHost(search.getDatacenter(), -1, "https"));
        }
    }
    
    protected boolean isRetryableStatus(Status status){
        switch(status){
            case ERROR_CAPTCHA_INCORRECT:
            case ERROR_NETWORK:
                return true;
            default:
                return false;
        }
    }
    
    protected Status downloadSerp(String url, String referrer, GoogleScrapSearch search){
        int status = http.get(url, referrer);
        switch(status){
            case 200:
                return Status.OK;

            case 302:
                ++captchas;
                return handleCaptchaRedirect(http.getResponseHeader("location"));
        }
        
        return Status.ERROR_NETWORK;
    }
    
    protected Status parseSerp(List<String> urls){
        String html = http.getContentAsString();
        if(html == null || html.isEmpty()){
            return Status.ERROR_NETWORK;
        }
        
        lastSerpHtml = Jsoup.parse(html);
        if(lastSerpHtml == null){
            return Status.ERROR_NETWORK;
        }
        
        Elements resultDivs = lastSerpHtml.getElementsByClass("g");
        for (Element resultDiv : resultDivs) {
            String link = extractLink(resultDiv.getElementsByTag("a").first());
            if(link != null){
                urls.add(link);
            }
        }
        
        return Status.OK;
    }
    
    protected String extractLink(Element element){
        if(element == null){
            return null;
        }
        String attr = element.attr("href");
        if(attr == null){
            return null;
        }
        if(attr.startsWith("http://") || attr.startsWith("https://")){
            return attr;
        }
        if(attr.startsWith("/url?")){
            try {
                List<NameValuePair> parse = URLEncodedUtils.parse(attr.substring(5), Charset.forName("utf-8"));
                Map<String,String> map = parse.stream().collect(Collectors.toMap(NameValuePair::getName,NameValuePair::getValue));
                return map.get("q");
            }catch(Exception ex){
                return null;
            }
        }
        return null;
    }
    
    protected boolean hasNextPage(){
        if(lastSerpHtml == null){
            return false;
        }
        
        Elements navEnd = lastSerpHtml.getElementsByClass("navend");
        if(navEnd.size() < 2){
            return false;
        }
        return !navEnd.last().getElementsByTag("a").isEmpty();
    }
    
    protected String buildRequestUrl(GoogleScrapSearch search, int page){
        String url = "https://";
        try {
            url += buildHost(search) + "/search?q=" + URLEncoder.encode(search.getKeyword(), "utf-8");
        } catch(UnsupportedEncodingException ex){
            url += buildHost(search) + "/search?q=" + search.getKeyword();
        }
        
        String uule = buildUule(search.getLocal());
        if(uule != null){
            url += "&uule=" + uule;
        }

        if(search.getCustomParameters() != null && !search.getCustomParameters().isEmpty()){
            if(!search.getCustomParameters().startsWith("&")){
                url += "&";
            }
            url += search.getCustomParameters();
        }

        if(search.getResultPerPage() != 10){
            url+="&num=" + search.getResultPerPage();
        }

        if(page > 0){
            url+="&start=" + (page*search.getResultPerPage());
        }
        return url;
    }
    
    protected String buildHost(GoogleScrapSearch search){
        if(search.getTld() != null && !search.getTld().isEmpty()) {
            return "www.google." + search.getTld();
        }
        return "www.google.com";
    }
    
    private final static String UULE_LENGTH = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    protected String buildUule(String location){
        if(location == null || location.isEmpty()){
            return null;
        }
        
        byte[] locationArray = location.getBytes();
        if(locationArray.length+1 > UULE_LENGTH.length()){
            LOG.warn("unencodable uule location, length is too long {}", location);
            return null;
        }
        
        return "w+CAIQICI" + 
            UULE_LENGTH.charAt(locationArray.length) +
            Base64.getEncoder().encodeToString(locationArray);
    }    
    
    final static Pattern PATTERN_CAPTCHA_ID = Pattern.compile("/sorry/image\\?id=([0-9]+)&?");
    protected Status handleCaptchaRedirect(String captchaRedirect){
        if(captchaRedirect == null || !captchaRedirect.contains("?continue=")){
            return Status.ERROR_NETWORK;
        }
        LOG.debug("captcha form detected via {}", http.getProxy() == null ? new DirectNoProxy() : http.getProxy());
        
        int status = http.get(captchaRedirect);
        if(status == 403){
            return Status.ERROR_IP_BANNED;
        }
        
        if(solver == null){
            return Status.ERROR_CAPTCHA_NO_SOLVER;
        }
        
        String content = http.getContentAsString();
        if(content == null){
            return Status.ERROR_NETWORK;
        }
        
        String imageSrc = null;
        Document captchaDocument = Jsoup.parse(content, captchaRedirect);
        Elements elements = captchaDocument.getElementsByTag("img");
        for (Element element : elements) {
            String src = element.attr("abs:src");
            if(src != null && src.contains("/sorry/image")){
                imageSrc = src;
            }
        }
        
        if(imageSrc == null){
            LOG.debug("can't find captcha img tag");
            return Status.ERROR_NETWORK;
        }
        
        Element form = captchaDocument.getElementsByTag("form").first();
        if(form == null){
            LOG.debug("can't find captcha form");
            return Status.ERROR_NETWORK;            
        }
        
        String continueValue = null;
        String captchaId = null;
        String action = form.attr("abs:action");
        String qValue = null;
        
        Element elementCaptchaId = form.getElementsByAttributeValue("name", "id").first();
        if(elementCaptchaId != null){
            captchaId = elementCaptchaId.attr("value");
        }
        Element elementContinue = form.getElementsByAttributeValue("name", "continue").first();
        if(elementContinue != null){
            continueValue = elementContinue.attr("value");
        }
        Element elementQ = form.getElementsByAttributeValue("name", "q").first();
        if(elementQ != null){
            qValue = elementQ.attr("value");
        }
        
        
        if(action == null || captchaId == null || continueValue == null){
            LOG.debug("invalid captcha form");
            return Status.ERROR_NETWORK;
        }
        
        int imgStatus = http.get(imageSrc, captchaRedirect);
        if(imgStatus != 200 || http.getContent() == null){
            LOG.debug("can't download captcha image {} (status code = {})", imageSrc, imgStatus);
            return Status.ERROR_NETWORK;
        }
        
        CaptchaImage captcha = new CaptchaImage(new byte[][]{http.getContent()});
        boolean solved = solver.solve(captcha);
        if(!solved || !Captcha.Status.SOLVED.equals(captcha.getStatus())){
            LOG.error("solver can't resolve captcha (overload ?) error = {}", captcha.getError());
            return Status.ERROR_CAPTCHA_INCORRECT;
        }
        LOG.debug("Got captcha response {} in {} seconds", captcha.getResponse(), captcha.getSolveDuration()/1000l);
        
        try {
            action += "?continue=" + URLEncoder.encode(continueValue, "utf-8");
        }catch(Exception ex){}
        action += "&id=" + captchaId;
        action += "&captcha=" + captcha.getResponse();
        if(qValue != null){
            action += "&q=" + qValue;
        }
        
        int postCaptchaStatus = http.get(action, captchaRedirect);
        
        if(postCaptchaStatus == 302){
            String redirectOnSuccess = http.getResponseHeader("location");
            if(redirectOnSuccess.startsWith("http://")){
                redirectOnSuccess = "https://" + redirectOnSuccess.substring(7);
            }
            
            int redirect1status = http.get(redirectOnSuccess, captchaRedirect);
            if(redirect1status == 200){
                return Status.OK;
            }
            
            if(redirect1status == 302){
                if(http.get(http.getResponseHeader("location"), captchaRedirect) == 200){
                    return Status.OK;
                }
            }
        }
        
        if(postCaptchaStatus == 503){
            LOG.debug("reporting incorrect captcha (incorrect response = {})", captcha.getResponse());
            solver.reportIncorrect(captcha);
        }
        
        return Status.ERROR_CAPTCHA_INCORRECT;
    }    
    
    public ScrapClient getHttp() {
        return http;
    }

    public void setHttp(ScrapClient http) {
        this.http = http;
    }

    public CaptchaSolver getSolver() {
        return solver;
    }

    public void setSolver(CaptchaSolver solver) {
        this.solver = solver;
    }
    
}
