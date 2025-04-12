package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.Explosion;
import com.mygdx.tankgame.GameOverScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.buildstuff.Wall;
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
    private static List<Wall> walls = new ArrayList<>();
    public static List<Explosion> globalExplosions = new ArrayList<>();
    // Heart texture for displaying health
    protected Texture heartTexture;
    protected final int heartWidth = 32;
    protected final int heartHeight = 32;
    protected final int heartMarginX = 10;
    protected final int heartMarginY = 10;
    protected Texture bossBarTexture;

    // Ability icon textures
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
    protected abstract void setupLevel();
    public static void addWall(Wall wall) {
        walls.add(wall);
    }

    // Update walls: remove expired ones.
    protected void updateWalls(float delta) {
        Iterator<Wall> iter = walls.iterator();
        while (iter.hasNext()) {
            Wall wall = iter.next();
            wall.update(delta);
            if (wall.isExpired()) {
                wall.dispose();
                iter.remove();
            }
        }
    }

    // Check for bullet-wall collisions.
    protected void checkBulletWallCollisions() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            for (Wall wall : walls) {
                if (bullet.getBoundingRectangle().overlaps(wall.getBoundingRectangle())) {
                    bulletIterator.remove();
                    break;
                }
            }
        }
    }

    private void renderWalls() {
        for (Wall wall : walls) {
            wall.draw(game.batch);
        }
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
        globalExplosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderGameElements();
        renderWalls();
        for (Explosion explosion : globalExplosions) {
            explosion.render(game.batch);
        }
        // Draw boss health bar if exists.
        for (EnemyTank enemy : enemies) {
            if (enemy instanceof BossTank) {
                BossTank boss = (BossTank) enemy;
                float healthPercentage = boss.getBossHealth() / (float) boss.getMaxBossHealth();
                float barWidth = Gdx.graphics.getWidth() * 0.6f;
                float barHeight = 30f;
                float barX = (Gdx.graphics.getWidth() - barWidth) / 2;
                float barY = (Gdx.graphics.getHeight() / 2f) - 100f;
                game.batch.setColor(Color.RED);
                game.batch.draw(bossBarTexture, barX, barY, barWidth, barHeight);
                game.batch.setColor(Color.GREEN);
                game.batch.draw(bossBarTexture, barX, barY, barWidth * healthPercentage, barHeight);
                game.batch.setColor(Color.WHITE);
                break;
            }
        }

        // Draw health hearts.
        int currentHealth = playerTank.getCurrentHealth();
        for (int i = 0; i < currentHealth; i++) {
            game.batch.draw(heartTexture,
                heartMarginX + i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }

        // Draw ability icon with cooldown overlay.
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
            float iconX = Gdx.graphics.getWidth() - iconWidth - margin;
            float iconY = margin;
            game.batch.draw(abilityTexture, iconX, iconY, iconWidth, iconHeight);
            float overlayHeight = iconHeight * abilityCooldownPercentage;
            game.batch.setColor(0, 0, 0, 0.5f);
            game.batch.draw(bossBarTexture, iconX, iconY, iconWidth, overlayHeight);
            game.batch.setColor(Color.WHITE);
        }

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            playerTank.shoot(bullets);
        }
    }

    private void updateGameElements(float delta) {
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        bullets.removeIf(Bullet::isOffScreen);

        // Check bullet collisions with walls.
        checkBulletWallCollisions();

        playerTank.update(delta, bullets, enemies);

        for (Iterator<EnemyTank> enemyIterator = enemies.iterator(); enemyIterator.hasNext();) {
            EnemyTank enemy = enemyIterator.next();
            enemy.update(delta);
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
        heartTexture.dispose();
        bossBarTexture.dispose();
        abilityTextureSniper.dispose();
        abilityTextureShotgun.dispose();
        // Dispose other resources if necessary.
    }
}
