package com.mygdx.tankgame.playertank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.levels.LevelScreen;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.buildstuff.Explosion;
import com.mygdx.tankgame.enemies.EnemyTank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerTank{
    private Texture tankTexture;
    protected Sprite sprite;
    private Vector2 position;
    private float rotation;
    private float speed = 200f; // Normal speed
    private float dashSpeed = 600f; // Speed when dashing
    private float dashDuration = 0.2f; // Dash lasts for 0.2 seconds
    private float dashCooldown = 1.0f; // Cooldown between dashes
    private float dashTimeRemaining = 0f; // Time left in dash
    private float dashCooldownRemaining = 0f; // Cooldown tracker
    private int maxDashCharges;
    private int dashCharges; // Current dash charges
    private int maxHealth = 3;
    private int currentHealth = maxHealth;
    private boolean isDestroyed = false;
    private com.badlogic.gdx.graphics.Camera camera;
    public void setCamera(com.badlogic.gdx.graphics.Camera camera) {
        this.camera = camera;
    }
    private List<Explosion> explosions;

    // --- New fields for invincibility and blinking ---
    private boolean isInvincible = false;
    private float invincibilityTime = 3f;     // Total invincibility duration in seconds
    private float invincibilityTimer = 0f;      // Countdown timer
    private float blinkTimer = 0f;              // Timer to control blink toggling
    private float blinkInterval = 0.2f;         // How frequently to toggle visibility
    private boolean drawSprite = true;          // Whether the sprite should be drawn this frame

    public PlayerTank(float x, float y) {
        tankTexture = new Texture("tank.png");
        sprite = new Sprite(tankTexture);
        sprite.setSize(64, 64);
        position = new Vector2(x, y);
        sprite.setPosition(position.x, position.y);
        sprite.setOriginCenter();
        explosions = new ArrayList<>();

        maxDashCharges = 3;
        dashCharges = maxDashCharges;
    }

    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks, List<Wall2> walls) {
        if (isDestroyed) return;

        // === 1. Compute mouse position in world coordinates ===
        Vector2 mouseWorld = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        if (camera != null) {
            // camera.update() should already have been called in LevelScreen.render()
            Vector3 unprojected = camera.unproject(new Vector3(mouseWorld.x, mouseWorld.y, 0));
            mouseWorld.set(unprojected.x, unprojected.y);
        }

        // === 2. Compute the tank's center in world coordinates ===
        // Make sure sprite.setOriginCenter() was invoked once in your constructor!
        float centerX = position.x + sprite.getOriginX();
        float centerY = position.y + sprite.getOriginY();

        // === 3. Calculate rotation so the tank faces the mouse ===
        rotation = (float) Math.toDegrees(
            Math.atan2(mouseWorld.y - centerY, mouseWorld.x - centerX)
        );
        sprite.setRotation(rotation);

        // === 4. Invincibility & blinking logic ===
        if (isInvincible) {
            invincibilityTimer -= deltaTime;
            blinkTimer += deltaTime;
            if (blinkTimer >= blinkInterval) {
                blinkTimer = 0f;
                drawSprite = !drawSprite;
            }
            if (invincibilityTimer <= 0f) {
                isInvincible = false;
                drawSprite = true; // ensure visible after invincibility ends
            }
        }

        // === 5. Dash cooldown logic ===
        if (dashCooldownRemaining > 0f) {
            dashCooldownRemaining -= deltaTime;
            if (dashCooldownRemaining < 0f) dashCooldownRemaining = 0f;
        }

        // === 6. Read movement input ===
        float moveX = 0f, moveY = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f;

        // === 7. Attempt dash if SPACE is pressed ===
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            attemptDash();
        }

        // === 8. Move tank based on dash or normal speed ===
        float currentSpeed = (dashTimeRemaining > 0f) ? dashSpeed : speed;
        if (moveX != 0f || moveY != 0f) {
            Vector2 direction = new Vector2(moveX, moveY).nor();
            Vector2 newPosition = new Vector2(position).add(direction.scl(currentSpeed * deltaTime));

            // Only move if no collision with walls
            if (!isCollidingWithWalls(newPosition, walls)) {
                position.set(newPosition);
            }
        }

        // === 9. Update dash duration ===
        if (dashTimeRemaining > 0f) {
            dashTimeRemaining -= deltaTime;
            if (dashTimeRemaining <= 0f) dashTimeRemaining = 0f;
        }

        // === 10. Clamp position to viewport’s logical (virtual) size ===
        // Use camera.viewportWidth / viewportHeight instead of Gdx.graphics.getWidth()
        float worldMaxX = camera.viewportWidth - sprite.getWidth();
        float worldMaxY = camera.viewportHeight - sprite.getHeight();
        position.x = Math.max(0f, Math.min(worldMaxX, position.x));
        position.y = Math.max(0f, Math.min(worldMaxY, position.y));

        sprite.setPosition(position.x, position.y);

        // === 11. Check collisions ===
        checkBulletCollision(bullets, explosions);
        checkTankCollisions(enemyTanks);
    }



    protected boolean isCollidingWithWalls(Vector2 newPosition, List<Wall2> walls) {
        Rectangle futureRect = new Rectangle(newPosition.x, newPosition.y, sprite.getWidth(), sprite.getHeight());

        for (Wall2 wall : walls) {
            if (wall.getBoundingRectangle().overlaps(futureRect)) {
                return true;
            }
        }
        return false;
    }

    public void checkBulletCollision(List<Bullet> enemyBullets, List<Explosion> explosions) {
        Iterator<Bullet> iterator = enemyBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            Rectangle bulletRect = bullet.getBoundingRectangle();
            Rectangle playerRect = getBoundingRectangle();
            if (bullet.isEnemyBullet() && bulletRect.overlaps(playerRect)) {
                takeDamage(1);
                iterator.remove(); // Safely remove the bullet during iteration
                break;
            }
        }
    }

    public void checkTankCollisions(List<EnemyTank> enemyTanks) {
        // Check for collisions with any enemy tanks
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                System.out.println("Tank collided with enemy!");
                takeDamage(1);
                break; // Optionally break after one collision is detected
            }
        }
    }

    public void takeDamage(int amount) {
        // If already invincible, ignore further damage.
        if (isInvincible) return;

        currentHealth -= amount;
        if (currentHealth <= 0) {
            triggerExplosion();
        } else {
            // Start invincibility and blinking effect after a collision.
            isInvincible = true;
            invincibilityTimer = invincibilityTime;
            blinkTimer = 0;
            drawSprite = true;
        }
    }

    private void triggerExplosion() {
        if (!isDestroyed) {
            LevelScreen.globalExplosions.add(new Explosion(position.x, position.y));
            isDestroyed = true;
            System.out.println("Tank destroyed!");
        }
    }

    private void attemptDash() {
        if (dashCharges > 0 && dashCooldownRemaining <= 0) {
            dashTimeRemaining = dashDuration;
            dashCharges--;
            dashCooldownRemaining = dashCooldown;
            System.out.println("Dashing! Charges left: " + dashCharges);
        }
    }

    public void applyUpgrade(int dashIncrease, int healthIncrease, float speedIncrease) {
        maxDashCharges += dashIncrease;
        dashCharges = maxDashCharges;
        maxHealth += healthIncrease;
        currentHealth = Math.min(currentHealth + healthIncrease, maxHealth);
        speed += speedIncrease; // Increase speed
        System.out.println("Upgrade applied! Dash: " + dashCharges + ", Health: " + currentHealth + "/" + maxHealth + ", Speed: " + speed);
    }

    public void shoot(List<Bullet> bullets) {
        if (isDestroyed) {
            return; // Prevent shooting if the tank is destroyed
        }
        float radians = (float) Math.toRadians(rotation);

        // Tank center position
        float tankCenterX = position.x + sprite.getWidth() / 2;
        float tankCenterY = position.y + sprite.getHeight() / 2;

        // Offset distance from the tank
        float offsetDistance = 20f; // Adjust this value for desired distance

        // Calculate bullet spawn position slightly ahead of the tank
        float bulletX = tankCenterX + (float) Math.cos(radians) * offsetDistance;
        float bulletY = tankCenterY + (float) Math.sin(radians) * offsetDistance;

        Bullet bullet = new Bullet(bulletX, bulletY, rotation, false);
        bullets.add(bullet);
    }

    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Only draw the tank if it's not destroyed.
        if (!isDestroyed) {
            // If invincible, draw based on the blink toggle.
            if (!isInvincible || (isInvincible && drawSprite)) {
                sprite.draw(batch);
            }
        }
        for (Explosion explosion : explosions) {
            explosion.draw(batch);
        }
    }
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
    public Vector2 getPosition() {
        return position;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void dispose() {
        tankTexture.dispose();
    }

    public Rectangle getBoundingRectangle() {
        return new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
    public float getRotation() {
        return rotation;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSpeed(float newSpeed) {
        speed = newSpeed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setRotation(float newRotation) {
        this.rotation = newRotation;
        sprite.setRotation(newRotation);
    }
    protected List<Explosion> getExplosions() {
        return explosions;
    }
    protected void updateCommon(float deltaTime) {
        // Update invincibility and blink timers if invincible
        if (isInvincible) {
            invincibilityTimer -= deltaTime;
            blinkTimer += deltaTime;
            if (blinkTimer >= blinkInterval) {
                blinkTimer = 0;
                drawSprite = !drawSprite;
            }
            if (invincibilityTimer <= 0) {
                isInvincible = false;
                drawSprite = true; // Ensure sprite is visible when invincibility ends
            }
        }

        // Handle dash cooldown
        if (dashCooldownRemaining > 0) dashCooldownRemaining -= deltaTime;

        // Update dash duration
        if (dashTimeRemaining > 0) {
            dashTimeRemaining -= deltaTime;
            if (dashTimeRemaining <= 0) {
                dashTimeRemaining = 0;
            }
        }

        // Boundary checking (if not handled by movement)
        position.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - sprite.getWidth(), position.x));
        position.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - sprite.getHeight(), position.y));
        sprite.setPosition(position.x, position.y);
    }

    public void reviveAt(float x, float y) {
        // Reset health
        currentHealth = maxHealth;
        // Reset destroyed flag
        isDestroyed = false;

        // Reset invincibility status so player is not invincible after revive,
        // or optionally set invincible for a brief time after revive
        isInvincible = true;
        invincibilityTimer = invincibilityTime;
        blinkTimer = 0;
        drawSprite = true;

        // Reset dash charges if you want (optional)
        dashCharges = maxDashCharges;

        // Set new position
        position.set(x, y);
        sprite.setPosition(x, y);

        System.out.println("Player revived at: " + x + ", " + y);
    }

    public void setWalls(List<Wall> sharedWalls) {
    }
    public void setPosition(float x, float y) {
        position.set(x, y);              // Update logical position
        sprite.setPosition(x, y);        // Update visual position
    }

}
