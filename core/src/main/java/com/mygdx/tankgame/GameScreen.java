package com.mygdx.tankgame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EliteEnemyTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private final TankGame game;
    private final PlayerTank playerTank;
    private final List<Bullet> bullets;
    private final List<EnemyTank> enemies; // ✅ Changed from single enemy to List
    private final List<Explosion> explosions;
    private float explosionTimer = 0; // Tracks explosion duration

    public GameScreen(TankGame game) {
        this.game = game;
        playerTank = new PlayerTank(100, 100);
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        enemies = new ArrayList<>(); // ✅ Initialize enemies list
        enemies.add(new EliteEnemyTank(400, 400, playerTank,bullets)); // ✅ Add an enemy to the list
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        updateGameElements(delta);

        // ✅ Switch to GameOverScreen if the player tank is destroyed
        if (playerTank.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return; // Stop further execution
        }

        // ✅ Switch to VictoryScreen after all enemies are destroyed and explosions finish
        if (enemies.isEmpty() && explosionTimer >= 1.5f) {
            game.setScreen(new VictoryScreen(game));
            return; // Stop further execution
        }

        game.batch.begin();
        renderGameElements(delta);
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            playerTank.shoot(bullets);
        }
    }

    private void updateGameElements(float delta) {
        //playerTank.update(delta, bullets, enemies, walls); // ✅ Pass list of enemies

        bullets.removeIf(bullet -> {
            bullet.update(delta);
            return bullet.isOffScreen();
        });

        // ✅ Iterate over enemies list instead of handling a single enemy
        enemies.removeIf(enemy -> {
            if (!enemy.isDestroyed()) {
                enemy.update(delta);
                enemy.handleBulletCollision(bullets, explosions);
                return false; // Keep enemy in list
            } else {
                // ✅ Ensure explosion animation completes before removing enemy
                enemy.setExplosionTimer(enemy.getExplosionTimer() + delta);
                if (enemy.getExplosionTimer() >= 1.5f) {
                    System.out.println("Enemy destroyed. Removing from list.");
                    enemy.dispose(); // ✅ Free resources
                    return true; // Remove enemy from list
                }
                return false; // Keep enemy until explosion is done
            }
        });

        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
    }

    private void renderGameElements(float delta) {
        playerTank.draw(game.batch);

        for (Bullet bullet : bullets) {
            bullet.draw(game.batch);
        }

        // ✅ Loop through all enemies to render them
        for (EnemyTank enemy : enemies) {
            if (!enemy.isDestroyed()) {
                enemy.render(game.batch);
            }
            for (Bullet bullet : enemy.getBullets()) {
                bullet.update(delta);
                bullet.draw(game.batch);
            }
        }

        for (Explosion explosion : explosions) {
            explosion.render(game.batch);
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        for (EnemyTank enemy : enemies) {
            enemy.dispose();
        }
        playerTank.dispose(); // ✅ Ensure resources are cleaned up
    }
}
