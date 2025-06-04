package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.tankgame.Explosion;
import com.mygdx.tankgame.GameOverScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.playertank.SniperPlayerTank;
import com.mygdx.tankgame.playertank.ShotgunPlayerTank;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LevelScreen implements Screen {
    protected final TankGame game;
    protected final PlayerTank playerTank;
    protected final List<Bullet> bullets;
    protected final List<EnemyTank> enemies;
    protected final List<Explosion> explosions;
    protected final List<EnemyTank> pendingEnemies;
    protected static List<Wall2> walls = new ArrayList<>();
    public static List<Explosion> globalExplosions = new ArrayList<>();

    // Camera & viewport for proper scaling & fullscreen support
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected final int VIRTUAL_WIDTH = 1280;  // your game's logical width
    protected final int VIRTUAL_HEIGHT = 720;  // your game's logical height

    protected Texture heartTexture;
    protected final int heartWidth = 32;
    protected final int heartHeight = 32;
    protected final int heartMarginX = 10;
    protected final int heartMarginY = 10;
    protected Texture bossBarTexture;

    private Texture abilityTextureSniper;
    private Texture abilityTextureShotgun;
    protected Texture backgroundTexture;

    public LevelScreen(TankGame game, PlayerTank playerTank) {
        this.game = game;
        this.playerTank = playerTank;
        bullets = new ArrayList<>();
        explosions = new ArrayList<>();
        enemies = new ArrayList<>();
        pendingEnemies = new ArrayList<>();

        // Initialize camera & viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        playerTank.setCamera(camera);
        heartTexture = new Texture(Gdx.files.internal("heart.jpg"));
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        bossBarTexture = new Texture(pixmap);
        pixmap.dispose();

        abilityTextureSniper = new Texture(Gdx.files.internal("wall.jpg"));
        abilityTextureShotgun = new Texture(Gdx.files.internal("shield.png"));

        setupLevel();
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport & camera when window size changes (fullscreen or window resize)
        viewport.update(width, height, true);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        updateGameElements(delta);

        if (playerTank.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        if (enemies.isEmpty()) {
            goToUpgradeScreen();
            return;
        }

        // IMPORTANT: Update camera & set projection matrix before drawing
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        globalExplosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });

        // Draw background stretching to viewport size (scaled coordinates)
        game.batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        renderGameElements();
        renderWalls();

        for (Explosion explosion : globalExplosions) {
            explosion.render(game.batch);
        }

        // Boss health bar and other UI elements should be drawn using scaled coordinates
        for (EnemyTank enemy : enemies) {
            if (enemy instanceof BossTank) {
                BossTank boss = (BossTank) enemy;
                float healthPercentage = boss.getBossHealth() / (float) boss.getMaxBossHealth();
                float barWidth = VIRTUAL_WIDTH * 0.6f;
                float barHeight = 30f;
                float barX = (VIRTUAL_WIDTH - barWidth) / 2;
                float barY = (VIRTUAL_HEIGHT / 2f) - 100f;
                game.batch.setColor(Color.RED);
                game.batch.draw(bossBarTexture, barX, barY, barWidth, barHeight);
                game.batch.setColor(Color.GREEN);
                game.batch.draw(bossBarTexture, barX, barY, barWidth * healthPercentage, barHeight);
                game.batch.setColor(Color.WHITE);
                break;
            }
        }

        // Draw health hearts
        int currentHealth = playerTank.getCurrentHealth();
        for (int i = 0; i < currentHealth; i++) {
            game.batch.draw(heartTexture,
                heartMarginX + i * (heartWidth + 5),
                VIRTUAL_HEIGHT - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }

        // Draw ability icon with cooldown overlay
        Texture abilityTexture = null;
        float abilityCooldownPercentage = 0f;
        if (playerTank instanceof SniperPlayerTank) {
            abilityTexture = abilityTextureSniper;
            abilityCooldownPercentage = ((SniperPlayerTank) playerTank).getAbilityCooldownPercentage();
        } else if (playerTank instanceof ShotgunPlayerTank) {
            abilityTexture = abilityTextureShotgun;
            abilityCooldownPercentage = ((ShotgunPlayerTank) playerTank).getAbilityCooldownPercentage();
        }
        if (abilityTexture != null) {
            float iconWidth = 64;
            float iconHeight = 64;
            float margin = 20;
            float iconX = VIRTUAL_WIDTH - iconWidth - margin;
            float iconY = margin;
            game.batch.draw(abilityTexture, iconX, iconY, iconWidth, iconHeight);
            float overlayHeight = iconHeight * abilityCooldownPercentage;
            game.batch.setColor(0, 0, 0, 0.5f);
            game.batch.draw(bossBarTexture, iconX, iconY, iconWidth, overlayHeight);
            game.batch.setColor(Color.WHITE);
        }

        game.batch.end();
    }


    public void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            playerTank.shoot(bullets);
        }
    }
    public void renderWalls() {
        for (Wall2 wall : walls) {
            wall.draw(game.batch);
        }
    }
    protected abstract void setupLevel();
    public static void addWall(Wall2 wall) {
        walls.add(wall);
    }

    // Update walls: remove expired ones.
    protected void updateWalls(float delta) {
        for (Wall2 wall : walls) {
            wall.update(delta);
        }
    }

    // Check for bullet-wall collisions.
    protected void checkBulletWallCollisions() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            for (Wall2 wall : walls) {
                if (bullet.getBoundingRectangle().overlaps(wall.getBoundingRectangle())) {
                    bulletIterator.remove();
                    break;
                }
            }
        }
    }
    void updateGameElements(float delta) {
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        bullets.removeIf(Bullet::isOffScreen);

        // Check bullet collisions with walls.
        checkBulletWallCollisions();

        playerTank.update(delta, bullets, enemies, walls);

        for (Iterator<EnemyTank> enemyIterator = enemies.iterator(); enemyIterator.hasNext();) {
            EnemyTank enemy = enemyIterator.next();

            // If this is a ChaserTank, call the new signature:
            if (enemy instanceof ChaserTank) {
                ((ChaserTank)enemy).update(delta, walls);
            } else {
                // For other EnemyTank subclasses that still use update(delta)
                enemy.update(delta);
            }

            enemy.handleBulletCollision(bullets, explosions);
            if (enemy.isDestroyed()) {
                enemy.dispose();
                enemyIterator.remove();
            }
        }

        if (!pendingEnemies.isEmpty()) {
            enemies.addAll(pendingEnemies);
            pendingEnemies.clear();
        }

        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });

        // Update walls.
        updateWalls(delta);
    }

    protected void renderGameElements() {
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
    protected PlayerTank getSecondPlayer() {
        return null; // By default, no second player exists.
    }

    public void spawnChaserTank(){
        PlayerTank secondPlayer = getSecondPlayer();
        if(secondPlayer != null) {
            // In coop mode, use a constructor that accepts two players.
            pendingEnemies.add(new ChaserTank(600, 600, playerTank, secondPlayer, bullets));
        } else {
            // In single-player mode, use the one-player constructor.
            pendingEnemies.add(new ChaserTank(600, 600, playerTank, bullets));
        }
    }

    protected abstract void goToUpgradeScreen();

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
        bossBarTexture.dispose();
        abilityTextureSniper.dispose();
        abilityTextureShotgun.dispose();
        // Dispose other resources if necessary.
    }
}
