// File: panels/ScenePanel.java
package panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import main.GameManager;
import main.ShouldbeMain;
import shapes.BallType;
import shapes.Circle;
import shapes.MyShape;
import utils.SoundUtils;

public class ScenePanel extends JPanel implements MouseListener, MouseMotionListener {

    private final ArrayList<MyShape> shapes;
    private final GameManager gameManager;
    private final ShouldbeMain mainPanel;

    private volatile boolean running = true;
    private Thread animationThread;

    private BallType nextBallTypeToDrop;
    private Point mouseDropPos = new Point();
    private boolean canDropBall = true;
    private static final long DROP_COOLDOWN = 500;
    private long lastDropTime = 0;

    private static final int GAME_OVER_LINE_Y_OFFSET = 50;
    private long gameOverCheckStartTime = -1;
    private static final long GAME_OVER_THRESHOLD_MS = 2000;

    private BufferedImage backgroundImage;

    public ScenePanel(ArrayList<MyShape> shapes, GameManager gameManager, ShouldbeMain mainPanel) {
        this.shapes = shapes;
        this.gameManager = gameManager;
        this.mainPanel = mainPanel;
        loadBackgroundImage();
        addMouseListener(this);
        addMouseMotionListener(this);
        startAnimation();
    }

    private void loadBackgroundImage() {
        try {
            URL imgUrl = getClass().getResource("/icons/bg.png");
            backgroundImage = (imgUrl != null) ? ImageIO.read(imgUrl) : null;
            if (backgroundImage == null) setBackground(new Color(173, 216, 230));
        } catch (IOException e) {
            setBackground(new Color(173, 216, 230));
        }
    }

    public void setNextBallTypeForDropping(BallType type) {
        this.nextBallTypeToDrop = type;
    }

