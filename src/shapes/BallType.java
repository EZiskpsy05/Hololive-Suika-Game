package shapes;

import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;

public enum BallType {
    // Enum constants now define radius, score, and image path.
    LEVEL_0(16, 10, "/icons/ball_1.png"),    // 32x32 pixels
    LEVEL_1(24, 20, "/icons/ball_2.png"),    // 48x48 pixels
    LEVEL_2(32, 40, "/icons/ball_3.png"),    // 64x64 pixels
    LEVEL_3(40, 80, "/icons/ball_4.png"),    // 80x80 pixels
    LEVEL_4(48, 160, "/icons/ball_5.png"),   // 96x96 pixels
    LEVEL_5(64, 320, "/icons/ball_6.png"),   // 128x128 pixels
    LEVEL_6(72, 450, "/icons/ball_7.png"),   // 144x144 pixels
    LEVEL_7(80, 640, "/icons/ball_8.png"),   // 160x160 pixels
    LEVEL_8(88, 880, "/icons/ball_9.png"),   // 176x176 pixels
    LEVEL_9(96, 1280, "/icons/ball_10.png"), // 192x192 pixels
    LEVEL_10(128, 2500, "/icons/ball_11.png"); // 256x256 pixels

    public final double radius;
    public final int scoreValue;
    public final String imagePath;
    
    // Fallback color in case image loading fails
    public final Color fallbackColor; 

    // A cached copy of the original, full-size image icon
    private ImageIcon originalImageIcon;

    BallType(double radius, int scoreValue, String imagePath) {
        this.radius = radius;
        this.scoreValue = scoreValue;
        this.imagePath = imagePath;
        // Generate a random fallback color for robustness
        this.fallbackColor = new Color((int)(Math.random() * 0x1000000));
    }

    // Lazily loads the original image icon and caches it.
    private ImageIcon getOriginalImageIcon() {
        if (originalImageIcon == null) {
            try {
                originalImageIcon = new ImageIcon(getClass().getResource(imagePath));
                // Check if the image was actually loaded
                if (originalImageIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    System.err.println("Failed to load image: " + imagePath);
                    originalImageIcon = null;
                }
            } catch (Exception e) {
                System.err.println("Error loading image resource: " + imagePath);
                originalImageIcon = null;
            }
        }
        return originalImageIcon;
    }

    /**
     * This is the new key method. It returns a resized version of the ball's image.
     * @param diameter The target width and height for the new image icon.
     * @return A resized ImageIcon, or null if the original image could not be loaded.
     */
    public ImageIcon getResizedImageIcon(int diameter) {
        ImageIcon originalIcon = getOriginalImageIcon();
        if (originalIcon == null) {
            return null; // Return null if the base image isn't available
        }
        
        // Use getScaledInstance to resize the image smoothly (shrinks without cutting)
        Image resizedImage = originalIcon.getImage().getScaledInstance(diameter, diameter, Image.SCALE_SMOOTH);
        
        // Return a new ImageIcon created from the resized Image
        return new ImageIcon(resizedImage);
    }

    // Helper method to get the next ball type in the sequence
    public static BallType getNext(BallType currentType) {
        if (currentType.ordinal() < BallType.values().length - 1) {
            return BallType.values()[currentType.ordinal() + 1];
        }
        return null; // This is the largest ball, cannot merge further
    }
}