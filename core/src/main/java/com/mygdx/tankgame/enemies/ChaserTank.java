package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.List;

public class ChaserTank extends EnemyTank {
    private boolean isExploding = false;
    private PlayerTank playerOne;
    private PlayerTank playerTwo;

    public ChaserTank(float x, float y, PlayerTank playerOne, PlayerTank playerTwo, List<Bullet> bullets) {
        super(x, y, playerOne, bullets); // Default target still required
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.texture = new Texture(Gdx.files.internal("enemy_tank.png"));
        this.speed = 120f;
    }

    public ChaserTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        this(x, y, player, null, bullets);
    }

    @Override
    public void update(float delta) {
        if (isDestroyed()) return;

        if (isExploding()) {
            setExplosionTimer(getExplosionTimer() - delta);
            setDestroyed(true);
            return;
        }

        chasePlayer(delta);
        checkPlayerCollision();
        super.update(delta);
    }

    public boolean isExploding() {
        return isExploding;
    }

    private void chasePlayer(float delta) {
        PlayerTank target = chooseAliveTarget();

        if (target == null) return; // No valid targets

        Vector2 direction = target.getPosition().cpy().sub(position).nor();
        position.add(direction.scl(speed * delta));

        sprite.setPosition(position.x, position.y);
        sprite.setRotation(direction.angleDeg());
    }

    private PlayerTank chooseAliveTarget() {
        boolean p1Alive = playerOne != null && !playerOne.isDestroyed();
        boolean p2Alive = playerTwo != null && !playerTwo.isDestroyed();

        if (p1Alive && p2Alive) {
            float distOne = playerOne.getPosition().dst(position);
            float distTwo = playerTwo.getPosition().dst(position);
            return (distTwo < distOne) ? playerTwo : playerOne;
        } else if (p1Alive) {
            return playerOne;
        } else if (p2Alive) {
            return playerTwo;
        }
        return null; // both are dead
    }

    public void checkPlayerCollision() {
        if (playerOne != null && !playerOne.isDestroyed()
            && getBoundingRectangle().overlaps(playerOne.getBoundingRectangle())) {
            System.out.println("ChaserTank hit PlayerOne! Exploding...");
            playerOne.takeDamage(1);
            setDestroyed(true);
        } else if (playerTwo != null && !playerTwo.isDestroyed()
            && getBoundingRectangle().overlaps(playerTwo.getBoundingRectangle())) {
            System.out.println("ChaserTank hit PlayerTwo! Exploding...");
            playerTwo.takeDamage(1);
            setDestroyed(true);
        }
    }

    @Override
    public void shoot() {
        // ChaserTank does not shoot
    }
}
