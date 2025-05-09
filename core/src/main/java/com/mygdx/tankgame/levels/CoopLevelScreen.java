package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.tankgame.*;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.enemies.ChaserTank;
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
        enemies.add(new BossTank(400, 400, playerTank, game, bullets)); // Example enemy
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
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        bullets.removeIf(Bullet::isOffScreen);

        // Check bullet-wall collisions.
        checkBulletWallCollisions();

        // Update players.
        // Update players.
        if (!playerTank.isDestroyed())
            playerTank.update(delta, bullets, enemies);
        if (!playerTwo.isDestroyed())
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

        // Check if both players are destroyed.
        if (playerTank.isDestroyed() && playerTwo.isDestroyed()) {
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
        // Render game elements (player one, bullets, enemies, explosions, walls, etc.)
        renderGameElements();
        // Also draw player two.
        playerTwo.draw(game.batch);

        // --- Draw Player Health UI ---
        // Draw player one's hearts (top left)
        int currentHealthOne = playerTank.getCurrentHealth();
        for (int i = 0; i < currentHealthOne; i++) {
            game.batch.draw(heartTexture,
                heartMarginX + i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }
        // Draw player two's hearts (top right)
        int currentHealthTwo = playerTwo.getCurrentHealth();
        for (int i = 0; i < currentHealthTwo; i++) {
            game.batch.draw(heartTexture,
                Gdx.graphics.getWidth() - heartMarginX - heartWidth - i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }

        // Optionally, you can draw ability icons or other UI elements here.

        game.batch.end();
    }
    @Override
    public void spawnChaserTank(){
        pendingEnemies.add(new ChaserTank(600, 600, playerTank, playerTwo, bullets));
    }

}