    private void startAnimation() {
        animationThread = new Thread(() -> {
            while (running) {
                long frameStart = System.currentTimeMillis();

                if (getWidth() > 0 && getHeight() > 0) {
                    updatePhysics(1.0 / ShouldbeMain.FPS);
                    for (int i = 0; i < 3; i++) {
                        checkAndResolveCollisions();
                        handleMerging();
                    }
                    checkGameOver();
                }

                SwingUtilities.invokeLater(this::repaint);

                long sleep = (1000L / ShouldbeMain.FPS) - (System.currentTimeMillis() - frameStart);
                if (sleep > 0) {
                    try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); running = false; }
                }
            }
        });
        animationThread.start();
    }

    private void updatePhysics(double dt) {
        for (MyShape shape : shapes) {
            shape.applyPhysics(dt, getHeight(), 0, getWidth());
        }
    }

    private void checkAndResolveCollisions() {
        for (int i = 0; i < shapes.size(); i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                MyShape s1 = shapes.get(i), s2 = shapes.get(j);

                double c1x = s1.getX() + s1.getWidth() / 2, c1y = s1.getY() + s1.getHeight() / 2;
                double c2x = s2.getX() + s2.getWidth() / 2, c2y = s2.getY() + s2.getHeight() / 2;
                double dx = c1x - c2x, dy = c1y - c2y;
                double distance = Math.hypot(dx, dy);
                double minDist = (s1.getWidth() + s2.getWidth()) / 2;

                if (distance < minDist && distance > 0) {
                    double overlap = minDist - distance;
                    double angle = Math.atan2(dy, dx);
                    double cos = Math.cos(angle), sin = Math.sin(angle);
                    double half = overlap / 2;

                    // Move both balls apart equally in both X and Y
                    s1.setX(s1.getX() + half * cos);
                    s1.setY(s1.getY() + half * sin);
                    s2.setX(s2.getX() - half * cos);
                    s2.setY(s2.getY() - half * sin);

                    // Optional: transfer a bit of velocity for realism
                    double push = 0.2;
                    s1.setVx(s1.getVx() + push * cos);
                    s2.setVx(s2.getVx() - push * cos);
                }
            }
        }
    }

    private void handleMerging() {
        java.util.List<MyShape> toAdd = new ArrayList<>();
        Set<MyShape> toRemove = new HashSet<>();
        for (int i = 0; i < shapes.size(); i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                MyShape s1 = shapes.get(i), s2 = shapes.get(j);
                if (toRemove.contains(s1) || toRemove.contains(s2)) continue;
                if (s1 instanceof Circle c1 && s2 instanceof Circle c2 && c1.getType() == c2.getType()) {
                    double c1x = c1.getX() + c1.getWidth() / 2, c1y = c1.getY() + c1.getHeight() / 2;
                    double c2x = c2.getX() + c2.getWidth() / 2, c2y = c2.getY() + c2.getHeight() / 2;
                    double distance = Math.hypot(c1x - c2x, c1y - c2y);
                    double combinedRadii = (c1.getWidth() + c2.getWidth()) / 2;
                    if (distance < combinedRadii * 1.02) {
                        toRemove.add(c1); toRemove.add(c2);
                        BallType nextType = BallType.getNext(c1.getType());
                        if (nextType != null) {
                            double mx = (c1x + c2x) / 2, my = (c1y + c2y) / 2;
                            toAdd.add(new Circle(nextType, mx, my));
                            mainPanel.addScore(nextType.scoreValue);
                        } else {
                            mainPanel.addScore(BallType.LEVEL_10.scoreValue * 2);
                        }
                        SoundUtils.playSound("/audio/combine.wav");
                        break;
                    }
                }
            }
        }
        if (!toRemove.isEmpty()) {
            shapes.removeAll(toRemove);
            shapes.addAll(toAdd);
        }
    }

    private void checkGameOver() {
        boolean isAnyBallAboveLine = shapes.stream().anyMatch(shape ->
            shape.getY() < GAME_OVER_LINE_Y_OFFSET &&
            Math.abs(shape.getV()) < 0.5 &&
            Math.abs(shape.getVx()) < 0.5
        );
        if (isAnyBallAboveLine) {
            if (gameOverCheckStartTime == -1) {
                gameOverCheckStartTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - gameOverCheckStartTime > GAME_OVER_THRESHOLD_MS) {
                if (running) {
                    running = false;
                    SwingUtilities.invokeLater(() -> gameManager.endGame(mainPanel.getScore()));
                }
            }
        } else {
            gameOverCheckStartTime = -1;
        }
    }

    private void attemptDropBall() {
        if (nextBallTypeToDrop == null || !canDropBall) return;
        if (System.currentTimeMillis() - lastDropTime < DROP_COOLDOWN) return;
        double radius = nextBallTypeToDrop.radius;
        double dropX = Math.max(radius, Math.min(mouseDropPos.x, getWidth() - radius));
        shapes.add(new Circle(nextBallTypeToDrop, dropX, radius));
        mainPanel.generateNewNextBall();
        canDropBall = false;
        lastDropTime = System.currentTimeMillis();
        new javax.swing.Timer((int) DROP_COOLDOWN, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            canDropBall = true;
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(240, 229, 210));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        if (nextBallTypeToDrop != null && canDropBall) {
            ImageIcon icon = nextBallTypeToDrop.getResizedImageIcon((int) nextBallTypeToDrop.radius * 2);
            if (icon != null) {
                double radius = nextBallTypeToDrop.radius;
                double previewX = Math.max(radius, Math.min(mouseDropPos.x, getWidth() - radius));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.drawImage(icon.getImage(), (int) (previewX - radius), (int) (GAME_OVER_LINE_Y_OFFSET / 2 - radius), null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }
        for (MyShape shape : shapes) shape.draw(g);
    }

    public void stopAnimation() {
        running = false;
        if (animationThread != null) animationThread.interrupt();
    }

    @Override public void mouseClicked(MouseEvent e) { if (e.getButton() == MouseEvent.BUTTON1) attemptDropBall(); }
    @Override public void mouseMoved(MouseEvent e) { mouseDropPos = e.getPoint(); }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) { setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)); }
    @Override public void mouseExited(MouseEvent e) { setCursor(Cursor.getDefaultCursor()); }
    @Override public void mouseDragged(MouseEvent e) {}
}
