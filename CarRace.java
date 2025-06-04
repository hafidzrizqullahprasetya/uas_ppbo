import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CarRace extends JFrame implements KeyListener, ActionListener {
    // === CORE GAME VARIABLES ===
    private GameState gameState = GameState.MAIN_MENU;
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    // Players
    private Player player1, player2;
    private javax.swing.Timer mainTimer;
    private Random random = new Random();

    // Input handling
    private Set<Integer> keysPressed = new HashSet<>();

    // Game timing
    private int gameTimer = 30;
    private javax.swing.Timer gameTimerObj, countdownTimerObj;
    private int countdownTimer = 3;

    // Graphics
    private BufferedImage backBuffer;
    private Graphics2D backGraphics;
    private GamePanel gamePanel;

    // Game objects
    private List<TreeObject> trees = new ArrayList<>();
    private List<ObstacleCar> obstacleCars = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();

    // Visual effects
    private int roadOffset = 0;
    private String winner = "";
    private int menuSelection = 0;
    private BufferedImage roadImage; // Changed to BufferedImage for better handling

    // === ENUMS ===
    enum GameState {
        MAIN_MENU, DIFFICULTY_SELECT, COUNTDOWN, PLAYING, PAUSED, GAME_OVER
    }

    enum DifficultyLevel {
        EASY(6, 4, 8, "MUDAH - Berkendara Santai"),
        MEDIUM(10, 6, 12, "SEDANG - Lalu Lintas Kota"),
        HARD(16, 8, 18, "SULIT - Kecepatan Tinggi");

        final int obstacleCount;
        final int minSpeed;
        final int maxSpeed;
        final String description;

        DifficultyLevel(int count, int min, int max, String desc) {
            this.obstacleCount = count;
            this.minSpeed = min;
            this.maxSpeed = max;
            this.description = desc;
        }
    }

    // === PLAYER CLASS ===
    class Player {
        int x, y;
        int lives = 3;
        boolean alive = true;
        BufferedImage carImage; // Changed to BufferedImage
        String name;
        Color statusColor;
        boolean invulnerable = false;
        int invulnerabilityTimer = 0;
        int lastHitTime = 0;

        Player(int startX, int startY, String carImagePath, String playerName, Color color) {
            this.x = startX;
            this.y = startY;
            this.name = playerName;
            this.statusColor = color;
            try {
                this.carImage = ImageIO.read(getClass().getResource(carImagePath));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Warning: Could not load " + carImagePath + ", using fallback");
                this.carImage = null;
            }
        }

        void takeDamage() {
            if (!invulnerable && alive) {
                lives--;
                invulnerable = true;
                invulnerabilityTimer = 120;

                createCrashEffect(x + 35, y + 50);

                if (lives <= 0) {
                    alive = false;
                    checkGameEnd();
                }
            }
        }

        void update() {
            if (invulnerabilityTimer > 0) {
                invulnerabilityTimer--;
                if (invulnerabilityTimer <= 0) {
                    invulnerable = false;
                }
            }
        }

        void draw(Graphics2D g) {
            if (!alive)
                return;

            if (invulnerable && (invulnerabilityTimer / 10) % 2 == 0)
                return;

            if (carImage != null) {
                g.drawImage(carImage, x, y, null);
            } else {
                g.setColor(statusColor);
                g.fillRect(x, y, 70, 100);
            }
        }

        void reset(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            this.lives = 3;
            this.alive = true;
            this.invulnerable = false;
            this.invulnerabilityTimer = 0;
        }
    }

    // === TREE OBJECT CLASS ===
    class TreeObject {
        double x, y;
        BufferedImage image; // Changed to BufferedImage
        double speed;

        TreeObject(double x, double y) {
            this.x = x;
            this.y = y;
            this.speed = 8 + random.nextDouble() * 4;
            try {
                this.image = ImageIO.read(getClass().getResource("tree.png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Warning: Could not load tree.png, using fallback");
                this.image = null;
            }
        }

        void update() {
            if (gameState == GameState.PLAYING) { // Only update when playing
                y += speed;
                if (y > 950) {
                    y = -100 - random.nextInt(200);
                    speed = 8 + random.nextDouble() * 4;
                }
            }
        }

        void draw(Graphics2D g) {
            if (image != null) {
                g.drawImage(image, (int) x, (int) y, null);
            } else {
                g.setColor(new Color(34, 139, 34));
                g.fillOval((int) x, (int) y, 40, 60);
            }
        }
    }

    // === OBSTACLE CAR CLASS ===
    class ObstacleCar {
        double x, y;
        double speed;
        BufferedImage image;
        String carType;

        ObstacleCar(double x, double y, String carType) {
            this.x = x;
            this.y = y;
            this.carType = carType;
            this.speed = difficulty.minSpeed + random.nextDouble() * (difficulty.maxSpeed - difficulty.minSpeed);
            loadImage();
        }

        void loadImage() {
            try {
                this.image = ImageIO.read(getClass().getResource(carType + ".png"));
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Warning: Could not load " + carType + ".png, using fallback");
                this.image = null;
            }
        }

        void update() {
            if (gameState == GameState.PLAYING) {
                y += speed;
                if (y > 950) {
                    y = -100 - random.nextInt(300);
                    x = getRandomXPosition();
                    speed = difficulty.minSpeed + random.nextDouble() * (difficulty.maxSpeed - difficulty.minSpeed);

                    String[] carTypes = { "gamecar1", "gamecar2", "gamecar3", "gamecar4" };
                    carType = carTypes[random.nextInt(carTypes.length)];
                    loadImage();
                }
            }
        }

        void draw(Graphics2D g) {
            if (image != null) {
                g.drawImage(image, (int) x, (int) y, null);
            } else {
                g.setColor(Color.ORANGE);
                g.fillRect((int) x, (int) y, 70, 100);
            }
        }

        boolean checkCollision(Player player) {
            return player.alive && !player.invulnerable &&
                    Math.abs(x - player.x) < 45 && Math.abs(y - player.y) < 65;
        }

        private double getRandomXPosition() {
            // PERBAIKAN: Obstacle harus menghormati marka jalan juga
            final int LEFT_BOUNDARY = 30;
            final int RIGHT_BOUNDARY = 1200;
            final int CENTER_DIVIDER_LEFT = 645;
            final int CENTER_DIVIDER_RIGHT = 655;
            final int CAR_WIDTH = 70;

            if (random.nextBoolean()) {
                // Jalur kiri (Player 1 area)
                return LEFT_BOUNDARY + random.nextInt(CENTER_DIVIDER_LEFT - LEFT_BOUNDARY - CAR_WIDTH);
            } else {
                // Jalur kanan (Player 2 area)
                return CENTER_DIVIDER_RIGHT + random.nextInt(RIGHT_BOUNDARY - CENTER_DIVIDER_RIGHT - CAR_WIDTH);
            }
        }
    }

    // === PARTICLE EFFECT CLASS ===
    class Particle {
        double x, y, vx, vy;
        Color color;
        int life, maxLife;

        Particle(double x, double y, double vx, double vy, Color color, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }

        void update() {
            if (gameState == GameState.PLAYING) { // Only update when playing
                x += vx;
                y += vy;
                vy += 0.3;
                life--;
            }
        }

        void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            Color fadeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (int) (255 * alpha));
            g.setColor(fadeColor);
            g.fillOval((int) x, (int) y, 6, 6);
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    // === GAME PANEL ===
    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backBuffer != null) {
                g.drawImage(backBuffer, 0, 0, null);
            }
        }
    }

    // === CONSTRUCTOR ===
    public CarRace() {
        super("Balap Survival");
        initializeGame();
    }

    private void initializeGame() {
        setBounds(200, 50, 1300, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        addKeyListener(this);
        setFocusable(true);

        // Initialize graphics
        backBuffer = new BufferedImage(1300, 900, BufferedImage.TYPE_INT_RGB);
        backGraphics = backBuffer.createGraphics();
        backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        backGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        backGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Load road image with better error handling
        loadRoadImage();

        // Initialize players
        player1 = new Player(200, 750, "gamecar3.png", "Pemain 1", new Color(0, 150, 255));
        player2 = new Player(1000, 750, "gamecar4.png", "Pemain 2", new Color(255, 100, 100));

        initializeTrees();

        // Initialize timers
        mainTimer = new javax.swing.Timer(16, this);
        mainTimer.start();

        countdownTimerObj = new javax.swing.Timer(1000, e -> {
            countdownTimer--;
            if (countdownTimer <= 0) {
                gameState = GameState.PLAYING;
                startGameTimer();
                countdownTimerObj.stop();
            }
        });

        gameTimerObj = new javax.swing.Timer(1000, e -> {
            gameTimer--;
            if (gameTimer <= 0) {
                endGame();
            }
        });

        setVisible(true);
    }

    private void loadRoadImage() {
        try {
            // Try to load jalan.png
            BufferedImage originalRoad = ImageIO.read(getClass().getResource("jalan.png"));

            // Scale to fit screen width while maintaining aspect ratio
            int targetWidth = 1300;
            int targetHeight = (int) ((double) originalRoad.getHeight() * targetWidth / originalRoad.getWidth());

            // Create smooth scaled image
            roadImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = roadImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalRoad, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            System.out.println("Road image loaded successfully: " + targetWidth + "x" + targetHeight);

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Warning: Could not load jalan.png, creating fallback road");
            createFallbackRoad();
        }
    }

    private void createFallbackRoad() {
        roadImage = new BufferedImage(1300, 900, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = roadImage.createGraphics();

        // Create a nice looking road with clear boundaries
        g2d.setColor(new Color(105, 105, 105)); // Road color
        g2d.fillRect(0, 0, 1300, 900);

        // Enhanced center divider (RED)
        g2d.setColor(Color.RED);
        g2d.fillRect(645, 0, 10, 900);

        // Warning stripes on center divider
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 900; i += 40) {
            g2d.fillRect(647, i, 6, 20);
        }

        // Side boundaries (YELLOW)
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(25, 0, 5, 900); // Left boundary
        g2d.fillRect(1270, 0, 5, 900); // Right boundary

        // Lane markings
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 900; i += 100) {
            // Left side lanes (Player 1 area)
            for (int x = 150; x < 645; x += 150) {
                g2d.fillRect(x, i, 8, 50);
            }
            // Right side lanes (Player 2 area)
            for (int x = 800; x < 1270; x += 150) {
                g2d.fillRect(x, i, 8, 50);
            }
        }

        // Grass/sidewalk areas
        g2d.setColor(new Color(34, 139, 34)); // Green
        g2d.fillRect(0, 0, 30, 900); // Left grass
        g2d.fillRect(1275, 0, 25, 900); // Right grass

        g2d.dispose();
    }

    private void initializeTrees() {
        trees.clear();
        for (int i = 0; i < 4; i++) {
            trees.add(new TreeObject(15, i * 200 - 300));
        }
        for (int i = 0; i < 4; i++) {
            trees.add(new TreeObject(1245, i * 200 - 200));
        }
    }

    private void initializeObstacles() {
        obstacleCars.clear();
        String[] carTypes = { "gamecar1", "gamecar2", "gamecar3", "gamecar4" };

        final int LEFT_BOUNDARY = 30;
        final int RIGHT_BOUNDARY = 1200;
        final int CENTER_DIVIDER_LEFT = 645;
        final int CENTER_DIVIDER_RIGHT = 655;
        final int CAR_WIDTH = 70;

        for (int i = 0; i < difficulty.obstacleCount; i++) {
            double x;
            if (random.nextBoolean()) {
                // Jalur kiri (Player 1 area)
                x = LEFT_BOUNDARY + random.nextInt(CENTER_DIVIDER_LEFT - LEFT_BOUNDARY - CAR_WIDTH);
            } else {
                // Jalur kanan (Player 2 area)
                x = CENTER_DIVIDER_RIGHT + random.nextInt(RIGHT_BOUNDARY - CENTER_DIVIDER_RIGHT - CAR_WIDTH);
            }

            obstacleCars.add(new ObstacleCar(x, -i * 150 - 100,
                    carTypes[random.nextInt(carTypes.length)]));
        }
    }

    private void createCrashEffect(int x, int y) {
        for (int i = 0; i < 15; i++) {
            double vx = (random.nextDouble() - 0.5) * 10;
            double vy = (random.nextDouble() - 0.5) * 10;
            Color[] colors = { Color.ORANGE, Color.RED, Color.YELLOW, Color.WHITE };
            particles.add(new Particle(x, y, vx, vy,
                    colors[random.nextInt(colors.length)], 60));
        }
    }

    // === PAUSE FUNCTIONALITY ===
    private void pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameTimerObj.stop();
            menuSelection = 0;
        }
    }

    private void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameTimerObj.start();
        }
    }

    // === GAME LOGIC ===
    private void handleMovement() {
        if (gameState != GameState.PLAYING)
            return;

        int moveSpeed = 8;

        // BATASAN JALAN - Konstanta untuk boundary
        final int LEFT_BOUNDARY = 30; // Batas kiri jalan
        final int RIGHT_BOUNDARY = 1200; // Batas kanan jalan
        final int CENTER_DIVIDER_LEFT = 645; // Marka merah kiri
        final int CENTER_DIVIDER_RIGHT = 655; // Marka merah kanan
        final int CAR_WIDTH = 70; // Lebar mobil

        // Player 1 (WASD) - HANYA BOLEH DI JALUR KIRI
        if (player1.alive) {
            if (keysPressed.contains(KeyEvent.VK_A)) {
                int newX = player1.x - moveSpeed;
                // Tidak boleh keluar batas kiri jalan
                if (newX >= LEFT_BOUNDARY) {
                    player1.x = newX;
                }
            }
            if (keysPressed.contains(KeyEvent.VK_D)) {
                int newX = player1.x + moveSpeed;
                // Tidak boleh melewati marka merah tengah
                if (newX + CAR_WIDTH <= CENTER_DIVIDER_LEFT) {
                    player1.x = newX;
                }
            }
            if (keysPressed.contains(KeyEvent.VK_W)) {
                player1.y = Math.max(0, player1.y - moveSpeed);
            }
            if (keysPressed.contains(KeyEvent.VK_S)) {
                player1.y = Math.min(800, player1.y + moveSpeed);
            }
        }

        // Player 2 (Arrow Keys) - HANYA BOLEH DI JALUR KANAN
        if (player2.alive) {
            if (keysPressed.contains(KeyEvent.VK_LEFT)) {
                int newX = player2.x - moveSpeed;
                // Tidak boleh melewati marka merah tengah
                if (newX >= CENTER_DIVIDER_RIGHT) {
                    player2.x = newX;
                }
            }
            if (keysPressed.contains(KeyEvent.VK_RIGHT)) {
                int newX = player2.x + moveSpeed;
                // Tidak boleh keluar batas kanan jalan
                if (newX + CAR_WIDTH <= RIGHT_BOUNDARY) {
                    player2.x = newX;
                }
            }
            if (keysPressed.contains(KeyEvent.VK_UP)) {
                player2.y = Math.max(0, player2.y - moveSpeed);
            }
            if (keysPressed.contains(KeyEvent.VK_DOWN)) {
                player2.y = Math.min(800, player2.y + moveSpeed);
            }
        }
    }

    private void startGameTimer() {
        gameTimer = 30;
        gameTimerObj.start();
    }

    private void checkGameEnd() {
        if (!player1.alive && !player2.alive) {
            winner = "Kedua Pemain Tabrakan! Tidak Ada Pemenang!";
            endGame();
        } else if (!player1.alive) {
            winner = player2.name + " Menang!";
            endGame();
        } else if (!player2.alive) {
            winner = player1.name + " Menang!";
            endGame();
        }
    }

    private void endGame() {
        gameState = GameState.GAME_OVER;
        gameTimerObj.stop();

        if (player1.alive && player2.alive) {
            winner = "Kedua Pemain Selamat! Hasil Seri!";
        }
    }

    // === RENDERING ===
    public void renderGame() {
        // Clear screen with gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(135, 206, 235),
                0, 900, new Color(70, 130, 180));
        backGraphics.setPaint(gradient);
        backGraphics.fillRect(0, 0, 1300, 900);

        switch (gameState) {
            case MAIN_MENU -> drawMainMenu();
            case DIFFICULTY_SELECT -> drawDifficultyMenu();
            case COUNTDOWN -> drawCountdown();
            case PLAYING -> drawGame();
            case PAUSED -> drawPauseMenu();
            case GAME_OVER -> drawGameOver();
        }
    }

    private void drawMainMenu() {
        // Background effect
        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRect(0, 0, 1300, 900);

        // Main container
        int containerX = 200;
        int containerY = 150;
        int containerWidth = 900;
        int containerHeight = 600;

        // Draw container background
        backGraphics.setColor(new Color(30, 30, 30, 200));
        backGraphics.fillRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);
        backGraphics.setColor(new Color(255, 255, 255, 100));
        backGraphics.drawRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);

        // Title
        backGraphics.setFont(new Font("Arial", Font.BOLD, 70));
        backGraphics.setColor(Color.WHITE);
        FontMetrics fm = backGraphics.getFontMetrics();
        String title = "BALAP SURVIVAL";
        int titleX = 650 - fm.stringWidth(title) / 2;
        backGraphics.drawString(title, titleX, 250);

        // Subtitle
        backGraphics.setFont(new Font("Arial", Font.BOLD, 35));
        fm = backGraphics.getFontMetrics();
        String subtitle = "EDISI PROFESIONAL";
        backGraphics.setColor(Color.YELLOW);
        int subtitleX = 650 - fm.stringWidth(subtitle) / 2;
        backGraphics.drawString(subtitle, subtitleX, 300);

        // Center divider line (RED)
        backGraphics.setColor(Color.RED);
        backGraphics.fillRect(620, 330, 60, 5);

        // Menu options
        String[] options = { "MULAI PERMAINAN", "KELUAR" };
        backGraphics.setFont(new Font("Arial", Font.BOLD, 45));

        for (int i = 0; i < options.length; i++) {
            int optionY = 420 + i * 90;

            if (menuSelection == i) {
                backGraphics.setColor(new Color(0, 255, 150, 180));
                backGraphics.fillRoundRect(containerX + 50, optionY - 50, containerWidth - 100, 70, 20, 20);
                backGraphics.setColor(Color.BLACK);
            } else {
                backGraphics.setColor(Color.WHITE);
            }

            fm = backGraphics.getFontMetrics();
            int optionX = 650 - fm.stringWidth(options[i]) / 2;
            backGraphics.drawString(options[i], optionX, optionY);
        }

        // Instructions
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
        backGraphics.setColor(Color.LIGHT_GRAY);
        fm = backGraphics.getFontMetrics();
        String instruction = "Gunakan ↑/↓ untuk navigasi, ENTER untuk memilih";
        int instrX = 650 - fm.stringWidth(instruction) / 2;
        backGraphics.drawString(instruction, instrX, 680);
    }

    private void drawDifficultyMenu() {
        // Background effect
        backGraphics.setColor(new Color(0, 0, 0, 150));
        backGraphics.fillRect(0, 0, 1300, 900);

        // Main container
        int containerX = 150;
        int containerY = 100;
        int containerWidth = 1000;
        int containerHeight = 700;

        // Draw container background
        backGraphics.setColor(new Color(30, 30, 30, 200));
        backGraphics.fillRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);
        backGraphics.setColor(new Color(255, 255, 255, 100));
        backGraphics.drawRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);

        // Title
        backGraphics.setFont(new Font("Arial", Font.BOLD, 55));
        backGraphics.setColor(Color.WHITE);
        FontMetrics fm = backGraphics.getFontMetrics();
        String title = "PILIH TINGKAT KESULITAN";
        int titleX = 650 - fm.stringWidth(title) / 2;
        backGraphics.drawString(title, titleX, 180);

        // Center divider line (RED)
        backGraphics.setColor(Color.RED);
        backGraphics.fillRect(620, 200, 60, 5);

        // Difficulty options
        DifficultyLevel[] levels = DifficultyLevel.values();
        backGraphics.setFont(new Font("Arial", Font.BOLD, 35));

        for (int i = 0; i < levels.length; i++) {
            int optionY = 280 + i * 120;

            if (menuSelection == i) {
                backGraphics.setColor(new Color(0, 255, 100, 180));
                backGraphics.fillRoundRect(containerX + 50, optionY - 50, containerWidth - 100, 90, 20, 20);
                backGraphics.setColor(Color.BLACK);
            } else {
                backGraphics.setColor(Color.WHITE);
            }

            fm = backGraphics.getFontMetrics();
            int optionX = 650 - fm.stringWidth(levels[i].description) / 2;
            backGraphics.drawString(levels[i].description, optionX, optionY);
        }

        // Instructions
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 22));
        backGraphics.setColor(Color.LIGHT_GRAY);
        fm = backGraphics.getFontMetrics();

        String instruction1 = "Pemain 1: WASD | Pemain 2: Panah | 3 Nyawa Setiap Pemain";
        int instr1X = 650 - fm.stringWidth(instruction1) / 2;
        backGraphics.drawString(instruction1, instr1X, 720);

        String instruction2 = "ESC untuk kembali, ENTER untuk mulai";
        int instr2X = 650 - fm.stringWidth(instruction2) / 2;
        backGraphics.drawString(instruction2, instr2X, 750);
    }

    private void drawCountdown() {
        drawGameBackground();

        // Countdown display
        backGraphics.setColor(new Color(0, 0, 0, 220));
        backGraphics.fillOval(500, 300, 300, 300);
        backGraphics.setColor(new Color(255, 255, 255, 50));
        backGraphics.drawOval(500, 300, 300, 300);

        backGraphics.setFont(new Font("Arial", Font.BOLD, 120));
        backGraphics.setColor(Color.YELLOW);

        FontMetrics fm = backGraphics.getFontMetrics();
        String countText = countdownTimer > 0 ? String.valueOf(countdownTimer) : "MULAI!";
        int textX = 650 - fm.stringWidth(countText) / 2;
        backGraphics.drawString(countText, textX, 480);
    }

    private void drawGame() {
        drawGameBackground();
        updateGameObjects();
        drawGameObjects();
        drawGameUI();
    }

    private void drawGameBackground() {
        if (roadImage != null) {
            // Only animate road when playing
            if (gameState == GameState.PLAYING) {
                roadOffset += 12;
                if (roadOffset >= roadImage.getHeight()) {
                    roadOffset = 0;
                }
            }

            // Draw road image with scrolling effect
            backGraphics.drawImage(roadImage, 0, roadOffset, null);
            // Draw second copy for seamless scrolling
            backGraphics.drawImage(roadImage, 0, roadOffset - roadImage.getHeight(), null);

            // HIGHLIGHT CENTER DIVIDER - Make it more visible
            backGraphics.setColor(new Color(255, 0, 0, 200)); // Semi-transparent red
            backGraphics.fillRect(645, 0, 10, 900);

            // Add warning stripes on center divider
            backGraphics.setColor(Color.WHITE);
            for (int i = 0; i < 900; i += 40) {
                backGraphics.fillRect(647, i, 6, 20);
            }

        } else {
            // Fallback road with enhanced center divider
            backGraphics.setColor(new Color(105, 105, 105));
            backGraphics.fillRect(0, 0, 1300, 900);

            // Enhanced center divider
            backGraphics.setColor(Color.RED);
            backGraphics.fillRect(645, 0, 10, 900);

            // Warning stripes
            backGraphics.setColor(Color.WHITE);
            for (int i = 0; i < 900; i += 40) {
                backGraphics.fillRect(647, i, 6, 20);
            }

            // Side boundaries
            backGraphics.setColor(Color.YELLOW);
            backGraphics.fillRect(25, 0, 5, 900); // Left boundary
            backGraphics.fillRect(1270, 0, 5, 900); // Right boundary
        }
    }

    private void drawPauseMenu() {
        // Draw the game background (frozen)
        if (roadImage != null) {
            // Draw static road image without animation
            backGraphics.drawImage(roadImage, 0, roadOffset, null);
            backGraphics.drawImage(roadImage, 0, roadOffset - roadImage.getHeight(), null);
        } else {
            backGraphics.setColor(new Color(105, 105, 105));
            backGraphics.fillRect(0, 0, 1300, 900);
            backGraphics.setColor(Color.RED);
            backGraphics.fillRect(645, 0, 10, 900);
        }

        // Draw static game objects
        drawGameObjects();
        drawGameUI();

        // Dark overlay
        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRect(0, 0, 1300, 900);

        // Main container
        int containerX = 300;
        int containerY = 250;
        int containerWidth = 700;
        int containerHeight = 400;

        // Draw container background
        backGraphics.setColor(new Color(30, 30, 30, 220));
        backGraphics.fillRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);
        backGraphics.setColor(new Color(255, 255, 255, 150));
        backGraphics.drawRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);

        // Title
        backGraphics.setFont(new Font("Arial", Font.BOLD, 60));
        backGraphics.setColor(Color.WHITE);
        FontMetrics fm = backGraphics.getFontMetrics();
        String title = "PERMAINAN DIJEDA";
        int titleX = 650 - fm.stringWidth(title) / 2;
        backGraphics.drawString(title, titleX, 330);

        // Center divider line (RED)
        backGraphics.setColor(Color.RED);
        backGraphics.fillRect(620, 350, 60, 5);

        // Menu options
        String[] options = { "LANJUTKAN", "MENU UTAMA", "KELUAR" };
        backGraphics.setFont(new Font("Arial", Font.BOLD, 35));

        for (int i = 0; i < options.length; i++) {
            int optionY = 420 + i * 60;

            if (menuSelection == i) {
                backGraphics.setColor(new Color(255, 255, 0, 180));
                backGraphics.fillRoundRect(containerX + 50, optionY - 35, containerWidth - 100, 50, 15, 15);
                backGraphics.setColor(Color.BLACK);
            } else {
                backGraphics.setColor(Color.WHITE);
            }

            fm = backGraphics.getFontMetrics();
            int optionX = 650 - fm.stringWidth(options[i]) / 2;
            backGraphics.drawString(options[i], optionX, optionY);
        }

        // Instructions
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        backGraphics.setColor(Color.LIGHT_GRAY);
        fm = backGraphics.getFontMetrics();
        String instruction = "↑/↓ untuk navigasi, ENTER untuk memilih, ESC untuk lanjutkan";
        int instrX = 650 - fm.stringWidth(instruction) / 2;
        backGraphics.drawString(instruction, instrX, 600);
    }

    private void updateGameObjects() {
        // All updates are now controlled by gameState check within each object
        handleMovement();

        player1.update();
        player2.update();

        for (TreeObject tree : trees) {
            tree.update();
        }

        for (ObstacleCar obstacle : obstacleCars) {
            obstacle.update();

            if (gameState == GameState.PLAYING) {
                if (obstacle.checkCollision(player1)) {
                    player1.takeDamage();
                }
                if (obstacle.checkCollision(player2)) {
                    player2.takeDamage();
                }
            }
        }

        particles.removeIf(Particle::isDead);
        for (Particle particle : particles) {
            particle.update();
        }
    }

    private void drawGameObjects() {
        for (TreeObject tree : trees) {
            tree.draw(backGraphics);
        }

        for (ObstacleCar obstacle : obstacleCars) {
            obstacle.draw(backGraphics);
        }

        for (Particle particle : particles) {
            particle.draw(backGraphics);
        }

        player1.draw(backGraphics);
        player2.draw(backGraphics);
    }

    private void drawGameUI() {
        // Timer
        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRoundRect(550, 20, 200, 80, 20, 20);
        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 36));
        backGraphics.drawString("Waktu: " + gameTimer, 570, 70);

        drawPlayerStatus(player1, 50, 20);
        drawPlayerStatus(player2, 950, 20);

        // BOUNDARY RULES REMINDER
        backGraphics.setColor(new Color(0, 0, 0, 120));
        backGraphics.fillRoundRect(400, 820, 500, 60, 15, 15);

        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 18));
        backGraphics.drawString("🚫 DILARANG MELEWATI MARKA MERAH! 🚫", 420, 845);

        backGraphics.setColor(Color.YELLOW);
        backGraphics.setFont(new Font("Arial", Font.PLAIN, 14));
        backGraphics.drawString("Player 1: Jalur Kiri | Player 2: Jalur Kanan", 450, 865);
    }

    private void drawPlayerStatus(Player player, int x, int y) {
        // Background
        backGraphics.setColor(new Color(0, 0, 0, 180));
        backGraphics.fillRoundRect(x, y, 250, 120, 20, 20);

        // Player name
        backGraphics.setColor(player.statusColor);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 24));
        backGraphics.drawString(player.name, x + 10, y + 30);

        // Lives
        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        backGraphics.drawString("Nyawa:", x + 10, y + 60);

        for (int i = 0; i < 3; i++) {
            if (i < player.lives) {
                backGraphics.setColor(Color.RED);
            } else {
                backGraphics.setColor(Color.DARK_GRAY);
            }
            backGraphics.fillOval(x + 80 + i * 30, y + 45, 20, 20);
        }

        // Status
        if (!player.alive) {
            backGraphics.setColor(Color.RED);
            backGraphics.setFont(new Font("Arial", Font.BOLD, 18));
            backGraphics.drawString("TABRAKAN", x + 10, y + 100);
        } else if (player.invulnerable) {
            backGraphics.setColor(Color.YELLOW);
            backGraphics.setFont(new Font("Arial", Font.BOLD, 18));
            backGraphics.drawString("KEBAL", x + 10, y + 100);
        } else {
            backGraphics.setColor(Color.GREEN);
            backGraphics.setFont(new Font("Arial", Font.BOLD, 18));
            backGraphics.drawString("AKTIF", x + 10, y + 100);
        }
    }

    private void drawGameOver() {
        drawGameBackground();
        drawGameObjects();

        // Main container
        int containerX = 200;
        int containerY = 200;
        int containerWidth = 900;
        int containerHeight = 500;

        // Game over overlay
        backGraphics.setColor(new Color(0, 0, 0, 220));
        backGraphics.fillRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);
        backGraphics.setColor(new Color(255, 255, 255, 100));
        backGraphics.drawRoundRect(containerX, containerY, containerWidth, containerHeight, 30, 30);

        // Winner text
        backGraphics.setFont(new Font("Arial", Font.BOLD, 45));
        backGraphics.setColor(new Color(255, 215, 0));
        FontMetrics fm = backGraphics.getFontMetrics();
        int winnerX = 650 - fm.stringWidth(winner) / 2;
        backGraphics.drawString(winner, winnerX, 320);

        // Center divider line (RED)
        backGraphics.setColor(Color.RED);
        backGraphics.fillRect(620, 340, 60, 5);

        // Statistics
        backGraphics.setFont(new Font("Arial", Font.BOLD, 28));
        backGraphics.setColor(Color.WHITE);
        fm = backGraphics.getFontMetrics();
        String statsTitle = "Statistik Akhir:";
        int statsTitleX = 650 - fm.stringWidth(statsTitle) / 2;
        backGraphics.drawString(statsTitle, statsTitleX, 400);

        backGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = backGraphics.getFontMetrics();

        String stat1 = player1.name + " Nyawa: " + player1.lives;
        int stat1X = 650 - fm.stringWidth(stat1) / 2;
        backGraphics.drawString(stat1, stat1X, 440);

        String stat2 = player2.name + " Nyawa: " + player2.lives;
        int stat2X = 650 - fm.stringWidth(stat2) / 2;
        backGraphics.drawString(stat2, stat2X, 470);

        String stat3 = "Kesulitan: " + difficulty.description;
        int stat3X = 650 - fm.stringWidth(stat3) / 2;
        backGraphics.drawString(stat3, stat3X, 500);

        // Restart instruction
        backGraphics.setFont(new Font("Arial", Font.BOLD, 26));
        backGraphics.setColor(Color.CYAN);
        fm = backGraphics.getFontMetrics();
        String restart = "Tekan R untuk Restart | ESC untuk Menu Utama";
        int restartX = 650 - fm.stringWidth(restart) / 2;
        backGraphics.drawString(restart, restartX, 620);
    }

    // === EVENT HANDLING ===
    @Override
    public void keyPressed(KeyEvent e) {
        keysPressed.add(e.getKeyCode());

        switch (gameState) {
            case MAIN_MENU -> handleMainMenuInput(e);
            case DIFFICULTY_SELECT -> handleDifficultyMenuInput(e);
            case PLAYING -> handlePlayingInput(e);
            case PAUSED -> handlePauseMenuInput(e);
            case GAME_OVER -> handleGameOverInput(e);
        }
    }

    private void handlePlayingInput(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            pauseGame();
        }
    }

    private void handlePauseMenuInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> menuSelection = Math.max(0, menuSelection - 1);
            case KeyEvent.VK_DOWN -> menuSelection = Math.min(2, menuSelection + 1);
            case KeyEvent.VK_ENTER -> {
                switch (menuSelection) {
                    case 0 -> resumeGame();
                    case 1 -> {
                        gameState = GameState.MAIN_MENU;
                        menuSelection = 0;
                        gameTimerObj.stop();
                    }
                    case 2 -> System.exit(0);
                }
            }
            case KeyEvent.VK_ESCAPE -> resumeGame();
        }
    }

    private void handleMainMenuInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> menuSelection = Math.max(0, menuSelection - 1);
            case KeyEvent.VK_DOWN -> menuSelection = Math.min(1, menuSelection + 1);
            case KeyEvent.VK_ENTER -> {
                if (menuSelection == 0) {
                    gameState = GameState.DIFFICULTY_SELECT;
                    menuSelection = 1;
                } else {
                    System.exit(0);
                }
            }
        }
    }

    private void handleDifficultyMenuInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> menuSelection = Math.max(0, menuSelection - 1);
            case KeyEvent.VK_DOWN -> menuSelection = Math.min(2, menuSelection + 1);
            case KeyEvent.VK_ENTER -> startNewGame();
            case KeyEvent.VK_ESCAPE -> {
                gameState = GameState.MAIN_MENU;
                menuSelection = 0;
            }
        }
    }

    private void handleGameOverInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R -> startNewGame();
            case KeyEvent.VK_ESCAPE -> {
                gameState = GameState.MAIN_MENU;
                menuSelection = 0;
            }
        }
    }

    private void startNewGame() {
        difficulty = DifficultyLevel.values()[menuSelection];
        gameState = GameState.COUNTDOWN;
        countdownTimer = 3;

        player1.reset(200, 750);
        player2.reset(1000, 750);

        initializeTrees();
        initializeObstacles();
        particles.clear();

        roadOffset = 0;
        winner = "";

        countdownTimerObj.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        renderGame();
        gamePanel.repaint();
    }

    // === MAIN METHOD ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new CarRace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}