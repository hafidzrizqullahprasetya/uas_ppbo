package managers;

import models.*;
import enums.*;
import interfaces.GameConstants;
import java.util.*;
import java.awt.Color;

public class GameManager {

    private GameState gameState;
    private DifficultyLevel difficulty;
    private int currentLevel;
    private int gameTimer;
    private int totalSurvivalTime;
    private Player player1, player2;
    private List<ObstacleCar> obstacles;
    private List<TreeObject> trees;
    private List<Particle> particles;
    private Random random;

    private InputManager inputManager;
    private CollisionManager collisionManager;

    public GameManager() {

        this.gameState = GameState.MAIN_MENU;
        this.difficulty = DifficultyLevel.MEDIUM;
        this.currentLevel = 1;
        this.gameTimer = 30;
        this.totalSurvivalTime = 0;
        this.random = new Random();

        this.inputManager = new InputManager();
        this.collisionManager = new CollisionManager();

        this.obstacles = new ArrayList<>();
        this.trees = new ArrayList<>();
        this.particles = new ArrayList<>();

        initializePlayers();
        initializeTrees();
    }

    private void initializePlayers() {
        player1 = new Player(200, 750, "gamecar3", "Pemain 1", new Color(0, 150, 255));
        player2 = new Player(1000, 750, "gamecar4", "Pemain 2", new Color(255, 100, 100));
    }

    private void initializeTrees() {
        trees = new ArrayList<>();

        try {

            String[] treeTypes = { "oak", "pine", "bush", "flower", "rock" };

            for (int i = 0; i < 10; i++) {
                String randomType = treeTypes[random.nextInt(treeTypes.length)];
                trees.add(new TreeObject(15, i * 200 - 300, randomType));
            }

            for (int i = 0; i < 10; i++) {
                String randomType = treeTypes[random.nextInt(treeTypes.length)];
                trees.add(new TreeObject(1245, i * 200 - 200, randomType));
            }

            if (GameConstants.DEBUG_MODE) {
                System.out.println("✅ Trees initialized: " + trees.size() + " objects");
            }

        } catch (Exception e) {
            System.err.println("❌ Error initializing trees: " + e.getMessage());
            trees = new ArrayList<>();
        }
    }

    public void update() {
        if (gameState != GameState.PLAYING)
            return;

        try {

            updateObstacles();

            updateParticles();
            updateTrees();

            checkCollisions();

            cleanupInactiveObjects();

        } catch (Exception e) {
            System.err.println("❌ Error in GameManager.update(): " + e.getMessage());
            if (GameConstants.DEBUG_MODE) {
                e.printStackTrace();
            }
        }
    }

    private void updateObstacles() {
        if (obstacles == null)
            return;

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            ObstacleCar obstacle = obstacles.get(i);
            if (obstacle != null && obstacle.isActive()) {
                obstacle.update();

                if (obstacle.getY() > GameConstants.SCREEN_HEIGHT + 100) {
                    respawnObstacleWithSeparation(obstacle);
                }
            } else {
                obstacles.remove(i);
            }
        }

        maintainObstacleSeparation();

