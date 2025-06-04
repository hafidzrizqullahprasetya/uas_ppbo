import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Player extends KeyAdapter {
    private Car car;
    private String controlType;
    private boolean up, down, left, right;
    private int laps;

    public Player(Car car, String controlType) {
        this.car = car;
        this.controlType = controlType;
        this.laps = 0;
    }

    public Car getCar() {
        return car;
    }

    public int getLaps() {
        return laps;
    }

    public void update(Track track) {
        car.update(up, down, left, right, track);
        if (car.checkFinishLine(track)) {
            laps++;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (controlType.equals("WASD")) {
            if (key == KeyEvent.VK_W) up = true;
            if (key == KeyEvent.VK_S) down = true;
            if (key == KeyEvent.VK_A) left = true;
            if (key == KeyEvent.VK_D) right = true;
        } else if (controlType.equals("ARROW")) {
            if (key == KeyEvent.VK_UP) up = true;
            if (key == KeyEvent.VK_DOWN) down = true;
            if (key == KeyEvent.VK_LEFT) left = true;
            if (key == KeyEvent.VK_RIGHT) right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (controlType.equals("WASD")) {
            if (key == KeyEvent.VK_W) up = false;
            if (key == KeyEvent.VK_S) down = false;
            if (key == KeyEvent.VK_A) left = false;
            if (key == KeyEvent.VK_D) right = false;
        } else if (controlType.equals("ARROW")) {
            if (key == KeyEvent.VK_UP) up = false;
            if (key == KeyEvent.VK_DOWN) down = false;
            if (key == KeyEvent.VK_LEFT) left = false;
            if (key == KeyEvent.VK_RIGHT) right = false;
        }
    }
}