package enums;

public enum DifficultyLevel {
    EASY(3, 3, 6, "MUDAH - Berkendara Santai"),
    MEDIUM(4, 5, 10, "SEDANG - Lalu Lintas Kota"),
    HARD(6, 7, 14, "SULIT - Kecepatan Tinggi");

    public final int baseObstacleCount;
    public final int baseMinSpeed;
    public final int baseMaxSpeed;
    public final String description;

    DifficultyLevel(int count, int min, int max, String desc) {
        this.baseObstacleCount = count;
        this.baseMinSpeed = min;
        this.baseMaxSpeed = max;
        this.description = desc;
    }

    public int getObstacleCount(int level) {
        int obstacles = baseObstacleCount + ((level - 1) * 2);
        return Math.min(obstacles, 25);
    }

    public int getMinSpeed(int level) {
        int speed = baseMinSpeed + (level - 1);
        return Math.min(speed, 18);
    }

    public int getMaxSpeed(int level) {
        int speed = baseMaxSpeed + ((level - 1) * 2);
        return Math.min(speed, 30);
    }

    public int getSpawnSpacing(int level) {
        int baseSpacing = 200;
        int reduction = (level - 1) * 15;
        return Math.max(baseSpacing - reduction, 80);
    }
}