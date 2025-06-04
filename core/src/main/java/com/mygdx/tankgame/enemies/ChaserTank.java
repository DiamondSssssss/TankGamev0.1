package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall2;
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

        this.texture = new Texture(Gdx.files.internal("chaser_tank.png"));
        this.sprite = new Sprite(this.texture);
        this.sprite.setPosition(position.x, position.y);

        this.speed = 120f;
    }

    public ChaserTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        this(x, y, player, null, bullets);
    }

    public void update(float delta, List<Wall2> walls) {
        if (isDestroyed()) return;

        if (isExploding()) {
            setExplosionTimer(getExplosionTimer() - delta);
            setDestroyed(true);
            return;
        }

        chasePlayer(delta, walls);
        checkPlayerCollision();
        super.update(delta);
    }

    public boolean isExploding() {
        return isExploding;
    }

    private void chasePlayer(float delta, List<Wall2> walls) {
        PlayerTank target = chooseAliveTarget();
        if (target == null) return;

        Vector2 direction = target.getPosition().cpy().sub(position).nor();
        Vector2 newPosition = position.cpy().add(direction.scl(speed * delta));

        Rectangle futureRect = new Rectangle(newPosition.x, newPosition.y, sprite.getWidth(), sprite.getHeight());
        boolean collides = false;

        for (Wall2 wall : walls) {
            if (wall.getBoundingRectangle().overlaps(futureRect)) {
                collides = true;
                break;
            }
        }

        if (!collides) {
            position.set(newPosition);
            sprite.setPosition(position.x, position.y);
            sprite.setRotation(direction.angleDeg());
        }
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
        return null;
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

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
