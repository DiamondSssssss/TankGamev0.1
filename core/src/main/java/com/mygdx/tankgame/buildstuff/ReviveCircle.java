package com.mygdx.tankgame.buildstuff;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.tankgame.playertank.PlayerTank;

public class ReviveCircle {
    private float x, y;
    private float radius = 50f; // Example radius
    private float timer = 0f;   // How long the helper player has stayed inside
    private float reviveDuration; // Time needed to revive

    private Texture texture;

    public ReviveCircle(float x, float y, float reviveDuration) {
        this.x = x;
        this.y = y;
        this.reviveDuration = reviveDuration;
        texture = new Texture("revive_circle.png"); // Your revive circle texture
    }

    public void update(float delta) {
        // Could animate circle or do effects here
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x - radius, y - radius, radius * 2, radius * 2);
    }

    public boolean isPlayerInside(PlayerTank player) {
        // Use PlayerTank's position vector and sprite size
        float px = player.getPosition().x + player.getSprite().getWidth() / 2f;
        float py = player.getPosition().y + player.getSprite().getHeight() / 2f;
        float dx = px - x;
        float dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void incrementTimer(float delta) {
        timer += delta;
    }

    public void resetTimer() {
        timer = 0f;
    }

    public float getTimer() {
        return timer;
    }

    public boolean isAtPosition(float px, float py) {
        // Small threshold to match position
        return Math.abs(px - x) < 5f && Math.abs(py - y) < 5f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
