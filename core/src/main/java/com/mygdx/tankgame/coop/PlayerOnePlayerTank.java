package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;
import java.util.List;

public class PlayerOnePlayerTank extends PlayerTank {
    // Stores the last nonzero movement direction for aiming.
    private Vector2 lastMovement = new Vector2(1, 0); // Default facing right

    public PlayerOnePlayerTank(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks,List<Wall2> walls) {
        // --- Process Movement Input using WASD ---
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
        // Force the tank's rotation to match the last movement direction.
        float angle = lastMovement.angleDeg();
        setRotation(angle);
        getSprite().setRotation(angle);

        // --- Process Shooting and Ability Input ---
        // Shooting key: J
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            shoot(bullets);
        }
        // Ability key: K
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            useAbility();
        }

        // --- Perform Collision and Boundary Checks ---
        // (We re-implement these parts from the base update())
        checkBulletCollision(bullets, getExplosions());
        checkTankCollisions(enemyTanks);
        updateCommon(deltaTime);
        // Keep tank within screen boundaries.
        Vector2 pos = getPosition();
        float spriteWidth = getSprite().getWidth();
        float spriteHeight = getSprite().getHeight();
        pos.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - spriteWidth, pos.x));
        pos.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - spriteHeight, pos.y));
        getSprite().setPosition(pos.x, pos.y);
    }

    @Override
    public void shoot(List<Bullet> bullets) {
        if (isDestroyed()) return;
        // Use lastMovement to determine shooting direction.
        float angle = lastMovement.angleDeg();
        float radians = (float) Math.toRadians(angle);
        float tankCenterX = getPosition().x + getSprite().getWidth() / 2;
        float tankCenterY = getPosition().y + getSprite().getHeight() / 2;
        float offsetDistance = 20f;
        float bulletX = tankCenterX + (float) Math.cos(radians) * offsetDistance;
        float bulletY = tankCenterY + (float) Math.sin(radians) * offsetDistance;
        Bullet bullet = new Bullet(bulletX, bulletY, angle, false);
        bullets.add(bullet);
    }

    public void useAbility() {
        System.out.println("Player One ability used!");
    }
}
