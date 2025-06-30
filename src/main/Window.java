package main;

import java.awt.EventQueue;
import java.net.URL; // Import URL for resource loading
import javax.swing.ImageIcon; // Import ImageIcon
import javax.swing.JFrame;

public class Window extends JFrame {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 550;

    private GameManager gameManager;
    private MusicPlayer musicPlayer; // ADD THIS LINE

    public Window() {
        super("Hololive Suika");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // --- ADDED: Set Application Icon ---
        try {
            URL iconURL = getClass().getResource("/icons/icon.png"); // Path to your icon
            if (iconURL != null) {
                ImageIcon appIcon = new ImageIcon(iconURL);
                setIconImage(appIcon.getImage());
            } else {
                System.err.println("App icon not found!");
            }
        } catch (Exception e) {
            System.err.println("Error loading app icon: " + e.getMessage());
        }
        // --- End of new code ---

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); 

        gameManager = new GameManager(this);

        // --- ADD THIS BLOCK TO START THE BACKGROUND MUSIC ---
        musicPlayer = new MusicPlayer();
        // Assuming your 'audio' folder is at the root of your classpath (like 'src' or 'resources').
        musicPlayer.playMusic("/audio/main.wav");
        // -----------------------------------------------

        setVisible(true);
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(() -> new Window());
    }
}
