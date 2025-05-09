package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.SniperPlayerTank;
import java.util.List;

public class CoopSniperPlayerTankTwo extends SniperPlayerTank {
    // Use this vector for determining aim based on keyboard input.
    private Vector2 lastMovement = new Vector2(1, 0); // default facing right

    public CoopSniperPlayerTankTwo(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        // Use arrow keys for movement
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
        // Set rotation to match last movement direction.
        float newAngle = lastMovement.angleDeg();
        setRotation(newAngle);
        getSprite().setRotation(newAngle);

        // Use NUMPAD keys for shooting/abilities (adjust as needed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) { // Example: NUMPAD_0 to shoot
            chargeTime = maxChargeTime; // full charge
            shootCharged(bullets);
            shootCooldownTimer = shootCooldownDuration; // reset cooldown
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2) && wallCooldownTimer <= 0) { // Example: NUMPAD_1 for ability
            createWall();
            wallCooldownTimer = wallCooldownDuration;
        }

        // Perform collision and boundary checks.
        checkBulletCollision(bullets, getExplosions());
        checkTankCollisions(enemyTanks);
        updateCommon(deltaTime);
        Vector2 pos = getPosition();
        float spriteW = getSprite().getWidth();
        float spriteH = getSprite().getHeight();
        pos.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - spriteW, pos.x));
        pos.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - spriteH, pos.y));
        getSprite().setPosition(pos.x, pos.y);
        shootCooldownTimer -= deltaTime;
        wallCooldownTimer -= deltaTime;
    }
}
