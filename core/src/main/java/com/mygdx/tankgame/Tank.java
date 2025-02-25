package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.enemies.EnemyTank;

import java.util.ArrayList;
import java.util.List;

public class Tank {
    private Texture tankTexture;
    private Sprite sprite;
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

    private List<Explosion> explosions;

    public Tank(float x, float y) {
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

    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        if (isDestroyed) return;

        // Handle dash cooldown
        if (dashCooldownRemaining > 0) dashCooldownRemaining -= deltaTime;

        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1;

        // Handle dashing
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            attemptDash();
        }

        float currentSpeed = (dashTimeRemaining > 0) ? dashSpeed : speed;
        if (moveX != 0 || moveY != 0) {
            Vector2 direction = new Vector2(moveX, moveY).nor();
            position.add(direction.scl(currentSpeed * deltaTime));
        }

        // Dash duration logic
        if (dashTimeRemaining > 0) {
            dashTimeRemaining -= deltaTime;
            if (dashTimeRemaining <= 0) {
                dashTimeRemaining = 0;
            }
        }

        position.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - sprite.getWidth(), position.x));
        position.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - sprite.getHeight(), position.y));
        sprite.setPosition(position.x, position.y);

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        rotation = (float) Math.toDegrees(Math.atan2(mouseY - (position.y + sprite.getHeight() / 2),
            mouseX - (position.x + sprite.getWidth() / 2)));

        sprite.setRotation(rotation);

        for (EnemyTank enemyTank : enemyTanks) {
            checkBulletCollision(enemyTank.getBullets());
            if (enemyTank.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                takeDamage(1);
            }
        }
    }

    public void checkBulletCollision(List<Bullet> enemyBullets) {
        for (Bullet bullet : enemyBullets) {
            if (bullet.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                takeDamage(1);
                enemyBullets.remove(bullet);
                break;
            }
        }
    }

    private void takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            triggerExplosion();
        }
    }

    private void triggerExplosion() {
        if (!isDestroyed) {
            explosions.add(new Explosion(position.x, position.y));
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
        if (!isDestroyed) {
            sprite.draw(batch);
        }
        for (Explosion explosion : explosions) {
            explosion.draw(batch);
        }
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
        return  currentHealth;
    }
}
