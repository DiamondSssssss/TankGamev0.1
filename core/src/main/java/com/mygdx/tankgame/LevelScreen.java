package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EnemyTank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LevelScreen implements Screen {
    protected final TankGame game;
    protected final Tank playerTank;
    protected final List<Bullet> bullets;
    protected final List<EnemyTank> enemies;
    protected final List<Explosion> explosions;
    protected final List<EnemyTank> pendingEnemies;
    // Heart texture for displaying health
    private Texture heartTexture;
    // Heart dimensions
    private final int heartWidth = 32;
    private final int heartHeight = 32;
    // Margin from top left corner
    private final int heartMarginX = 10;
    private final int heartMarginY = 10;

    public LevelScreen(TankGame game, Tank playerTank) {
        this.game = game;
        this.playerTank = playerTank; // Use the passed Tank instance
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        enemies = new ArrayList<>();
        pendingEnemies = new ArrayList<>();
        // Load the heart texture for the health display
        heartTexture = new Texture(Gdx.files.internal("heart.jpg"));
        setupLevel();  // Each level will override this
    }

    protected abstract void setupLevel();  // Each level implements this

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        updateGameElements(delta);

        // If player dies, go to GameOverScreen
        if (playerTank.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        // If all enemies are destroyed, move to upgrade screen
        if (enemies.isEmpty()) {
            goToUpgradeScreen();
            return;
        }

        game.batch.begin();
        renderGameElements();
        // Draw health hearts at the top-left corner
        int currentHealth = playerTank.getCurrentHealth(); // Ensure Tank has this getter
        for (int i = 0; i < currentHealth; i++) {
            game.batch.draw(heartTexture,
                heartMarginX + i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            playerTank.shoot(bullets);
        }
    }

    private void updateGameElements(float delta) {
        playerTank.update(delta, bullets, enemies);
        bullets.removeIf(bullet -> {
            bullet.update(delta);
            return bullet.isOffScreen();
        });

        for (Iterator<EnemyTank> enemyIterator = enemies.iterator(); enemyIterator.hasNext();) {
            EnemyTank enemy = enemyIterator.next();
            enemy.update(delta);
            enemy.handleBulletCollision(bullets, explosions);
            if (enemy.isDestroyed()) {
                enemy.dispose();
                enemyIterator.remove();
            }
        }
// After processing current enemies, add any pending enemies.
        if (!pendingEnemies.isEmpty()) {
            enemies.addAll(pendingEnemies);
            pendingEnemies.clear();
        }
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
    }

    private void renderGameElements() {
        playerTank.draw(game.batch);
        for (Bullet bullet : bullets) {
            bullet.draw(game.batch);
        }
        for (EnemyTank enemy : enemies) {
            enemy.render(game.batch);
        }
        for (Explosion explosion : explosions) {
            explosion.render(game.batch);
        }
    }
    public void spawnChaserTank(){
        pendingEnemies.add(new ChaserTank(600,600, playerTank));
    }
    protected abstract void goToUpgradeScreen();

    @Override
    public void resize(int width, int height) {
        // Optionally update viewport here
    }

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
        heartTexture.dispose();
        // Dispose other resources if necessary
    }
}
