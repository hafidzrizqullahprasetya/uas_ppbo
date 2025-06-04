import java.awt.*;
import javax.swing.ImageIcon;

public class Car {
    private double x, y;
    private double speed, maxSpeed, angle;
    private Image carImage;
    private static final int WIDTH = 20, HEIGHT = 40;
    private boolean spinning;

    public Car(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.carImage = new ImageIcon(imagePath).getImage();
        this.speed = 0;
        this.maxSpeed = 5;
        this.angle = -Math.PI / 2; // Menghadap atas
        this.spinning = false;
    }

    public void update(boolean up, boolean down, boolean left, boolean right, Track track) {
        if (spinning) return;

        if (up) speed = Math.min(speed + 0.1, maxSpeed);
        if (down) speed = Math.max(speed - 0.1, -maxSpeed / 2);
        if (!up && !down) speed *= 0.98;

        if (left) angle -= 0.05;
        if (right) angle += 0.05;

        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);

        if (track.isBarrier((int) x, (int) y)) {
            speed *= 0.5;
        }
        if (track.isGrass((int) x, (int) y)) {
            speed *= 0.7;
        }
        if (track.isTire((int) x, (int) y)) {
            speed = 0;
            spinning = true;
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    spinning = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public boolean checkFinishLine(Track track) {
        return track.isFinishLine((int) x, (int) y);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.rotate(angle);
        g2d.drawImage(carImage, -WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT, null);
        g2d.rotate(-angle);
        g2d.translate(-x, -y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}