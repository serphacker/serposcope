/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class VersionTest {
    
    public VersionTest() {
    }

    @Test
    public void testBuildVersion() {
        assertEquals(Version.CURRENT, new Version(System.getProperty("serposcopeversion")));
    }
    
    @Test
    public void testCompareVersion(){
        assertTrue(new Version("2.0.0").compareTo(new Version("2.0.0")) == 0);
        assertTrue(new Version("2.0.1").compareTo(new Version("2.0.0")) > 0);
        assertTrue(new Version("2.1.0").compareTo(new Version("2.0.0")) > 0);
        assertTrue(new Version("3.0.0").compareTo(new Version("2.0.0")) > 0);
        assertTrue(new Version("1.0.1").compareTo(new Version("2.0.0")) < 0);
    }
    
    @Test
    public void testVersion(){
        assertTrue(new Version("2.0.0").compareTo(new Version("2.0.0-BETA1")) > 0);
        assertTrue(new Version("2.0.0-BETA2").compareTo(new Version("2.0.0-BETA1")) > 0);
        assertTrue(new Version("2.0.0-BETA1").compareTo(new Version("2.0.0-BETA1")) == 0);
        assertTrue(new Version("2.0.0-BETA1").compareTo(new Version("2.0.0-ALPHA9")) > 0);
        assertTrue(new Version("2.0.0-RC1").compareTo(new Version("2.0.0-BETA1")) > 0);
        assertTrue(new Version("2.0.0-RC2").compareTo(new Version("2.0.0-RC1")) > 0);
        assertTrue(new Version("2.0.0-RC2").compareTo(new Version("2.0.0-RC1")) > 0);
    }
    
}
