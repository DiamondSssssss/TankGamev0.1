package com.mygdx.tankgame.playertank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.enemies.EnemyTank;
import java.util.List;

public class ShotgunPlayerTank extends PlayerTank {
    // --- Shield fields ---
    protected boolean shieldActive = false;
    protected float shieldTimer = 0f;
    private final float shieldDuration = 3f;
    private Texture shieldTexture;
    private Sprite shieldSprite;
    private Texture shotgunTankTexture;

    // --- Shield cooldown fields ---
    protected float shieldCooldownTimer = 0f;
    protected final float shieldCooldownDuration = 10f;

    // --- Knockback configuration ---
    private final float knockbackForce = 50f;

    public ShotgunPlayerTank(float x, float y) {
        super(x, y);
        Texture shotgunTankTexture = new Texture(Gdx.files.internal("shotgun_tank.png"));
        Sprite shotgunTankSprite = new Sprite(shotgunTankTexture);
        shotgunTankSprite.setPosition(x, y);
        shotgunTankSprite.setOriginCenter();
        setSprite(shotgunTankSprite);
        shieldTexture = new Texture(Gdx.files.internal("shield.png"));
        shieldSprite = new Sprite(shieldTexture);
        Sprite tankSprite = getSprite();
        shieldSprite.setSize(tankSprite.getWidth() + 20, tankSprite.getHeight() + 20);
        shieldSprite.setOriginCenter();
    }

    @Override
    public void update(float deltaTime, List<Bullet> bullets, List<EnemyTank> enemyTanks) {
        if (shieldCooldownTimer > 0) {
            shieldCooldownTimer -= deltaTime;
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && !shieldActive && shieldCooldownTimer <= 0) {
            activateShield();
            shieldCooldownTimer = shieldCooldownDuration;
        }
        if (shieldActive) {
            shieldTimer -= deltaTime;
            if (shieldTimer <= 0) {
                shieldActive = false;
            }
        }
        super.update(deltaTime, bullets, enemyTanks);
    }

    @Override
    public void shoot(List<Bullet> bullets) {
        float baseAngle = getRotation();
        float[] offsets = {-10f, -5f, 0f, 5f, 10f};
        float tankCenterX = getPosition().x + getSprite().getWidth() / 2;
        float tankCenterY = getPosition().y + getSprite().getHeight() / 2;
        for (float offset : offsets) {
            float bulletAngle = baseAngle + offset;
            Bullet bullet = new Bullet(tankCenterX, tankCenterY, bulletAngle, false);
            bullets.add(bullet);
        }
        float radians = MathUtils.degreesToRadians * baseAngle;
        Vector2 knockback = new Vector2(MathUtils.cos(radians + MathUtils.PI), MathUtils.sin(radians + MathUtils.PI))
            .scl(knockbackForce);
        getPosition().add(knockback);
    }

    @Override
    public void takeDamage(int amount) {
        if (shieldActive) return;
        super.takeDamage(amount);
    }

    protected void activateShield() {
        shieldActive = true;
        shieldTimer = shieldDuration;
    }

    private void updateShieldSprite() {
        Sprite tankSprite = getSprite();
        shieldSprite.setPosition(tankSprite.getX() - 10, tankSprite.getY() - 10);
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        super.draw(batch);
        if (shieldActive) {
            updateShieldSprite();
            shieldSprite.draw(batch);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        shieldTexture.dispose();
        shotgunTankTexture.dispose();
    }

    /**
     * Returns the cooldown percentage for the shield ability.
     * 0 means ready; 1 means full cooldown.
     */
    public float getAbilityCooldownPercentage() {
        return shieldCooldownTimer <= 0f ? 0f : Math.min(shieldCooldownTimer / shieldCooldownDuration, 1f);
    }
}
