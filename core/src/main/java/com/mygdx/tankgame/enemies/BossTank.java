package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.buildstuff.Explosion;
import com.mygdx.tankgame.levels.LevelScreen;
import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.TankGame;

import java.util.List;

public class BossTank extends EnemyTank {
    public int bossHealth = 100;
    public int maxBossHealth = bossHealth;
    private TankGame game; // Reference to the game instance

    // Timers for special moves
    private float shootTimer = 3f;   // Every 3 seconds, shoots in 6 directions.
    private float spawnTimer = 5f;   // Every 5 seconds, spawn 3 ChaserTanks.
    private float dashCooldownTimer = 10f;   // Cooldown timer before next dash.

    // Warning duration before each moveset (in seconds)
    private final float warningDuration = 1f;
    // Flags to ensure warning is only issued once per moveset
    private boolean shootWarning = false;
    private boolean spawnWarning = false;
    private boolean dashWarning = false;

    // --- Advanced Movement Variables (Elliptical Pattern) ---
    private Vector2 basePosition;       // Center of the movement pattern (set to initial position)
    private float movementTime = 0f;      // Time accumulator for the parametric path
    private float radiusX = 150f;         // Horizontal amplitude
    private float radiusY = 100f;         // Vertical amplitude
    private float angularSpeed = 0.5f;    // Speed at which the boss moves along the pattern

    // --- Dash Variables using your snippet ---
    private boolean isDashing = false;
    private float dashDuration = 0.3f;   // How long the dash lasts
    private float dashTimeLeft = 0f;
    private float dashSpeed = 300f;      // Speed multiplier during dash

    public BossTank(float x, float y, PlayerTank player, TankGame game, List<Bullet> bullets) {
        super(x, y, player, bullets);
        this.game = game;
        // Override the boss's texture.
        this.texture = new Texture(Gdx.files.internal("boss.png"));
        this.sprite.setTexture(this.texture);
        // Set the base position to the initial position.
        this.basePosition = new Vector2(x, y);
    }

    @Override
    public void update(float delta) {
        if (isDestroyed()) return;

        // --- Update Special Move Timers ---
        shootTimer -= delta;
        spawnTimer -= delta;
        dashCooldownTimer -= delta;

        if (shootTimer <= warningDuration && !shootWarning) {
            shootWarning = true;
        }
        if (dashCooldownTimer <= warningDuration && !dashWarning) {
            dashWarning = true;
        }
        if (shootTimer <= 0) {
            shootSixDirections();
            shootTimer = 3f; // Reset shooting timer.
            shootWarning = false;
        }
        if (dashCooldownTimer <= 0) {
            triggerDash();
            dashCooldownTimer = 10f; // Reset dash cooldown.
            dashWarning = false;
        }

        movementTime += delta;
        float targetX = basePosition.x + radiusX * MathUtils.cos(angularSpeed * movementTime);
        float targetY = basePosition.y + radiusY * MathUtils.sin(angularSpeed * movementTime);

        // Dash movement vector
        Vector2 dashMovement = new Vector2(0, 0);
        if (isDashing) {
            float moveX = player.getPosition().x - position.x;
            float moveY = player.getPosition().y - position.y;
            if (moveX != 0 || moveY != 0) {
                dashMovement = new Vector2(moveX, moveY).nor().scl(dashSpeed * delta);
            }
            dashTimeLeft -= delta;
            if (dashTimeLeft <= 0) {
                isDashing = false;
            }
        }
        // New tentative position after movement and dash
        float newX = targetX + dashMovement.x;
        float newY = targetY + dashMovement.y;
        // Get walls from current level screen
        List<Wall2> walls = null;
        if (game.getScreen() instanceof LevelScreen currentLevelScreen) {
            walls = currentLevelScreen.getWalls();  // You need to implement this getter in LevelScreen
        }

        // Check collision before moving
        if (walls == null || !collidesWithWalls(newX, position.y, walls)) {
            position.x = newX;
        }
        if (walls == null || !collidesWithWalls(position.x, newY, walls)) {
            position.y = newY;
        }
        // --- Clamp position within screen boundaries ---
        float clampedX = MathUtils.clamp(position.x, 0, Gdx.graphics.getWidth() - sprite.getWidth());
        float clampedY = MathUtils.clamp(position.y, 0, Gdx.graphics.getHeight() - sprite.getHeight());
        position.set(clampedX, clampedY);
        sprite.setPosition(position.x, position.y);
    }

    // Trigger dash by enabling the dash flag and resetting the dash timer.
    private void triggerDash() {
        System.out.println("Boss dashes!");
        isDashing = true;
        dashTimeLeft = dashDuration;
    }
    private boolean collidesWithWalls(float x, float y, List<Wall2> walls) {
        Rectangle futureRect = new Rectangle(x, y, sprite.getWidth(), sprite.getHeight());
        for (Wall2 wall : walls) {
            if (futureRect.overlaps(wall.getBoundingRectangle())) {
                return true;
            }
        }
        return false;
    }


    private void shootSixDirections() {
        float angleStep = 360f / 6f;
        for (int i = 0; i < 6; i++) {
            float angle = i * angleStep;
            float rad = angle * MathUtils.degreesToRadians;
            float bulletX = position.x + sprite.getWidth() / 2;
            float bulletY = position.y + sprite.getHeight() / 2;
            bullets.add(new Bullet(bulletX, bulletY, rad, true));
            System.out.println("Boss shoots bullet at angle: " + angle);
        }
    }

    private void spawnChaserTanks() {
        if (game.getScreen() instanceof LevelScreen currentLevelScreen) {
            currentLevelScreen.spawnChaserTank();
        } else {
            System.out.println("Error: Current screen is not a LevelScreen!");
        }
    }

    @Override
    public void handleBulletCollision(List<Bullet> playerBullets, List<Explosion> explosions) {
        if (isDestroyed()) return;
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet bullet = playerBullets.get(i);
            if (!bullet.isEnemyBullet() && bullet.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                bossHealth-= bullet.getDamage();
                playerBullets.remove(i);
                i--;
                if (bossHealth <= 0) {
                    explosions.add(new Explosion(getPosition().x, getPosition().y));
                    setExploding(true);
                }
                break;
            }
        }
    }

    @Override
    public boolean isDestroyed() {
        return bossHealth <= 0 || super.isDestroyed();
    }

    public void takeDamage(int amount) {
        bossHealth -= amount;
        if (bossHealth <= 0) {
            bossHealth = 0;
            setDestroyed(true);
            System.out.println("Boss destroyed!");
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isDestroyed()) {
            sprite.draw(batch);
        }
    }

    private void setExploding(boolean exploding) {
        this.isExploding = exploding;
        if (exploding) {
            setExplosionTimer(1.5f);
        }
    }

    // Helper method to get boss position.
    public Vector2 getPosition() {
        return new Vector2(getBoundingRectangle().x, getBoundingRectangle().y);
    }
    public int getBossHealth() {
        return bossHealth;
    }

    public int getMaxBossHealth() {
        return maxBossHealth;
    }

}
