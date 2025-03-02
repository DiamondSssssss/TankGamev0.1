package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.List;

public class ChaserTank extends EnemyTank {
    private boolean isExploding = false;
    // Reference to the primary target and a secondary target.
    private PlayerTank playerOne;
    private PlayerTank playerTwo;

    public ChaserTank(float x, float y, PlayerTank playerOne, PlayerTank playerTwo, List<Bullet> bullets) {
        super(x, y, playerOne, bullets); // We pass playerOne for backward compatibility if needed.
        this.playerOne = playerOne;
        this.playerTwo = playerTwo; // Store the second player.
        this.texture = new Texture(Gdx.files.internal("enemy_tank.png")); // Unique texture for ChaserTank
        this.speed = 120f; // Custom speed for chasing
    }
    public ChaserTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        this(x, y, player, null, bullets);
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
        // Determine the target: if both players are alive, choose the closer one.
        PlayerTank target = playerOne;
        if (playerTwo != null && !playerTwo.isDestroyed()) {
            float distOne = playerOne.getPosition().dst(position);
            float distTwo = playerTwo.getPosition().dst(position);
            if (distTwo < distOne) {
                target = playerTwo;
            }
        }

        // Now chase the chosen target.
        Vector2 direction = target.getPosition().cpy().sub(position).nor();
        position.add(direction.scl(speed * delta));

        sprite.setPosition(position.x, position.y);
        sprite.setRotation(direction.angleDeg());
    }

    public void checkPlayerCollision() {
        // Check collision against both players.
        if (getBoundingRectangle().overlaps(playerOne.getBoundingRectangle())) {
            System.out.println("ChaserTank hit PlayerOne! Exploding...");
            playerOne.takeDamage(1);
            setDestroyed(true);
        } else if (playerTwo != null && getBoundingRectangle().overlaps(playerTwo.getBoundingRectangle())) {
            System.out.println("ChaserTank hit PlayerTwo! Exploding...");
            playerTwo.takeDamage(1);
            setDestroyed(true);
        }
    }

    @Override
    public void shoot() {
        // Do nothing: ChaserTank doesn't shoot.
    }
}
