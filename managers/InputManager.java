package managers;

import models.Player;
import enums.GameState;
import interfaces.GameConstants;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputManager {
    
    private Set<Integer> keysPressed = new HashSet<>();
    private Set<Integer> keysJustPressed = new HashSet<>();
    private int menuSelection = 0;
    
    private long lastInputTime = 0;
    private static final long INPUT_COOLDOWN = 16;
    
    public void keyPressed(int keyCode) {
        if (!keysPressed.contains(keyCode)) {
            keysJustPressed.add(keyCode);
        }
        keysPressed.add(keyCode);
    }
    
    public void keyReleased(int keyCode) {
        keysPressed.remove(keyCode);
        keysJustPressed.remove(keyCode);
    }
    
    public void clearJustPressed() {
        keysJustPressed.clear();
    }
    
    public void handleMovement(Player player1, Player player2, GameState gameState) {
        if (gameState != GameState.PLAYING) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInputTime < INPUT_COOLDOWN) {
            return;
        }
        lastInputTime = currentTime;
        
        if (player1.isAlive()) {
            if (keysPressed.contains(KeyEvent.VK_W)) {
                player1.moveUp();
            }
            if (keysPressed.contains(KeyEvent.VK_S)) {
                player1.moveDown();
            }
            if (keysPressed.contains(KeyEvent.VK_A)) {
                player1.moveLeft();
            }
            if (keysPressed.contains(KeyEvent.VK_D)) {
                player1.moveRight();
            }
        }
        
        if (player2.isAlive()) {
            if (keysPressed.contains(KeyEvent.VK_UP)) {
                player2.moveUp();
            }
            if (keysPressed.contains(KeyEvent.VK_DOWN)) {
                player2.moveDown();
            }
            if (keysPressed.contains(KeyEvent.VK_LEFT)) {
                player2.moveLeft();
            }
            if (keysPressed.contains(KeyEvent.VK_RIGHT)) {
                player2.moveRight();
            }
        }
    }
    
    public int handleMenuInput(KeyEvent e, int maxOptions) {
        int oldSelection = menuSelection;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                menuSelection = Math.max(0, menuSelection - 1);
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                menuSelection = Math.min(maxOptions - 1, menuSelection + 1);
            }
        }
        
        if (oldSelection != menuSelection) {
            System.out.println("🎮 Menu selection: " + menuSelection);
        }
        
        return menuSelection;
    }
    
    public boolean isPlayer1Moving() {
        return keysPressed.contains(KeyEvent.VK_W) ||
               keysPressed.contains(KeyEvent.VK_A) ||
               keysPressed.contains(KeyEvent.VK_S) ||
               keysPressed.contains(KeyEvent.VK_D);
    }
    
    public boolean isPlayer2Moving() {
        return keysPressed.contains(KeyEvent.VK_UP) ||
               keysPressed.contains(KeyEvent.VK_LEFT) ||
               keysPressed.contains(KeyEvent.VK_DOWN) ||
               keysPressed.contains(KeyEvent.VK_RIGHT);
    }
    
    public boolean isKeyComboPressed(int... keyCodes) {
        for (int keyCode : keyCodes) {
            if (!keysPressed.contains(keyCode)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean wasKeyJustPressed(int keyCode) {
        return keysJustPressed.contains(keyCode);
    }
    
    public String getActiveKeysString() {
        if (keysPressed.isEmpty()) {
            return "No keys pressed";
        }
        
        StringBuilder sb = new StringBuilder("Active keys: ");
        for (int key : keysPressed) {
            sb.append(KeyEvent.getKeyText(key)).append(" ");
        }
        return sb.toString().trim();
    }
    
    public boolean hasValidMovementInput(Player player) {
        if (player.getName().contains("1")) {
            return isPlayer1Moving();
        } else {
            return isPlayer2Moving();
        }
    }
    
    public void clearAllInputs() {
        keysPressed.clear();
        keysJustPressed.clear();
        System.out.println("🛑 All inputs cleared");
    }
    
    public int getMenuSelection() {
        return menuSelection;
    }
    
    public void setMenuSelection(int selection) {
        this.menuSelection = Math.max(0, selection);
    }
    
    public boolean isKeyPressed(int keyCode) {
        return keysPressed.contains(keyCode);
    }
    
    public Set<Integer> getAllPressedKeys() {
        return new HashSet<>(keysPressed);
    }
    
    public int getActiveKeyCount() {
        return keysPressed.size();
    }
    
    public void printInputStats() {
        System.out.println("📊 INPUT STATS:");
        System.out.println("   Active keys: " + keysPressed.size());
        System.out.println("   Menu selection: " + menuSelection);
        System.out.println("   Player 1 moving: " + isPlayer1Moving());
        System.out.println("   Player 2 moving: " + isPlayer2Moving());
    }
}