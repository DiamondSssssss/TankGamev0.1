package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.tankgame.*;
import com.mygdx.tankgame.buildstuff.ReviveCircle;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.enemies.ChaserTank;
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

    private static final float REVIVE_TIME = 3f; // seconds to revive

    public CoopLevelScreen(TankGame game, PlayerTank playerOne, PlayerTank playerTwo) {
        super(game, playerOne);
        this.playerTwo = playerTwo;

        // Load arrow textures
        p1ArrowTexture = new Texture(Gdx.files.internal("p1.png"));
        p2ArrowTexture = new Texture(Gdx.files.internal("p2.png"));
    }

    @Override
    protected void setupLevel() {
        enemies.add(new BossTank(400, 400, playerTank, game, bullets)); // Example enemy
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Update ---

        // Update bullets
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        bullets.removeIf(Bullet::isOffScreen);

        checkBulletWallCollisions(bullets,walls1,walls);

        // Update players only if alive
        if (!playerTank.isDestroyed())
            playerTank.update(delta, bullets, enemies, walls);
        if (!playerTwo.isDestroyed())
            playerTwo.update(delta, bullets, enemies, walls);

        // Update enemies
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

        // Update explosions
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });

        updateWalls(delta);

        // --- Revive Logic ---

// Spawn revive circles when a player just died and no revive circle exists for them
        if (playerTank.isDestroyed() && !reviveCircleExistsFor(playerTank)) {
            reviveCircles.add(new ReviveCircle(playerTank.getPosition().x, playerTank.getPosition().y, REVIVE_TIME));
        }
        if (playerTwo.isDestroyed() && !reviveCircleExistsFor(playerTwo)) {
            reviveCircles.add(new ReviveCircle(playerTwo.getPosition().x, playerTwo.getPosition().y, REVIVE_TIME));
        }


        // Update each revive circle and check if the other player is standing inside
        Iterator<ReviveCircle> rcIter = reviveCircles.iterator();
        while (rcIter.hasNext()) {
            ReviveCircle rc = rcIter.next();

            // Update the revive circle timer and draw position
            rc.update(delta);

            // Determine which player is dead for this revive circle
            PlayerTank deadPlayer = getDeadPlayerAt(rc.getX(), rc.getY());

            if (deadPlayer == null) {
                // If no dead player at revive circle pos (maybe revived already), remove circle
                rcIter.remove();
                continue;
            }

            PlayerTank otherPlayer = (deadPlayer == playerTank) ? playerTwo : playerTank;

            if (!otherPlayer.isDestroyed() && rc.isPlayerInside(otherPlayer)) {
                rc.incrementTimer(delta);
                if (rc.getTimer() >= REVIVE_TIME) {
                    // Revive the dead player at revive circle position
                    revive(deadPlayer, rc.getX(), rc.getY());
                    rcIter.remove();
                }
            } else {
                rc.resetTimer();
            }

        }

        // --- Check game over / level clear ---
        if (playerTank.isDestroyed() && playerTwo.isDestroyed()) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        if (enemies.isEmpty()) {
            goToUpgradeScreen();
            return;
        }

        // --- Render ---
        game.batch.begin();

        renderGameElements();

        // Draw walls
        for (Wall2 wall : walls) {
            wall.draw(game.batch);
        }

        // Draw players if alive
        if (!playerTank.isDestroyed())
            playerTank.draw(game.batch);
        if (!playerTwo.isDestroyed())
            playerTwo.draw(game.batch);

        // Draw revive circles on top if any
        // Draw revive progress bars at the top of the screen (if reviving)
        // Begin sprite batch first for textures
        for (ReviveCircle rc : reviveCircles) {
            rc.draw(game.batch); // Draw the circle texture
        }

// Then draw revive bars with ShapeRenderer
        for (ReviveCircle rc : reviveCircles) {
            PlayerTank deadPlayer = getDeadPlayerAt(rc.getX(), rc.getY());
            if (deadPlayer == null) continue;

            PlayerTank otherPlayer = (deadPlayer == playerTank) ? playerTwo : playerTank;

            if (!otherPlayer.isDestroyed() && rc.isPlayerInside(otherPlayer)) {
                float progress = rc.getTimer() / REVIVE_TIME;
                float barWidth = 200;
                float barHeight = 20;
                float x = (Gdx.graphics.getWidth() - barWidth) / 2f;
                float y = Gdx.graphics.getHeight() - 40; // 40 px from top

                game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                game.shapeRenderer.setColor(Color.DARK_GRAY);
                game.shapeRenderer.rect(x, y, barWidth, barHeight);

                game.shapeRenderer.setColor(Color.GREEN);
                game.shapeRenderer.rect(x, y, barWidth * progress, barHeight);
                game.shapeRenderer.end();
            }
        }




        // --- Draw arrow indicators above tanks ---
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

        // --- Draw health UI ---
        int currentHealthOne = playerTank.getCurrentHealth();
        for (int i = 0; i < currentHealthOne; i++) {
            game.batch.draw(heartTexture,
                heartMarginX + i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }

        int currentHealthTwo = playerTwo.getCurrentHealth();
        for (int i = 0; i < currentHealthTwo; i++) {
            game.batch.draw(heartTexture,
                Gdx.graphics.getWidth() - heartMarginX - heartWidth - i * (heartWidth + 5),
                Gdx.graphics.getHeight() - heartHeight - heartMarginY,
                heartWidth,
                heartHeight);
        }

        game.batch.end();
    }

    private boolean reviveCircleExistsFor(PlayerTank player) {
        for (ReviveCircle rc : reviveCircles) {
            // Use player.getPosition().x and .y instead of getX()/getY()
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
        return Math.abs(a - b) < 5f; // small threshold for floating point comparison
    }

    @Override
    public void spawnChaserTank() {
        pendingEnemies.add(new ChaserTank(600, 600, playerTank, playerTwo, bullets));
    }

    public void revive(PlayerTank player, float x, float y) {
        if (!player.isDestroyed()) return;
        player.reviveAt(x, y);
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
