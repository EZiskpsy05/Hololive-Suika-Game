package main;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

class ScoreManager {
    private static final String SCORE_FILE = "scores.txt";

    public static void saveScore(String userId, int score) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE, true))) {
            writer.println(userId + " - " + score);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving score: " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<String> loadScores() {
        List<String> scores = new ArrayList<>();
        File file = new File(SCORE_FILE);
        if (!file.exists()) return scores;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) scores.add(scanner.nextLine());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading scores: " + e.getMessage(), "File I/O Error", JOptionPane.ERROR_MESSAGE);
        }
        scores.sort((s1, s2) -> {
            try {
                int score1 = Integer.parseInt(s1.substring(s1.lastIndexOf(" ") + 1));
                int score2 = Integer.parseInt(s2.substring(s2.lastIndexOf(" ") + 1));
                return Integer.compare(score2, score1);
            } catch (NumberFormatException e) { return 0; }
        });
        return scores;
    }
}

public class GameManager {
    private final JFrame window;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanelContainer = new JPanel(cardLayout);

    private final StartScreen startScreen;
    private ShouldbeMain gameScreen; // This is the main game panel
    private EndScreen endScreen;

    public static final String START_SCREEN_KEY = "START";
    public static final String GAME_SCREEN_KEY = "GAME";
    public static final String END_SCREEN_KEY = "END";

    private String currentUserId;
    private int lastScore;

    public GameManager(JFrame window) {
        this.window = window;
        startScreen = new StartScreen(this);
        mainPanelContainer.add(startScreen, START_SCREEN_KEY);
        window.add(mainPanelContainer);
    }

    public void startGame(String userId) {
        this.currentUserId = userId;
        // Clean up old game screen if it exists
        if (gameScreen != null) {
            gameScreen.prepareToClose(); // Stop animations and timers
            mainPanelContainer.remove(gameScreen);
        }
        gameScreen = new ShouldbeMain(this); // Create new game instance
        mainPanelContainer.add(gameScreen, GAME_SCREEN_KEY);
        cardLayout.show(mainPanelContainer, GAME_SCREEN_KEY);
        gameScreen.requestFocusInWindow();
    }

    /**
     * MODIFIED: Now cleans up the game screen by calling prepareToClose()
     * before switching to the end screen. This stops the game timer and physics.
     */
    public void endGame(int score) {
        this.lastScore = score;
        if (currentUserId == null || currentUserId.trim().isEmpty()) currentUserId = "Guest";
        
        // Stop all game activities before showing the end screen
        if (gameScreen != null) {
            gameScreen.prepareToClose();
        }
        
        // Clean up old end screen if it exists
        if (endScreen != null) {
            mainPanelContainer.remove(endScreen);
        }
        endScreen = new EndScreen(this, currentUserId, lastScore);
        mainPanelContainer.add(endScreen, END_SCREEN_KEY);
        cardLayout.show(mainPanelContainer, END_SCREEN_KEY);
    }

    public void showStartScreen() {
        // Clean up the game screen before showing the start screen
        if (gameScreen != null) {
            gameScreen.prepareToClose(); // Stop animations and timers
            mainPanelContainer.remove(gameScreen);
            gameScreen = null;
        }
        cardLayout.show(mainPanelContainer, START_SCREEN_KEY);
        startScreen.startAnimation();
    }

    public void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(window, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) System.exit(0);
    }

    // --- GETTER METHODS ---

    public String getCurrentUserId() { return currentUserId; }
    public int getLastScore() { return lastScore; }
    public JFrame getWindow() { return window; }
    
    public ShouldbeMain getGameScreen() {
        return gameScreen;
    }
}
