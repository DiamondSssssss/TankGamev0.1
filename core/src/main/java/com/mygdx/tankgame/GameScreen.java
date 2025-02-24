package com.mygdx.tankgame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private final TankGame game;
    private final Tank playerTank;
    private final List<Bullet> bullets;
    private EnemyTank enemy;
    private final List<Explosion> explosions;
    private float explosionTimer = 0; // Tracks explosion duration

    public GameScreen(TankGame game) {
        this.game = game;
        playerTank = new Tank(100, 100);
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        enemy = new EnemyTank(400, 400, playerTank); // Initialize enemy tank
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

        // ✅ Switch to VictoryScreen after enemy is destroyed and explosion finishes
        if (enemy==null && explosionTimer >= 1.5f) {
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
        playerTank.update(delta, bullets, enemy);

        bullets.removeIf(bullet -> {
            bullet.update(delta);
            return bullet.isOffScreen();
        });

        if (enemy != null) {
            if (!enemy.isDestroyed()) {
                enemy.update(delta);
                enemy.handleBulletCollision(bullets, explosions);
            } else {
                // ✅ Ensure explosion animation completes before switching screen
                explosionTimer += delta;
                if (explosionTimer >= 1.5f) {
                    System.out.println("Enemy destroyed. Switching to Victory Screen.");
                    enemy.dispose(); // ✅ Free resources before nullifying
                    enemy = null;
                }
            }
        }

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

        if (enemy != null && !enemy.isDestroyed()) { // ✅ Ensure enemy isn't null before rendering
            enemy.render(game.batch);
        }
        if (enemy != null) {
            for (Bullet bullet : enemy.getBullets()) {
                bullet.update(delta);
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
        if (enemy != null) {
            enemy.dispose();
        }
        playerTank.dispose(); // ✅ Ensure resources are cleaned up
    }
}
