/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.serphacker.serposcope.db.base.UserDB;
import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.User;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class UserDBIT extends AbstractDBIT {

    public UserDBIT() {
    }
    
    @Inject
    BaseDB baseDB;

    @Test
    public void testInsert() {
        User insertUser = new User();
        insertUser.setEmail("test@email.com");
        insertUser.setAdmin(true);
        insertUser.setPasswordHash(new byte[]{1, 2, 3});

        int insertID = baseDB.user.insert(insertUser);
        assertTrue(insertID > 0);
        assertEquals(insertID, insertUser.getId());
    }
    
    @Test
    public void testPassword() throws Exception {
        User user = new User();
        user.setEmail("x@x.com");
        user.setAdmin(true);
        user.setPassword("x@x.com");
        
        int insertID = baseDB.user.insert(user);
        assertTrue(insertID > 0);
        assertEquals(insertID, user.getId());
        
        User userDB = baseDB.user.findById(insertID);
        
        assertTrue(userDB.verifyPassword("x@x.com"));
    }    

    @Test
    public void testList() {

        List<User> insertedUsers = new ArrayList<>();
        int nUser = 5;

        for (int i = 0; i < nUser; i++) {
            User user = new User();
            user.setEmail("user-" + i + "@email.com");
            user.setAdmin(true);
            baseDB.user.insert(user);
            insertedUsers.add(user);
        }

        List<User> users = baseDB.user.list();
        assertEquals(nUser, users.size());

    }

    @Test
    public void hasAdmin() {
        assertFalse(baseDB.user.hasAdmin());
        User user = new User();
        user.setEmail("user@email.com");
        user.setAdmin(true);
        baseDB.user.insert(user);
        assertTrue(baseDB.user.hasAdmin());
    }
    
    @Test
    public void testPerms(){
        User user1 = new User();
        user1.setEmail("user1@email.com");
        assertTrue(baseDB.user.insert(user1) > 0);
        
        User user2 = new User();
        user2.setEmail("user2@email.com");
        assertTrue(baseDB.user.insert(user2) > 0);        
        
        Group grp1 = new Group(Group.Module.GOOGLE, "grp1");
        assertTrue(baseDB.group.insert(grp1) > 0);
        
        Group grp2 = new Group(Group.Module.GOOGLE, "grp2");
        assertTrue(baseDB.group.insert(grp2) > 0);
        
        user1 = baseDB.user.findById(user1.getId());
        assertTrue(user1.getGroups().isEmpty());
        
        assertTrue(baseDB.user.addPerm(user1, grp1));
        user1 = baseDB.user.findById(user1.getId());
        assertTrue(user1.getGroups().contains(grp1));        
        assertTrue(user1.canRead(grp1));
        assertTrue(baseDB.user.hasPerm(user1, grp1));
        assertFalse(user1.canRead(grp2));
        assertFalse(user2.canRead(grp1));
        
        
        assertTrue(baseDB.user.delPerm(user1, grp1));
        user1 = baseDB.user.findById(user1.getId());
        assertTrue(user1.getGroups().isEmpty());        
        assertFalse(user1.canRead(grp1));
        assertFalse(baseDB.user.hasPerm(user1, grp1));
        
        assertFalse(baseDB.user.delPerm(user1, grp1));
        
        
        assertTrue(baseDB.user.addPerm(user2, grp2));
        assertTrue(baseDB.user.addPerm(user2, grp2));
        
        user2 = baseDB.user.findById(user2.getId());
        assertTrue(baseDB.user.hasPerm(user2, grp2));
        assertTrue(user2.canRead(grp2));
        assertFalse(user2.canRead(grp1));
        assertFalse(user1.canRead(grp1));
        assertFalse(user1.canRead(grp2));
        
    }
    

}
