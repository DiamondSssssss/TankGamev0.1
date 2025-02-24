package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class Tank {
    private Texture tankTexture;
    private Sprite sprite;
    private Vector2 position;
    private float rotation; // Tank's rotation angle
    private float speed = 200f; // Adjust speed as needed
    private long lastShotTime = 0;
    private float shotCooldown = 0.3f; // 300ms cooldown
    private boolean isDestroyed = false; // Flag to track if the tank is destroyed
    private List<Explosion> explosions;  // List of explosions triggered when the tank is destroyed

    public Tank(float x, float y) {
        tankTexture = new Texture("tank.png"); // Ensure correct path
        sprite = new Sprite(tankTexture);

        // Set fixed dimensions instead of scaling
        sprite.setSize(64, 64); // Example: Setting width and height to 64x64 pixels

        position = new Vector2(x, y);
        sprite.setPosition(position.x, position.y);
        sprite.setOriginCenter(); // Ensure rotation is centered
        explosions = new ArrayList<>();
    }

    public void update(float deltaTime, List<Bullet> bullets, EnemyTank enemyTank) {
        if (isDestroyed) {
            return; // Nếu xe tăng bị phá hủy, không thực hiện cập nhật
        }

        // Xử lý di chuyển
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1;

        if (moveX != 0 || moveY != 0) {
            Vector2 direction = new Vector2(moveX, moveY).nor();
            position.add(direction.scl(speed * deltaTime));
        }

        // Giới hạn xe tăng trong màn hình
        position.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - sprite.getWidth(), position.x));
        position.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - sprite.getHeight(), position.y));

        sprite.setPosition(position.x, position.y);

        // Lấy vị trí chuột và xoay xe tăng
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float angle = (float) Math.toDegrees(Math.atan2(mouseY - (position.y + sprite.getHeight() / 2),
            mouseX - (position.x + sprite.getWidth() / 2)));

        rotation = angle;
        sprite.setRotation(rotation);

        // Kiểm tra va chạm với đạn kẻ địch
        checkBulletCollision(enemyTank.getBullets(), explosions);

        // Kiểm tra va chạm với xe tăng địch
        if (enemyTank.getBoundingRectangle().overlaps(sprite.getBoundingRectangle())) {
            triggerExplosion();
        }
    }


    public void checkBulletCollision(List<Bullet> enemyBullets, List<Explosion> explosions) {
        for (Bullet bullet : enemyBullets) {
            if (bullet.getBoundingRectangle().overlaps(sprite.getBoundingRectangle())) {
                explosions.add(new Explosion(position.x, position.y));
                isDestroyed = true;
                enemyBullets.remove(bullet);
                break;
            }
        }
    }


    private void triggerExplosion() {
        if (!isDestroyed) { // Ensure explosion triggers only once
            explosions.add(new Explosion(position.x, position.y));
            isDestroyed = true;
            System.out.println("Player tank destroyed! Trigger explosion.");
        }
    }


    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (!isDestroyed) {
            sprite.draw(batch);
        }
        for (Explosion explosion : explosions) {
            explosion.draw(batch); // Draw each explosion
        }
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

    public float getRotation() {
        return rotation;
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
}
