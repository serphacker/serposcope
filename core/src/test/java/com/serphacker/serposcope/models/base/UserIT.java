/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class UserIT {
    
    public UserIT() {
    }

    @Test
    public void testHashing() throws Exception {
        
        User user1 = new User();
        user1.setPassword("blabla");
        
        assertTrue(user1.verifyPassword("blabla"));
        assertFalse(user1.verifyPassword("blabla2"));
        
        user1.getPasswordHash()[0] = (byte)(user1.getPasswordHash()[0]+1);
        assertFalse(user1.verifyPassword("blabla"));
    }
    
    
    
}
