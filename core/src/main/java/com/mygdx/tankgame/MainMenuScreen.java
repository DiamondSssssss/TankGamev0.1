package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.tankgame.coop.CoopShotgunPlayerTankOne;
import com.mygdx.tankgame.coop.CoopShotgunPlayerTankTwo;
import com.mygdx.tankgame.coop.CoopSniperPlayerTankOne;
import com.mygdx.tankgame.coop.CoopSniperPlayerTankTwo;
import com.mygdx.tankgame.coop.PlayerOnePlayerTank;
import com.mygdx.tankgame.coop.PlayerTwoPlayerTank;
import com.mygdx.tankgame.levels.CoopLevelScreen;
import com.mygdx.tankgame.levels.EndlessLevelScreen;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.online.MultiplayerLobbyScreen;
import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.playertank.ShotgunPlayerTank;
import com.mygdx.tankgame.playertank.SniperPlayerTank;

public class MainMenuScreen implements Screen {
    private TankGame game;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;

    private enum MenuState { MODE_SELECTION, TANK_SELECTION }
    private enum GameMode { CLASSIC, COOP , ONLINE , ENDLESS }

    private MenuState menuState = MenuState.MODE_SELECTION;
    private GameMode selectedMode = GameMode.CLASSIC;

    private String[] tankOptions = {"Standard Tank", "Sniper Tank", "Shotgun Tank"};
    private int selectedTankIndexClassic = 0;
    private int selectedTankIndexP1 = 0;
    private int selectedTankIndexP2 = 0;

    public MainMenuScreen(TankGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("menu.jpg");
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menuState == MenuState.MODE_SELECTION) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedMode = (selectedMode == GameMode.CLASSIC) ? GameMode.COOP :
                    (selectedMode == GameMode.COOP) ? GameMode.ONLINE :
                        (selectedMode == GameMode.ONLINE) ? GameMode.ENDLESS :
                            GameMode.CLASSIC;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (selectedMode == GameMode.ONLINE) {
                    game.setScreen(new MultiplayerLobbyScreen(game));
                } else {
                    menuState = MenuState.TANK_SELECTION;
                }
            }

            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            font.draw(batch, "Select Mode:", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 + 50);
            String modeText = switch (selectedMode) {
                case CLASSIC -> "Classic Mode";
                case COOP -> "Coop Mode";
                case ONLINE -> "Online Mode";
                case ENDLESS -> "Endless Mode";
            };
            font.draw(batch, modeText, Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);
            font.draw(batch, "Use LEFT/RIGHT arrows to toggle mode.", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 - 30);
            font.draw(batch, "Press ENTER to continue.", Gdx.graphics.getWidth() / 2 - 80, Gdx.graphics.getHeight() / 2 - 60);
            batch.end();
            return;
        }

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (selectedMode == GameMode.CLASSIC) {
            font.draw(batch, "Classic Mode - Select Your Tank", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 50);
            font.draw(batch, "Tank Options: " + tankOptions[selectedTankIndexClassic], Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2);
            font.draw(batch, "Use LEFT/RIGHT to change selection.", Gdx.graphics.getWidth() / 2 - 130, Gdx.graphics.getHeight() / 2 - 30);
            font.draw(batch, "Press ENTER to start.", Gdx.graphics.getWidth() / 2 - 80, Gdx.graphics.getHeight() / 2 - 60);

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                PlayerTank chosenPlayerTank = switch (selectedTankIndexClassic) {
                    case 1 -> new SniperPlayerTank(100, 100);
                    case 2 -> new ShotgunPlayerTank(100, 100);
                    default -> new PlayerTank(100, 100);
                };
                game.setScreen(new Level1Screen(game, chosenPlayerTank));
            }

        } else if (selectedMode == GameMode.COOP) {
            font.draw(batch, "Coop Mode - Select Tanks for Each Player", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() / 2 + 70);
            font.draw(batch, "Player 1: " + tankOptions[selectedTankIndexP1], Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() / 2 + 20);
            font.draw(batch, "Player 2: " + tankOptions[selectedTankIndexP2], Gdx.graphics.getWidth() / 2 + 50, Gdx.graphics.getHeight() / 2 + 20);
            font.draw(batch, "P1 use A/D; P2 use LEFT/RIGHT to change selection.", Gdx.graphics.getWidth() / 2 - 180, Gdx.graphics.getHeight() / 2 - 20);
            font.draw(batch, "Press ENTER to start Coop mode.", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 - 60);

            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedTankIndexP1 = (selectedTankIndexP1 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedTankIndexP1 = (selectedTankIndexP1 + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexP2 = (selectedTankIndexP2 + 1) % tankOptions.length;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
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

                game.setScreen(new CoopLevelScreen(game, playerOne, playerTwo));
            }

        } else if (selectedMode == GameMode.ENDLESS) {
            font.draw(batch, "Endless Mode - Select Your Tank", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 50);
            font.draw(batch, "Tank Options: " + tankOptions[selectedTankIndexClassic], Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2);
            font.draw(batch, "Use LEFT/RIGHT to change selection.", Gdx.graphics.getWidth() / 2 - 130, Gdx.graphics.getHeight() / 2 - 30);
            font.draw(batch, "Press ENTER to start Endless mode.", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 - 60);

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic - 1 + tankOptions.length) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedTankIndexClassic = (selectedTankIndexClassic + 1) % tankOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                PlayerTank chosenPlayerTank = switch (selectedTankIndexClassic) {
                    case 1 -> new SniperPlayerTank(100, 100);
                    case 2 -> new ShotgunPlayerTank(100, 100);
                    default -> new PlayerTank(100, 100);
                };
                game.setScreen(new EndlessLevelScreen(game, chosenPlayerTank));
            }
        }

        batch.end();
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
        batch.dispose();
        background.dispose();
        font.dispose();
    }
}
