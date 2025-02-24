package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    private Texture texture;
    private Sprite sprite;
    private float angle;
    private Vector2 position;
    private Vector2 velocity;
    private float speed = 300f;
    private boolean isEnemyBullet;

    // Constructor to create a bullet, either for the player or the enemy
    public Bullet(float x, float y, float angle, boolean isEnemyBullet) {
        this.isEnemyBullet = isEnemyBullet;
        this.texture = new Texture(Gdx.files.internal(isEnemyBullet ? "enemy_bullet.png" : "bullet.jpg"));
        this.sprite = new Sprite(texture);
        this.position = new Vector2(x, y);
        this.angle=angle;
        // Set velocity based on angle, and scale it with speed
        if (isEnemyBullet) {
            // Enemy Bullet: Use MathUtils for radians as it works with degrees directly
            this.velocity = new Vector2(MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed);
        } else {
            // Player Bullet: Use Math to convert degrees to radians explicitly
            this.velocity = new Vector2((float) Math.cos(Math.toRadians(angle)),
                (float) Math.sin(Math.toRadians(angle))).scl(speed);
        }
        sprite.setPosition(x, y);
        sprite.setSize(10, 10);
    }

    // Update method to move the bullet independently
    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);  // Move the bullet based on its velocity
        sprite.setPosition(position.x, position.y);

    }

    // Draw the bullet
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        sprite.draw(batch);
    }

    // Check if the bullet has moved off the screen
    public boolean isOffScreen() {
        return position.x < 0 || position.x > Gdx.graphics.getWidth() || position.y < 0 || position.y > Gdx.graphics.getHeight();
    }

    // Get the current position of the bullet
    public Vector2 getPosition() {
        return position;
    }

    // Check if the bullet is from the enemy
    public boolean isEnemyBullet() {
        return isEnemyBullet;
    }

    // Dispose of resources
    public void dispose() {
        texture.dispose();
    }

    // Get the bounding rectangle of the bullet for collision detection
    public Rectangle getBoundingRectangle() {
        return new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }
}
