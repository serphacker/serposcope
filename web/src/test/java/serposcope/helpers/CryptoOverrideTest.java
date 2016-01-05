/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import ninja.utils.NinjaConstant;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class CryptoOverrideTest {
    
    public CryptoOverrideTest() {
    }

    @Test
    public void testSomeMethod() throws Exception {
        NinjaPropertiesImpl props = new NinjaPropertiesImpl(NinjaMode.prod);
        props.setProperty(NinjaConstant.applicationSecret, "initial");
        
        CryptoOverride crypto = new CryptoOverride(props);
        
        assertEquals("initial", crypto.getApplicationSecret());
        
        props.setProperty(NinjaConstant.applicationSecret, "secret");
        crypto.update(props);
        assertEquals("secret", crypto.getApplicationSecret());
        
    }
    
}
