package com.mygdx.tankgame.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.bullets.Bullet;
import com.mygdx.tankgame.buildstuff.Explosion;
import com.mygdx.tankgame.playertank.PlayerTank;

import java.util.List;

public class EliteEnemyTank extends EnemyTank {
    private int hitPoints = 3;
    private boolean isExploding = false;

    private Sprite tankSprite; // Sprite for elite enemy tank

    public EliteEnemyTank(float x, float y, PlayerTank player, List<Bullet> bullets) {
        super(x, y, player, bullets);

        // Load the texture and create the sprite
        Texture tankTexture = new Texture("enemy_elite.png"); // Make sure this file exists in assets
        tankSprite = new Sprite(tankTexture);
        tankSprite.setPosition(x, y);
    }
    private void chasePlayer(float delta, List<Wall2> walls) {
        Vector2 playerPos = player.getPosition();
        Vector2 direction = playerPos.cpy().sub(getPosition()).nor();
        float speed = 80f; // Slightly slower than ChaserTank for balance
        Vector2 movement = direction.scl(speed * delta);

        Vector2 newPosition = getPosition().cpy().add(movement);
        Rectangle futureRect = new Rectangle(newPosition.x, newPosition.y, tankSprite.getWidth(), tankSprite.getHeight());

        boolean collides = false;
        for (Wall2 wall : walls) {
            if (wall.getBoundingRectangle().overlaps(futureRect)) {
                collides = true;
                break;
            }
        }

        if (!collides) {
            // Apply movement
            getBoundingRectangle().setPosition(newPosition);
            tankSprite.setPosition(newPosition.x, newPosition.y);
            tankSprite.setRotation(direction.angleDeg());
        }
    }

    public void update(float delta, List<Wall2> walls) {
        if (isDestroyed()) return;

        if (isExploding()) {
            setExplosionTimer(getExplosionTimer() - delta);
            setDestroyed(true);
            return;
        }

        chasePlayer(delta, walls); // Movement logic
        super.update(delta); // Any inherited behavior

        // Sync sprite with new position
        tankSprite.setPosition(getBoundingRectangle().x, getBoundingRectangle().y);
    }



    @Override
    public void handleBulletCollision(List<Bullet> playerBullets, List<Explosion> explosions) {
        if (isDestroyed()) return;

        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet bullet = playerBullets.get(i);
            if (bullet.getBoundingRectangle().overlaps(getBoundingRectangle())) {
                hitPoints--;
                playerBullets.remove(i);
                i--;

                if (hitPoints <= 0) {
                    explosions.add(new Explosion(getPosition().x, getPosition().y));
                    setExploding(true);
                }
                break;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isDestroyed()) {
            tankSprite.draw(batch);
        }
    }

    public Vector2 getPosition() {
        return new Vector2(getBoundingRectangle().x, getBoundingRectangle().y);
    }

    public boolean isExploding() {
        return isExploding;
    }

    private void setExploding(boolean exploding) {
        this.isExploding = exploding;
        if (exploding) {
            setExplosionTimer(1.5f);
        }
    }

    public void dispose() {
        tankSprite.getTexture().dispose();
    }
}
