package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.tankgame.coop.PlayerOneTank;
import com.mygdx.tankgame.coop.PlayerTwoTank;
import com.mygdx.tankgame.levels.Level3Screen;
import com.mygdx.tankgame.levels.CoopLevelScreen;
import com.mygdx.tankgame.playertank.ShotgunTank;
import com.mygdx.tankgame.playertank.SniperTank;
import com.mygdx.tankgame.playertank.Tank;

public class MainMenuScreen implements Screen {
    private TankGame game;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;

    // --- State enums ---
    private enum MenuState { MODE_SELECTION, TANK_SELECTION }
    private enum GameMode { CLASSIC, COOP }

    private MenuState menuState = MenuState.MODE_SELECTION;
    private GameMode selectedMode = GameMode.CLASSIC; // default

    // Tank options (for Classic mode, one tank; for Coop, separate selections).
    private String[] tankOptions = {"Standard Tank", "Sniper Tank", "Shotgun Tank"};
    // For Classic mode:
    private int selectedTankIndexClassic = 0;
    // For Coop mode:
    private int selectedTankIndexP1 = 0;
    private int selectedTankIndexP2 = 0;

    public MainMenuScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("menu_background.png"); // Ensure this is in assets/
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Mode Selection State ---
        if (menuState == MenuState.MODE_SELECTION) {
            // Use LEFT/RIGHT keys to change mode.
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                // Toggle mode (if currently CLASSIC, switch to COOP; if COOP, switch to CLASSIC)
                selectedMode = (selectedMode == GameMode.CLASSIC) ? GameMode.COOP : GameMode.CLASSIC;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedMode = (selectedMode == GameMode.CLASSIC) ? GameMode.COOP : GameMode.CLASSIC;
            }
            // Press ENTER to move to tank selection.
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                menuState = MenuState.TANK_SELECTION;
            }

            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            font.draw(batch, "Select Mode:", Gdx.graphics.getWidth()/2 - 50, Gdx.graphics.getHeight()/2 + 50);
            String modeText = (selectedMode == GameMode.CLASSIC) ? "Classic Mode" : "Coop Mode";
            font.draw(batch, modeText, Gdx.graphics.getWidth()/2 - 50, Gdx.graphics.getHeight()/2);
            font.draw(batch, "Use LEFT/RIGHT arrows to toggle mode.", Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2 - 30);
            font.draw(batch, "Press ENTER to continue.", Gdx.graphics.getWidth()/2 - 80, Gdx.graphics.getHeight()/2 - 60);
            batch.end();
            return;
        }

        // --- Tank Selection State ---
        // In Classic mode: one tank selection; in Coop mode: two separate selections.
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (selectedMode == GameMode.CLASSIC) {
            font.draw(batch, "Classic Mode - Select Your Tank", Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 + 50);
            font.draw(batch, "Tank Options: " + tankOptions[selectedTankIndexClassic], Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2);
            font.draw(batch, "Use LEFT/RIGHT to change selection.", Gdx.graphics.getWidth()/2 - 130, Gdx.graphics.getHeight()/2 - 30);
            font.draw(batch, "Press ENTER to start.", Gdx.graphics.getWidth()/2 - 80, Gdx.graphics.getHeight()/2 - 60);

            // Process input for Classic mode selection.
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                Tank chosenTank;
                if (selectedTankIndexClassic == 0) {
                    chosenTank = new Tank(100, 100);
                } else if (selectedTankIndexClassic == 1) {
                    chosenTank = new SniperTank(100, 100);
                } else if (selectedTankIndexClassic == 2) {
                    chosenTank = new ShotgunTank(100, 100);
                } else {
                    chosenTank = new Tank(100, 100);
                }
                game.setScreen(new Level3Screen(game, chosenTank));
            }
        } else if (selectedMode == GameMode.COOP) {
            font.draw(batch, "Coop Mode - Select Tanks for Each Player", Gdx.graphics.getWidth()/2 - 150, Gdx.graphics.getHeight()/2 + 70);
            // Display Player One selection.
            font.draw(batch, "Player 1: " + tankOptions[selectedTankIndexP1], Gdx.graphics.getWidth()/2 - 200, Gdx.graphics.getHeight()/2 + 20);
            // Display Player Two selection.
            font.draw(batch, "Player 2: " + tankOptions[selectedTankIndexP2], Gdx.graphics.getWidth()/2 + 50, Gdx.graphics.getHeight()/2 + 20);
            font.draw(batch, "P1 use A/D; P2 use LEFT/RIGHT to change selection.", Gdx.graphics.getWidth()/2 - 180, Gdx.graphics.getHeight()/2 - 20);
            font.draw(batch, "Press ENTER to start Coop mode.", Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 60);

            // Process input for Player One (A/D keys).
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedTankIndexP1 = (selectedTankIndexP1 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedTankIndexP1 = (selectedTankIndexP1 + 1) % tankOptions.length;
            }
            // Process input for Player Two (LEFT/RIGHT arrow keys).
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                PlayerOneTank playerOne;
                PlayerTwoTank playerTwo;
                if (selectedTankIndexP1 == 0) {
                    playerOne = new PlayerOneTank(100, 100); // Standard tank using WASD/J/K
                } else if (selectedTankIndexP1 == 1) {
                    playerOne = new PlayerOneTank(100, 100); // Or if you want a Sniper variant, you could have a subclass or use composition
                } else if (selectedTankIndexP1 == 2) {
                    playerOne = new PlayerOneTank(100, 100); // Similarly for Shotgun
                } else {
                    playerOne = new PlayerOneTank(100, 100);
                }

                if (selectedTankIndexP2 == 0) {
                    playerTwo = new PlayerTwoTank(200, 100); // Standard tank using arrow keys/NUMPAD
                } else if (selectedTankIndexP2 == 1) {
                    playerTwo = new PlayerTwoTank(200, 100);
                } else if (selectedTankIndexP2 == 2) {
                    playerTwo = new PlayerTwoTank(200, 100);
                } else {
                    playerTwo = new PlayerTwoTank(200, 100);
                }
                game.setScreen(new CoopLevelScreen(game, playerOne, playerTwo));
            }
            }
        batch.end();
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
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
    }
}
