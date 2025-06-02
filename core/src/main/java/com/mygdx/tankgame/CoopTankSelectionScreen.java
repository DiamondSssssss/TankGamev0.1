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
import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.playertank.ShotgunPlayerTank;
import com.mygdx.tankgame.playertank.SniperPlayerTank;
public class CoopTankSelectionScreen implements Screen {

    private final TankGame game;

    private SpriteBatch batch;
    private BitmapFont font;

    private final String[] tankOptions = {"DefaultTank", "SniperTank", "ShotgunTank"};
    private Texture[] tankImages;

    private int selectedTankIndexP1 = 0;
    private int selectedTankIndexP2 = 0;

    private boolean lockedP1 = false;
    private boolean lockedP2 = false;

    public CoopTankSelectionScreen(TankGame game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();

        tankImages = new Texture[tankOptions.length];
        tankImages[0] = new Texture("tank.png");
        tankImages[1] = new Texture("sniper_tank.jpg");
        tankImages[2] = new Texture("shotgun_tank.jpg");
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.draw(batch, "Coop Mode - Select Tanks for Each Player", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight() - 50);

        // Player 1
        font.draw(batch, "Player 1: " + tankOptions[selectedTankIndexP1] + (lockedP1 ? " (Locked)" : ""), 100, Gdx.graphics.getHeight() / 2 + 100);
        batch.draw(tankImages[selectedTankIndexP1], 100, Gdx.graphics.getHeight() / 2 - 50, 200, 150);

        // Player 2
        font.draw(batch, "Player 2: " + tankOptions[selectedTankIndexP2] + (lockedP2 ? " (Locked)" : ""), Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() / 2 + 100);
        batch.draw(tankImages[selectedTankIndexP2], Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() / 2 - 50, 200, 150);

        font.draw(batch, "P1 use A/D to switch, J to lock", 100, Gdx.graphics.getHeight() / 2 - 120);
        font.draw(batch, "P2 use LEFT/RIGHT to switch, NUMPAD1 to lock", Gdx.graphics.getWidth() - 400, Gdx.graphics.getHeight() / 2 - 120);

        batch.end();

        handleInput();
    }

    private void handleInput() {
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

            game.setScreen(new CoopLevelScreen(game, playerOne, playerTwo));
        }
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
        font.dispose();
        for (Texture texture : tankImages) {
            texture.dispose();
        }
    }
}
