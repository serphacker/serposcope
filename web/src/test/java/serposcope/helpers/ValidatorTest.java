/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class ValidatorTest {
    
    public ValidatorTest() {
    }

    @Test
    public void testSomeMethod() {
        
        assertFalse(Validator.isGenericName(null));
        assertFalse(Validator.isGenericName(" "));
        assertFalse(Validator.isGenericName("'"));
        assertFalse(Validator.isGenericName("\""));
        assertTrue(Validator.isGenericName("abc def"));
        assertTrue(Validator.isGenericName("环保部"));
        
    }
    
}

