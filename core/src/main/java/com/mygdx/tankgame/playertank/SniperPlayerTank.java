package com.mygdx.tankgame.playertank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.levels.LevelScreen; // Used to add the wall to the level.
import com.mygdx.tankgame.buildstuff.Wall; // A simple Wall class.
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import java.util.List;

public class SniperPlayerTank extends PlayerTank {
    // Time the shot has been charged so far.
    protected float chargeTime = 0f;
    // Maximum time needed for full charge.
    protected final float maxChargeTime = 3f;
    // Whether the tank is currently charging a shot.
    protected boolean isCharging = false;
    // Cache the normal movement speed.
    protected float normalSpeed;

    // --- Wall Ability Cooldown Fields ---
    protected float wallCooldownTimer = 0f;
    protected final float wallCooldownDuration = 10f;

    // --- Shooting Cooldown Fields ---
    protected float shootCooldownTimer = 0f;
    // Increase the cooldown duration so the tank shoots slower.
    protected final float shootCooldownDuration = 3f;  // 3 seconds between shots.

    // --- Texture for Charging Indicator ---
    protected Texture chargeIndicatorTexture;

    public SniperPlayerTank(float x, float y) {
        super(x, y);
        // Assume the base tank’s normal speed is 200.
        normalSpeed = 200f;
        // Create a 1x1 white texture for drawing the charge indicator.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        chargeIndicatorTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        // Update wall cooldown timer.
        if (wallCooldownTimer > 0) {
            wallCooldownTimer -= deltaTime;
        }

        // Update shooting cooldown timer.
        if (shootCooldownTimer > 0) {
            shootCooldownTimer -= deltaTime;
        }

        // --- Charging Logic for Left Click ---
        // Start charging when the left button is just pressed, regardless of cooldown.
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (shootCooldownTimer <= 0) {  // Only start charging if cooldown has expired.
                isCharging = true;
                chargeTime = 0f;
            }
        }

        // If currently charging, accumulate charge as long as the button is held.
        if (isCharging) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                chargeTime += deltaTime;
                if (chargeTime > maxChargeTime) {
                    chargeTime = maxChargeTime;
                }
            } else {
                // Button was released.
                // Fire the shot only if cooldown is ready.
                if (shootCooldownTimer <= 0) {
                    shootCharged(bullets);
                    shootCooldownTimer = shootCooldownDuration; // Reset cooldown.
                }
                isCharging = false;
            }
        }

        // --- Wall Creation on Right Click ---
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && wallCooldownTimer <= 0) {
            createWall();
            wallCooldownTimer = wallCooldownDuration;
        }

        // --- Movement Speed Modification ---
        if (isCharging) {
            setSpeed(normalSpeed * 0.25f);
        } else {
            setSpeed(normalSpeed);
        }

        super.update(deltaTime, bullets, enemyTanks);
    }


    @Override
    public void shoot(List<Bullet> bullets) {
        // Do nothing: SniperTank fires only via the charge mechanism in update().
    }

    protected void shootCharged(List<Bullet> bullets) {
        float damage = 1f + 9f * (chargeTime / maxChargeTime);
        float maxDeviation = 30f;
        float deviation = maxDeviation * (1 - (chargeTime / maxChargeTime));
        float randomDeviation = (float) ((Math.random() * 2 - 1) * deviation);
        float finalRotation = getRotation() + randomDeviation;
        float radians = (float) Math.toRadians(finalRotation);
        Vector2 center = getPosition().cpy();
        center.add(getSprite().getWidth() / 2f, getSprite().getHeight() / 2f);
        float offsetDistance = 20f;
        float bulletX = center.x + (float) Math.cos(radians) * offsetDistance;
        float bulletY = center.y + (float) Math.sin(radians) * offsetDistance;
        Bullet bullet = new Bullet(bulletX, bulletY, finalRotation, damage, false);
        bullets.add(bullet);
    }

    protected void createWall() {
        float offsetDistance = 60f; // Place the wall further in front of the tank.
        float radians = (float) Math.toRadians(getRotation());
        Vector2 center = getPosition().cpy();
        center.add(getSprite().getWidth() / 2f, getSprite().getHeight() / 2f);

        float wallX = center.x + (float) Math.cos(radians) * offsetDistance;
        float wallY = center.y + (float) Math.sin(radians) * offsetDistance;

        float wallWidth = 150f; // Increased wall width.
        float wallHeight = 20f;

        // Create the wall with a lifetime of 3 seconds.
        Wall wall = new Wall(wallX - wallWidth / 2, wallY - wallHeight / 2, wallWidth, wallHeight, 3f);

        // Rotate the wall 90 degrees relative to the tank's facing direction so it blocks that area.
        wall.setRotation(getRotation() + 90);

        LevelScreen.addWall(wall);
    }

    // --- Helper Methods Delegating to Tank ---
    public float getRotation() {
        return super.getRotation();
    }
    public Vector2 getPosition() {
        return super.getPosition();
    }
    public Sprite getSprite() {
        return super.getSprite();
    }
    public void setSpeed(float newSpeed) {
        super.setSpeed(newSpeed);
    }

    /**
     * Returns the cooldown percentage for the wall ability.
     * 0 means ready; 1 means full cooldown.
     */
    public float getAbilityCooldownPercentage() {
        return wallCooldownTimer <= 0f ? 0f : Math.min(wallCooldownTimer / wallCooldownDuration, 1f);
    }

    /**
     * Returns the current charge percentage for the shot.
     * 0 means not charging; 1 means fully charged.
     */
    public float getChargePercentage() {
        if (isCharging) {
            return chargeTime / maxChargeTime;
        }
        return 0f;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw the tank normally.
        super.draw(batch);
        // If charging, draw a charging indicator bar above the tank.
        if (isCharging) {
            // Get tank's sprite position and dimensions.
            Sprite tankSprite = getSprite();
            float x = tankSprite.getX();
            float y = tankSprite.getY() + tankSprite.getHeight() + 5; // Slightly above the tank.
            float width = tankSprite.getWidth();
            float height = 5f; // Height of the indicator bar.

            // Draw the background (red) bar.
            batch.setColor(Color.RED);
            batch.draw(chargeIndicatorTexture, x, y, width, height);

            // Draw the foreground (green) bar based on charge percentage.
            float chargePercentage = getChargePercentage();
            batch.setColor(Color.GREEN);
            batch.draw(chargeIndicatorTexture, x, y, width * chargePercentage, height);

            // Reset the batch color.
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        chargeIndicatorTexture.dispose();
    }
}