        ensureMinimumObstacles();
    }

    private void respawnObstacleWithSeparation(ObstacleCar obstacle) {
        int maxAttempts = 10;
        boolean respawned = false;

        for (int attempt = 0; attempt < maxAttempts && !respawned; attempt++) {

            int laneIndex = random.nextInt(GameConstants.LANE_COUNT);
            double newX = GameConstants.LEFT_BOUNDARY + (laneIndex * GameConstants.LANE_WIDTH) +
                    (GameConstants.LANE_WIDTH - obstacle.getWidth()) / 2;

            double newY = -obstacle.getHeight() - random.nextInt(400) - (attempt * 50);

            if (isRespawnPositionSafe(newX, newY, obstacle)) {
                obstacle.setPosition(newX, newY);

                DifficultyLevel diff = getDifficulty();
                double minSpeed = diff.getMinSpeed(currentLevel);
                double maxSpeed = diff.getMaxSpeed(currentLevel);
                double newSpeed = minSpeed + (maxSpeed - minSpeed) * random.nextDouble();

                obstacle.setSpeed(newSpeed);
                obstacle.setActive(true);
                respawned = true;

                if (GameConstants.DEBUG_MODE) {
                    System.out.println("🔄 Obstacle respawned with separation at lane " + laneIndex);
                }
            }
        }

        if (!respawned) {
            obstacle.setActive(false);
            if (GameConstants.DEBUG_MODE) {
                System.out.println("⚠️ Obstacle deactivated - couldn't find safe respawn position");
            }
        }
    }

    private boolean isRespawnPositionSafe(double x, double y, ObstacleCar respawningObstacle) {
        final double MIN_DISTANCE = GameConstants.CAR_HEIGHT * 2;

        for (ObstacleCar obstacle : obstacles) {
            if (obstacle != null && obstacle.isActive() && obstacle != respawningObstacle) {
                double distance = Math.sqrt(
                        Math.pow(x - obstacle.getX(), 2) +
                                Math.pow(y - obstacle.getY(), 2));

                if (distance < MIN_DISTANCE) {
                    return false;
                }
            }
        }

        return true;
    }

    private void maintainObstacleSeparation() {
        final double MIN_SEPARATION = GameConstants.CAR_HEIGHT * 1.5;

        for (int i = 0; i < obstacles.size(); i++) {
            ObstacleCar obstacle1 = obstacles.get(i);
            if (obstacle1 == null || !obstacle1.isActive())
                continue;

            for (int j = i + 1; j < obstacles.size(); j++) {
                ObstacleCar obstacle2 = obstacles.get(j);
                if (obstacle2 == null || !obstacle2.isActive())
                    continue;

                double distance = Math.sqrt(
                        Math.pow(obstacle1.getX() - obstacle2.getX(), 2) +
                                Math.pow(obstacle1.getY() - obstacle2.getY(), 2));

                if (distance < MIN_SEPARATION) {
                    if (obstacle1.getY() > obstacle2.getY()) {
                        obstacle1.setSpeed(obstacle1.getSpeed() * 0.8);
                    } else {
                        obstacle2.setSpeed(obstacle2.getSpeed() * 0.8);
                    }
                }
            }
        }
    }

    private void respawnObstacle(ObstacleCar obstacle) {
        if (obstacle == null)
            return;

        try {

            int laneIndex = random.nextInt(GameConstants.LANE_COUNT);
            double newX = GameConstants.LEFT_BOUNDARY + (laneIndex * GameConstants.LANE_WIDTH) +
                    (GameConstants.LANE_WIDTH - obstacle.getWidth()) / 2;

            double newY = -obstacle.getHeight() - random.nextInt(300);

            obstacle.setPosition(newX, newY);

            DifficultyLevel diff = getDifficulty();
            double minSpeed = diff.getMinSpeed(currentLevel);
            double maxSpeed = diff.getMaxSpeed(currentLevel);
            double newSpeed = minSpeed + (maxSpeed - minSpeed) * random.nextDouble();

            obstacle.setSpeed(newSpeed);
            obstacle.setActive(true);

            System.out.println("🔄 Obstacle respawned at lane " + laneIndex +
                    " with speed " + String.format("%.1f", newSpeed));

        } catch (Exception e) {
            System.err.println("❌ Error respawning obstacle: " + e.getMessage());
        }
    }

    private void ensureMinimumObstacles() {
        int activeCount = 0;
        for (ObstacleCar obstacle : obstacles) {
            if (obstacle != null && obstacle.isActive()) {
                activeCount++;
            }
        }

        int requiredCount = difficulty.getObstacleCount(currentLevel);
        if (activeCount < requiredCount) {
            int missingCount = requiredCount - activeCount;
            for (int i = 0; i < missingCount; i++) {
                spawnNewObstacle();
            }
        }
    }

    private void spawnNewObstacle() {
        int maxAttempts = 20;
        boolean spawned = false;

        for (int attempt = 0; attempt < maxAttempts && !spawned; attempt++) {
            try {
                int laneIndex = random.nextInt(GameConstants.LANE_COUNT);
                double x = GameConstants.LEFT_BOUNDARY + (laneIndex * GameConstants.LANE_WIDTH) +
                        (GameConstants.LANE_WIDTH - GameConstants.CAR_WIDTH) / 2;
                double y = -GameConstants.CAR_HEIGHT - random.nextInt(500);

                if (isSpawnPositionSafe(x, y)) {

                    DifficultyLevel diff = getDifficulty();
                    double minSpeed = diff.getMinSpeed(currentLevel);
                    double maxSpeed = diff.getMaxSpeed(currentLevel);
                    double speed = minSpeed + (maxSpeed - minSpeed) * random.nextDouble();

                    ObstacleCar newObstacle = new ObstacleCar(x, y, speed);
                    obstacles.add(newObstacle);
                    spawned = true;

                    if (GameConstants.DEBUG_MODE) {
                        System.out.println("✨ New obstacle spawned safely at lane " + laneIndex +
                                " attempt: " + (attempt + 1));
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Error spawning obstacle (attempt " + attempt + "): " + e.getMessage());
            }
        }

        if (!spawned && GameConstants.DEBUG_MODE) {
            System.out.println("⚠️ Failed to spawn obstacle after " + maxAttempts + " attempts");
        }
    }

    private boolean isSpawnPositionSafe(double x, double y) {
        final double MIN_DISTANCE = GameConstants.CAR_HEIGHT * 2.5;

        for (ObstacleCar obstacle : obstacles) {
            if (obstacle != null && obstacle.isActive()) {
                double distance = Math.sqrt(
                        Math.pow(x - obstacle.getX(), 2) +
                                Math.pow(y - obstacle.getY(), 2));

                if (distance < MIN_DISTANCE) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateParticles() {
        try {
            if (particles != null) {
                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle particle = particles.get(i);
                    if (particle != null) {
                        particle.update();
                        if (!particle.isActive()) {
                            particles.remove(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating particles: " + e.getMessage());
        }
    }

    private void updateTrees() {
        try {
            if (trees != null) {
                for (TreeObject tree : trees) {
                    if (tree != null) {
                        tree.update();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating trees: " + e.getMessage());
        }
    }

    private void checkCollisions() {
        try {
            if (player1 != null && obstacles != null && collisionManager != null) {
                for (ObstacleCar obstacle : obstacles) {
                    if (obstacle != null && obstacle.isActive()) {

                        collisionManager.checkAllCollisions(player1, player2, obstacles, this);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking collisions: " + e.getMessage());
        }
    }

    private void cleanupInactiveObjects() {
        try {

            if (obstacles != null) {
                obstacles.removeIf(obstacle -> obstacle == null || !obstacle.isActive());
            }

            if (particles != null) {
                particles.removeIf(particle -> particle == null || !particle.isActive());
            }
        } catch (Exception e) {
            System.err.println("❌ Error cleaning up objects: " + e.getMessage());
        }
    }

    private void handleCollision(ObstacleCar obstacle) {
        try {

            if (obstacle != null) {
                obstacle.setActive(false);
            }

            if (player1 != null) {

                System.out.println("⚠️ Collision detected!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling collision: " + e.getMessage());
        }
    }

    public void startNewGame() {
        currentLevel = 1;
        gameTimer = 30;
        totalSurvivalTime = 0;
        gameState = GameState.COUNTDOWN;

        player1.reset(200, 750);
        player2.reset(1000, 750);

        initializeObstaclesForLevel();
    }

    public void nextLevel() {
        currentLevel++;
        player1.completeLevel();
        player2.completeLevel();

        initializeObstaclesForLevel();
        gameTimer = (currentLevel == 1) ? 30 : 15;
        gameState = GameState.LEVEL_TRANSITION;
    }

    public void createCrashEffect(double x, double y) {
        for (int i = 0; i < GameConstants.CRASH_PARTICLES; i++) {
            double vx = (random.nextDouble() - 0.5) * 10;
            double vy = (random.nextDouble() - 0.5) * 10;
            Color[] colors = { Color.ORANGE, Color.RED, Color.YELLOW, Color.WHITE };
            particles.add(new Particle(x, y, vx, vy,
                    colors[random.nextInt(colors.length)], GameConstants.PARTICLE_LIFE));
        }
    }

    private void initializeObstaclesForLevel() {
        obstacles.clear();
        String[] carTypes = { "gamecar1", "gamecar2", "gamecar3", "gamecar4" };
        int obstacleCount = difficulty.getObstacleCount(currentLevel);

        for (int i = 0; i < obstacleCount; i++) {
            double x = (i % 2 == 0) ? GameConstants.LEFT_BOUNDARY + random.nextInt(500)
                    : GameConstants.CENTER_DIVIDER_RIGHT + random.nextInt(500);
            double y = -200 * (i + 1);

            obstacles.add(new ObstacleCar(x, y, carTypes[random.nextInt(carTypes.length)],
                    difficulty, currentLevel, random));
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState state) {
        this.gameState = state;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getGameTimer() {
        return gameTimer;
    }

    public void setGameTimer(int timer) {
        this.gameTimer = timer;
    }

    public int getTotalSurvivalTime() {
        return totalSurvivalTime;
    }

    public void incrementSurvivalTime() {
        this.totalSurvivalTime++;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public List<ObstacleCar> getObstacles() {
        return obstacles;
    }

    public List<TreeObject> getTrees() {
        return trees;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public InputManager getInputManager() {
        return inputManager;
    }
}