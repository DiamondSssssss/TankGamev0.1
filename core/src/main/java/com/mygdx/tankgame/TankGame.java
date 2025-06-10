package com.mygdx.tankgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.tankgame.Screen.WelcomeScreen;

public class TankGame extends Game {
    public SpriteBatch batch; // Fix: Make batch accessible
    public ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch(); // Initialize batch
        shapeRenderer = new ShapeRenderer();
        this.setScreen(new WelcomeScreen(this)); // Fix: Use MainMenuScreen first
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        super.dispose();
    }
}
