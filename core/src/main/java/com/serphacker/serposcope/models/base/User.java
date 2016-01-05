/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class User {

    int id;
    String email;
    byte[] passwordHash;
    byte[] passwordSalt;
    boolean admin;
    LocalDateTime logout;
    List<Group> groups = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public LocalDateTime getLogout() {
        return logout;
    }

    public void setLogout(LocalDateTime logout) {
        this.logout = logout;
    }
    
    public boolean loggedOutAfter(Long epochSecond){
        if(logout == null){
            return false;
        }
        
        return logout.toEpochSecond(ZoneOffset.UTC) > epochSecond;
    }
    
    public byte[] getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
    
    public void setPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
        passwordSalt = new byte[HASH_SALT_SIZE];
        random.nextBytes(passwordSalt);
        passwordHash = hashPassword(password, passwordSalt);
    }
    
    public boolean verifyPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
        return Arrays.equals(passwordHash, hashPassword(password, passwordSalt));
    }
    
    final static int HASH_SALT_SIZE = 16;
    final static int HASH_ITERATIONS = 65536; 
    final static int HASH_DERIVED_KEY_SIZE = 160; 
    final static SecureRandom random = new SecureRandom();
    
    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
        KeySpec pbeKeySpec = new PBEKeySpec(
            password.toCharArray(),
            salt, 
            HASH_ITERATIONS, 
            HASH_DERIVED_KEY_SIZE
        );
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey key = secretKeyFactory.generateSecret(pbeKeySpec);
        return key.getEncoded();
    }
        
    public void addGroup(Group group){
        groups.add(group);
    }
    
    public boolean canRead(Group group){
        return groups.contains(group);
    }
    
    public boolean canRead(int groupId){
        for (Group group : groups) {
            if(group.getId() == groupId){
                return true;
            }
        }
        return false;
    }    

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
    
}
