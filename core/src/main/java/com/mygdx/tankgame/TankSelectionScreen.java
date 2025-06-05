package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.playertank.*;

public class TankSelectionScreen implements Screen {

    private final TankGame game;
    private final String gameMode;

    private Texture backgroundTexture, basicTankTexture, sniperTankTexture, shotgunTankTexture;
    private Rectangle basicRect, sniperRect, shotgunRect;
    private BitmapFont font;

    private OrthographicCamera camera;
    private Viewport viewport;

    private boolean screenChanged = false;

    public TankSelectionScreen(TankGame game, String gameMode) {
        this.game = game;
        this.gameMode = gameMode;
    }

    @Override
    public void show() {
        // Set up camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        // Load assets
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        basicTankTexture = new Texture(Gdx.files.internal("tank.png"));
        sniperTankTexture = new Texture(Gdx.files.internal("sniper_tank.png"));
        shotgunTankTexture = new Texture(Gdx.files.internal("shotgun_tank.png"));

        // Set up UI elements using virtual coordinates
        float width = 150, height = 150;
        float centerY = 600 / 2f - height / 2f;
        float spacing = 100;
        float startX = (800 - (width * 3 + spacing * 2)) / 2;

        basicRect = new Rectangle(startX, centerY, width, height);
        sniperRect = new Rectangle(startX + width + spacing, centerY, width, height);
        shotgunRect = new Rectangle(startX + (width + spacing) * 2, centerY, width, height);

        // Set up font
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, 800, 600);

        drawTank(basicTankTexture, basicRect, "Basic", basicRect.contains(getMouse()));
        drawTank(sniperTankTexture, sniperRect, "Sniper", sniperRect.contains(getMouse()));
        drawTank(shotgunTankTexture, shotgunRect, "Shotgun", shotgunRect.contains(getMouse()));
        game.batch.end();

        handleInput();
    }

    private void drawTank(Texture texture, Rectangle rect, String label, boolean hovered) {
        Color original = game.batch.getColor().cpy();
        if (hovered) game.batch.setColor(1f, 1f, 1f, 0.8f);

        game.batch.draw(texture, rect.x, rect.y, rect.width, rect.height);

        GlyphLayout layout = new GlyphLayout(font, label);
        font.draw(game.batch, layout,
            rect.x + (rect.width - layout.width) / 2,
            rect.y - 10);

        game.batch.setColor(original);
    }

    private void handleInput() {
        if (screenChanged) return;

        if (Gdx.input.justTouched()) {
            Vector2 mouse = getMouse();
            PlayerTank selectedTank = null;

            if (basicRect.contains(mouse)) {
                selectedTank = new PlayerTank(100, 100);
            } else if (sniperRect.contains(mouse)) {
                selectedTank = new SniperPlayerTank(100, 100);
            } else if (shotgunRect.contains(mouse)) {
                selectedTank = new ShotgunPlayerTank(100, 100);
            }

            if (selectedTank != null) {
                screenChanged = true;
                if ("ENDLESS".equalsIgnoreCase(gameMode)) {
                    game.setScreen(new com.mygdx.tankgame.levels.EndlessLevelScreen(game, selectedTank));
                } else {
                    game.setScreen(new Level1Screen(game, selectedTank));
                }
                dispose();
            }
        }
    }

    private Vector2 getMouse() {
        Vector2 screenCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        return viewport.unproject(screenCoords);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        basicTankTexture.dispose();
        sniperTankTexture.dispose();
        shotgunTankTexture.dispose();
        font.dispose();
    }
}
