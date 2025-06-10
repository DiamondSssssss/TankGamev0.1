package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.buildstuff.Explosion;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.Iterator;
import java.util.List;

public class EnemyTank {
    protected Texture texture;
    protected Sprite sprite;
    protected Vector2 position;
    private Vector2 targetPosition;
    protected float speed = 100f;
    private float fireCooldown = 2f;
    private float timeSinceLastShot = 0f;
    protected PlayerTank player;
    public List<Bullet> bullets;
    private boolean isDestroyed = false;
    public boolean isExploding = false;
    private float explosionTimer = 1.5f; // 1.5 seconds explosion animation

    public Rectangle getBoundingRectangle() {
        return new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    private enum State {
        MOVING, STATIONARY, ROTATING, SHOOTING
    }

    private State currentState;
    private float moveTimer = 0f;
    private float shootTimer = 0f;
    private static final float MOVE_DURATION = 2f;  // Move for 2 seconds
    private static final float ROTATE_DURATION = 1f; // Rotate for 1 second

    public EnemyTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        this.texture = new Texture(Gdx.files.internal("enemy_tank.png"));
        this.sprite = new Sprite(texture);
        sprite.setSize(64, 64);
        this.sprite.setOriginCenter();
        this.position = new Vector2(x, y);
        this.targetPosition = new Vector2(x, y);
        this.player = player;
        this.bullets = bullets;
        sprite.setPosition(x, y);
        sprite.setRotation(-180);
        this.currentState = State.MOVING;
    }

    public void update(float delta) {
        if (isDestroyed) return; // Don't update if destroyed

        if (isExploding) {
            explosionTimer -= delta;
            if (explosionTimer <= 0) {
                isDestroyed = true; // Now destroy the tank after explosion
            }
            return; // Stop updating other behaviors when exploding
        }

        // Normal behavior if not exploding
        switch (currentState) {
            case MOVING:
                moveTowardsTarget(delta);
                break;
            case STATIONARY:
                shootTimer += delta;
                if (shootTimer >= ROTATE_DURATION) {
                    currentState = State.ROTATING;
                }
                break;
            case ROTATING:
                rotateToPlayer(delta);
                break;
            case SHOOTING:
                shoot();
                shootTimer = 0f;
                currentState = State.MOVING;
                break;
        }

        // Update bullets
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(delta);
            if (bullet.isOffScreen()) {
                bulletIterator.remove(); // Remove bullets that go off-screen
            }
        }
    }

    private void moveTowardsTarget(float delta) {
        Vector2 direction = targetPosition.cpy().sub(position).nor();
        position.add(direction.scl(speed * delta));

        sprite.setPosition(position.x, position.y);

        moveTimer += delta;
        if (moveTimer >= MOVE_DURATION) {
            moveTimer = 0f;
            currentState = State.STATIONARY;
        }
    }

    private float rotationTime = 0;

    private void rotateToPlayer(float delta) {
        Vector2 directionToPlayer = player.getPosition().cpy().sub(position);
        float targetAngle = directionToPlayer.angleDeg();
        float currentAngle = sprite.getRotation();
        float angleDifference = targetAngle - currentAngle;

        if (angleDifference > 180) {
            angleDifference -= 360;
        } else if (angleDifference < -180) {
            angleDifference += 360;
        }

        float rotationSpeed = angleDifference / 2.0f;

        currentAngle += rotationSpeed * delta;
        sprite.setRotation(currentAngle);

        rotationTime += delta;
        if (rotationTime >= 2.0f) {
            currentState = State.SHOOTING;
            rotationTime = 0;
        }
    }

    protected void shoot() {
        float angleRadians = sprite.getRotation() * MathUtils.degreesToRadians;
        float bulletX = position.x + MathUtils.cos(angleRadians) * 20;
        float bulletY = position.y + MathUtils.sin(angleRadians) * 20;
        bullets.add(new Bullet(bulletX, bulletY, angleRadians, true));

        timeSinceLastShot = 0f;
    }

    public void render(SpriteBatch batch) {
        if (!isDestroyed) {
            sprite.draw(batch);
            for (Bullet bullet : bullets) {
                bullet.draw(batch);
            }
        }
    }

    public void handleBulletCollision(List<Bullet> playerBullets, List<Explosion> explosions) {
        Iterator<Bullet> iterator = playerBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if ( !bullet.isEnemyBullet() && bullet.getBoundingRectangle().overlaps(sprite.getBoundingRectangle())) {
                explosions.add(new Explosion(position.x, position.y));
                isExploding = true;
                isDestroyed=true;
                explosionTimer = 1.5f;
                iterator.remove();
                break;
            }
        }
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }

    public void dispose() {
        texture.dispose();
    }
    // ✅ Add explosion timer getter & setter
    public float getExplosionTimer() {
        return explosionTimer;
    }

    public void setExplosionTimer(float explosionTimer) {
        this.explosionTimer = explosionTimer;
    }
}
