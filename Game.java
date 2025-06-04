import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Game extends JPanel implements ActionListener {
    private Player player1, player2;
    private Track track;
    private Timer timer;
    private JPanel player1Panel, player2Panel;

    public Game() {
        setLayout(new GridLayout(1, 2));

        track = new Track();
        player1 = new Player(new Car(100, 500, "assets/car_red.png"), "WASD");
        player2 = new Player(new Car(200, 500, "assets/car_blue.png"), "ARROW");

        player1Panel = new PlayerPanel(player1, player2, track);
        player2Panel = new PlayerPanel(player2, player1, track);
        add(player1Panel);
        add(player2Panel);

        addKeyListener(player1);
        addKeyListener(player2);
        setFocusable(true);

        timer = new Timer(16, this); // 60 FPS
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player1.update(track);
        player2.update(track);
        player1Panel.repaint();
        player2Panel.repaint();
    }
}