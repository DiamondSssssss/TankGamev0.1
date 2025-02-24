package com.mygdx.tankgame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import java.util.ArrayList;
import java.util.List;

public abstract class LevelScreen implements Screen {
    protected final TankGame game;
    protected final Tank playerTank;
    protected final List<Bullet> bullets;
    protected final List<EnemyTank> enemies;
    protected final List<Explosion> explosions;

    public LevelScreen(TankGame game) {
        this.game = game;
        playerTank = new Tank(100, 100);
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        enemies = new ArrayList<>();
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

        // If all enemies are destroyed, move to next level
        if (enemies.isEmpty()) {
            goToNextLevel();
            return;
        }

        game.batch.begin();
        renderGameElements();
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            playerTank.shoot(bullets);
        }
    }

    private void updateGameElements(float delta) {
        playerTank.update(delta, bullets, null);

        bullets.removeIf(bullet -> {
            bullet.update(delta);
            return bullet.isOffScreen();
        });

        enemies.removeIf(enemy -> {
            enemy.update(delta);
            enemy.handleBulletCollision(bullets, explosions);
            return enemy.isDestroyed();
        });

        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
    }

    private void renderGameElements() {
        playerTank.draw(game.batch);
        bullets.forEach(bullet -> bullet.draw(game.batch));
        enemies.forEach(enemy -> enemy.render(game.batch));
        explosions.forEach(explosion -> explosion.render(game.batch));
    }

    protected abstract void goToNextLevel();  // Each level overrides this

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
    public void dispose() {}
}
