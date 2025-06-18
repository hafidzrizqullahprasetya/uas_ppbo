package models;

import enums.DifficultyLevel;
import interfaces.GameConstants;
import java.awt.Color;
import java.util.Random;

public class ObstacleCar extends Vehicle {

    private Random random;
    private DifficultyLevel difficulty;
    private int currentLevel;
    private String[] carTypes = { "gamecar1", "gamecar2", "gamecar3", "gamecar4" };
    private boolean needsRespawn = false;
    private double lastRespawnY = 0;

    public ObstacleCar(double x, double y, String carType, DifficultyLevel difficulty,
            int currentLevel, Random random) {
        super(x, y, carType, Color.ORANGE);

        this.difficulty = difficulty != null ? difficulty : DifficultyLevel.EASY;
        this.currentLevel = Math.max(1, currentLevel);
        this.random = random != null ? random : new Random();

        updateSpeedForLevel();
        setActive(true);

        if (GameConstants.DEBUG_MODE) {
            System.out.println("🚗 ObstacleCar created at (" + x + ", " + y +
                    ") with speed: " + String.format("%.1f", speed));
        }
    }

    public ObstacleCar(double x, double y, double speed) {
        super(x, y, "gamecar1", Color.ORANGE);
        this.random = new Random();
        this.difficulty = DifficultyLevel.EASY;
        this.currentLevel = 1;
        this.speed = speed;
        setActive(true);
    }

    @Override
    public void update() {
        if (!isActive())
            return;

        updateMovement();

        if (needsRespawn || getY() > GameConstants.SCREEN_HEIGHT + 100) {
            respawnWithSafePosition();
            needsRespawn = false;
        }

        maintainLaneBounds();

        if (getY() > GameConstants.SCREEN_HEIGHT + 200) {
            setActive(false);
        }
    }

    @Override
    public void updateMovement() {

        setY(getY() + speed);

        if (random.nextDouble() < 0.002) {
            double horizontalDrift = (random.nextDouble() - 0.5) * 1.5;
            setX(getX() + horizontalDrift);
        }

        if (GameConstants.DEBUG_MODE && random.nextDouble() < 0.001) {
            System.out.println("🚗 Obstacle moving: Y=" + String.format("%.1f", getY()) +
                    " Speed=" + String.format("%.1f", speed));
        }
    }

    private void respawnWithSafePosition() {
        int maxAttempts = 15;
        boolean positionFound = false;

        for (int attempt = 0; attempt < maxAttempts && !positionFound; attempt++) {
            double newX = getRandomXPosition();
            double newY = getRandomYPosition();

            if (isPositionSafe(newX, newY)) {
                setX(newX);
                setY(newY);
                lastRespawnY = newY;
                positionFound = true;

                updateSpeedForLevel();
                randomizeCarType();
                setActive(true);

                if (GameConstants.DEBUG_MODE) {
                    System.out.println("🔄 Obstacle respawned safely at (" +
                            String.format("%.1f", newX) + ", " +
                            String.format("%.1f", newY) + ") attempt: " +
                            (attempt + 1));
                }
            }
        }

        if (!positionFound) {
            forceSpawnAtSafeDistance();
        }
    }

    private boolean isPositionSafe(double newX, double newY) {

        final double MIN_HORIZONTAL_DISTANCE = getWidth() + 10;
        final double MIN_VERTICAL_DISTANCE = getHeight() * 2;

        if (Math.abs(newY - lastRespawnY) < MIN_VERTICAL_DISTANCE) {
            return false;
        }

        if (newX + getWidth() > GameConstants.CENTER_DIVIDER_LEFT - 10 &&
                newX < GameConstants.CENTER_DIVIDER_RIGHT + 10) {
            return false;
        }

        return true;
    }

    private void forceSpawnAtSafeDistance() {

        double safeY = -200 - random.nextInt(400);
        double safeX = getRandomXPosition();

        setX(safeX);
        setY(safeY);
        lastRespawnY = safeY;
        updateSpeedForLevel();
        randomizeCarType();
        setActive(true);

        if (GameConstants.DEBUG_MODE) {
            System.out.println("🆘 Obstacle force-spawned at safe distance");
        }
    }

    private double getRandomXPosition() {

        int[] availableLanes = getAvailableLanes();
        int selectedLane = availableLanes[random.nextInt(availableLanes.length)];

        return getLaneXPosition(selectedLane);
    }

    private int[] getAvailableLanes() {

        return new int[] { 0, 1, 2, 3, 4, 5 };
    }

