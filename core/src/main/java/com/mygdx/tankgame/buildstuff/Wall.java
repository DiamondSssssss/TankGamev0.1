package com.mygdx.tankgame.buildstuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Wall {
    private Texture texture;
    private Sprite sprite;
    private Vector2 position;
    private float width, height;

    // Lifetime variables (in seconds)
    private float lifetime;      // Total time the wall should exist (-1 for infinite)
    private float timeElapsed;   // Time elapsed since creation

    // Constructor without lifetime (infinite lifetime)
    public Wall(float x, float y, float width, float height) {
        this(x, y, width, height, -1f);
    }

    // Constructor with lifetime
    public Wall(float x, float y, float width, float height, float lifetime) {
        this.texture = new Texture(Gdx.files.internal("wall.jpg")); // Ensure wall.png exists.
        this.sprite = new Sprite(texture);
        this.width = width;
        this.height = height;
        sprite.setSize(width, height);
        this.position = new Vector2(x, y);
        sprite.setPosition(x, y);
        this.lifetime = lifetime;
        this.timeElapsed = 0f;
    }

    /**
     * Sets the rotation of the wall sprite.
     * @param rotation Rotation in degrees.
     */
    public void setRotation(float rotation) {
        sprite.setRotation(rotation);
    }

    /**
     * Draws the wall sprite.
     */
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    /**
     * Updates the wall's lifetime.
     * Call this method each frame with the elapsed time (delta).
     */
    public void update(float delta) {
        if (lifetime > 0) {
            timeElapsed += delta;
        }
    }

    /**
     * Returns whether the wall's lifetime has expired.
     */
    public boolean isExpired() {
        return lifetime > 0 && timeElapsed >= lifetime;
    }

    /**
     * Returns the wall's bounding rectangle.
     * Uses the sprite's bounding rectangle to account for rotation.
     */
    public Rectangle getBoundingRectangle() {
        return sprite.getBoundingRectangle();
    }

    /**
     * Disposes of the wall's texture.
     */
    public void dispose() {
        texture.dispose();
    }
}
