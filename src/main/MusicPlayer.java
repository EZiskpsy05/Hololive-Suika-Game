package main;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.*;

public class MusicPlayer {

    private Clip clip;

    /**
     * Plays a music file from the given resource path and loops it continuously.
     * @param resourcePath The path to the audio file within the project's resources.
     */
    public void playMusic(String resourcePath) {
        try {
            // Use getResourceAsStream to load the file from the classpath.
            // A leading slash "/" means the path is relative to the root of the classpath.
            InputStream audioSrc = getClass().getResourceAsStream(resourcePath);

            if (audioSrc == null) {
                System.err.println("Could not find audio file at: " + resourcePath);
                // Try without the leading slash for different classpath setups
                audioSrc = getClass().getResourceAsStream(resourcePath.substring(1));
                 if (audioSrc == null) {
                    System.err.println("Also could not find audio file at: " + resourcePath.substring(1));
                    return;
                 }
            }

            // We need to buffer the input stream to support the mark/reset methods
            // used by some audio decoders.
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
            clip.start();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("The audio file format is not supported.");
            System.err.println("To play .ogg files, you need to add specific libraries (like JOrbis, JOgg, and VorbisSPI) to your project's classpath.");
            System.err.println("Alternatively, you can convert 'main.ogg' to a .wav file, which is natively supported.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An error occurred while trying to play music:");
            e.printStackTrace();
        }
    }

    /**
     * Stops the currently playing music and releases the resources.
     */
    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
