package com.mygdx.tankgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TankGame extends Game {
    public SpriteBatch batch; // Fix: Make batch accessible

    @Override
    public void create() {
        batch = new SpriteBatch(); // Initialize batch
        this.setScreen(new MainMenuScreen(this)); // Fix: Use MainMenuScreen first
    }

    @Override
    public void dispose() {
        batch.dispose();
        super.dispose();
    }
}
