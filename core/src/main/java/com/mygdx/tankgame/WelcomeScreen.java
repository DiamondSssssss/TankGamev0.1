package com.mygdx.tankgame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class WelcomeScreen implements Screen {
    private final TankGame game;
    private Stage stage;
    private Texture background;
    private Table table;
    private Skin skin;

    public WelcomeScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        background = new Texture("background.jpg"); // Ensure you have welcome.jpg in your assets folder
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("WELCOME TO TANK GAME", skin);
        TextButton startBtn = new TextButton("Start", skin);
        TextButton highscoreBtn = new TextButton("High Scores", skin);
        TextButton quitBtn = new TextButton("Quit", skin);

        table.add(title).padBottom(40).row();
        table.add(startBtn).size(200, 60).padBottom(20).row();
        table.add(highscoreBtn).size(200, 60).padBottom(20).row();
        table.add(quitBtn).size(200, 60).padBottom(20).row();

        stage.addActor(table);

        startBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        highscoreBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                System.out.println("Highscore button clicked (implement screen later)");
                game.setScreen(new HighScoreScreen(game));
            }
        });

        quitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        skin.dispose();
    }
}
