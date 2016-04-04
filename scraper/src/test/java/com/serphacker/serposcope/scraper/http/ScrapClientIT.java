/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicHeader;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author admin
 */
public class ScrapClientIT {
    
    public ScrapClientIT() {
    }
    
    @BeforeClass
    public static void before(){
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "false");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "fatal");
    }

    @Test
    public void testUserAgent() throws Exception {
        
        ScrapClient cli = new ScrapClient();
        
        {
            cli.get("http://httpbin.org/user-agent");
            Assert.assertThat(cli.getContentAsString(), CoreMatchers.containsString(ScrapClient.DEFAULT_USER_AGENT));
        }
        
        cli.setUseragent("lolua");
        {
            cli.get("http://httpbin.org/user-agent");
            Assert.assertThat(cli.getContentAsString(), CoreMatchers.containsString("lolua"));
        }
        
        {
            HttpGet getRequest = new HttpGet("http://httpbin.org/user-agent");
            getRequest.setHeader("user-agent", "xxxx");
            cli.request(getRequest);
            Assert.assertThat(cli.getContentAsString(), CoreMatchers.containsString("xxxx"));
        }
        
        {
            cli.get("http://httpbin.org/user-agent");
            Assert.assertThat(cli.getContentAsString(), CoreMatchers.containsString("lolua"));
        }
        
        cli.setUseragent(null);
        {
            cli.get("http://httpbin.org/user-agent");
            Assert.assertThat(cli.getContentAsString(), CoreMatchers.containsString("\"user-agent\": \"\""));
        }        
    }
    
    @Test
    public void testHeaders() throws Exception {
        ScrapClient cli = new ScrapClient();
//        cli.setProxy(new HttpProxy("127.0.0.1",8080));
        cli.setRequestHeader(new BasicHeader("host", "www.google.com"));
        System.out.println(cli.get("https://173.194.32.247/"));
        cli.setRequestHeader(new BasicHeader("host", "www.google.com"));
        System.out.println(cli.get("https://173.194.32.247/"));        
        
    }    
    
    
    @Test
    public void testCookies() throws Exception{
        ScrapClient cli = new ScrapClient();
        
        
        cli.get("http://httpbin.org/cookies/set?testcookie1=value1");
        
        cli.get("http://httpbin.org/cookies");
        assertThat(cli.getContentAsString(), CoreMatchers.containsString("\"testcookie1\": \"value1\""));
        
        
        cli.get("http://httpbin.org/cookies");
        assertThat(cli.getContentAsString(), CoreMatchers.containsString("\"testcookie1\": \"value1\""));
        
        List<Cookie> cookies = cli.getCookies();
        assertEquals("testcookie1", cookies.get(0).getName());
        assertEquals("value1", cookies.get(0).getValue());
        
        cli.clearCookies();
        assertTrue(cli.getCookies().isEmpty());
        
        cli.get("http://httpbin.org/cookies");
        assertThat(cli.getContentAsString(), CoreMatchers.containsString("\"cookies\": {}"));
    }
    
    @Test
    public void testRedirects() throws Exception {
        int status = 0;
        ScrapClient cli = new ScrapClient();

        for (int i = 0; i < 2; i++) {
            assertEquals(302, cli.get("https://httpbin.org/redirect/1"));
            
            cli.setMaxRedirect(1);
            assertEquals(200, cli.get("https://httpbin.org/redirect/1"));
            assertEquals(-1, cli.get("https://httpbin.org/redirect/2"));
            assertTrue(cli.getException().getCause() instanceof RedirectException);
            
            cli.setMaxRedirect(2);
            assertEquals(200, cli.get("https://httpbin.org/redirect/2"));
            assertEquals(-1, cli.get("https://httpbin.org/redirect/3"));
            assertTrue(cli.getException().getCause() instanceof RedirectException);
            
            cli.disableFollowRedirect();
            assertEquals(302, cli.get("https://httpbin.org/redirect/1"));
        }
    }
    
    @Test
    public void testMaxResponseLength() throws Exception{
        ScrapClient cli = new ScrapClient();
        int status = 0;
        
        // test with content length
        cli.setMaxResponseLength(ScrapClient.DEFAULT_MAX_RESPONSE_LENGTH);
        status = cli.get("https://httpbin.org/bytes/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        cli.setMaxResponseLength(1024);
        status = cli.get("https://httpbin.org/bytes/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        status = cli.get("https://httpbin.org/bytes/1025");
        assertEquals(-1, status);
        assertTrue(cli.getException() instanceof ResponseTooBigException);
        assertNull(cli.getContent());          
        
        // test chunked
        cli.setMaxResponseLength(ScrapClient.DEFAULT_MAX_RESPONSE_LENGTH);
        status = cli.get("https://httpbin.org/stream-bytes/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        cli.setMaxResponseLength(1024);
        status = cli.get("https://httpbin.org/stream-bytes/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        status = cli.get("https://httpbin.org/stream-bytes/1025");
        assertEquals(-1, status);
        assertTrue(cli.getException() instanceof ResponseTooBigException);
        assertNull(cli.getContent());  
        
        // test range
        cli.setMaxResponseLength(ScrapClient.DEFAULT_MAX_RESPONSE_LENGTH);
        status = cli.get("https://httpbin.org/range/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        cli.setMaxResponseLength(1024);
        status = cli.get("https://httpbin.org/range/1024");
        assertEquals(200, status);
        assertEquals(1024, cli.getContent().length);
        assertNull(cli.getException());
        
        status = cli.get("https://httpbin.org/range/1025");
        assertEquals(-1, status);
        assertTrue(cli.getException() instanceof ResponseTooBigException);
        assertNull(cli.getContent());
        
    }
    
    @Test
    public void testDetectCharsetFromHtmlMeta(){
        ScrapClient cli = new ScrapClient();
        cli.content = "qdsfqsdf<meta charset=\"utf-8\" />qsdfs".getBytes();
        assertEquals(Charset.forName("utf-8"), cli.detectCharsetFromHtmlMeta());
        
        cli.content = "http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />".getBytes();
        assertEquals(Charset.forName("utf-8"), cli.detectCharsetFromHtmlMeta());        
        
        cli.content = "http-equiv=\"Content-Type\" content=\"text/html; charset=xxx\" />".getBytes();
        assertNull(cli.detectCharsetFromHtmlMeta());
    }
    
    @Test
    public void testGetContentAsString() {
        ScrapClient cli = new ScrapClient();
        
        int status = cli.get("http://www.w3.org/Style/Examples/010/iso-8859-1-correct.html");
        assertEquals(200, status);
        String contentAsString = cli.getContentAsString();
        byte[] content = cli.getContent();
        assertNotNull(contentAsString);
        assertEquals(Charset.forName("ISO-8859-1"), cli.getDetectedCharset());
        assertEquals(content.length, contentAsString.length());
    }
    
    @Test
    public void testSupportedEncoding() throws Exception {
        ScrapClient cli = new ScrapClient();
        
        int status = cli.get("https://httpbin.org/get");
        HashMap<String,Object> props = new ObjectMapper()
            .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
        
        String encoding = (String)((Map)props.get("headers")).get("Accept-Encoding");
        Assert.assertThat(encoding, CoreMatchers.containsString("gzip"));
        Assert.assertThat(encoding, CoreMatchers.containsString("deflate"));
    }
    
    
    @Test
    public void testGzip() throws Exception {
        ScrapClient cli = new ScrapClient();
        
        int status = cli.get("https://httpbin.org/gzip");
        assertEquals(200, status);
        
        HashMap<String,Object> props = new ObjectMapper()
            .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
        
        assertTrue((boolean)props.get("gzipped"));
    }
    
    @Test
    public void testDefalte() throws Exception {
        ScrapClient cli = new ScrapClient();
        
        int status = cli.get("https://httpbin.org/deflate");
        assertEquals(200, status);
        
        HashMap<String,Object> props = new ObjectMapper()
            .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
        
        assertTrue((boolean)props.get("deflated"));
    }    
    
    @Test
    public void testDeflateOnWebsites() throws Exception {
        ScrapClient cli = new ScrapClient();
        
        String[] urls = new String[]{
            "http://www.bing.com",
            "https://twitter.com",
            "http://vk.com",
            "http://edition.cnn.com",
            "http://espn.go.com",
            "http://www.jd.com",
            "http://www.flipkart.com",
            "http://www.nytimes.com"
        };
        
        for (String url : urls) {
            int status = cli.get(url);
            System.out.println(url + "|" + status + "|" + cli.getException());
        }
    }    
    
    @Test
    public void testPostForm() throws Exception  {
        try(ScrapClient cli = new ScrapClient()){
            Map<String,Object> data = new HashMap<>();
            data.put("key1", "value1é");
            data.put("key2", "value2é");
            data.put("key3", "value3é");
            
            assertEquals(200, cli.post("http://httpbin.org/post", data, ScrapClient.PostType.URL_ENCODED));
            
            HashMap<String,Object> content = new ObjectMapper()
                .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
            
            assertEquals("application/x-www-form-urlencoded; charset=UTF-8", ((Map)content.get("headers")).get("Content-Type"));
            Map<String,String> postContent = (Map)content.get("form");
            assertEquals(3, postContent.size());
            
            String contentRaw = cli.getContentAsString();
            assertTrue(contentRaw.contains("\"key1\": \"value1\\u00e9\""));
            assertTrue(contentRaw.contains("\"key2\": \"value2\\u00e9\""));
            assertTrue(contentRaw.contains("\"key3\": \"value3\\u00e9\""));
        }
    }
    
    @Test
    public void testPostFormISO88591() throws Exception  {
        try(ScrapClient cli = new ScrapClient()){
            Map<String,Object> data = new HashMap<>();
            data.put("key1", "value1é");
            data.put("key2", "value2é");
            data.put("key3", "value3é");
            
            assertEquals(200, cli.post("http://httpbin.org/post", data, ScrapClient.PostType.URL_ENCODED, "iso-8859-1"));
            
            HashMap<String,Object> content = new ObjectMapper()
                .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
            
            assertEquals("application/x-www-form-urlencoded; charset=ISO-8859-1", ((Map)content.get("headers")).get("Content-Type"));
            Map<String,String> postContent = (Map)content.get("form");
            assertEquals(3, postContent.size());
            
            String contentRaw = cli.getContentAsString();
            assertTrue(contentRaw.contains("\"key1\": \"value1\\ufffd\""));
            assertTrue(contentRaw.contains("\"key2\": \"value2\\ufffd\""));
            assertTrue(contentRaw.contains("\"key3\": \"value3\\ufffd\""));
        }
    }
        
    @Test
    public void testPostMultipart() throws Exception  {
        try(ScrapClient cli = new ScrapClient()){
            Map<String,Object> data = new HashMap<>();
            data.put("key1", "value1é");
            data.put("key2", "value2é");
            data.put("key3", "value3é");
            data.put("filename", new ByteArrayBody("file-content".getBytes(), "filename"));
            
            assertEquals(200, cli.post("http://httpbin.org/post", data, ScrapClient.PostType.MULTIPART));
            
            HashMap<String,Object> content = new ObjectMapper()
                .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
            
            String contentType = (String) ((Map)content.get("headers")).get("Content-Type");
            assertTrue(contentType.contains("multipart/form-data;"));
            assertTrue(contentType.contains("; charset=UTF-8"));
            
            Map<String,String> postContent = (Map)content.get("form");
            assertEquals(3, postContent.size());
            Map<String,String> fileContent = (Map)content.get("files");
            assertEquals(1, fileContent.size());
            
            String contentRaw = cli.getContentAsString();
            assertTrue(contentRaw.contains("\"key1\": \"value1\\u00e9\""));
            assertTrue(contentRaw.contains("\"key2\": \"value2\\u00e9\""));
            assertTrue(contentRaw.contains("\"key3\": \"value3\\u00e9\""));
            assertTrue(contentRaw.contains("\"filename\": \"file-content\""));            
        }
    }
    
    @Test
    public void testPostMultipartISO88591() throws Exception  {
        try(ScrapClient cli = new ScrapClient()){
            Map<String,Object> data = new HashMap<>();
            data.put("key1", "value1é");
            data.put("key2", "value2é");
            data.put("key3", "value3é");
            data.put("filename", new ByteArrayBody("file-content".getBytes(), "filename"));
            
            assertEquals(200, cli.post("http://httpbin.org/post", data, ScrapClient.PostType.MULTIPART, "iso-8859-1"));
            
            HashMap<String,Object> content = new ObjectMapper()
                .readValue(cli.getContentAsString(), new TypeReference<HashMap<String,Object>>() {});
            
            String contentType = (String) ((Map)content.get("headers")).get("Content-Type");
            assertTrue(contentType.contains("multipart/form-data;"));
            assertTrue(contentType.contains("; charset=ISO-8859-1"));
            
            Map<String,String> postContent = (Map)content.get("form");
            assertEquals(3, postContent.size());
            Map<String,String> fileContent = (Map)content.get("files");
            assertEquals(1, fileContent.size());
            
            
            
            String contentRaw = cli.getContentAsString();
            assertTrue(contentRaw.contains("\"key1\": \"value1\\ufffd\""));
            assertTrue(contentRaw.contains("\"key2\": \"value2\\ufffd\""));
            assertTrue(contentRaw.contains("\"key3\": \"value3\\ufffd\""));
            assertTrue(contentRaw.contains("\"filename\": \"file-content\""));            
        }
    }    
    
    @Test
    public void testSetRoute() throws Exception {
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        client.setRoute(new HttpHost("www.google.fr", -1, "https"), new HttpHost("54.175.219.8", -1, "https"));
        client.get("https://www.google.fr/headers");
        assertTrue(client.getContentAsString().contains("\"Host\": \"www.google.fr\""));
    }
    
    @Test
    public void testSetRouteWithProxy() throws Exception {
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        client.setProxy(new HttpProxy("127.0.0.1",3128, "user", "pass"));
        
        client.setRoute(new HttpHost("www.google.fr", -1, "https"), new HttpHost("54.175.219.8", -1, "https"));
        
        client.get("https://www.google.fr/headers");
        assertTrue(client.getContentAsString().contains("\"Host\": \"www.google.fr\""));
    }    
    
    @Test
    public void testProxyContext() throws Exception {
        HttpProxy httpProxy1 = new HttpProxy("127.0.0.2",3128, "user", "pass");
        HttpProxy httpProxy2 = new HttpProxy("127.0.0.3",3128, "user", "pass");
        
        httpProxy1.setAttr("key", "v1");
        httpProxy2.setAttr("key", "v2");
        
        System.out.println(httpProxy1.getAttr("key", String.class));
        System.out.println(httpProxy2.getAttr("key", String.class));
        
        
//        client.setProxy(new HttpProxy("127.0.0.1",3128, "user", "pass"));
//        
//        client.setRoute(new HttpHost("www.google.fr", -1, "https"), new HttpHost("54.175.219.8", -1, "https"));
//        
//        client.get("https://www.google.fr/headers");
//        assertTrue(client.getContentAsString().contains("\"Host\": \"www.google.fr\""));
    }        
    
    @Test
    public void testSimpleGet() throws Exception {
        int statusCode;String htmlContent;
        ScrapClient client = new ScrapClient();
        client.setInsecureSSL(true);
        
        assertEquals(200, client.get("http://httpbin.org/get"));
        htmlContent = client.getContentAsString();
        assertTrue(htmlContent.contains("http://httpbin.org/get") && !htmlContent.contains("https://httpbin.org/get"));
        
        assertEquals(200, client.get("https://httpbin.org/get"));
        htmlContent = client.getContentAsString();
        assertTrue(htmlContent.contains("https://httpbin.org/get") && !htmlContent.contains("http://httpbin.org/get"));
        
        assertEquals(200, client.get("http://httpbin.org/get"));
        htmlContent = client.getContentAsString();
        assertTrue(htmlContent.contains("http://httpbin.org/get") && !htmlContent.contains("https://httpbin.org/get"));
        
        assertEquals(200, client.get("https://httpbin.org/get"));
        htmlContent = client.getContentAsString();
        assertTrue(htmlContent.contains("https://httpbin.org/get") && !htmlContent.contains("http://httpbin.org/get"));        
    }
    
}
