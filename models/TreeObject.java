package models;

import interfaces.Drawable;
import interfaces.Updatable;
import interfaces.GameConstants;
import java.awt.*;
import java.util.Random;

public class TreeObject implements Drawable, Updatable {
    private double x, y;
    private double width, height;
    private boolean active;
    private Color treeColor;
    private Color trunkColor;
    private String treeType;
    private Random random;
    private double animationOffset;

    public static final String[] TREE_TYPES = {
            "oak", "pine", "bush", "flower", "rock", "grass"
    };

    public TreeObject(double x, double y, String type) {
        this.x = x;
        this.y = y;
        this.treeType = type != null ? type : "bush";
        this.random = new Random();
        this.active = true;
        this.animationOffset = random.nextDouble() * Math.PI * 2;

        switch (treeType) {
            case "oak", "pine" -> {
                width = 25 + random.nextInt(15);
                height = 35 + random.nextInt(20);
                treeColor = new Color(34, 139, 34);
                trunkColor = new Color(139, 69, 19);
            }
            case "bush" -> {
                width = 15 + random.nextInt(10);
                height = 12 + random.nextInt(8);
                treeColor = new Color(50, 205, 50);
                trunkColor = new Color(85, 107, 47);
            }
            case "flower" -> {
                width = 8 + random.nextInt(6);
                height = 10 + random.nextInt(8);
                treeColor = getRandomFlowerColor();
                trunkColor = new Color(34, 139, 34);
            }
            case "rock" -> {
                width = 12 + random.nextInt(10);
                height = 8 + random.nextInt(6);
                treeColor = Color.GRAY;
                trunkColor = Color.DARK_GRAY;
            }
            default -> {
                width = 20;
                height = 25;
                treeColor = Color.GREEN;
                trunkColor = new Color(139, 69, 19);
            }
        }
    }

    private Color getRandomFlowerColor() {
        Color[] colors = {
                Color.RED, Color.YELLOW, Color.PINK, Color.MAGENTA,
                Color.ORANGE, new Color(138, 43, 226),
                new Color(255, 20, 147)
        };
        return colors[random.nextInt(colors.length)];
    }

    @Override
    public void draw(Graphics2D g) {
        if (!active)
            return;

        Color originalColor = g.getColor();

        int intX = (int) x;
        int intY = (int) y;
        int intWidth = (int) width;
        int intHeight = (int) height;

        switch (treeType) {
            case "oak" -> drawOakTree(g, intX, intY, intWidth, intHeight);
            case "pine" -> drawPineTree(g, intX, intY, intWidth, intHeight);
            case "bush" -> drawBush(g, intX, intY, intWidth, intHeight);
            case "flower" -> drawFlower(g, intX, intY, intWidth, intHeight);
            case "rock" -> drawRock(g, intX, intY, intWidth, intHeight);
            default -> drawBush(g, intX, intY, intWidth, intHeight);
        }

        g.setColor(originalColor);
    }

    private void drawOakTree(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(trunkColor);
        g.fillRect(x + w / 3, y + h * 2 / 3, w / 3, h / 3);

        g.setColor(treeColor);
        g.fillOval(x, y, w, h * 2 / 3);
        g.fillOval(x + w / 4, y - h / 6, w / 2, h / 2);
        g.fillOval(x + w / 6, y + h / 8, w * 2 / 3, h / 2);

        g.setColor(treeColor.brighter());
        g.fillOval(x + w / 4, y + h / 6, w / 3, h / 4);
    }

    private void drawPineTree(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(trunkColor);
        g.fillRect(x + w / 3, y + h * 4 / 5, w / 3, h / 5);

        g.setColor(treeColor);
        int[] xPoints = { x + w / 2, x, x + w };
        int[] yPoints1 = { y, y + h / 3, y + h / 3 };
        int[] yPoints2 = { y + h / 6, y + h * 2 / 3, y + h * 2 / 3 };
        int[] yPoints3 = { y + h / 3, y + h, y + h };

        g.fillPolygon(xPoints, yPoints1, 3);
        g.fillPolygon(xPoints, yPoints2, 3);
        g.fillPolygon(xPoints, yPoints3, 3);
    }

    private void drawBush(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(treeColor);
        g.fillOval(x, y + h / 4, w * 2 / 3, h * 3 / 4);
        g.fillOval(x + w / 4, y, w * 2 / 3, h * 3 / 4);
        g.fillOval(x + w / 3, y + h / 3, w * 2 / 3, h * 2 / 3);

        g.setColor(treeColor.brighter());
        g.fillOval(x + w / 3, y + h / 4, w / 4, h / 4);
    }

    private void drawFlower(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(trunkColor);
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + w / 2, y + h / 2, x + w / 2, y + h);

        g.setColor(treeColor);
        int petalSize = w / 3;
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            int petalX = x + w / 2 + (int) (Math.cos(angle) * w / 4) - petalSize / 2;
            int petalY = y + h / 3 + (int) (Math.sin(angle) * w / 4) - petalSize / 2;
            g.fillOval(petalX, petalY, petalSize, petalSize);
        }

        g.setColor(Color.YELLOW);
        g.fillOval(x + w / 2 - w / 8, y + h / 3 - w / 8, w / 4, w / 4);
    }

    private void drawRock(Graphics2D g, int x, int y, int w, int h) {

        g.setColor(treeColor);
        g.fillOval(x, y + h / 4, w, h * 3 / 4);
        g.fillOval(x + w / 4, y, w / 2, h / 2);

        g.setColor(trunkColor);
        for (int i = 0; i < 3; i++) {
            int dotX = x + random.nextInt(w);
            int dotY = y + random.nextInt(h);
            g.fillOval(dotX, dotY, 2, 2);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(x + w / 4, y + h / 4, w / 4, h / 4);
    }

    @Override
    public void update() {

        animationOffset += 0.05;

        if (treeType.equals("flower") || treeType.equals("bush")) {
            double sway = Math.sin(animationOffset) * 0.5;

        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    public String getTreeType() {
        return treeType;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}