    private double getLaneXPosition(int laneIndex) {
        int totalLanes = 6;
        double laneWidth = (GameConstants.RIGHT_BOUNDARY - GameConstants.LEFT_BOUNDARY) / totalLanes;

        if (laneIndex >= 3) {
            laneIndex++;
        }

        double laneStartX = GameConstants.LEFT_BOUNDARY + (laneIndex * laneWidth);
        double laneCenterX = laneStartX + (laneWidth / 2) - (getWidth() / 2);

        double randomOffset = (random.nextDouble() - 0.5) * (laneWidth * 0.3);

        return Math.max(GameConstants.LEFT_BOUNDARY + 10,
                Math.min(laneCenterX + randomOffset,
                        GameConstants.RIGHT_BOUNDARY - getWidth() - 10));
    }

    private double getRandomYPosition() {
        return -getHeight() - 50 - random.nextInt(400);
    }

    private void maintainLaneBounds() {
        double leftBound = GameConstants.LEFT_BOUNDARY + 5;
        double rightBound = GameConstants.RIGHT_BOUNDARY - getWidth() - 5;

        double centerLeft = GameConstants.CENTER_DIVIDER_LEFT - 5;
        double centerRight = GameConstants.CENTER_DIVIDER_RIGHT + 5;

        if (getX() < leftBound) {
            setX(leftBound);
        } else if (getX() > rightBound) {
            setX(rightBound);
        } else if (getX() + getWidth() > centerLeft && getX() < centerRight) {

            if (getX() < (centerLeft + centerRight) / 2) {
                setX(centerLeft - getWidth());
            } else {
                setX(centerRight);
            }
        }
    }

    public void updateSpeedForLevel() {
        if (difficulty == null) {
            speed = 2.0 + random.nextDouble() * 3.0;
            return;
        }

        try {
            double minSpeed = difficulty.getMinSpeed(currentLevel);
            double maxSpeed = difficulty.getMaxSpeed(currentLevel);

            if (maxSpeed <= minSpeed) {
                maxSpeed = minSpeed + 2.0;
            }

            double newSpeed = minSpeed + random.nextDouble() * (maxSpeed - minSpeed);
            setSpeed(newSpeed);

            if (GameConstants.DEBUG_MODE) {
                System.out.println("🎯 Speed updated: " + String.format("%.1f", newSpeed) +
                        " (Range: " + minSpeed + "-" + maxSpeed + ")");
            }

        } catch (Exception e) {
            System.err.println("❌ Error updating speed: " + e.getMessage());
            speed = 3.0;
        }
    }

    private void randomizeCarType() {
        try {
            String oldType = this.imageType;
            String newType = carTypes[random.nextInt(carTypes.length)];

            int attempts = 0;
            while (newType.equals(oldType) && attempts < 5) {
                newType = carTypes[random.nextInt(carTypes.length)];
                attempts++;
            }

            this.imageType = newType;

            Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
                    Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW.darker() };
            setColor(colors[random.nextInt(colors.length)]);

        } catch (Exception e) {
            System.err.println("❌ Error randomizing car type: " + e.getMessage());
        }
    }

    public boolean checkCollisionWithPlayer(Player player) {
        if (player == null || !player.isAlive() || player.isInvulnerable()) {
            return false;
        }

        double margin = 5.0;
        return checkCollision(
                player.getX() + margin,
                player.getY() + margin,
                player.getWidth() - 2 * margin,
                player.getHeight() - 2 * margin);
    }

    public boolean checkCollisionWithOtherCar(ObstacleCar other) {
        if (this == other || other == null || !other.isActive()) {
            return false;
        }

        double minDistance = Math.max(getHeight(), other.getHeight()) * 1.5;
        double actualDistance = Math.abs(getY() - other.getY());

        if (actualDistance < minDistance) {
            return checkCollision(other.getX(), other.getY(),
                    other.getWidth(), other.getHeight());
        }

        return false;
    }

    public void triggerRespawn() {
        needsRespawn = true;
    }

    public void reset(double x, double y, DifficultyLevel difficulty, int level) {
        setPosition(x, y);
        this.difficulty = difficulty;
        this.currentLevel = level;
        setActive(true);
        needsRespawn = false;
        updateSpeedForLevel();
        randomizeCarType();
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = Math.max(1, level);
        updateSpeedForLevel();
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty != null ? difficulty : DifficultyLevel.EASY;
        updateSpeedForLevel();
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = Math.max(0.5, Math.min(speed, 15.0));
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public String getCarType() {
        return imageType;
    }

    public boolean isNeedRespawn() {
        return needsRespawn;
    }

    public double getDistanceToPlayer(Player player) {
        if (player == null)
            return Double.MAX_VALUE;

        double dx = getX() - player.getX();
        double dy = getY() - player.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isInView() {
        return getY() > -100 && getY() < GameConstants.SCREEN_HEIGHT + 100;
    }

    @Override
    public String toString() {
        return String.format("ObstacleCar[pos=(%.1f,%.1f), speed=%.1f, level=%d, active=%s]",
                getX(), getY(), speed, currentLevel, isActive());
    }
}