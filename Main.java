import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel {
    private String playerName;
    private int score;
    
    public PlayerPanel(String playerName) {
        this.playerName = playerName;
        this.score = 0;
        setPreferredSize(new Dimension(200, 100));
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createTitledBorder(playerName));
    }
    
    public void setScore(int score) {
        this.score = score;
        repaint();
    }
    
    public int getScore() {
        return score;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 30);
    }
}