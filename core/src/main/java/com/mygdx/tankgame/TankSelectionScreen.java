package com.mygdx.tankgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.playertank.*;

public class TankSelectionScreen implements Screen {

    private final TankGame game;
    private String gameMode;
    private Texture backgroundTexture;
    private Texture basicTankTexture, sniperTankTexture, shotgunTankTexture;
    private Rectangle basicRect, sniperRect, shotgunRect;
    private BitmapFont font;

    public TankSelectionScreen(TankGame game, String gameMode) {
        this.game = game;
        this.gameMode = gameMode;

        backgroundTexture = new Texture(Gdx.files.internal("button.jpg")); // Replace with your background image
        basicTankTexture = new Texture(Gdx.files.internal("button.jpg"));
        sniperTankTexture = new Texture(Gdx.files.internal("button.jpg"));
        shotgunTankTexture = new Texture(Gdx.files.internal("button.jpg"));

        float width = 150, height = 150;
        float centerY = Gdx.graphics.getHeight() / 2f - height / 2f;
        float spacing = 100;
        float startX = (Gdx.graphics.getWidth() - (width * 3 + spacing * 2)) / 2;

        basicRect = new Rectangle(startX, centerY, width, height);
        sniperRect = new Rectangle(startX + width + spacing, centerY, width, height);
        shotgunRect = new Rectangle(startX + (width + spacing) * 2, centerY, width, height);

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw tank icons
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
    private boolean screenChanged = false;
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
        return new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        basicTankTexture.dispose();
        sniperTankTexture.dispose();
        shotgunTankTexture.dispose();
        font.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
