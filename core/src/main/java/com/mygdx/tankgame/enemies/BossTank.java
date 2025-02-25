package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.Bullet;
import com.mygdx.tankgame.Explosion;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.Tank;
import com.mygdx.tankgame.TankGame;

import java.util.List;

public class BossTank extends EnemyTank {
    private int bossHealth = 100;
    private TankGame game; // Reference to the game instance

    // Timers for special moves
    private float shootTimer = 3f;   // Every 3 seconds, shoots in 6 directions.
    private float spawnTimer = 5f;   // Every 5 seconds, spawn 3 ChaserTanks.
    private float dashTimer = 10f;   // Every 10 seconds, dash at the player.

    // Warning duration before each moveset (in seconds)
    private final float warningDuration = 1f;
    // Flags to ensure warning is only issued once per moveset
    private boolean shootWarning = false;
    private boolean spawnWarning = false;
    private boolean dashWarning = false;

    public BossTank(float x, float y, Tank player, TankGame game) {
        super(x, y, player);
        this.game=game;
        // Override the boss's texture and adjust its speed as needed.
        this.texture = new Texture(Gdx.files.internal("boss_tank.jpg"));
        this.sprite.setTexture(this.texture);
        this.speed = 80f;  // Boss moves slower.
    }

    @Override
    public void update(float delta) {
        if (isDestroyed()) return;

        // --- Handle Shooting in 6 Directions ---
        shootTimer -= delta;
        if (shootTimer <= warningDuration && !shootWarning) {
            System.out.println("Boss Warning: Shooting in 1 second!");
            shootWarning = true;
        }
        if (shootTimer <= 0) {
            shootSixDirections();
            shootTimer = 3f; // Reset timer for shooting move.
            shootWarning = false;
        }

        // --- Handle Spawning Chaser Tanks ---
        spawnTimer -= delta;
        if (spawnTimer <= warningDuration && !spawnWarning) {
            System.out.println("Boss Warning: Spawning Chaser Tanks in 1 second!");
            spawnWarning = true;
        }
        if (spawnTimer <= 0) {
            spawnChaserTanks();
            spawnTimer = 5f; // Reset timer for spawn move.
            spawnWarning = false;
        }

        // --- Handle Dashing at the Player ---
        dashTimer -= delta;
        if (dashTimer <= warningDuration && !dashWarning) {
            System.out.println("Boss Warning: Dashing at player in 1 second!");
            dashWarning = true;
        }
        if (dashTimer <= 0) {
            dashAtPlayer();
            dashTimer = 10f; // Reset timer for dash move.
            dashWarning = false;
        }
    }

    // Shoots bullets in 6 directions evenly spaced (60° apart)
    private void shootSixDirections() {
        float angleStep = 360f / 6f;
        for (int i = 0; i < 6; i++) {
            float angle = i * angleStep;
            float rad = angle * MathUtils.degreesToRadians;
            float bulletX = position.x + sprite.getWidth() / 2;
            float bulletY = position.y + sprite.getHeight() / 2;
            // Create a bullet. Assuming 'true' marks it as an enemy bullet.
            Bullet bullet = new Bullet(bulletX, bulletY, angle, true);
            // You need to add the bullet to your game's bullet list.
            System.out.println("Boss shoots bullet at angle: " + angle);
        }
    }

    // Boss dashes toward the player.
    private void dashAtPlayer() {
        System.out.println("Boss dashes at the player!");
        Vector2 direction = player.getPosition().cpy().sub(position).nor();
        float dashDistance = 150f;
        position.add(direction.scl(dashDistance));
        sprite.setPosition(position.x, position.y);
    }
    @Override
    public void handleBulletCollision(List<Bullet> playerBullets, List<Explosion> explosions) {
        if (isDestroyed()) return; // Already destroyed, ignore further collisions

        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet bullet = playerBullets.get(i);
            if (bullet.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                bossHealth--; // Reduce HP when hit
                playerBullets.remove(i);
                i--; // Adjust index after removal

                if (bossHealth <= 0) {
                    explosions.add(new Explosion(getPosition().x, getPosition().y));
                    setExploding(true); // Set explosion state
                }
                break; // Only process one hit at a time
            }
        }
    }
    private void spawnChaserTanks() {
        System.out.println("Boss spawns 3 Chaser Tanks!");
        // Get the current screen from the game and cast it to LevelScreen
        if (game.getScreen() instanceof LevelScreen) {
            LevelScreen currentLevelScreen = (LevelScreen) game.getScreen();
            currentLevelScreen.spawnChaserTank();
        } else {
            System.out.println("Error: Current screen is not a LevelScreen!");
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
            setExplosionTimer(1.5f); // Reset explosion timer
        }
    }
    // Helper method to get boss position.
    public Vector2 getPosition() {
        return new Vector2(getBoundingRectangle().x, getBoundingRectangle().y);
    }
}
