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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serposcope.helpers.CookieEncryptionOverride;
import serposcope.helpers.CryptoOverride;


public class SessionIT extends NinjaTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(SessionIT.class);
    
    @Before
    public void before() throws Exception {
    }
    
    @Test
    public void testLoginSession() throws Exception{
        
        NinjaPropertiesImpl props = (NinjaPropertiesImpl)getInjector().getInstance(NinjaProperties.class);
        assertNotEquals("0000000000000000000000000000000000000000000000000000000000000000", props.get(NinjaConstant.applicationSecret));
        
        Map<String,String> form = new HashMap<>();
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Create admin</title>"));
        
        User user = createAdmin();
        
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Login</title>"));
        
        form.put("email", user.getEmail());
        form.put("password", "fakepassword");
        assertTrue(ninjaTestBrowser.makePostRequestWithFormParameters(getServerAddress() + "/login", null, form)
            .contains("Invalid credentials"));

        form.put("email", user.getEmail());
        form.put("password", "password" );
        assertTrue(ninjaTestBrowser.makePostRequestWithFormParameters(getServerAddress() + "/login", null, form)
            .contains("<title>Home</title>"));
        
        // check session OK
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Home</title>"));
        
        System.out.println(ninjaTestBrowser.getCookies().size());
        System.out.println(ninjaTestBrowser.getCookies());        
        
        changeSecret(SecretGenerator.generateSecret());
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Login</title>"));
    }
    
//    @Test
    public void testSessionRestore() throws Exception {
        User user = createAdmin();
        
        BasicClientCookie cookie = new BasicClientCookie("NINJA_SESSION", "2fdc4813da344559c9fe1ec62afecb871b36c370-vatEq13ojGHP-L4RPMoOUUfYu5ZfnAsc6-LmFxYnnxcAbBzuA8WI2vMO5X43_m2h4SDU-LXP6O1rZytSchbkp7cna8a010ek5YUcBMxpcQbLVIVPqMPvxnOIUbn9C4IT");
        cookie.setVersion(0);
        cookie.setPath("/");
        cookie.setDomain("0.0.0.0");
        
       ((DefaultHttpClient)ninjaTestBrowser.getHttpClient()).getCookieStore().addCookie(cookie);
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Login</title>"));
    }
    
    // 
//    @Test
    public void testSessionRestore2() throws Exception {
        User user = createAdmin();
        
        changeSecret("OktxTG6epOMQ0jVKQvScIqepPNmHOGAhuGN7ZHnikfw6QxOZPTyZ8WiUrOEcgHmU");
        
        BasicClientCookie cookie = new BasicClientCookie("NINJA_SESSION", "886e1a60b62eecf0047f1eef240b0289e92ab77c-x_j5exYJWm1syBZailhr-OsZPp0lwBdYmGn3CnO9lIsqbufLzXu1xuSaqRKSN6nq1dOImm65XwJPVdiXRJLdr2LSlsqQN-SkG272zMGbJuzLVIVPqMPvxnOIUbn9C4IT");
        cookie.setVersion(0);
        cookie.setPath("/");
        cookie.setDomain("0.0.0.0");
        
       ((DefaultHttpClient)ninjaTestBrowser.getHttpClient()).getCookieStore().addCookie(cookie);
        assertTrue(ninjaTestBrowser.makeRequest(getServerAddress() + "/").contains("<title>Home</title>"));
    }
    
    protected User createAdmin() throws Exception {
        BaseDB baseDB = getInjector().getInstance(BaseDB.class);
        User user = new User();
        user.setAdmin(true);
        user.setEmail("email@email.com");
        String password = "password";
        user.setPassword(password);
        baseDB.user.insert(user); 
        return user;
    }
    
    protected void changeSecret(String secret) throws Exception{
        NinjaPropertiesImpl props = (NinjaPropertiesImpl)getInjector().getInstance(NinjaProperties.class);
        CookieEncryptionOverride cookieEncryption = getInjector().getInstance(CookieEncryptionOverride.class);
        CryptoOverride crypto = getInjector().getInstance(CryptoOverride.class);
        
        LOG.info("setting new secret : " + secret);
        props.setProperty(NinjaConstant.applicationSecret, secret);
        crypto.update(props);
        cookieEncryption.update(props);
    }

}
