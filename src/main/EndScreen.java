package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class EndScreen extends JPanel {
    private final GameManager gameManager;
    private final JLabel titleLabel, scoreLabel, userIdLabel;
    private final JButton saveScoreButton, restartButton, mainMenuButton, exitButton;
    private final JTextArea leaderboardArea;
    private final JScrollPane leaderboardScrollPane;
    private final String userId;
    private final int score;
    private BufferedImage backgroundImage;

    public EndScreen(GameManager gameManager, String userId, int score) {
        this.gameManager = gameManager;
        this.userId = userId;
        this.score = score;

        loadResources();
        setPreferredSize(new Dimension(Window.WINDOW_WIDTH, Window.WINDOW_HEIGHT));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        Color textColor = Color.BLACK;

        titleLabel = createLabel("Game Over!", new Font("Arial", Font.BOLD, 48), textColor);
        userIdLabel = createLabel("User: " + userId, new Font("Arial", Font.PLAIN, 24), textColor);
        scoreLabel = createLabel("Your Score: " + score, new Font("Arial", Font.BOLD, 32), textColor);

        saveScoreButton = createButton("Save Score");
        restartButton = createButton("Restart Game");
        mainMenuButton = createButton("Main Menu");
        exitButton = createButton("Exit Game");

        saveScoreButton.addActionListener(e -> {
            ScoreManager.saveScore(this.userId, this.score);
            JOptionPane.showMessageDialog(this, "Score saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            saveScoreButton.setEnabled(false);
            loadAndDisplayLeaderboard();
        });
        restartButton.addActionListener(e -> gameManager.startGame(this.userId));
        mainMenuButton.addActionListener(e -> gameManager.showStartScreen());
        exitButton.addActionListener(e -> gameManager.exitGame());

        leaderboardArea = new JTextArea(10, 30);
        leaderboardArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        leaderboardArea.setForeground(Color.BLACK);
        leaderboardArea.setBackground(new Color(255, 255, 255, 180));
        leaderboardArea.setEditable(false);
        leaderboardArea.setMargin(new Insets(10, 10, 10, 10));

        leaderboardScrollPane = new JScrollPane(leaderboardArea);
        leaderboardScrollPane.setOpaque(false);
        leaderboardScrollPane.getViewport().setOpaque(false);
        leaderboardScrollPane.setBorder(BorderFactory.createTitledBorder("Leaderboard"));

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        add(titleLabel, gbc);
        add(userIdLabel, gbc);
        add(scoreLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveScoreButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(mainMenuButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 50, 50, 50);
        add(leaderboardScrollPane, gbc);

        loadAndDisplayLeaderboard();
    }

    private JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        return btn;
    }

    private void loadResources() {
        try {
            URL bgUrl = getClass().getResource("/icons/start_bg.png");
            if (bgUrl != null) {
                backgroundImage = ImageIO.read(bgUrl);
            } else {
                System.err.println("End screen background not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load end screen background.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void loadAndDisplayLeaderboard() {
        List<String> scores = ScoreManager.loadScores();
        leaderboardArea.setText("");
        if (scores.isEmpty()) {
            leaderboardArea.append("No scores saved yet.\nBe the first!");
        } else {
            for (String scoreEntry : scores) {
                leaderboardArea.append(scoreEntry + "\n");
            }
        }
        leaderboardArea.setCaretPosition(0);
    }
}
