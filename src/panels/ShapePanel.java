package panels;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import shapes.BallType;

public class ShapePanel extends JPanel {

    private static final int PANEL_WIDTH = 150;
    private BallType nextBallType;
    private ImageIcon nextBackgroundImage;
    private ImageIcon panelBackgroundImage;
    private final JLabel displayLabel;

    public ShapePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.DARK_GRAY));

        loadResources();

        displayLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (nextBallType != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int diameter = 80;
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;
                    ImageIcon ballIcon = nextBallType.getResizedImageIcon(diameter);
                    if (ballIcon != null) {
                        g2.drawImage(ballIcon.getImage(), x, y, null);
                    }
                }
            }
        };

        if (nextBackgroundImage != null) {
            displayLabel.setIcon(nextBackgroundImage);
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        add(displayLabel, gbc);
    }

    private void loadResources() {
        try {
            // Load the panel background image from /icons/panel_bg.png (change filename as needed)
            URL bgUrl = getClass().getResource("/icons/start_bg.png");
            if (bgUrl != null) {
                panelBackgroundImage = new ImageIcon(bgUrl);
            } else {
                System.err.println("Panel background image not found: /icons/panel_bg.png");
            }

            URL imgUrl = getClass().getResource("/icons/next.png");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                int newWidth = PANEL_WIDTH - 20;
                Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, -1, Image.SCALE_SMOOTH);
                nextBackgroundImage = new ImageIcon(scaledImage);
            } else {
                System.err.println("Next ball background not found: /icons/next.png");
            }
        } catch (Exception e) {
            System.err.println("Failed to load images for ShapePanel.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the panel background image if available, cut/crop if needed
        if (panelBackgroundImage != null) {
            Image img = panelBackgroundImage.getImage();
            int imgW = img.getWidth(this);
            int imgH = img.getHeight(this);
            int panelW = getWidth();
            int panelH = getHeight();

            // Only draw the top-left portion of the image that fits the panel
            g.drawImage(
                img,
                0, 0, panelW, panelH, // destination rectangle (panel)
                0, 0, Math.min(panelW, imgW), Math.min(panelH, imgH), // source rectangle (image)
                this
            );
        }
    }

    public void setNextBallType(BallType type) {
        this.nextBallType = type;
        displayLabel.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
    }
}
