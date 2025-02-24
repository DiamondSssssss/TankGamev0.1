package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Explosion {
    private static Texture explosionTexture;
    private Sprite sprite;
    private float timer = 0;
    private float explosionDuration = 0.5f; // Explosion lasts 0.5 seconds
    private boolean finished = false;

    public Explosion(float x, float y) {
        if (explosionTexture == null) {
            explosionTexture = new Texture(Gdx.files.internal("explosion.png"));
        }
        sprite = new Sprite(explosionTexture);
        sprite.setSize(64, 64);
        sprite.setPosition(x, y);
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= explosionDuration) {
            finished = true;
        }
    }

    public void render(SpriteBatch batch) {
        if (!finished) {
            sprite.draw(batch);
        }
    }

    public boolean isFinished() {
        return finished;
    }
    public void draw(SpriteBatch batch) {
        sprite.draw(batch); // Render explosion sprite
    }
    public void dispose() {
        explosionTexture.dispose();
    }
}
