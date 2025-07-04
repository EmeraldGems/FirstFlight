package com.csols.FirstFlight;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static Map<String, String> users = new HashMap<>();
    
    public static void registerUser(String username, String password) {
        users.put(username, hashPassword(password));
        UserStorage.saveUsers(users);
    }
    public static boolean userExists(String username) {
    return users.containsKey(username);
    }
    public static boolean validateUser(String username, String password) {
    System.out.println("Validating user: " + username);
    System.out.println("Current users: " + users);
    String storedHash = users.get(username);
    return storedHash != null && storedHash.equals(hashPassword(password));
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
    
    static { // Load users on startup
        users = UserStorage.loadUsers();
    }
}