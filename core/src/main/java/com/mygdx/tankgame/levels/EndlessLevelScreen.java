package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;

public class EndlessLevelScreen extends LevelScreen {
    private float spawnTimer;  // Timer for enemy spawning
    private float warningDuration = 1.5f;  // Duration for the warning image to be displayed
    private Texture warningTexture;  // Warning image texture
    private float warningX, warningY;  // Position of the warning image

    private boolean isWarningActive = false; // Is warning currently showing?
    private float warningTimer = 0f; // Timer for how long warning has been shown

    public EndlessLevelScreen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
        warningTexture = new Texture(Gdx.files.internal("warning.png"));
        spawnTimer = 0f;  // Start the timer
    }

    @Override
    protected void setupLevel() {
        // Directly define walls and obstacles for endless mode
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));

        // Define walls manually
        addWall(new Wall(0, 0, 1280, 50));  // Top wall
        addWall(new Wall(0, 670, 1280, 50));  // Bottom wall
        addWall(new Wall(0, 0, 50, 720));  // Left wall
        addWall(new Wall(1230, 0, 50, 720));  // Right wall
    }

    @Override
    protected void updateGameElements(float delta) {
        super.updateGameElements(delta);

        if (!isWarningActive) {
            spawnTimer += delta;
            if (spawnTimer >= 5f) {
                spawnTimer = 0f;
                activateWarning();  // Start the warning phase
            }
        } else {
            warningTimer += delta;
            if (warningTimer >= warningDuration) {
                isWarningActive = false;
                warningTimer = 0f;
                spawnChaserTank();  // Spawn enemy after warning
            }
        }
    }

    private void activateWarning() {
        isWarningActive = true;
        warningTimer = 0f;
        warningX = MathUtils.random(0, Gdx.graphics.getWidth() - warningTexture.getWidth());
        warningY = MathUtils.random(0, Gdx.graphics.getHeight() - warningTexture.getHeight());
    }

    @Override
    protected void renderGameElements() {
        super.renderGameElements();

        if (isWarningActive) {
            game.batch.begin();
            game.batch.draw(warningTexture, warningX, warningY);
            game.batch.end();
        }
    }

    @Override
    protected void goToUpgradeScreen() {
        // Transition to upgrade screen if needed
    }

    @Override
    public void dispose() {
        super.dispose();
        warningTexture.dispose();  // Properly dispose of warning image
    }
}
