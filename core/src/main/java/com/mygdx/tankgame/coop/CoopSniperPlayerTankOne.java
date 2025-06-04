package com.mygdx.tankgame.coop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.levels.LevelScreen;
import com.mygdx.tankgame.playertank.SniperPlayerTank;
import java.util.List;

public class CoopSniperPlayerTankOne extends SniperPlayerTank {
    private Vector2 lastMovement = new Vector2(1, 0); // Default facing right
    private float wallCooldownTimer = 0f;
    private final float wallCooldownDuration = 5f; // 5 seconds cooldown for wall

    public CoopSniperPlayerTankOne(float x, float y) {
        super(x, y);
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks,List<Wall2> walls) {
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && shootCooldownTimer <= 0) {
            chargeTime = maxChargeTime; // Full charge
            shootCharged(bullets);
            shootCooldownTimer = shootCooldownDuration; // Reset cooldown
        }

        // Create wall if key K is pressed and cooldown allows.
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && wallCooldownTimer <= 0) {
            System.out.println("K pressed, trying to create wall");
            createWall(); // Calls the wall creation method
            wallCooldownTimer = wallCooldownDuration; // Reset wall cooldown
        }

        // Perform collision and boundary checks.
        checkBulletCollision(bullets, getExplosions());
        checkTankCollisions(enemyTanks);

        updateCommon(deltaTime);

        // Ensure player stays within screen boundaries.
        Vector2 pos = getPosition();
        float spriteW = getSprite().getWidth();
        float spriteH = getSprite().getHeight();
        pos.x = Math.max(0, Math.min(Gdx.graphics.getWidth() - spriteW, pos.x));
        pos.y = Math.max(0, Math.min(Gdx.graphics.getHeight() - spriteH, pos.y));
        getSprite().setPosition(pos.x, pos.y);

        // Decrement the cooldown timers.
        shootCooldownTimer -= deltaTime;
        wallCooldownTimer -= deltaTime;
    }

    // Method to create a wall in front of the tank
    protected void createWall() {
        float offsetDistance = 60f; // Place the wall further in front of the tank.
        float radians = (float) Math.toRadians(getRotation());
        Vector2 center = getPosition().cpy();
        center.add(getSprite().getWidth() / 2f, getSprite().getHeight() / 2f);

        // Wall position based on tank's facing direction.
        float wallX = center.x + (float) Math.cos(radians) * offsetDistance;
        float wallY = center.y + (float) Math.sin(radians) * offsetDistance;

        float wallWidth = 150f; // Increased wall width.
        float wallHeight = 20f;

        // Create the wall with a lifetime of 3 seconds.
        Wall2 wall = new Wall2(wallX - wallWidth / 2, wallY - wallHeight / 2, wallWidth, wallHeight);

        // Rotate the wall 90 degrees relative to the tank's facing direction.
        wall.setRotation(getRotation() + 90);

        // Add wall to the level or game world.
        LevelScreen.addWall(wall);
    }
}
