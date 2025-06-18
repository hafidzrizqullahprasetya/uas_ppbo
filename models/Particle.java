package models;

import interfaces.*;
import java.awt.*;

public class Particle implements Drawable, Updatable {
    
    private double x, y, vx, vy;
    private Color color;
    private int life, maxLife;
    private boolean active;
    
    public Particle(double x, double y, double vx, double vy, Color color, int life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color != null ? color : Color.WHITE;
        this.life = Math.max(1, life);
        this.maxLife = this.life;
        this.active = true;
    }
    
    public Particle(double x, double y, Color color) {
        this(x, y, 
             (Math.random() - 0.5) * 8,
             (Math.random() - 0.5) * 8,
             color, 
             30);
    }
    
    @Override
    public void update() {
        if (!active) return;
        
        x += vx;
        y += vy;
        
        vy += 0.3;
        vx *= 0.98;
        
        life--;
        
        if (life <= 0) {
            active = false;
        }
        
        if (x < -100 || x > interfaces.GameConstants.SCREEN_WIDTH + 100 ||
            y > interfaces.GameConstants.SCREEN_HEIGHT + 100) {
            active = false;
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        if (!active || life <= 0) return;
        
        float alpha = Math.max(0.0f, Math.min(1.0f, (float) life / maxLife));
        
        Color fadeColor = new Color(
            color.getRed(), 
            color.getGreen(), 
            color.getBlue(),
            (int) (255 * alpha)
        );
        
        g.setColor(fadeColor);
        
        int size = Math.max(2, (int) (6 * alpha));
        g.fillOval((int) x - size/2, (int) y - size/2, size, size);
        
        if (alpha > 0.7f) {
            Color glowColor = new Color(
                Math.min(255, color.getRed() + 50),
                Math.min(255, color.getGreen() + 50), 
                Math.min(255, color.getBlue() + 50),
                (int) (100 * alpha)
            );
            g.setColor(glowColor);
            g.fillOval((int) x - size, (int) y - size, size * 2, size * 2);
        }
    }
    
    public boolean isActive() {
        return active && life > 0;
    }
    
    public boolean isDead() { 
        return !active || life <= 0; 
    }
    
    public void deactivate() {
        this.active = false;
        this.life = 0;
    }
    
    public void reset(double newX, double newY, double newVx, double newVy, Color newColor, int newLife) {
        this.x = newX;
        this.y = newY;
        this.vx = newVx;
        this.vy = newVy;
        this.color = newColor != null ? newColor : Color.WHITE;
        this.life = Math.max(1, newLife);
        this.maxLife = this.life;
        this.active = true;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityX() { return vx; }
    public double getVelocityY() { return vy; }
    public Color getColor() { return color; }
    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }
    public float getAlpha() { return (float) life / maxLife; }
    
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }
    
    public void setColor(Color color) {
        this.color = color != null ? color : this.color;
    }
    
    public void addLife(int additionalLife) {
        if (active) {
            this.life += additionalLife;
            this.maxLife = Math.max(this.maxLife, this.life);
        }
    }
    
    public void applyForce(double fx, double fy) {
        if (active) {
            this.vx += fx;
            this.vy += fy;
        }
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getDistanceFrom(double targetX, double targetY) {
        double dx = x - targetX;
        double dy = y - targetY;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return String.format("Particle[pos=(%.1f,%.1f), vel=(%.1f,%.1f), life=%d/%d, active=%s]",
                x, y, vx, vy, life, maxLife, active);
    }
    
    public static Particle createExplosionParticle(double x, double y) {
        Color[] explosionColors = {
            Color.ORANGE, Color.RED, Color.YELLOW, Color.WHITE
        };
        Color randomColor = explosionColors[(int)(Math.random() * explosionColors.length)];
        
        return new Particle(
            x + (Math.random() - 0.5) * 20,
            y + (Math.random() - 0.5) * 20,
            (Math.random() - 0.5) * 12,
            (Math.random() - 0.5) * 12,
            randomColor,
            30 + (int)(Math.random() * 20)
        );
    }
    
    public static Particle createTrailParticle(double x, double y, Color baseColor) {
        return new Particle(
            x,
            y,
            (Math.random() - 0.5) * 2,
            (Math.random() - 0.5) * 2,
            baseColor,
            15 + (int)(Math.random() * 10)
        );
    }
    
    public static Particle createSmokeParticle(double x, double y) {
        Color smokeColor = new Color(128, 128, 128, 180);
        
        return new Particle(
            x,
            y,
            (Math.random() - 0.5) * 3,
            -Math.random() * 2 - 1,
            smokeColor,
            40 + (int)(Math.random() * 20)
        );
    }
}