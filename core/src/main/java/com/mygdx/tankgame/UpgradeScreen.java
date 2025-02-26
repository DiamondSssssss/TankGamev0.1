package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.levels.Level2Screen;
import com.mygdx.tankgame.levels.Level3Screen;
import com.mygdx.tankgame.playertank.Tank;

public class UpgradeScreen implements Screen {
    private TankGame game;
    private Tank playerTank;
    private SpriteBatch batch;
    private BitmapFont font;
    private int currentLevel; // The level the player just completed

    public UpgradeScreen(TankGame game, Tank playerTank, int currentLevel) {
        this.game = game;
        this.playerTank = playerTank;
        this.currentLevel = currentLevel;
        batch = new SpriteBatch();
        font = new BitmapFont(); // Default built-in font
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "Upgrade Screen", 50, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Press 1: Increase Speed", 50, Gdx.graphics.getHeight() - 100);
        font.draw(batch, "Press 2: Increase Dash Charges", 50, Gdx.graphics.getHeight() - 150);
        font.draw(batch, "Press 3: Increase Health", 50, Gdx.graphics.getHeight() - 200);
        batch.end();

        // Check for upgrade selection using number keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            playerTank.applyUpgrade(0, 0, 50f); // Increase speed by 50
            returnToLevel();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            playerTank.applyUpgrade(1, 0, 0); // Increase dash charges by 1
            returnToLevel();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            playerTank.applyUpgrade(0, 1, 0); // Increase health (max and current) by 10
            returnToLevel();
        }
    }

    private void returnToLevel() {
        // Transition to the next level based on the current level
        switch (currentLevel) {
            case 1:
                game.setScreen(new Level2Screen(game, playerTank)); // Go to Level 2
                break;
            case 2:
                game.setScreen(new Level3Screen(game, playerTank)); // Go to Level 3
                break;
            default:
                game.setScreen(new Level1Screen(game, playerTank)); // Restart Level 1 as fallback
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Handle viewport resizing if necessary
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void pause() {
        // Called when the game is paused
    }

    @Override
    public void resume() {
        // Called when the game is resumed
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
