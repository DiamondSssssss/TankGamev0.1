package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.SniperTank;
import java.util.List;

public class CoopSniperTank extends SniperTank {
    // Use this vector for determining aim based on keyboard input.
    private Vector2 lastMovement = new Vector2(1, 0); // default facing right

    public CoopSniperTank(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        // Process movement input using WASD.
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
        // Set rotation to match last movement direction.
        float newAngle = lastMovement.angleDeg();
        setRotation(newAngle);
        getSprite().setRotation(newAngle);

        // Process shooting (e.g., key J) and ability (e.g., key K) input.
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            shoot(bullets);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && wallCooldownTimer <= 0) {
            createWall();
            wallCooldownTimer = wallCooldownDuration;
        }

        // Perform collision and boundary checks.
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
