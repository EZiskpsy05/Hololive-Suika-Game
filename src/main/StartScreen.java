// File: main/StartScreen.java
package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 * A custom JTextField with a rounded border, padding, and placeholder text.
 */
class StyledTextField extends JTextField {
    private String placeholder;

    public StyledTextField(String placeholder) {
        this.placeholder = placeholder;
        
        // --- STYLING ---
        // Set a transparent background to see the panel's background
        setOpaque(false);
        // Set font style and color
        setFont(new Font("Arial", Font.PLAIN, 16));
        setForeground(Color.DARK_GRAY);
        // Set the color of the text cursor
        setCaretColor(Color.DARK_GRAY);
        // Set a custom rounded border with padding
        setBorder(new RoundBorder(15, new Insets(5, 15, 5, 15)));
        // Center the text
        setHorizontalAlignment(JTextField.CENTER);

        // --- PLACEHOLDER LOGIC ---
        // Add a listener to show/hide the placeholder text
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(Color.DARK_GRAY); // Text color when typing
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(Color.GRAY); // Placeholder text color
                }
            }
        });

        // Initialize with placeholder text
        setText(placeholder);
        setForeground(Color.GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // This ensures the rounded background is painted correctly
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint a white rounded rectangle as the background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
        g2.dispose();
        
        // Let the default painting mechanism handle text, cursor, etc.
        super.paintComponent(g);
    }
}

/**
 * A custom border that creates a rounded rectangle shape.
 */
class RoundBorder extends AbstractBorder {
    private int cornerRadius;
    private Insets insets;

    public RoundBorder(int cornerRadius, Insets insets) {
        this.cornerRadius = cornerRadius;
        this.insets = insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground()); // Use component's foreground color for the border
        g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, cornerRadius, cornerRadius));
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = this.insets.left;
        insets.right = this.insets.right;
        insets.top = this.insets.top;
        insets.bottom = this.insets.bottom;
        return insets;
    }
}


class BouncingCircle {
    int x, y, radius, speedX, speedY;
    Color color;
    private static final Random rand = new Random();

    public BouncingCircle(int panelWidth, int panelHeight) {
        radius = rand.nextInt(20) + 10;
        x = rand.nextInt(panelWidth - 2 * radius) + radius;
        y = rand.nextInt(panelHeight - 2 * radius) + radius;
        speedX = (rand.nextInt(5) + 1) * (rand.nextBoolean() ? 1 : -1);
        speedY = (rand.nextInt(5) + 1) * (rand.nextBoolean() ? 1 : -1);
        color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 200); // Added alpha for transparency
    }

    public void move(int panelWidth, int panelHeight) {
        x += speedX;
        y += speedY;
        if (x - radius <= 0 || x + radius >= panelWidth) {
            speedX *= -1;
            x = Math.max(radius, Math.min(x, panelWidth - radius));
        }
        if (y - radius <= 0 || y + radius >= panelHeight) {
            speedY *= -1;
            y = Math.max(radius, Math.min(y, panelHeight - radius));
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public boolean contains(Point p) {
        return p.distance(x, y) <= radius;
    }

    public void changeColor() {
        color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 200);
    }
}

public final class StartScreen extends JPanel implements ActionListener {
    private final GameManager gameManager;
    // --- MODIFIED: Use the new StyledTextField ---
    private final StyledTextField userIdField;
    private final ArrayList<BouncingCircle> circles = new ArrayList<>();
    private final Timer timer;
    private static final int NUM_CIRCLES = 15, WINDOW_WIDTH = 800, WINDOW_HEIGHT = 550;

    private BufferedImage backgroundImage;

    public StartScreen(GameManager gameManager) {
        this.gameManager = gameManager;
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        
        // --- MODIFIED: Initialize the new text field ---
        userIdField = new StyledTextField("Enter User ID...");

        loadResources();
        initComponents();
        initCircles();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (BouncingCircle c : circles)
                    if (c.contains(e.getPoint())) c.changeColor();
            }
        });
        timer = new Timer(40, this);
        startAnimation();
    }
    
    private void loadResources() {
        try {
            URL bgUrl = getClass().getResource("/icons/start_bg.png");
            if (bgUrl != null) {
                backgroundImage = ImageIO.read(bgUrl);
            } else {
                System.err.println("Start screen background not found!");
            }
        } catch (IOException e) {
            System.err.println("Failed to load start screen background.");
        }
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Welcome to Hololive Suika!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, gbc);

        // --- MODIFIED: Remove the old JLabel and just add our new text field ---
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Make the text field wider
        gbc.ipadx = 100; 
        add(userIdField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0; // Reset padding

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setOpaque(false);

        int buttonWidth = 80;
        JButton startButton = createScaledButton("/icons/playButton.png", buttonWidth);
        startButton.addActionListener(e -> {
            stopAnimation();
            String userId = userIdField.getText();
            // Do not start game if the placeholder is still showing
            if (userId.equals("Enter User ID...")) {
                userId = "";
            }
            gameManager.startGame(userId);
        });
        buttonPanel.add(startButton);

        JButton exitButton = createScaledButton("/icons/closeButton.png", buttonWidth);
        exitButton.addActionListener(e -> gameManager.exitGame());
        buttonPanel.add(exitButton);

        gbc.insets = new Insets(20, 10, 10, 10);
        add(buttonPanel, gbc);
    }
    
    private JButton createScaledButton(String imagePath, int targetWidth) {
        JButton button;
        try {
            URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl == null) {
                System.err.println("Button image not found: " + imagePath);
                return new JButton("?"); 
            }
            
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image originalImage = originalIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance(targetWidth, -1, Image.SCALE_SMOOTH);
            button = new JButton(new ImageIcon(scaledImage));
            
        } catch (Exception e) {
            e.printStackTrace();
            button = new JButton("Error"); 
        }
        
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void initCircles() {
        for (int i = 0; i < NUM_CIRCLES; i++)
            circles.add(new BouncingCircle(WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    public void startAnimation() {
        if (!timer.isRunning()) timer.start();
    }

    public void stopAnimation() {
        if (timer.isRunning()) timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (BouncingCircle c : circles) {
            c.draw(g2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int w = getWidth() > 0 ? getWidth() : WINDOW_WIDTH;
        int h = getHeight() > 0 ? getHeight() : WINDOW_HEIGHT;
        for (BouncingCircle c : circles) c.move(w, h);
        repaint();
    }
}
