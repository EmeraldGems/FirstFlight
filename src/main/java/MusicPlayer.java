package com.csols.FirstFlight;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class MusicPlayer {

    private Clip clip;

    public void playMusic(String filepath) {
        try {
            // If a clip is already playing or open, stop and close it first
            if (clip != null && (clip.isRunning() || clip.isOpen())) {
                clip.stop();
                clip.close();
            }

            URL soundURL = getClass().getResource(filepath);
            if (soundURL == null) {
                System.err.println("Music file not found: " + filepath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Play on loop
            clip.start();
            System.out.println("Playing music from: " + filepath); // For debugging
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("Error playing music: " + e.getMessage()); // More specific error
        }
    }

    public void stopMusic() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // Stop playback
                System.out.println("Music stopped."); // For debugging
            }
            clip.close(); // Close the clip and release resources
            System.out.println("Music clip closed."); // For debugging
            clip = null; // Set clip to null to indicate it's no longer active
        } else {
            System.out.println("No music clip to stop."); // For debugging
        }
    }
}