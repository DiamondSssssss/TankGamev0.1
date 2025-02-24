package com.mygdx.tankgame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class VictoryScreen implements Screen {
    private final TankGame game;
    private Stage stage;
    private Skin skin;
    private TextButton mainMenuButton;
    private SpriteBatch batch;
    private BitmapFont font;

    public VictoryScreen(TankGame game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Load skin (make sure uiskin.json exists)
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        TextButton mainMenuButton = new TextButton("Main Menu", buttonStyle);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game)); // Navigate to Main Menu
                dispose();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(mainMenuButton).padTop(20);
        stage.addActor(table);
    }

    @Override
    public void show() {
        // Called when this screen is set
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Congratulations! You Won!", Gdx.graphics.getWidth() / 2f - 80, Gdx.graphics.getHeight() / 2f + 50);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
    }
}
