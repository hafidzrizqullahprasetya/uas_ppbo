package models;

import interfaces.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public abstract class Vehicle implements Drawable, Updatable {

    protected double x, y;
    protected double width, height;
    protected double speed;
    protected Color color; 
    protected String imageType;
    protected boolean active;
    protected BufferedImage image;
    protected Color fallbackColor;

    public Vehicle(double x, double y, String imageType, Color fallbackColor) {
        this.x = Math.max(0, x);
        this.y = Math.max(0, y);
        this.imageType = imageType;
        this.fallbackColor = fallbackColor;
        this.active = true;
        loadImage();
    }

    private void loadImage() {
        try {
            this.image = ImageIO.read(getClass().getResource("/assets/" + imageType + ".png"));
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            System.out.println("Warning: Could not load " + imageType + ".png from assets folder");
            this.image = null;
        }
    }

    public abstract void updateMovement();

    @Override
    public void update() {
        if (active) {
            updateMovement();
            validateBounds();
        }
    }

    protected void validateBounds() {
        if (x < 0)
            x = 0;
        if (x > GameConstants.SCREEN_WIDTH - getWidth()) {
            x = GameConstants.SCREEN_WIDTH - getWidth();
        }
        if (y < 0)
            y = 0;
        if (y > GameConstants.SCREEN_HEIGHT - getHeight()) {
            y = GameConstants.SCREEN_HEIGHT - getHeight();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!active)
            return;

        if (image != null) {
            g.drawImage(image, (int) x, (int) y, null);
        } else {

            g.setColor(fallbackColor);
            g.fillRect((int) x, (int) y, (int) getWidth(), (int) getHeight());
        }
    }

    public boolean checkCollision(double otherX, double otherY, double otherWidth, double otherHeight) {
        return active &&
                x < otherX + otherWidth &&
                x + getWidth() > otherX &&
                y < otherY + otherHeight &&
                y + getHeight() > otherY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return GameConstants.CAR_WIDTH;
    }

    public double getHeight() {
        return GameConstants.CAR_HEIGHT;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSpeed(double speed) {
        this.speed = Math.max(0, speed);
    }

    public void setColor(Color color) {
        this.color = color;
        this.fallbackColor = color;
    }

    protected void setX(double x) {
        this.x = Math.max(0, x);
    }

    protected void setY(double y) {
        this.y = Math.max(0, y);
    }
}