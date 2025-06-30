// File: shapes/MyShape.java
package shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class MyShape extends Rectangle {

    private static final long serialVersionUID = 1L;

    protected double x, y, width, height;
    
    // --- Physics Properties ---
    private double mass;
    private double gravity = 300.0; // Increased gravity for faster falling
    private double bounce = 0.10;   // Lower bounce for less vertical movement
    private double vx, v;          // Linear velocities
    
    // --- Rotational Physics Properties ---
    private double rotationAngle = 0;
    private double angularVelocity = 0;
    private double momentOfInertia;

    private boolean selected = false;

    public MyShape(double x, double y, double width, double height, Color borderColor, Color insiderColor, double mass) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.mass = mass > 0 ? mass : 1;
        
        double radius = width / 2.0;
        this.momentOfInertia = 0.5 * this.mass * (radius * radius);
        if (this.momentOfInertia == 0) this.momentOfInertia = 1;

        updateRectangleBounds();
    }
    
    protected void updateRectangleBounds() {
        super.x = (int) Math.round(this.x);
        super.y = (int) Math.round(this.y);
        super.width = (int) Math.round(this.width);
        super.height = (int) Math.round(this.height);
    }

    public MyShape(double x, double y, double width, double height) {
        this(x, y, width, height, Color.BLACK, Color.GRAY, 1.0);
    }

    /**
     * MODIFIED: Contains improved ground collision logic to prevent unwanted bouncing.
     */
    public void applyPhysics(double dt, double sceneBottomY, double sceneLeft, double sceneRight) {
        // Update position based on velocity
        v += gravity * dt;
        setX(this.x + vx * dt);
        setY(this.y + v * dt);

        // Update rotation and apply angular damping (air/rolling resistance)
        rotationAngle += angularVelocity * dt;
        angularVelocity *= 0.98;

        boolean onGround = false;

        // --- Bottom boundary collision ---
        if (getY() + getHeight() >= sceneBottomY) {
            setY(sceneBottomY - getHeight());
            onGround = true;

            // --- Only bounce if velocity is significant and not being pushed horizontally ---
            if (Math.abs(v) > 2.0) {
                v = -v * bounce;
            } else {
                v = 0; // Stop vertical movement
            }

            // Apply friction with the floor for rolling
            vx *= 0.94; // More friction for less sliding
            angularVelocity *= 0.94;
        }

        // Side boundary collisions
        if (getX() < sceneLeft) {
            setX(sceneLeft);
            vx = -vx * bounce;
        } else if (getX() + getWidth() > sceneRight) {
            setX(sceneRight - getWidth());
            vx = -vx * bounce;
        }

        // Stop tiny movements to prevent jittering and save performance
        if (Math.abs(v) < 0.1) v = 0;
        if (Math.abs(vx) < 0.1) vx = 0;
        if (Math.abs(angularVelocity) < 0.01) angularVelocity = 0;
    }

    public void applyTorque(double torque) {
        if (momentOfInertia > 0) {
            this.angularVelocity += torque / this.momentOfInertia;
        }
    }

    public abstract void draw(Graphics g);
    public abstract double getArea();

    // Standard Getters and Setters
    @Override public double getX() { return x; }
    public void setX(double x) { this.x = x; updateRectangleBounds(); }
    @Override public double getY() { return y; }
    public void setY(double y) { this.y = y; updateRectangleBounds(); }
    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return height; }
    public double getV() { return v; }
    public void setV(double v) { this.v = v; }
    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }
    public double getMass() { return mass; }
    public double getBounce() { return bounce; }
    public double getRotationAngle() { return rotationAngle; }
    public boolean getSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    @Override public Rectangle getBounds() { return new Rectangle((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height)); }
}
