package com.mygdx.tankgame.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.coop.*;
import com.mygdx.tankgame.levels.CoopLevelScreen;
import com.mygdx.tankgame.playertank.PlayerTank;

public class CoopTankSelectionScreen implements Screen {

    private final TankGame game;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private final Texture backgroundTexture;
    private final Texture[] tankImages;
    private final String[] tankOptions = {"DefaultTank", "SniperTank", "ShotgunTank"};

    private int selectedTankIndexP1 = 0;
    private int selectedTankIndexP2 = 0;
    private boolean lockedP1 = false;
    private boolean lockedP2 = false;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    public CoopTankSelectionScreen(TankGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(1280, 720, camera); // Virtual resolution
        this.viewport.apply();

        this.batch = new SpriteBatch();
        this.font = new BitmapFont();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        this.backgroundTexture = new Texture("tank_select.jpg");
        this.tankImages = new Texture[]{
            new Texture("tank.png"),
            new Texture("sniper_tank.png"),
            new Texture("shotgun_tank.png")
        };
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw background
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        float centerY = viewport.getWorldHeight() / 2f;

        // Title
        font.draw(batch, "Coop Mode - Select Tanks for Each Player", viewport.getWorldWidth() / 2f - 150, viewport.getWorldHeight() - 50);

        // Player 1 UI
        font.draw(batch, "Player 1: " + tankOptions[selectedTankIndexP1] + (lockedP1 ? " (Locked)" : ""), 100, centerY + 100);
        batch.draw(tankImages[selectedTankIndexP1], 100, centerY - 100, 200, 150);
        font.draw(batch, "P1 use A/D to switch, J to lock", 100, centerY - 180);

        // Player 2 UI
        font.draw(batch, "Player 2: " + tankOptions[selectedTankIndexP2] + (lockedP2 ? " (Locked)" : ""),
            viewport.getWorldWidth() - 300, centerY + 100);
        batch.draw(tankImages[selectedTankIndexP2], viewport.getWorldWidth() - 300, centerY - 100, 200, 150);
        font.draw(batch, "P2 use LEFT/RIGHT to switch, NUMPAD1 to lock",
            viewport.getWorldWidth() - 400, centerY - 180);

        batch.end();

        handleInput();
    }

    private void handleInput() {
        // Player 1 input
        if (!lockedP1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedTankIndexP1 = (selectedTankIndexP1 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedTankIndexP1 = (selectedTankIndexP1 + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                lockedP1 = true;
            }
        }

        // Player 2 input
        if (!lockedP2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                lockedP2 = true;
            }
        }

        // Both locked: start game
        if (lockedP1 && lockedP2) {
            PlayerTank playerOne = switch (selectedTankIndexP1) {
                case 1 -> new CoopSniperPlayerTankOne(100, 100);
                case 2 -> new CoopShotgunPlayerTankOne(100, 100);
                default -> new PlayerOnePlayerTank(100, 100);
            };

            PlayerTank playerTwo = switch (selectedTankIndexP2) {
                case 1 -> new CoopSniperPlayerTankTwo(100, 100);
                case 2 -> new CoopShotgunPlayerTankTwo(100, 100);
                default -> new PlayerTwoPlayerTank(100, 100);
            };

            System.out.println("playerOne: " + playerOne);
            System.out.println("playerTwo: " + playerTwo);
            game.setScreen(new CoopLevelScreen(game, playerOne, playerTwo));

        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        backgroundTexture.dispose();
        for (Texture texture : tankImages) {
            texture.dispose();
        }
    }
}
