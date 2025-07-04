    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author FRANCIS
 */
package com.csols.FirstFlight;

import java.util.Random;

public class ReCaptcha {
    private static final String[] WORDS = {
        "Dragon", "Castle", "Sword", "Shadow", "Phoenix", 
        "Rune", "Storm", "Ember", "Wizard", "Knight"
    };
    
    private static final String[] SYMBOLS = {"!", "@", "#", "$", "%", "&", "*", "?"};
    private static final Random random = new Random();

    public static String generateCaptcha() {
        // Get random word with random capitalization
        String word = WORDS[random.nextInt(WORDS.length)];
        word = random.nextBoolean() ? word.toUpperCase() : word.toLowerCase();
        
        // Add random number (0-9)
        int number = random.nextInt(10);
        
        // Add random symbol
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        
        // Combine elements in random order
        return shuffleElements(word, String.valueOf(number), symbol);
    }
    
    private static String shuffleElements(String... elements) {
        StringBuilder sb = new StringBuilder();
        while (elements.length > 0) {
            int index = random.nextInt(elements.length);
            sb.append(elements[index]);
            elements = removeElement(elements, index);
        }
        return sb.toString();
    }
    
    private static String[] removeElement(String[] arr, int index) {
        String[] newArray = new String[arr.length - 1];
        System.arraycopy(arr, 0, newArray, 0, index);
        System.arraycopy(arr, index + 1, newArray, index, arr.length - index - 1);
        return newArray;
    }
}
