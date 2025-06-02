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

public class MainMenuScreen implements Screen {
    private final TankGame game;
    private Stage stage;
    private Texture background;
    private Table table;
    private Skin skin;

    public MainMenuScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        background = new Texture("menu.jpg"); // Image should be in assets/menu.jpg
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Your skin JSON file

        table = new Table();
        table.setFillParent(true);
        table.center();

        // Use default style for Label to avoid missing style error
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

        stage.addActor(table);

        classicBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to TankSelectionScreen with mode CLASSIC");
                game.setScreen(new TankSelectionScreen(game, "CLASSIC"));
            }
        });

        coopBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to TankSelectionScreen with mode COOP");
                game.setScreen(new CoopTankSelectionScreen(game));
            }
        });

        endlessBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to TankSelectionScreen with mode ENDLESS");
                game.setScreen(new TankSelectionScreen(game, "ENDLESS"));
            }
    });

        // You can add onlineBtn listener later if you want
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
    @Override public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        skin.dispose();
    }
}
