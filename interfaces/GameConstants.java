package interfaces;

import java.awt.Color;

public interface GameConstants {

    int SCREEN_WIDTH = 1300;
    int SCREEN_HEIGHT = 900;
    String GAME_TITLE = "🏁 Car Race Survival - OOP Edition 🏁";

    int GAME_DURATION = 30;
    int COUNTDOWN_TIME = 3;
    int FRAME_RATE = 16;
    int LEVEL_TRANSITION_TIME = 3;

    int LEFT_BOUNDARY = 35;
    int RIGHT_BOUNDARY = 1265;
    int CENTER_DIVIDER_LEFT = 640;
    int CENTER_DIVIDER_RIGHT = 660;
    int ROAD_WIDTH = RIGHT_BOUNDARY - LEFT_BOUNDARY;
    int LEFT_LANE_WIDTH = CENTER_DIVIDER_LEFT - LEFT_BOUNDARY;
    int RIGHT_LANE_WIDTH = RIGHT_BOUNDARY - CENTER_DIVIDER_RIGHT;

    int CAR_WIDTH = 70;
    int CAR_HEIGHT = 100;
    int OBSTACLE_MIN_WIDTH = 60;
    int OBSTACLE_MAX_WIDTH = 80;
    int OBSTACLE_MIN_HEIGHT = 90;
    int OBSTACLE_MAX_HEIGHT = 110;

    int MOVE_SPEED = 8;
    int ROAD_SCROLL_SPEED = 12;
    double COLLISION_TOLERANCE = 0.8;
    double SEPARATION_FORCE = 0.3;
    int MIN_SEPARATION_DISTANCE = 80;

    int PLAYER_LIVES = 3;
    int MAX_PLAYER_LIVES = 6;
    int INVULNERABILITY_TIME = 120;
    int BONUS_LIFE_INTERVAL = 3;

    int BASE_OBSTACLE_COUNT = 4;
    int MAX_OBSTACLE_COUNT = 25;
    int OBSTACLE_SPAWN_MARGIN = 200;
    double OBSTACLE_MIN_SPEED = 3.0;
    double OBSTACLE_MAX_SPEED = 15.0;

    int CRASH_PARTICLES = 15;
    int PARTICLE_LIFE = 60;
    int EXPLOSION_RADIUS = 50;
    int TRAIL_PARTICLES = 5;

    Color ROAD_COLOR = new Color(60, 60, 60);
    Color DIVIDER_COLOR = Color.YELLOW;
    Color BOUNDARY_COLOR = Color.RED;
    Color GRASS_COLOR = new Color(34, 139, 34);
    Color PLAYER1_COLOR = new Color(0, 150, 255);
    Color PLAYER2_COLOR = new Color(255, 100, 100);
    Color OBSTACLE_COLOR = new Color(128, 128, 128);
    Color PARTICLE_COLORS[] = {
            Color.ORANGE, Color.RED, Color.YELLOW, Color.WHITE, Color.CYAN
    };

    double EASY_SPEED_MULTIPLIER = 0.7;
    double MEDIUM_SPEED_MULTIPLIER = 1.0;
    double HARD_SPEED_MULTIPLIER = 1.5;

    int LEVELS_PER_DIFFICULTY_INCREASE = 5;
    double LEVEL_SPEED_INCREASE = 0.1;
    double MAX_SPEED_MULTIPLIER = 3.0;

    boolean SOUND_ENABLED = true;
    double MASTER_VOLUME = 0.8;
    double SFX_VOLUME = 0.7;
    double MUSIC_VOLUME = 0.5;

    long INPUT_COOLDOWN = 16;
    int MENU_NAVIGATION_DELAY = 150;

    boolean DEBUG_MODE = false;
    boolean SHOW_COLLISION_BOXES = false;
    boolean SHOW_FPS = true;
    boolean SHOW_COORDINATES = false;
    boolean INVINCIBLE_MODE = false;

    int POINTS_PER_SECOND = 10;
    int POINTS_PER_LEVEL = 100;
    int POINTS_PER_OBSTACLE_AVOIDED = 5;
    int BONUS_MULTIPLIER = 2;

    int LANE_COUNT = 6;
    int LANE_WIDTH = ROAD_WIDTH / LANE_COUNT;

    int LEFT_LANE_1 = LEFT_BOUNDARY + (LANE_WIDTH * 0);
    int LEFT_LANE_2 = LEFT_BOUNDARY + (LANE_WIDTH * 1);
    int LEFT_LANE_3 = LEFT_BOUNDARY + (LANE_WIDTH * 2);

    int RIGHT_LANE_1 = CENTER_DIVIDER_RIGHT + (LANE_WIDTH * 0);
    int RIGHT_LANE_2 = CENTER_DIVIDER_RIGHT + (LANE_WIDTH * 1);
    int RIGHT_LANE_3 = CENTER_DIVIDER_RIGHT + (LANE_WIDTH * 2);

    int POWERUP_SPAWN_CHANCE = 15;
    int POWERUP_DURATION = 300;
    int SHIELD_DURATION = 180;
    int SPEED_BOOST_MULTIPLIER = 2;

    boolean RAIN_EFFECT = false;
    double RAIN_SPEED_PENALTY = 0.8;
    int RAIN_PARTICLES = 100;

    int MAX_PARTICLES = 200;
    int MAX_TRAIL_LENGTH = 10;
    boolean VSYNC_ENABLED = true;
    int TARGET_FPS = 60;

    String ASSETS_PATH = "/assets/";
    String IMAGES_PATH = ASSETS_PATH + "images/";
    String SOUNDS_PATH = ASSETS_PATH + "sounds/";
    String FONTS_PATH = ASSETS_PATH + "fonts/";

    String[] CAR_IMAGES = {
            "gamecar1.png", "gamecar2.png", "gamecar3.png", "gamecar4.png"
    };
    String TREE_IMAGE = "tree.png";
    String ROAD_IMAGE = "jalan.png";

    int MAX_COLLISION_CHECKS_PER_FRAME = 1000;
    int MAX_RETRY_ATTEMPTS = 3;
    long ERROR_COOLDOWN = 1000;

    static int getLaneCenterX(int laneIndex) {
        if (laneIndex < 3) {

            return LEFT_BOUNDARY + (laneIndex * LANE_WIDTH) + (LANE_WIDTH / 2);
        } else {

            int rightLaneIndex = laneIndex - 3;
            return CENTER_DIVIDER_RIGHT + (rightLaneIndex * LANE_WIDTH) + (LANE_WIDTH / 2);
        }
    }

    static boolean isInLeftPlayerArea(double x) {
        return x >= LEFT_BOUNDARY && x <= CENTER_DIVIDER_LEFT;
    }

    static boolean isInRightPlayerArea(double x) {
        return x >= CENTER_DIVIDER_RIGHT && x <= RIGHT_BOUNDARY;
    }

    static double getSpeedMultiplier(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> EASY_SPEED_MULTIPLIER;
            case "HARD" -> HARD_SPEED_MULTIPLIER;
            default -> MEDIUM_SPEED_MULTIPLIER;
        };
    }
}