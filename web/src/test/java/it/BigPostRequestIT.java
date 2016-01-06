/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package it;

import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.db.base.ConfigDB;
import com.serphacker.serposcope.models.base.User;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import ninja.NinjaTest;
import ninja.utils.CookieEncryption;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;
import ninja.utils.SecretGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.helpers.CookieEncryptionOverride;
import serposcope.helpers.CryptoOverride;


public class BigPostRequestIT extends SerposcopeNinjaTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(BigPostRequestIT.class);
    
    @Before
    public void before() throws Exception {
    }
    
    
    @Test
    public void testVeryBig() throws Exception {
        
        User admin = createAdmin();
        
        
        String postParam = StringUtils.repeat(" ", 100);
        
        int inputs = 10000;
        Map<String,String> parameters = new HashMap<>();
        
        LOG.info("before building request");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inputs; i++) {
            parameters.put("param" + i, postParam);
            builder.append("param").append(i).append(postParam);
            for (int j = 0; j < 5; j++) {
                parameters.put("param" + j + "_" + i, "");
                builder.append("param").append(j).append("_").append(i);
            }
        }
        
        LOG.info("big request size : {}, inputs : {}", builder.length(), inputs);
        
        assertTrue(login(admin.getEmail(), "password"));
        String result = ninjaTestBrowser
            .makePostRequestWithFormParameters(getServerAddress() + "/admin/debug/dummy-post", null, parameters);
        assertFalse(result.contains("internal server error"));
    }
    

}
