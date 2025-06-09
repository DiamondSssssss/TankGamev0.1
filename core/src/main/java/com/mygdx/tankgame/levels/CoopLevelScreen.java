package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.tankgame.*;
import com.mygdx.tankgame.buildstuff.ReviveCircle;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EliteEnemyTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.ArrayList;
import java.util.Iterator;

public class CoopLevelScreen extends LevelScreen {
    private PlayerTank playerTwo;
    private ArrayList<ReviveCircle> reviveCircles = new ArrayList<>();
    // Arrow textures for indicating P1 and P2
    private Texture p1ArrowTexture;
    private Texture p2ArrowTexture;
    private OrthographicCamera coopCamera;
    private Viewport coopViewport;
    private BitmapFont font;

    private static final float REVIVE_TIME = 3f; // seconds to revive
    private static final float VIRTUAL_WIDTH = 1280;  // Virtual viewport width
    private static final float VIRTUAL_HEIGHT = 720; // Virtual viewport height

    public CoopLevelScreen(TankGame game, PlayerTank playerOne, PlayerTank playerTwo) {
        super(game, playerOne);
        this.playerTwo = playerTwo;
        font = new BitmapFont(); // Default font
        font.getData().setScale(2); // Optional: make text larger

        // Create camera and viewport using virtual dimensions
        coopCamera = new OrthographicCamera();
        coopViewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, coopCamera);
        coopViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        coopCamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        coopCamera.update();

