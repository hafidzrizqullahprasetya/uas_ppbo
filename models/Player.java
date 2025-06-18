package models;

import interfaces.GameConstants;
import java.awt.*;

public class Player extends Vehicle {

    private int lives;
    private String name;
    private boolean invulnerable;
    private int invulnerabilityTimer;
    private int levelsCompleted;
    private Color statusColor;

    private static final int PLAYER_LIVES = 3;
    private static final int INVULNERABILITY_TIME = 60;

    public Player(double x, double y, String imageType, String name, Color statusColor) {
        super(x, y, imageType, statusColor);
        this.name = name != null ? name : "Unknown Player";
        this.statusColor = statusColor;
        this.lives = PLAYER_LIVES;
        this.invulnerable = false;
        this.invulnerabilityTimer = 0;
        this.levelsCompleted = 0;

        System.out.println("✅ Player created: " + this.name + " at (" + (int) x + ", " + (int) y + ")");
    }

    @Override
    public void updateMovement() {

        if (invulnerable && invulnerabilityTimer > 0) {

            double shakeX = (Math.random() - 0.5) * 2;
            double shakeY = (Math.random() - 0.5) * 2;
            setX(getX() + shakeX);
            setY(getY() + shakeY);
            validateBounds();
        }
    }

    @Override
    public void update() {
        super.update();

        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
                System.out.println("🛡️ " + name + " tidak lagi invulnerable");
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!active)
            return;

        if (invulnerable && (invulnerabilityTimer / 5) % 2 == 0) {
            return;
        }

        super.draw(g);

        if (active) {
            g.setColor(statusColor);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String shortName = name.length() > 10 ? name.substring(0, 10) : name;
            int nameX = (int) (getX() + (getWidth() - fm.stringWidth(shortName)) / 2);
            int nameY = (int) (getY() - 5);
            g.drawString(shortName, nameX, nameY);
        }
    }

    public void takeDamage() {
        if (!invulnerable && active) {
            lives--;
            invulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_TIME;

            System.out.println("💥 " + name + " terkena damage! Nyawa tersisa: " + lives);

            if (lives <= 0) {
                active = false;
                System.out.println("💀 " + name + " kalah!");
            }
        } else if (invulnerable) {
            System.out.println("🛡️ " + name + " masih invulnerable!");
        }
    }

    public void completeLevel() {
        levelsCompleted++;
        System.out.println("🏆 " + name + " menyelesaikan level! Total: " + levelsCompleted);

        if (levelsCompleted % 3 == 0 && lives < 6) {
            lives++;
            System.out.println("❤️ " + name + " mendapat bonus nyawa! Total: " + lives);
        }
    }

    public void reset(double startX, double startY) {
        setX(startX);
        setY(startY);
        this.lives = PLAYER_LIVES;
        this.active = true;
        this.invulnerable = false;
        this.invulnerabilityTimer = 0;
        this.levelsCompleted = 0;

        System.out.println("🔄 " + name + " direset ke posisi (" + (int) startX + ", " + (int) startY + ")");
    }

    public void moveUp() {
        if (!isAlive())
            return;

        double newY = getY() - GameConstants.MOVE_SPEED;
        setY(Math.max(0, newY));
        validateBounds();

    }

    public void moveDown() {
        if (!isAlive())
            return;

        double newY = getY() + GameConstants.MOVE_SPEED;
        setY(Math.min(GameConstants.SCREEN_HEIGHT - getHeight(), newY));
        validateBounds();

    }

    public void moveLeft() {
        if (!isAlive())
            return;

        double newX = getX() - GameConstants.MOVE_SPEED;
        setX(newX);
        validateBounds();

    }

    public void moveRight() {
        if (!isAlive())
            return;

        double newX = getX() + GameConstants.MOVE_SPEED;
        setX(newX);
        validateBounds();

    }

    @Override
    protected void validateBounds() {

        if (y < 0) {
            y = 0;
        }
        if (y > GameConstants.SCREEN_HEIGHT - getHeight()) {
            y = GameConstants.SCREEN_HEIGHT - getHeight();
        }

        if (name.contains("1") || name.toLowerCase().contains("pemain 1")) {

            enforceLeftLaneBounds();
        } else if (name.contains("2") || name.toLowerCase().contains("pemain 2")) {

            enforceRightLaneBounds();
        } else {

            enforceGeneralBounds();
        }
    }

    private void enforceLeftLaneBounds() {

        if (x < GameConstants.LEFT_BOUNDARY) {
            x = GameConstants.LEFT_BOUNDARY;
        }
        if (x + getWidth() > GameConstants.CENTER_DIVIDER_LEFT) {
            x = GameConstants.CENTER_DIVIDER_LEFT - getWidth();
        }
    }

    private void enforceRightLaneBounds() {

        if (x < GameConstants.CENTER_DIVIDER_RIGHT) {
            x = GameConstants.CENTER_DIVIDER_RIGHT;
        }
        if (x + getWidth() > GameConstants.RIGHT_BOUNDARY) {
            x = GameConstants.RIGHT_BOUNDARY - getWidth();
        }
    }

    private void enforceGeneralBounds() {

        if (x < GameConstants.LEFT_BOUNDARY) {
            x = GameConstants.LEFT_BOUNDARY;
        }
        if (x + getWidth() > GameConstants.RIGHT_BOUNDARY) {
            x = GameConstants.RIGHT_BOUNDARY - getWidth();
        }
    }

    public void heal(int amount) {
        lives = Math.min(6, lives + amount);
        System.out.println("❤️ " + name + " heal +" + amount + "! Nyawa: " + lives);
    }

    public void addBonusLife() {
        if (lives < 6) {
            lives++;
            System.out.println("🎁 " + name + " bonus life! Total: " + lives);
        }
    }

    public int getLives() {
        return lives;
    }

    public String getName() {
        return name;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public int getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public Color getStatusColor() {
        return statusColor;
    }

    public boolean isAlive() {
        return active && lives > 0;
    }

    public double getHealthPercentage() {
        return (double) lives / PLAYER_LIVES;
    }

    public String getStatusString() {
        String status = isAlive() ? "HIDUP" : "MATI";
        String invulnStatus = invulnerable ? " (INVULNERABLE)" : "";
        return status + invulnStatus;
    }

    @Override
    public String toString() {
        return String.format("%s - Lives: %d, Level: %d, Status: %s",
                name, lives, levelsCompleted, getStatusString());
    }
}