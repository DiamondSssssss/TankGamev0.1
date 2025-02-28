package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.tankgame.*;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;
import java.util.Iterator;

public class CoopLevelScreen extends LevelScreen {
    private PlayerTank playerTwo;

    public CoopLevelScreen(TankGame game, PlayerTank playerOne, PlayerTank playerTwo) {
        // Call the base constructor with playerOne as the primary tank.
        super(game, playerOne);
        this.playerTwo = playerTwo;
    }

    @Override
    protected void setupLevel() {
        // Setup your level here – for example, spawn initial enemies.
        // For demonstration, we’ll spawn a ChaserTank.
        enemies.add(new BossTank(400, 400, playerTank,game,bullets)); // One enemy
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game));
    }

    @Override
    public void render(float delta) {
        // Clear the screen.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Update Game Elements ---
        // Update bullets.
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        bullets.removeIf(Bullet::isOffScreen);

        // Check bullet-wall collisions.
        checkBulletWallCollisions();

        // Update player one (the primary tank) using the base LevelScreen update.
        playerTank.update(delta, bullets, enemies);
        // Update player two.
        playerTwo.update(delta, bullets, enemies);

        // Update enemies.
        for (Iterator<EnemyTank> enemyIterator = enemies.iterator(); enemyIterator.hasNext();) {
            EnemyTank enemy = enemyIterator.next();
            enemy.update(delta);
            enemy.handleBulletCollision(bullets, explosions);
            if (enemy.isDestroyed()) {
                enemy.dispose();
                enemyIterator.remove();
            }
        }

        // Process pending enemies.
        if (!pendingEnemies.isEmpty()) {
            enemies.addAll(pendingEnemies);
            pendingEnemies.clear();
        }

        // Update explosions.
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });

        // Update walls.
        updateWalls(delta);

        // Check if either player is destroyed.
        if (playerTank.isDestroyed() || playerTwo.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return;
        }
        // If all enemies are cleared, transition.
        if (enemies.isEmpty()) {
            goToUpgradeScreen();
            return;
        }

        // --- Render Everything ---
        game.batch.begin();
        // Render game elements from LevelScreen (player one, bullets, enemies, explosions, walls, UI).
        renderGameElements();
        // Render player two.
        playerTwo.draw(game.batch);
        game.batch.end();
    }

    // Note: We assume that checkBulletWallCollisions, updateWalls, and renderGameElements
    // are declared as protected in LevelScreen so they are accessible here.
}
