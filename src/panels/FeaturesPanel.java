package panels;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import main.GameManager;


public class FeaturesPanel extends JPanel {

    private static final int PANEL_WIDTH = 150;
    private static final int INITIAL_TIME_SECONDS = 300;

    private final GameManager gameManager;
    private final JLabel userIdLabel = new JLabel("User: Guest", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel timerLabel = new JLabel("Time: 5:00", SwingConstants.CENTER);
    private final JButton endGameButton = new JButton("End Game");
    private ImageIcon scoreBackgroundImage;
    private ImageIcon panelBackgroundImage;

    private final Timer gameTimer;
    private int timeLeftInSeconds = INITIAL_TIME_SECONDS;

    public FeaturesPanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.DARK_GRAY));

        loadResources();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // User ID Display
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userIdLabel.setForeground(Color.BLACK);
        if (gameManager != null && gameManager.getCurrentUserId() != null) {
            setUserId(gameManager.getCurrentUserId());
        }
        add(userIdLabel, gbc);

        // Timer Display
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.BLACK);
        gbc.insets = new Insets(20, 10, 20, 10);
        add(timerLabel, gbc);
        gbc.insets = new Insets(10, 10, 10, 10);

        // Score Display
        if (scoreBackgroundImage != null) {
            scoreLabel.setIcon(scoreBackgroundImage);
        }
        scoreLabel.setHorizontalTextPosition(JLabel.CENTER);
        scoreLabel.setVerticalTextPosition(JLabel.CENTER);
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        scoreLabel.setForeground(Color.BLACK);
        add(scoreLabel, gbc);

        // Spacer
        gbc.weighty = 1.0;
        add(Box.createVerticalGlue(), gbc);

        // End Game Button
        endGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        endGameButton.setForeground(Color.BLACK);
        endGameButton.addActionListener(e -> {
            if (gameManager != null) {
                int currentScore = (gameManager.getGameScreen() != null)
                        ? gameManager.getGameScreen().getScore() : 0;
                gameManager.endGame(currentScore);
            }
        });
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(endGameButton, gbc);

        // Timer setup
        gameTimer = new Timer(1000, e -> updateTimer());
    }

    private void updateTimer() {
        timeLeftInSeconds--;
        int minutes = timeLeftInSeconds / 60;
        int seconds = timeLeftInSeconds % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, seconds));

        if (timeLeftInSeconds <= 0) {
            stopTimer();
            SwingUtilities.invokeLater(() -> {
                if (gameManager != null) {
                    int currentScore = (gameManager.getGameScreen() != null)
                            ? gameManager.getGameScreen().getScore() : 0;
                    gameManager.endGame(currentScore);
                }
            });
        }
    }

    public void startTimer() {
        if (!gameTimer.isRunning()) {
            timeLeftInSeconds = INITIAL_TIME_SECONDS;
            timerLabel.setText("Time: 5:00");
            gameTimer.start();
        }
    }

    public void stopTimer() {
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    private void loadResources() {
        try {
            // Load the panel background image
            URL bgUrl = getClass().getResource("/icons/start_bg.png");
            if (bgUrl != null) {
                panelBackgroundImage = new ImageIcon(bgUrl);
            } else {
                System.err.println("Panel background image not found: /icons/panel_bg.png");
            }

            URL imgUrl = getClass().getResource("/icons/score.png");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image originalImage = originalIcon.getImage();
                int newWidth = PANEL_WIDTH - 20;
                Image scaledImage = originalImage.getScaledInstance(newWidth, -1, Image.SCALE_SMOOTH);
                scoreBackgroundImage = new ImageIcon(scaledImage);
                scoreLabel.setIcon(scoreBackgroundImage);
            } else {
                System.err.println("Score background image not found: /icons/score.png");
            }
        } catch (Exception e) {
            System.err.println("Failed to load score or panel background image.");
        }
    }

    public void setUserId(String userId) {
        userIdLabel.setText("User: " + (userId == null || userId.trim().isEmpty() ? "Guest" : userId));
    }

    public void setScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (panelBackgroundImage != null) {
            Image img = panelBackgroundImage.getImage();
            int imgW = img.getWidth(this);
            int imgH = img.getHeight(this);
            int panelW = getWidth();
            int panelH = getHeight();

            // Draw only the top-left portion of the image that fits the panel (cut, not stretch)
            g.drawImage(
                img,
                0, 0, panelW, panelH, // destination rectangle (panel)
                0, 0, Math.min(panelW, imgW), Math.min(panelH, imgH), // source rectangle (image)
                this
            );
        }
    }
}
