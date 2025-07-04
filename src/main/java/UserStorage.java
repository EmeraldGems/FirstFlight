/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author FRANCIS
 */
package com.csols.FirstFlight;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class UserStorage {
    private static final String FILE_PATH = System.getProperty("user.home") + 
                                     "/FirstFlight/users.dat";

    static {
        new File(System.getProperty("user.home") + "/FirstFlight").mkdirs();
    }

    public static void saveUsers(Map<String, String> users) {
        try {
            // Use a more robust serialization approach
            ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(FILE_PATH)));
            oos.writeObject(users);
            oos.close();
            System.out.println("Users saved successfully to: " + 
                new File(FILE_PATH).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, String> loadUsers() {
        File file = new File(FILE_PATH);
        System.out.println("Loading users from: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.out.println("No user data found, creating new empty map");
            return new HashMap<>();
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(FILE_PATH)));
            @SuppressWarnings("unchecked")
            Map<String, String> loadedUsers = (Map<String, String>) ois.readObject();
            ois.close();
            System.out.println("Successfully loaded " + loadedUsers.size() + " users");
            return loadedUsers;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

private static final String TRANSACTIONS_PATH = System.getProperty("user.home") + 
                                     "/FirstFlight/transactions/";

static {
    new File(TRANSACTIONS_PATH).mkdirs();
}

public static void addUserTransaction(String username, String transaction) {
    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    String record = timestamp + " - " + transaction + "\n";
    
    try (FileWriter fw = new FileWriter(TRANSACTIONS_PATH + username + ".txt", true);
         BufferedWriter bw = new BufferedWriter(fw)) {
        bw.write(record);
    } catch (IOException e) {
        System.err.println("Error saving transaction: " + e.getMessage());
    }
}

public static List<String> getUserTransactions(String username) {
    List<String> transactions = new ArrayList<>();
    File file = new File(TRANSACTIONS_PATH + username + ".txt");
    
    if (file.exists()) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                transactions.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
    }
    return transactions;
}
}