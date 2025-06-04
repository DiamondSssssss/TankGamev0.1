package com.mygdx.tankgame.buildstuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Wall2 {
    private Texture texture;
    private Sprite sprite;
    private Vector2 position;
    private float width, height;

    /**
     * Constructs a permanent, impassable wall at (x, y) with the given size.
     *
     * @param x      x‐coordinate (bottom‐left)
     * @param y      y‐coordinate (bottom‐left)
     * @param width  wall width in world units
     * @param height wall height in world units
     */
    public Wall2(float x, float y, float width, float height) {
        this.texture = new Texture(Gdx.files.internal("wall2.jpg")); // Make sure this file exists in assets/
        this.sprite = new Sprite(texture);

        this.width = width;
        this.height = height;

        sprite.setSize(width, height);
        sprite.setPosition(x, y);

        this.position = new Vector2(x, y);
    }

    public void setRotation(float degrees) {
        sprite.setRotation(degrees);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Rectangle getBoundingRectangle() {
        return sprite.getBoundingRectangle();
    }

    public void dispose() {
        texture.dispose();
    }

    public void update(float delta) {
        // Future updates (animation, state, etc.)
    }
}
