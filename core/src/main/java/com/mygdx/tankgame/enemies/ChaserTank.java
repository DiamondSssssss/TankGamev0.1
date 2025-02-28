package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.List;

public class ChaserTank extends EnemyTank {
    private boolean isExploding = false;

    public ChaserTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        super(x, y, player,bullets); // Keep all logic from EnemyTank
        this.texture = new Texture(Gdx.files.internal("enemy_tank.png")); // Unique texture for ChaserTank
        this.speed = 120f; // Custom speed for chasing
    }

    @Override
    public void update(float delta) {
        if (isDestroyed()) return; // Stop updating if destroyed

        if (isExploding()) {
            setExplosionTimer(getExplosionTimer() - delta);
            setDestroyed(true);
            return; // Stop updating movement/shooting while exploding
        }
        chasePlayer(delta);
        checkPlayerCollision();
        super.update(delta);
    }
    public boolean isExploding() {
        return isExploding;
    }
    private void chasePlayer(float delta) {
        Vector2 direction = player.getPosition().cpy().sub(position).nor();
        position.add(direction.scl(speed * delta));

        sprite.setPosition(position.x, position.y);
        sprite.setRotation(direction.angleDeg());
    }
    public void checkPlayerCollision() {
        if (getBoundingRectangle().overlaps(player.getBoundingRectangle())) {
            System.out.println("ChaserTank hit the player! Exploding...");
            player.takeDamage(1);
            setDestroyed(true);
        }
    }
    @Override
    public void shoot() {
        // Do nothing: ChaserTank doesn't shoot.
    }

}
