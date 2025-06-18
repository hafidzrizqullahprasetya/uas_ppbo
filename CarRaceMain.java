import managers.GameManager;
import enums.*;
import interfaces.GameConstants;
import models.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class CarRaceMain extends JFrame implements KeyListener, ActionListener {

    private GameManager gameManager;
    private Timer mainTimer;
    private Timer gameTimerObj;
    private Timer countdownTimerObj;
    private Timer levelTransitionTimer;

    private BufferedImage backBuffer;
    private Graphics2D backGraphics;
    private GamePanel gamePanel;

    private int countdownTimer = GameConstants.COUNTDOWN_TIME;
    private int transitionCountdown = GameConstants.LEVEL_TRANSITION_TIME;
    private double roadOffset = 0;

    private long lastFrameTime = 0;
    private int frameCount = 0;
    private double currentFPS = 0;

    public CarRaceMain() {
        super(GameConstants.GAME_TITLE);

        try {
            this.gameManager = new GameManager();

            if (this.gameManager == null) {
                throw new IllegalStateException("GameManager initialization failed");
            }

            initializeUI();
            initializeTimers();

            setVisible(true);
            printStartupInfo();

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize CarRaceMain: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        try {
            backBuffer = new BufferedImage(
                    GameConstants.SCREEN_WIDTH,
                    GameConstants.SCREEN_HEIGHT,
                    BufferedImage.TYPE_INT_RGB);

            backGraphics = backBuffer.createGraphics();

            backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
            backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            backGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize graphics: " + e.getMessage());
            throw new RuntimeException("Graphics initialization failed", e);
        }

        addKeyListener(this);
        setFocusable(true);
        requestFocus();
    }

    private void initializeTimers() {

        mainTimer = new Timer(16, this);
        mainTimer.setRepeats(true);
        mainTimer.setCoalesce(true);
        mainTimer.start();

        gameTimerObj = new Timer(1000, e -> {
            if (gameManager.getGameState() == GameState.PLAYING) {
                gameManager.setGameTimer(gameManager.getGameTimer() - 1);
                gameManager.incrementSurvivalTime();

                if (gameManager.getGameTimer() <= 0) {
                    checkLevelCompletion();
                }
            }
        });

        countdownTimerObj = new Timer(1000, e -> {
            countdownTimer--;
            if (countdownTimer <= 0) {
                countdownTimerObj.stop();
                gameManager.setGameState(GameState.PLAYING);
                int duration = (gameManager.getCurrentLevel() == 1) ? GameConstants.GAME_DURATION : 15;
                gameManager.setGameTimer(duration);
                gameTimerObj.start();
            }
        });

        levelTransitionTimer = new Timer(1000, e -> {
            transitionCountdown--;
            if (transitionCountdown <= 0) {
                levelTransitionTimer.stop();
                startNextLevel();
            }
        });
    }

    private void printStartupInfo() {
        System.out.println("🎮 " + GameConstants.GAME_TITLE + " Started!");
        System.out.println("✅ INHERITANCE: Vehicle -> Player, ObstacleCar, TreeObject");
        System.out.println("✅ ENCAPSULATION: Private fields, controlled access");
        System.out.println("✅ ABSTRACTION: Interfaces & Abstract classes");
        System.out.println("✅ POLYMORPHISM: Method overriding & interfaces");
        System.out.println("🎯 Target FPS: 60 | Timer Interval: 16ms");

        if (GameConstants.DEBUG_MODE) {
            System.out.println("🔧 DEBUG MODE ENABLED - Additional info available");
        }
    }

    private void renderGame() {

        long currentTime = System.currentTimeMillis();
        if (lastFrameTime > 0) {
            long deltaTime = currentTime - lastFrameTime;
            if (deltaTime > 0) {
                currentFPS = 1000.0 / deltaTime;
            }
        }
        lastFrameTime = currentTime;

        backGraphics.setColor(GameConstants.GRASS_COLOR);
        backGraphics.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        switch (gameManager.getGameState()) {
            case MAIN_MENU -> drawMainMenu();
            case DIFFICULTY_SELECT -> drawDifficultyMenu();
            case COUNTDOWN -> drawCountdown();
            case PLAYING -> drawGameplay();
            case LEVEL_TRANSITION -> drawLevelTransition();
            case GAME_OVER -> drawGameOver();
            case PAUSED -> drawPauseMenu();
        }
    }

    private void drawGameplay() {
        drawGameBackground();

        if (GameConstants.DEBUG_MODE) {
            drawRoadDebugInfo();
        }

        drawGameObjects();
        drawGameUI();

        if (GameConstants.DEBUG_MODE) {
            drawDebugInfo();
        }
    }

    private void drawGameBackground() {

        backGraphics.setColor(GameConstants.ROAD_COLOR);
        backGraphics.fillRect(GameConstants.LEFT_BOUNDARY, 0,
                GameConstants.RIGHT_BOUNDARY - GameConstants.LEFT_BOUNDARY,
                GameConstants.SCREEN_HEIGHT);

        backGraphics.setColor(new Color(255, 255, 255, 200));

        final int DASH_HEIGHT = 20;
        final int DASH_GAP = 20;
        final int DASH_CYCLE = DASH_HEIGHT + DASH_GAP;

        roadOffset += 3.0 + (gameManager.getCurrentLevel() - 1) * 0.3;
        double animOffset = roadOffset % DASH_CYCLE;

        int dashCount = (GameConstants.SCREEN_HEIGHT / DASH_CYCLE) + 4;

        for (int lane = 1; lane < GameConstants.LANE_COUNT; lane++) {
            int laneX = GameConstants.LEFT_BOUNDARY + (lane * GameConstants.LANE_WIDTH);

            if (laneX == GameConstants.CENTER_DIVIDER_LEFT ||
                    laneX == GameConstants.CENTER_DIVIDER_RIGHT)
                continue;

            for (int dash = -2; dash < dashCount; dash++) {
                double dashTop = (dash * DASH_CYCLE) - animOffset;
                double dashBottom = dashTop + DASH_HEIGHT;

                if (dashBottom >= 0 && dashTop <= GameConstants.SCREEN_HEIGHT) {
                    backGraphics.fillRect(laneX - 2, (int) dashTop, 4, DASH_HEIGHT);
                }
            }
        }

        backGraphics.setColor(GameConstants.DIVIDER_COLOR);
        for (int dash = -2; dash < dashCount; dash++) {
            double dashTop = (dash * DASH_CYCLE) - animOffset;
            double dashBottom = dashTop + DASH_HEIGHT;

            if (dashBottom >= 0 && dashTop <= GameConstants.SCREEN_HEIGHT) {
                backGraphics.fillRect(GameConstants.CENTER_DIVIDER_LEFT + 6, (int) dashTop, 18, DASH_HEIGHT + 3);
            }
        }

        backGraphics.setColor(GameConstants.BOUNDARY_COLOR);
        backGraphics.fillRect(GameConstants.LEFT_BOUNDARY - 8, 0, 8, GameConstants.SCREEN_HEIGHT);
        backGraphics.fillRect(GameConstants.RIGHT_BOUNDARY, 0, 8, GameConstants.SCREEN_HEIGHT);
        backGraphics.fillRect(GameConstants.CENTER_DIVIDER_LEFT - 8, 0, 8, GameConstants.SCREEN_HEIGHT);
        backGraphics.fillRect(GameConstants.CENTER_DIVIDER_RIGHT, 0, 8, GameConstants.SCREEN_HEIGHT);

        backGraphics.setColor(Color.WHITE);
        backGraphics.fillRect(GameConstants.LEFT_BOUNDARY, 0, 3, GameConstants.SCREEN_HEIGHT);
        backGraphics.fillRect(GameConstants.RIGHT_BOUNDARY - 3, 0, 3, GameConstants.SCREEN_HEIGHT);
    }

    private void drawSimpleContinuousRoad() {

        backGraphics.setColor(new Color(255, 255, 255, 200));

        int dashLength = 20;
        int dashSpacing = 40;

        int totalHeight = GameConstants.SCREEN_HEIGHT + 200;
        int cyclesNeeded = (totalHeight / dashSpacing) + 2;

        for (int lane = 1; lane < GameConstants.LANE_COUNT; lane++) {
            int laneX = GameConstants.LEFT_BOUNDARY + (lane * GameConstants.LANE_WIDTH);

            if (laneX != GameConstants.CENTER_DIVIDER_LEFT &&
                    laneX != GameConstants.CENTER_DIVIDER_RIGHT) {

                double startOffset = roadOffset % dashSpacing;

                for (int i = 0; i < cyclesNeeded; i++) {
                    double dashY = (i * dashSpacing) - startOffset - dashSpacing;

                    if (dashY > -100 && dashY < GameConstants.SCREEN_HEIGHT + 100) {
                        backGraphics.fillRect(laneX - 2, (int) dashY, 4, dashLength);
                    }
                }
            }
        }

        backGraphics.setColor(GameConstants.DIVIDER_COLOR);
        double centerStartOffset = roadOffset % dashSpacing;

        for (int i = 0; i < cyclesNeeded; i++) {
            double dividerY = (i * dashSpacing) - centerStartOffset - dashSpacing;

            if (dividerY > -100 && dividerY < GameConstants.SCREEN_HEIGHT + 100) {
                backGraphics.fillRect(GameConstants.CENTER_DIVIDER_LEFT + 6, (int) dividerY, 18, dashLength + 5);
            }
        }
    }

    private void drawRoadDebugInfo() {
        if (!GameConstants.DEBUG_MODE)
            return;

        backGraphics.setColor(Color.CYAN);
        backGraphics.setFont(new Font("Monospace", Font.PLAIN, 12));
        backGraphics.drawString(String.format("Road Offset: %.2f", roadOffset), 10, 300);
        backGraphics.drawString(String.format("Pattern Mod: %.2f", roadOffset % 60.0), 10, 315);
        backGraphics.drawString(
                String.format("Animation Speed: %.2f", 4.0 * (1.0 + (gameManager.getCurrentLevel() - 1) * 0.1)), 10,
                330);

        backGraphics.setColor(Color.RED);
        double patternBoundary = 60.0 - (roadOffset % 60.0);
        if (patternBoundary < GameConstants.SCREEN_HEIGHT) {
            backGraphics.drawLine(0, (int) patternBoundary, GameConstants.SCREEN_WIDTH, (int) patternBoundary);
        }
    }

    private void drawGameObjects() {

        List<TreeObject> trees = gameManager.getTrees();
        if (trees != null) {
            for (TreeObject tree : trees) {
                if (tree != null && tree.isActive() && isTreeVisible(tree)) {
                    tree.draw(backGraphics);
                }
            }
        }

        List<ObstacleCar> obstacles = gameManager.getObstacles();
        if (obstacles != null) {
            for (ObstacleCar obstacle : obstacles) {
                if (obstacle != null && obstacle.isActive() && isObjectVisible(obstacle)) {
                    obstacle.draw(backGraphics);
                }
            }
        }

        Player player1 = gameManager.getPlayer1();
        Player player2 = gameManager.getPlayer2();

        if (player1 != null && player1.isActive())
            player1.draw(backGraphics);
        if (player2 != null && player2.isActive())
            player2.draw(backGraphics);

        List<Particle> particles = gameManager.getParticles();
        if (particles != null) {
            for (Particle particle : particles) {
                if (particle != null && particle.isActive() &&
                        particle.getX() > -50 && particle.getX() < GameConstants.SCREEN_WIDTH + 50 &&
                        particle.getY() > -50 && particle.getY() < GameConstants.SCREEN_HEIGHT + 50) {
                    particle.draw(backGraphics);
                }
            }
        }
    }

    private boolean isObjectVisible(Vehicle vehicle) {
        if (vehicle == null)
            return false;

        double x = vehicle.getX();
        double y = vehicle.getY();
        double width = vehicle.getWidth();
        double height = vehicle.getHeight();

        return (x + width > -50 && x < GameConstants.SCREEN_WIDTH + 50 &&
                y + height > -50 && y < GameConstants.SCREEN_HEIGHT + 50);
    }

    private boolean isTreeVisible(TreeObject tree) {
        if (tree == null)
            return false;

        double x = tree.getX();
        double y = tree.getY();
        double width = tree.getWidth();
        double height = tree.getHeight();

        return (x + width > -50 && x < GameConstants.SCREEN_WIDTH + 50 &&
                y + height > -50 && y < GameConstants.SCREEN_HEIGHT + 50);
    }

    private void drawGameUI() {

        Color timerColor = getTimerColor(gameManager.getGameTimer());

        backGraphics.setColor(new Color(timerColor.getRed(), timerColor.getGreen(),
                timerColor.getBlue(), 180));
        backGraphics.fillRoundRect(550, 15, 200, 70, 15, 15);
        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 32));
        backGraphics.drawString("Waktu: " + gameManager.getGameTimer(), 565, 60);

        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRoundRect(320, 15, 220, 70, 15, 15);
        backGraphics.setColor(Color.YELLOW);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 26));
        backGraphics.drawString("LEVEL " + gameManager.getCurrentLevel(), 335, 45);

        drawPlayerStatus(gameManager.getPlayer1(), 20, 15);
        drawPlayerStatus(gameManager.getPlayer2(), 1020, 15);

        drawBottomInfoPanel();
    }

    private Color getTimerColor(int timeLeft) {
        if (timeLeft <= 5)
            return Color.RED;
        if (timeLeft <= 10)
            return Color.ORANGE;
        return Color.BLACK;
    }

    private void drawPlayerStatus(Player player, int x, int y) {
        if (player == null)
            return;

        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRoundRect(x, y, 280, 120, 15, 15);

        backGraphics.setColor(player.getStatusColor());
        backGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        backGraphics.drawString(player.getName(), x + 10, y + 25);

        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 16));
        backGraphics.drawString("Nyawa:", x + 10, y + 45);

        for (int i = 0; i < GameConstants.MAX_PLAYER_LIVES; i++) {
            Color heartColor = i < player.getLives() ? Color.RED : Color.DARK_GRAY;
            backGraphics.setColor(heartColor);
            backGraphics.fillOval(x + 65 + i * 20, y + 30, 15, 15);
        }

        backGraphics.setColor(Color.CYAN);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 14));
        backGraphics.drawString("Level Selesai: " + player.getLevelsCompleted(), x + 10, y + 65);

        String status = getPlayerStatus(player);
        Color statusColor = getPlayerStatusColor(player);

        backGraphics.setColor(statusColor);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 16));
        backGraphics.drawString(status, x + 10, y + 85);
    }

    private String getPlayerStatus(Player player) {
        if (!player.isAlive())
            return "TABRAKAN";
        if (player.isInvulnerable())
            return "KEBAL";
        return "AKTIF";
    }

    private Color getPlayerStatusColor(Player player) {
        if (!player.isAlive())
            return Color.RED;
        if (player.isInvulnerable())
            return Color.YELLOW;
        return Color.GREEN;
    }

    private void drawBottomInfoPanel() {
        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRoundRect(50, 810, 1200, 80, 15, 15);

        backGraphics.setColor(Color.RED);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 16));
        String warning = "🚫 DILARANG MELEWATI SEMUA MARKA MERAH! 🚫";
        drawCenteredText(warning, 830);

        backGraphics.setColor(Color.YELLOW);
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 14));
        DifficultyLevel diff = gameManager.getDifficulty();
        String levelInfo = String.format("Level %d - %d obstacles | Speed: %d-%d",
                gameManager.getCurrentLevel(),
                diff.getObstacleCount(gameManager.getCurrentLevel()),
                diff.getMinSpeed(gameManager.getCurrentLevel()),
                diff.getMaxSpeed(gameManager.getCurrentLevel()));
        drawCenteredText(levelInfo, 850);

        backGraphics.setColor(Color.CYAN);
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 12));
        String challengeInfo = String.format("Bertahan %d detik lagi untuk Level %d! Total survival: %ds",
                gameManager.getGameTimer(),
                gameManager.getCurrentLevel() + 1,
                gameManager.getTotalSurvivalTime());
        drawCenteredText(challengeInfo, 870);
    }

    private void drawCenteredText(String text, int y) {
        FontMetrics fm = backGraphics.getFontMetrics();
        int x = (GameConstants.SCREEN_WIDTH - fm.stringWidth(text)) / 2;
        backGraphics.drawString(text, x, y);
    }

    private void drawDebugInfo() {
        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRoundRect(10, 150, 250, 140, 10, 10);

        backGraphics.setColor(Color.GREEN);
        backGraphics.setFont(new Font("Monospace", Font.PLAIN, 12));

        String[] debugInfo = {
                "DEBUG MODE",
                String.format("FPS: %.1f", currentFPS),
                "Objects: " + getTotalObjectCount(),
                "Input Keys: " + gameManager.getInputManager().getActiveKeyCount(),
                String.format("Road Offset: %.1f", roadOffset),
                "Game State: " + gameManager.getGameState(),
                "Obstacles: " + gameManager.getObstacles().size(),
                "Particles: " + gameManager.getParticles().size()
        };

        for (int i = 0; i < debugInfo.length; i++) {
            backGraphics.drawString(debugInfo[i], 15, 170 + i * 15);
        }
    }

    private int getTotalObjectCount() {
        int count = 2;

        if (gameManager.getObstacles() != null) {
            count += gameManager.getObstacles().size();
        }
        if (gameManager.getParticles() != null) {
            count += gameManager.getParticles().size();
        }
        if (gameManager.getTrees() != null) {
            count += gameManager.getTrees().size();
        }

        return count;
    }

    private void drawMainMenu() {
        backGraphics.setFont(new Font("Arial", Font.BOLD, 60));

        backGraphics.setColor(new Color(0, 0, 0, 100));
        drawCenteredText("🏁 CAR RACE SURVIVAL 🏁", 203);

        backGraphics.setColor(Color.WHITE);
        drawCenteredText("🏁 CAR RACE SURVIVAL 🏁", 200);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
        backGraphics.setColor(Color.YELLOW);

        drawMenuOptions(new String[] { "MULAI PERMAINAN", "KELUAR" }, 400, 80);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 18));
        backGraphics.setColor(Color.LIGHT_GRAY);
        drawCenteredText("Navigasi: ↑↓ | Pilih: ENTER", 650);
    }

    private void drawDifficultyMenu() {

        backGraphics.setColor(new Color(30, 30, 30, 200));
        backGraphics.fillRoundRect(150, 100, 1000, 700, 30, 30);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 45));
        backGraphics.setColor(Color.WHITE);
        drawCenteredText("PILIH TINGKAT KESULITAN", 180);

        DifficultyLevel[] levels = DifficultyLevel.values();
        int menuSelection = gameManager.getInputManager().getMenuSelection();

        backGraphics.setFont(new Font("Arial", Font.BOLD, 32));
        for (int i = 0; i < levels.length; i++) {
            int optionY = 280 + i * 120;
            boolean isSelected = (menuSelection == i);

            if (isSelected) {
                backGraphics.setColor(new Color(0, 255, 100, 180));
                backGraphics.fillRoundRect(200, optionY - 50, 900, 90, 20, 20);
                backGraphics.setColor(Color.BLACK);
            } else {
                backGraphics.setColor(Color.WHITE);
            }

            drawCenteredText(levels[i].description, optionY);

            if (isSelected) {
                backGraphics.setFont(new Font("Arial", Font.PLAIN, 18));
                backGraphics.setColor(Color.WHITE);
                String details = String.format("Base Obstacles: %d | Speed: %d-%d",
                        levels[i].baseObstacleCount,
                        levels[i].baseMinSpeed,
                        levels[i].baseMaxSpeed);
                drawCenteredText(details, optionY + 30);
                backGraphics.setFont(new Font("Arial", Font.BOLD, 32));
            }
        }

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 16));
        backGraphics.setColor(Color.CYAN);
        drawCenteredText("ESC untuk kembali | ENTER untuk pilih", 750);
    }

    private void drawMenuOptions(String[] options, int startY, int spacing) {
        int menuSelection = gameManager.getInputManager().getMenuSelection();

        backGraphics.setFont(new Font("Arial", Font.BOLD, 36));
        for (int i = 0; i < options.length; i++) {
            int optionY = startY + i * spacing;

            if (menuSelection == i) {
                backGraphics.setColor(new Color(0, 255, 100, 180));
                FontMetrics fm = backGraphics.getFontMetrics();
                int textWidth = fm.stringWidth(options[i]);
                int rectX = (GameConstants.SCREEN_WIDTH - textWidth) / 2 - 20;
                backGraphics.fillRoundRect(rectX, optionY - 40, textWidth + 40, 60, 20, 20);
                backGraphics.setColor(Color.BLACK);
            } else {
                backGraphics.setColor(Color.WHITE);
            }

            drawCenteredText(options[i], optionY);
        }
    }

    private void drawCountdown() {
        drawGameplay();

        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 120));
        backGraphics.setColor(Color.YELLOW);
        String countdown = (countdownTimer > 0) ? String.valueOf(countdownTimer) : "MULAI!";
        drawCenteredText(countdown, 450);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        backGraphics.setColor(Color.WHITE);
        drawCenteredText("SPACE untuk Skip | ESC untuk Menu", 550);
    }

    private void drawLevelTransition() {
        drawGameplay();

        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 50));
        backGraphics.setColor(new Color(255, 215, 0));
        drawCenteredText("LEVEL " + (gameManager.getCurrentLevel() - 1) + " SELESAI!", 300);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 35));
        backGraphics.setColor(Color.WHITE);
        drawCenteredText("BERSIAP UNTUK LEVEL " + gameManager.getCurrentLevel(), 380);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 80));
        backGraphics.setColor(Color.YELLOW);
        drawCenteredText(String.valueOf(transitionCountdown), 500);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 18));
        backGraphics.setColor(Color.CYAN);
        drawCenteredText("SPACE untuk Skip | ESC untuk Menu", 580);
    }

    private void drawGameOver() {
        drawGameplay();

        backGraphics.setColor(new Color(0, 0, 0, 200));
        backGraphics.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 60));
        backGraphics.setColor(Color.RED);
        drawCenteredText("GAME OVER", 200);

        String winner = getWinner();
        backGraphics.setFont(new Font("Arial", Font.BOLD, 40));
        backGraphics.setColor(new Color(255, 215, 0));
        drawCenteredText(winner, 280);

        drawGameStats();

        backGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        backGraphics.setColor(Color.CYAN);
        drawCenteredText("R untuk Restart | ESC untuk Menu", 650);
    }

    private void drawGameStats() {
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
        backGraphics.setColor(Color.WHITE);

        String[] stats = {
                "STATISTIK PERMAINAN:",
                "",
                "Level Tertinggi: " + gameManager.getCurrentLevel(),
                "Total Survival: " + gameManager.getTotalSurvivalTime() + " detik",
                "Kesulitan: " + gameManager.getDifficulty().description,
                "",
                gameManager.getPlayer1().getName() + " - Level: " + gameManager.getPlayer1().getLevelsCompleted(),
                gameManager.getPlayer2().getName() + " - Level: " + gameManager.getPlayer2().getLevelsCompleted()
        };

        int startY = 350;
        for (int i = 0; i < stats.length; i++) {
            if (!stats[i].isEmpty()) {
                if (i == 0) {
                    backGraphics.setColor(Color.YELLOW);
                    backGraphics.setFont(new Font("Arial", Font.BOLD, 24));
                } else {
                    backGraphics.setColor(Color.WHITE);
                    backGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
                }
                drawCenteredText(stats[i], startY + i * 25);
            }
        }
    }

    private void drawPauseMenu() {
        drawGameplay();

        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 60));
        backGraphics.setColor(Color.YELLOW);
        drawCenteredText("PAUSE", 300);

        drawMenuOptions(new String[] { "LANJUTKAN", "MENU UTAMA", "KELUAR" }, 400, 60);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 16));
        backGraphics.setColor(Color.LIGHT_GRAY);
        drawCenteredText("ESC untuk lanjutkan langsung", 580);
    }

    private void checkLevelCompletion() {
        boolean bothAlive = gameManager.getPlayer1().isAlive() && gameManager.getPlayer2().isAlive();

        if (bothAlive) {

            gameManager.nextLevel();
            transitionCountdown = GameConstants.LEVEL_TRANSITION_TIME;
            gameManager.setGameState(GameState.LEVEL_TRANSITION);
            levelTransitionTimer.start();
            gameTimerObj.stop();
        } else {

            gameManager.setGameState(GameState.GAME_OVER);
            gameTimerObj.stop();
        }
    }

    private void startNextLevel() {
        gameManager.setGameState(GameState.PLAYING);
        int duration = (gameManager.getCurrentLevel() == 1) ? GameConstants.GAME_DURATION : 15;
        gameManager.setGameTimer(duration);
        gameTimerObj.start();
    }

    private String getWinner() {
        Player p1 = gameManager.getPlayer1();
        Player p2 = gameManager.getPlayer2();

        if (p1.isAlive() && !p2.isAlive()) {
            return "🏆 " + p1.getName() + " MENANG! 🏆";
        } else if (!p1.isAlive() && p2.isAlive()) {
            return "🏆 " + p2.getName() + " MENANG! 🏆";
        } else {
            return "🤝 SERI - Kedua Pemain Gugur Bersamaan 🤝";
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        gameManager.getInputManager().keyPressed(e.getKeyCode());

        switch (gameManager.getGameState()) {
            case MAIN_MENU -> handleMainMenuInput(e);
            case DIFFICULTY_SELECT -> handleDifficultyMenuInput(e);
            case COUNTDOWN -> handleCountdownInput(e);
            case PLAYING -> handlePlayingInput(e);
            case LEVEL_TRANSITION -> handleLevelTransitionInput(e);
            case PAUSED -> handlePauseMenuInput(e);
            case GAME_OVER -> handleGameOverInput(e);
        }
    }

    private void handlePlayingInput(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameManager.setGameState(GameState.PAUSED);
            gameTimerObj.stop();
        }
    }

    private void handleCountdownInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> {
                countdownTimerObj.stop();
                gameManager.setGameState(GameState.MAIN_MENU);
                gameManager.getInputManager().setMenuSelection(0);
            }
            case KeyEvent.VK_SPACE -> {
                countdownTimerObj.stop();
                countdownTimer = 0;
                gameManager.setGameState(GameState.PLAYING);
                int duration = (gameManager.getCurrentLevel() == 1) ? GameConstants.GAME_DURATION : 15;
                gameManager.setGameTimer(duration);
                gameTimerObj.start();
                System.out.println("⏩ Countdown skipped!");
            }
        }
    }

    private void handleLevelTransitionInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> {
                levelTransitionTimer.stop();
                gameTimerObj.stop();
                gameManager.setGameState(GameState.MAIN_MENU);
                gameManager.getInputManager().setMenuSelection(0);
            }
            case KeyEvent.VK_SPACE -> {
                levelTransitionTimer.stop();
                transitionCountdown = 0;
                startNextLevel();
                System.out.println("⏩ Level transition skipped!");
            }
        }
    }

    private void handleMainMenuInput(KeyEvent e) {
        int selection = gameManager.getInputManager().handleMenuInput(e, 2);

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (selection == 0) {
                gameManager.setGameState(GameState.DIFFICULTY_SELECT);
                gameManager.getInputManager().setMenuSelection(0);
            } else {
                System.exit(0);
            }
        }
    }

    private void handleDifficultyMenuInput(KeyEvent e) {
        int selection = gameManager.getInputManager().handleMenuInput(e, 3);

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            gameManager.setDifficulty(DifficultyLevel.values()[selection]);
            startNewGame();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameManager.setGameState(GameState.MAIN_MENU);
            gameManager.getInputManager().setMenuSelection(0);
        }
    }

    private void handlePauseMenuInput(KeyEvent e) {
        int selection = gameManager.getInputManager().handleMenuInput(e, 3);

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            switch (selection) {
                case 0 -> {
                    gameManager.setGameState(GameState.PLAYING);
                    gameTimerObj.start();
                }
                case 1 -> {
                    gameManager.setGameState(GameState.MAIN_MENU);
                    gameTimerObj.stop();
                    gameManager.getInputManager().setMenuSelection(0);
                }
                case 2 -> System.exit(0);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameManager.setGameState(GameState.PLAYING);
            gameTimerObj.start();
        }
    }

    private void handleGameOverInput(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            startNewGame();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameManager.setGameState(GameState.MAIN_MENU);
            gameManager.getInputManager().setMenuSelection(0);
        }
    }

    private void startNewGame() {
        gameManager.setGameState(GameState.COUNTDOWN);
        gameManager.startNewGame();
        countdownTimer = GameConstants.COUNTDOWN_TIME;
        transitionCountdown = GameConstants.LEVEL_TRANSITION_TIME;
        countdownTimerObj.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gameManager.getInputManager().keyReleased(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mainTimer) {
            try {

                if (gameManager.getGameState() == GameState.PLAYING) {
                    gameManager.getInputManager().handleMovement(
                            gameManager.getPlayer1(),
                            gameManager.getPlayer2(),
                            gameManager.getGameState());
                }

                gameManager.update();

                gameManager.getInputManager().clearJustPressed();

                renderGame();
                gamePanel.repaint();

            } catch (Exception ex) {
                System.err.println("❌ Error in game loop: " + ex.getMessage());
                if (GameConstants.DEBUG_MODE) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            setDoubleBuffered(false);
            setBackground(GameConstants.GRASS_COLOR);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (backBuffer != null) {

                g.drawImage(backBuffer, 0, 0, this);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        }
    }

    public static void main(String[] args) {

        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");
        System.setProperty("sun.java2d.opengl", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new CarRaceMain();

            } catch (Exception e) {
                System.err.println("❌ Failed to start application: " + e.getMessage());
                e.printStackTrace();

                try {
                    new CarRaceMain();
                } catch (Exception fallbackError) {
                    System.err.println("❌ Fallback also failed: " + fallbackError.getMessage());
                    System.exit(1);
                }
            }
        });
    }
}