package com.mygdx.tankgame.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.db.HighScoreManager;

import java.util.List;

public class HighScoreScreen implements Screen {
    private final TankGame game;
    private Stage stage;
    private Skin skin;
    private Texture background;

    public HighScoreScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        background = new Texture("background.jpg");
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("High Scores", skin);
        table.add(title).padBottom(40).row();

        // Load highscore từ DB
        HighScoreManager manager = new HighScoreManager();
        List<HighScoreManager.ScoreEntry> scores = manager.getTopScores(5);

        if (scores.isEmpty()) {
            table.add(new Label("No scores found.", skin)).padBottom(10).row();
        } else {
            int rank = 1;
            for (HighScoreManager.ScoreEntry score : scores) {
                String text = rank + ". " + score.name + " - " + score.score;
                table.add(new Label(text, skin)).padBottom(10).row();
                rank++;
            }
        }

        TextButton backButton = new TextButton("Back", skin);
        table.add(backButton).size(200, 50).padTop(30);

        stage.addActor(table);

        backButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new WelcomeScreen(game));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        if (background != null) {
            game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        if (background != null) background.dispose();
    }
}