        // Load arrow textures
        p1ArrowTexture = new Texture(Gdx.files.internal("p1.png"));
        p2ArrowTexture = new Texture(Gdx.files.internal("p2.png"));
    }

    @Override
    protected void setupLevel() {
        // Load map data from JSON file specific for coop level
        LevelMapLoader.MapData mapData = LevelMapLoader.load("map_coop_level.json");

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal(mapData.background));

        // Add walls and obstacles
        for (Wall2 wall : mapData.walls) {
            addWall(wall);
        }
        // Spawn boss
        if (mapData.bossData != null && mapData.bossData.type.equals("BossTank")) {
            enemies.add(new BossTank(mapData.bossData.x, mapData.bossData.y, playerTank, game, bullets));
        } else {
            Gdx.app.log("ERROR", "No valid boss data in map_level3.json");
        }
        for (LevelMapLoader.EnemyData enemy : mapData.enemyData) {
            switch (enemy.type) {
                case "ChaserTank":
                    enemies.add(new ChaserTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                case "EliteEnemyTank":
                    enemies.add(new EliteEnemyTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                case "EnemyTank":
                    enemies.add(new EnemyTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                default:
                    Gdx.app.log("ERROR", "Unknown enemy type: " + enemy.type);
                    enemies.add(new ChaserTank(enemy.x, enemy.y, playerTank, bullets)); // Fallback
                    break;
            }
        }
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game));
    }

    @Override
    public void render(float delta) {

        // --- Clear Screen ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.B)) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        // --- Update Camera and Projections ---
        coopCamera.update();
        game.batch.setProjectionMatrix(coopCamera.combined);
        game.shapeRenderer.setProjectionMatrix(coopCamera.combined);

        // --- Update Bullets ---
        for (Bullet bullet : bullets) bullet.update(delta);
        bullets.removeIf(Bullet::isOffScreen);
        checkBulletWallCollisions(bullets, walls1, walls);

        // --- Update Players ---
        if (!playerTank.isDestroyed())
            playerTank.update(delta, bullets, enemies, walls);
        if (!playerTwo.isDestroyed())
            playerTwo.update(delta, bullets, enemies, walls);

        // --- Update Enemies ---
        for (Iterator<EnemyTank> it = enemies.iterator(); it.hasNext();) {
            EnemyTank enemy = it.next();
            enemy.update(delta);
            enemy.handleBulletCollision(bullets, explosions);
            if (enemy.isDestroyed()) {
                enemy.dispose();
                it.remove();
            }
        }
        if (!pendingEnemies.isEmpty()) {
            enemies.addAll(pendingEnemies);
            pendingEnemies.clear();
        }

        // --- Update Explosions ---
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });

        updateWalls(delta);

        // --- Revive Circle Logic ---
        if (playerTank.isDestroyed() && !reviveCircleExistsFor(playerTank)) {
            reviveCircles.add(new ReviveCircle(playerTank.getPosition().x, playerTank.getPosition().y, REVIVE_TIME));
        }
        if (playerTwo.isDestroyed() && !reviveCircleExistsFor(playerTwo)) {
            reviveCircles.add(new ReviveCircle(playerTwo.getPosition().x, playerTwo.getPosition().y, REVIVE_TIME));
        }

        for (Iterator<ReviveCircle> it = reviveCircles.iterator(); it.hasNext();) {
            ReviveCircle rc = it.next();
            rc.update(delta);

            PlayerTank dead = getDeadPlayerAt(rc.getX(), rc.getY());
            if (dead == null) {
                it.remove();
                continue;
            }

            PlayerTank other = (dead == playerTank) ? playerTwo : playerTank;

            if (!other.isDestroyed() && rc.isPlayerInside(other)) {
                rc.incrementTimer(delta);
                if (rc.getTimer() >= REVIVE_TIME) {
                    revive(dead, rc.getX(), rc.getY());
                    it.remove();
                }
            } else {
                rc.resetTimer();
            }
        }

        // --- Game Over / Level Clear ---
        if (playerTank.isDestroyed() && playerTwo.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        if (enemies.isEmpty()) {
            game.setScreen(new VictoryScreen(game));
            return;
        }

        // --- DRAW SPRITES ---
        game.batch.begin();

        // Background
        if (backgroundTexture != null) {
            game.batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        }

        // Game elements
        renderGameElements();
        renderWalls();

        // Co-op Tanks & Revive Circles
        if (!playerTank.isDestroyed()) playerTank.draw(game.batch);
        if (!playerTwo.isDestroyed()) playerTwo.draw(game.batch);
        for (ReviveCircle rc : reviveCircles) rc.draw(game.batch);

        // Arrows above tanks
        Sprite p1Sprite = playerTank.getSprite();
        Sprite p2Sprite = playerTwo.getSprite();

        game.batch.draw(p1ArrowTexture,
            p1Sprite.getX() + p1Sprite.getWidth() / 2f - 10,
            p1Sprite.getY() + p1Sprite.getHeight() + 10,
            20, 20);

        game.batch.draw(p2ArrowTexture,
            p2Sprite.getX() + p2Sprite.getWidth() / 2f - 10,
            p2Sprite.getY() + p2Sprite.getHeight() + 10,
            20, 20);

        // Health UI
        drawHealthUI(playerTank.getCurrentHealth(), true);
        drawHealthUI(playerTwo.getCurrentHealth(), false);
        font.draw(game.batch, "Press B to return to Main Menu", 20, 40);
        game.batch.end();

        // --- DRAW SHAPE RENDERING ---
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Boss Health Bar
        BossTank boss = getBossTank();
        if (boss != null && !boss.isDestroyed()) {
            float bw = 400, bh = 25;
            float bx = (VIRTUAL_WIDTH - bw)/2, by = VIRTUAL_HEIGHT - 80;
            float hp = (float) boss.getBossHealth() / boss.getMaxBossHealth();

            game.shapeRenderer.setColor(Color.DARK_GRAY);
            game.shapeRenderer.rect(bx, by, bw, bh);
            game.shapeRenderer.setColor(Color.RED);
            game.shapeRenderer.rect(bx, by, bw * hp, bh);
        }

        // Revive progress bar
        for (ReviveCircle rc : reviveCircles) {
            PlayerTank dead = getDeadPlayerAt(rc.getX(), rc.getY());
            if (dead == null) continue;

            PlayerTank other = (dead == playerTank) ? playerTwo : playerTank;

            if (!other.isDestroyed() && rc.isPlayerInside(other)) {
                float progress = rc.getTimer() / REVIVE_TIME;
                float barWidth = 200;
                float barHeight = 20;
                float x = (VIRTUAL_WIDTH - barWidth) / 2f;
                float y = VIRTUAL_HEIGHT - 40;

                game.shapeRenderer.setColor(Color.DARK_GRAY);
                game.shapeRenderer.rect(x, y, barWidth, barHeight);

                game.shapeRenderer.setColor(Color.GREEN);
                game.shapeRenderer.rect(x, y, barWidth * progress, barHeight);
            }
        }

        game.shapeRenderer.end();
    }

    private BossTank getBossTank() {
        for (EnemyTank enemy : enemies) {
            if (enemy instanceof BossTank) {
                return (BossTank) enemy;
            }
        }
        return null;
    }

    private boolean reviveCircleExistsFor(PlayerTank player) {
        for (ReviveCircle rc : reviveCircles) {
            if (rc.isAtPosition(player.getPosition().x, player.getPosition().y)) {
                return true;
            }
        }
        return false;
    }

    private PlayerTank getDeadPlayerAt(float x, float y) {
        if (playerTank.isDestroyed() && closeEnough(playerTank.getPosition().x, x) && closeEnough(playerTank.getPosition().y, y)) {
            return playerTank;
        }
        if (playerTwo.isDestroyed() && closeEnough(playerTwo.getPosition().x, x) && closeEnough(playerTwo.getPosition().y, y)) {
            return playerTwo;
        }
        return null;
    }

    private boolean closeEnough(float a, float b) {
        return Math.abs(a - b) < 5f;
    }

    private void drawHealthUI(int currentHealth, boolean isPlayerOne) {
        float heartMarginX = 20;
        float heartMarginY = 20;
        float heartWidth = 30;
        float heartHeight = 30;

        for (int i = 0; i < currentHealth; i++) {
            float x;
            if (isPlayerOne) {
                x = heartMarginX + i * (heartWidth + 5);
            } else {
                x = VIRTUAL_WIDTH - heartMarginX - heartWidth - i * (heartWidth + 5);
            }

            float y = VIRTUAL_HEIGHT - heartHeight - heartMarginY;
            game.batch.draw(heartTexture, x, y, heartWidth, heartHeight);
        }
    }

    @Override
    public void spawnChaserTank() {
        pendingEnemies.add(new ChaserTank(300, 300, playerTank, playerTwo, bullets));
    }

    public void revive(PlayerTank player, float x, float y) {
        if (!player.isDestroyed()) return;
        player.reviveAt(x, y);
    }

    @Override
    public void resize(int width, int height) {
        coopViewport.update(width, height, true);
        coopCamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (p1ArrowTexture != null) p1ArrowTexture.dispose();
        if (p2ArrowTexture != null) p2ArrowTexture.dispose();

        for (ReviveCircle rc : reviveCircles) {
            rc.dispose();
        }
    }
}
