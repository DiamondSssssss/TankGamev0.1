package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.ShotgunTank;
import java.util.List;

public class CoopShotgunTank extends ShotgunTank {
    // Store the last movement direction to set rotation
    private Vector2 lastMovement = new Vector2(1, 0); // Default facing right

    public CoopShotgunTank(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        // Handle movement using WASD
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1;

        Vector2 movement = new Vector2(moveX, moveY);
        if (movement.len() > 0) {
            movement.nor();
            lastMovement.set(movement);
            getPosition().add(movement.scl(getSpeed() * deltaTime));
        }

        // Set rotation based on movement direction
        float newAngle = lastMovement.angleDeg();
        setRotation(newAngle);
        getSprite().setRotation(newAngle);

        // Handle shooting (e.g., J for shoot, K for ability)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            shoot(bullets);
        }
        if (shieldCooldownTimer > 0) {
            shieldCooldownTimer -= deltaTime;
        }
        if (Gdx.input.isButtonJustPressed(Input.Keys.K) && !shieldActive && shieldCooldownTimer <= 0) {
            activateShield();
            shieldCooldownTimer = shieldCooldownDuration;
        }
        if (shieldActive) {
            shieldTimer -= deltaTime;
            if (shieldTimer <= 0) {
                shieldActive = false;
            }
        }

        // Check collisions and boundaries
        checkBulletCollision(bullets, getExplosions());
        checkTankCollisions(enemyTanks);
        Vector2 pos = getPosition();
        float spriteW = getSprite().getWidth();
        float spriteH = getSprite().getHeight();
        pos.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - spriteW, pos.x));
        pos.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - spriteH, pos.y));
        getSprite().setPosition(pos.x, pos.y);
    }
}
