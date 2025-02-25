package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.levels.Level3Screen;

public class MainMenuScreen implements Screen {
    private TankGame game;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;
    //private Music menuMusic;

    public MainMenuScreen(TankGame game) { // Fix: Use TankGame
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("menu_background.png"); // Ensure this is in core/assets/
        font = new BitmapFont();
        //menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_music.mp3")); // Optional
        //menuMusic.setLooping(true);
        //menuMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, "Press ENTER to Start", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new Level3Screen(game,new Tank(100,100))); // Fix: Pass game to GameScreen
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        //menuMusic.stop();
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
        //menuMusic.dispose();
    }
}
