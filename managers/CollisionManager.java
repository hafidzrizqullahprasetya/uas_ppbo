package managers;

import models.*;
import interfaces.GameConstants;
import java.util.List;

public class CollisionManager {

    private static final double COLLISION_TOLERANCE = 0.8;
    private static final double SEPARATION_FORCE = 0.3;
    private static final int MIN_SEPARATION_DISTANCE = 80;

    public void checkAllCollisions(Player player1, Player player2,
            List<ObstacleCar> obstacles, GameManager gameManager) {

        checkPlayerObstacleCollisions(player1, obstacles, gameManager);
        checkPlayerObstacleCollisions(player2, obstacles, gameManager);

        separateOverlappingCars(obstacles);

        checkBoundaryViolations(player1, gameManager);
        checkBoundaryViolations(player2, gameManager);

        checkPlayerCollision(player1, player2, gameManager);
    }

    private void checkPlayerObstacleCollisions(Player player, List<ObstacleCar> obstacles,
            GameManager gameManager) {
        if (!player.isAlive() || player.isInvulnerable()) {
            return;
        }

        for (ObstacleCar obstacle : obstacles) {
            if (obstacle.isActive() && isColliding(player, obstacle)) {

                gameManager.createCrashEffect(
                        player.getX() + player.getWidth() / 2,
                        player.getY() + player.getHeight() / 2);

                player.takeDamage();

                obstacle.setSpeed(obstacle.getSpeed() * 0.5);

                System.out.println("💥 COLLISION: " + player.getName());
                break;
            }
        }
    }

    private boolean isColliding(Player player, ObstacleCar obstacle) {
        return player.getX() < obstacle.getX() + obstacle.getWidth() &&
                player.getX() + player.getWidth() > obstacle.getX() &&
                player.getY() < obstacle.getY() + obstacle.getHeight() &&
                player.getY() + player.getHeight() > obstacle.getY();
    }

    private void separateOverlappingCars(List<ObstacleCar> obstacles) {
        for (int i = 0; i < obstacles.size(); i++) {
            for (int j = i + 1; j < obstacles.size(); j++) {
                ObstacleCar car1 = obstacles.get(i);
                ObstacleCar car2 = obstacles.get(j);

                if (car1.isActive() && car2.isActive() &&
                        isOverlapping(car1, car2)) {
                    separateTwoCars(car1, car2);
                }
            }
        }
    }

    private boolean isOverlapping(ObstacleCar car1, ObstacleCar car2) {
        double distance = Math.sqrt(
                Math.pow(car1.getX() - car2.getX(), 2) +
                        Math.pow(car1.getY() - car2.getY(), 2));
        return distance < MIN_SEPARATION_DISTANCE;
    }

    private void separateTwoCars(ObstacleCar car1, ObstacleCar car2) {

        if (car1.getSpeed() < car2.getSpeed()) {
            car1.setSpeed(car1.getSpeed() * 0.7);
        } else {
            car2.setSpeed(car2.getSpeed() * 0.7);
        }

        double minSpeed = 2.0;
        if (car1.getSpeed() < minSpeed)
            car1.setSpeed(minSpeed);
        if (car2.getSpeed() < minSpeed)
            car2.setSpeed(minSpeed);
    }

    private void checkBoundaryViolations(Player player, GameManager gameManager) {
        if (!player.isAlive())
            return;

        boolean violation = false;

        if (player.getX() < GameConstants.LEFT_BOUNDARY ||
                player.getX() + player.getWidth() > GameConstants.RIGHT_BOUNDARY) {
            violation = true;
        }

        if (player.getName().contains("1") &&
                player.getX() + player.getWidth() > GameConstants.CENTER_DIVIDER_LEFT) {
            violation = true;
        } else if (player.getName().contains("2") &&
                player.getX() < GameConstants.CENTER_DIVIDER_RIGHT) {
            violation = true;
        }

        if (violation) {
            gameManager.createCrashEffect(
                    player.getX() + player.getWidth() / 2,
                    player.getY() + player.getHeight() / 2);
            player.takeDamage();
            System.out.println("🚫 BOUNDARY VIOLATION: " + player.getName());
        }
    }

    private void checkPlayerCollision(Player player1, Player player2, GameManager gameManager) {
        if (!player1.isAlive() || !player2.isAlive())
            return;

        if (isColliding(player1, player2)) {
            player1.takeDamage();
            player2.takeDamage();

            double centerX = (player1.getX() + player2.getX()) / 2;
            double centerY = (player1.getY() + player2.getY()) / 2;

            gameManager.createCrashEffect(centerX, centerY);
            System.out.println("💥💥 PLAYER COLLISION!");
        }
    }

    private boolean isColliding(Player player1, Player player2) {
        return player1.getX() < player2.getX() + player2.getWidth() &&
                player1.getX() + player1.getWidth() > player2.getX() &&
                player1.getY() < player2.getY() + player2.getHeight() &&
                player1.getY() + player1.getHeight() > player2.getY();
    }
}