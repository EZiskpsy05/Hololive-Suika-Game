package shapes;

import java.awt.*;
import javax.swing.ImageIcon;

public class Circle extends MyShape {
    private final BallType type;

    public Circle(BallType type, double centerX, double centerY) {
        // Constructor now takes CENTER coordinates for easier spawning
        super(centerX - type.radius, centerY - type.radius, type.radius * 2, type.radius * 2, Color.BLACK, Color.GRAY, type.radius);
        this.type = type;
    }

    public BallType getType() {
        return type;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create(); // Create a copy of the Graphics2D context

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ImageIcon icon = type.getResizedImageIcon((int) Math.round(getWidth()));
            
            // Calculate center for rotation
            double centerX = getX() + getWidth() / 2.0;
            double centerY = getY() + getHeight() / 2.0;

            // --- ADDED: Rotation Logic ---
            // 1. Translate the canvas origin to the center of the circle
            g2.translate(centerX, centerY);
            // 2. Rotate the canvas
            g2.rotate(getRotationAngle());
            // 3. Translate back so the image is drawn centered on the new origin
            g2.translate(-getWidth() / 2.0, -getHeight() / 2.0);

            if (icon != null && icon.getImage() != null) {
                // Draw the image at (0,0) of the transformed canvas
                g2.drawImage(icon.getImage(), 0, 0, null);
            } else {
                // Fallback drawing if image fails, drawn at (0,0) relative to transformed context
                g2.setColor(type.fallbackColor);
                g2.fillOval(0, 0, (int) Math.round(getWidth()), (int) Math.round(getHeight()));
                g2.setColor(Color.BLACK);
                g2.drawOval(0, 0, (int) Math.round(getWidth()), (int) Math.round(getHeight()));
            }

        } finally {
            // Dispose of the copied context to restore the original transform
            g2.dispose();
        }

        // Debugging border (drawn with original un-rotated graphics context)
        if (getSelected()) {
            Graphics2D g2border = (Graphics2D) g;
            g2border.setStroke(new BasicStroke(3));
            g2border.setColor(Color.CYAN);
            g2border.drawOval((int) Math.round(getX()), (int) Math.round(getY()), (int) Math.round(getWidth()), (int) Math.round(getHeight()));
        }
    }

    @Override
    public double getArea() {
        return Math.PI * (getWidth() / 2.0) * (getWidth() / 2.0);
    }
}
