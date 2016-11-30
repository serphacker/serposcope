/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import javax.crypto.Cipher;
import static ninja.utils.CookieEncryption.ALGORITHM;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaPropertiesImpl;
import ninja.utils.SecretGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class CookieEncryptionOverrideTest {
    
    public CookieEncryptionOverrideTest() {
    }

    @Test
    public void testSomeMethod() throws Exception {
        
        String secret1 = "4MeHInvL49wEH4DVmD9DHYwCCTaRl7u7r886FgIoGG5CAe92lN972JNxeUhMXVln";
        String secret2 = "Vso90TgeMWCdw0pqSXxcI4iTqERnoiicKNM14l0oKU6xHERBRhIPOYxM4QxfEVgo";
        
        NinjaPropertiesImpl props = new NinjaPropertiesImpl(NinjaMode.prod);
        props.setProperty(NinjaConstant.applicationSecret, secret1);
        props.setProperty(NinjaConstant.applicationCookieEncrypted, "false");
        
        CookieEncryptionOverride cookie = new CookieEncryptionOverride(props);
        
        assertEquals("abcdef", cookie.encrypt("abcdef"));
        props.setProperty(NinjaConstant.applicationCookieEncrypted, "true");
        cookie.update(props);
        assertEquals("3ue3IUUnTHfT2wvATBQiqg", cookie.encrypt("abcdef"));
        
        CookieEncryptionOverride cookie2 = new CookieEncryptionOverride(props);
        assertEquals(cookie.encrypt("abcdef"), cookie2.encrypt("abcdef"));
        
        assertEquals(cookie.fieldSecretKeySpec.get(cookie), cookie2.fieldSecretKeySpec.get(cookie2));
        
        props.setProperty(NinjaConstant.applicationSecret, secret2);
        cookie.update(props);
        assertNotEquals(cookie.fieldSecretKeySpec.get(cookie), cookie2.fieldSecretKeySpec.get(cookie2));        
        assertNotEquals(cookie.encrypt("abcdef"), cookie2.encrypt("abcdef"));
        
    }
    
    
    
    
}
