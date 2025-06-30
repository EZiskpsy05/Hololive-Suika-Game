// File: main/ShouldbeMain.java
package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import panels.*;
import shapes.*;

public final class ShouldbeMain extends JPanel {

    public static final int FPS = 75;

    private final ScenePanel scenePanel;
    private final ShapePanel shapePanel;
    private final FeaturesPanel featuresPanel;
    private final GameManager gameManager;
    private final ArrayList<MyShape> shapesInScene = new ArrayList<>();
    private final Random random = new Random();

    private int currentScore = 0;
    private BallType currentNextBallType;

    public ShouldbeMain(GameManager gameManager) {
        this.gameManager = gameManager;
        setLayout(new BorderLayout());

        shapePanel = new ShapePanel();
        featuresPanel = new FeaturesPanel(gameManager);
        scenePanel = new ScenePanel(shapesInScene, gameManager, this);

        add(shapePanel, BorderLayout.WEST);
        add(featuresPanel, BorderLayout.EAST);
        add(scenePanel, BorderLayout.CENTER);

        generateNewNextBall();
        String userId = gameManager.getCurrentUserId();
        if (userId != null) {
            featuresPanel.setUserId(userId);
        }
        featuresPanel.setScore(currentScore);
        featuresPanel.startTimer();

        scenePanel.setFocusable(true);
    }

    public void generateNewNextBall() {
        int initialLevelsRange = 3;
        currentNextBallType = BallType.values()[random.nextInt(initialLevelsRange)];
        shapePanel.setNextBallType(currentNextBallType);
        scenePanel.setNextBallTypeForDropping(currentNextBallType);
    }

    public void addScore(int points) {
        currentScore += points;
        featuresPanel.setScore(currentScore);
    }

    public int getScore() {
        return currentScore;
    }

    /**
     * Stops the game timer and animation to ensure all game-related processes are terminated.
     */
    public void prepareToClose() {
        if (scenePanel != null) scenePanel.stopAnimation();
        if (featuresPanel != null) featuresPanel.stopTimer();
    }
}
