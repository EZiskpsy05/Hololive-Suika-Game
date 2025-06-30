package utils;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public class SoundUtils {
    public static void playSound(String resourcePath) {
        try {
            URL url = SoundUtils.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound file not found: " + resourcePath);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.err.println("Failed to play sound: " + resourcePath);
        }
    }
}