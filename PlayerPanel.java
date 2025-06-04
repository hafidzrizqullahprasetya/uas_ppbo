import javax.swing.*;
import java.awt.*;

public class PlayerPanel extends JPanel {
    private Player player, opponent;
    private Track track;

    public PlayerPanel(Player player, Player opponent, Track track) {
        this.player = player;
        this.opponent = opponent;
        this.track = track;
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE); // Add background color
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw track first (as background)
        track.draw(g);
        
        // Draw both cars
        player.getCar().draw(g);
        opponent.getCar().draw(g);

        // Display player info with better formatting
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Player: " + getPlayerName(), 10, 20);
        g.drawString("Laps: " + player.getLaps(), 10, 40);
        
        // Optional: Show opponent info too
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Opponent Laps: " + opponent.getLaps(), 10, 60);
    }
    
    // Helper method to get player identifier
    private String getPlayerName() {
        // You might want to add a method to Player class to return control scheme
        // For now, return a generic name
        return "Player";
    }
}