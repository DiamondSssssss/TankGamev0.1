package com.mygdx.tankgame.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.mygdx.tankgame.TankGame;

public class MainMenuScreen implements Screen {
    private final TankGame game;
    private Stage stage;
    private Texture background;
    private Table table;
    private Skin skin;

    // Define virtual resolution
    private static final int VIRTUAL_WIDTH = 1280;
    private static final int VIRTUAL_HEIGHT = 720;

    public MainMenuScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Use StretchViewport for fullscreen and resolution independence
        stage = new Stage(new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        background = new Texture("menu.jpg"); // Use high-res image (1280x720 or better)
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("TANK GAME", skin);
        TextButton classicBtn = new TextButton("Classic Mode", skin);
        TextButton coopBtn = new TextButton("Coop Mode", skin);
        TextButton endlessBtn = new TextButton("Endless Mode", skin);
        TextButton onlineBtn = new TextButton("Online Mode", skin);

        table.add(title).padBottom(40).row();
        table.add(classicBtn).size(250, 60).padBottom(20).row();
        table.add(coopBtn).size(250, 60).padBottom(20).row();
        table.add(endlessBtn).size(250, 60).padBottom(20).row();
        table.add(onlineBtn).size(250, 60).padBottom(20).row();
        // Back button setup
        TextButton backButton = new TextButton("Back", skin);
        Table topRightTable = new Table();
        topRightTable.top().right();
        topRightTable.setFillParent(true);
        topRightTable.add(backButton).pad(10); // top right with padding

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Returning to WelcomeScreen");
                game.setScreen(new WelcomeScreen(game));
            }
        });
        stage.addActor(table);
        stage.addActor(topRightTable);
        classicBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to TankSelectionScreen with mode CLASSIC");
                game.setScreen(new TankSelectionScreen(game, "CLASSIC"));
            }
        });

        coopBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to CoopTankSelectionScreen");
                game.setScreen(new CoopTankSelectionScreen(game));
            }
        });

        endlessBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to TankSelectionScreen with mode ENDLESS");
                game.setScreen(new TankSelectionScreen(game, "ENDLESS"));
            }
        });

        // TODO: Implement onlineBtn functionality when ready
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(stage.getCamera().combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
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
