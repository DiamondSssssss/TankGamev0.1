package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.ShotgunPlayerTank;
import java.util.List;

public class CoopShotgunPlayerTankTwo extends ShotgunPlayerTank {
    // Store the last movement direction to set rotation
    private Vector2 lastMovement = new Vector2(1, 0); // Default facing right

    public CoopShotgunPlayerTankTwo(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks, List<Wall2> walls) {
        // Use arrow keys for movement instead of WASD
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1;

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

        // Use NUMPAD keys for shooting/abilities (adjust as needed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) { // Example: NUMPAD_0 to shoot
            shoot(bullets);
        }
        if (shieldCooldownTimer > 0) {
            shieldCooldownTimer -= deltaTime;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2) && !shieldActive && shieldCooldownTimer <= 0) { // Example: NUMPAD_1 for ability
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
        updateCommon(deltaTime);
        Vector2 pos = getPosition();
        float spriteW = getSprite().getWidth();
        float spriteH = getSprite().getHeight();
        pos.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - spriteW, pos.x));
        pos.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - spriteH, pos.y));
        getSprite().setPosition(pos.x, pos.y);
    }
}
