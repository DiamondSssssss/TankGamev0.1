package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.mygdx.tankgame.levels.Level3Screen;
import com.mygdx.tankgame.playertank.ShotgunTank;
import com.mygdx.tankgame.playertank.Tank;
import com.mygdx.tankgame.playertank.SniperTank;

public class MainMenuScreen implements Screen {
    private TankGame game;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;

    // Available tank choices
    private String[] tankOptions = {"Standard Tank", "Sniper Tank", "Shotgun Tank"};
    private int selectedTankIndex = 0;

    public MainMenuScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("menu_background.png"); // Ensure this is in core/assets/
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Process input to change the selected tank
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedTankIndex = (selectedTankIndex - 1 + tankOptions.length) % tankOptions.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedTankIndex = (selectedTankIndex + 1) % tankOptions.length;
        }

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, "Use LEFT/RIGHT arrows to select a tank",
            Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 + 50);
        font.draw(batch, "Selected Tank: " + tankOptions[selectedTankIndex],
            Gdx.graphics.getWidth() / 2 - 80, Gdx.graphics.getHeight() / 2 + 20);
        font.draw(batch, "Press ENTER to Start",
            Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 - 20);
        batch.end();

        // When ENTER is pressed, create the chosen tank and start the level.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Tank chosenTank;
            if (selectedTankIndex == 0) {
                chosenTank = new Tank(100, 100);
            } else if (selectedTankIndex == 1) {
                chosenTank = new SniperTank(100, 100);}
                else if (selectedTankIndex == 2) {
                    chosenTank = new ShotgunTank(100, 100);
            } else {
                chosenTank = new Tank(100, 100);
            }
            game.setScreen(new Level3Screen(game, chosenTank));
        }
    }

    @Override
    public void resize(int width, int height) {
        // Optionally update viewport here.
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Optionally stop music or animations here.
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
    }
}
