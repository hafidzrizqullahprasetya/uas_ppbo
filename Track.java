import java.awt.*;
import javax.swing.ImageIcon;

public class Track {
    private static final int WIDTH = 600, HEIGHT = 600;
    private static final int ROAD_WIDTH = 150;
    private static final int FINISH_LINE_Y = 50;
    private static final int BARRIER_WIDTH = 10;
    private Image roadImage, tireImage;

    public Track() {
        roadImage = new ImageIcon("assets/jalan.png").getImage();
        tireImage = new ImageIcon("assets/ban.png").getImage();
    }

    public boolean isBarrier(int x, int y) {
        if (x < BARRIER_WIDTH || x > WIDTH - BARRIER_WIDTH) return true;
        if (x < WIDTH / 2 - ROAD_WIDTH / 2 || x > WIDTH / 2 + ROAD_WIDTH / 2) {
            if (y > HEIGHT / 3 && y < 2 * HEIGHT / 3) return false;
            return true;
        }
        return false;
    }

    public boolean isGrass(int x, int y) {
        return (x < WIDTH / 2 - ROAD_WIDTH / 2 || x > WIDTH / 2 + ROAD_WIDTH / 2) &&
                (y > HEIGHT / 3 && y < 2 * HEIGHT / 3);
    }

    public boolean isTire(int x, int y) {
        int[][] tirePositions = {{50, 100}, {50, 500}, {550, 100}, {550, 500}};
        for (int[] pos : tirePositions) {
            if (Math.abs(x - pos[0]) < 20 && Math.abs(y - pos[1]) < 20) return true;
        }
        return false;
    }

    public boolean isFinishLine(int x, int y) {
        return y < FINISH_LINE_Y && x > WIDTH / 2 - ROAD_WIDTH / 2 && x < WIDTH / 2 + ROAD_WIDTH / 2;
    }

    public void draw(Graphics g) {
        // Gambar lintasan dari asset
        g.drawImage(roadImage, WIDTH / 2 - ROAD_WIDTH / 2, 0, ROAD_WIDTH, HEIGHT, null);

        // Gambar ban dari asset
        int[][] tirePositions = {{50, 100}, {50, 500}, {550, 100}, {550, 500}};
        for (int[] pos : tirePositions) {
            g.drawImage(tireImage, pos[0] - 20, pos[1] - 20, 40, 40, null);
        }

        // Gambar garis finish (disesuaikan jika ada asset)
        g.setColor(java.awt.Color.BLACK);
        for (int i = 0; i < ROAD_WIDTH / 20; i++) {
            if (i % 2 == 0) g.setColor(java.awt.Color.WHITE);
            else g.setColor(java.awt.Color.BLACK);
            g.fillRect(WIDTH / 2 - ROAD_WIDTH / 2 + i * 20, FINISH_LINE_Y - 20, 20, 20);
        }
    }
}