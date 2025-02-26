package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.Explosion;
import com.mygdx.tankgame.playertank.Tank;

import java.util.List;

public class EliteEnemyTank extends EnemyTank {
    private int hitPoints = 3; // Takes 3 bullets before being destroyed

    public EliteEnemyTank(float x, float y, Tank player,List<Bullet> bullets) {
        super(x, y, player, bullets);
    }
    @Override
    public void update(float delta) {
        if (isDestroyed()) return; // Stop updating if destroyed

        if (isExploding()) {
            setExplosionTimer(getExplosionTimer() - delta);
            setDestroyed(true);
            return; // Stop updating movement/shooting while exploding
        }

        super.update(delta);
    }
    private boolean isExploding = false;

    public boolean isExploding() {
        return isExploding;
    }

    @Override
    public void handleBulletCollision(List<Bullet> playerBullets, List<Explosion> explosions) {
        if (isDestroyed()) return; // Already destroyed, ignore further collisions

        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet bullet = playerBullets.get(i);
            if (bullet.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                hitPoints--; // Reduce HP when hit
                playerBullets.remove(i);
                i--; // Adjust index after removal

                if (hitPoints <= 0) {
                    explosions.add(new Explosion(getPosition().x, getPosition().y));
                    setExploding(true); // Set explosion state
                }
                break; // Only process one hit at a time
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isDestroyed()) {
            super.render(batch);
        }
    }
    // ✅ Add missing getPosition() method
    public Vector2 getPosition() {
        return new Vector2(getBoundingRectangle().x, getBoundingRectangle().y);
    }

    private void setExploding(boolean exploding) {
        this.isExploding = exploding;
        if (exploding) {
            setExplosionTimer(1.5f); // Reset explosion timer
        }
    }

}
