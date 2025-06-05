package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.tankgame.MainMenuScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.db.HighScoreDAO;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.bullets.Bullet;

public class EndlessLevelScreen extends LevelScreen {
    private float spawnTimer;
    private float spawnInterval = 2f;
    private final float minSpawnInterval = 1f;
    private final float spawnDecreaseRate = 0.05f;

    private final float warningDuration = 1.5f;
    private Texture warningTexture;
    private float warningX, warningY;
    private boolean isWarningActive = false;
    private float warningTimer = 0f;

    private int score = 0;
    private BitmapFont font;
    private int highestScore;
    private BitmapFont highestScoreFont;

    private boolean isGameOver = false;  // Flag kiểm soát trạng thái Game Over

    private final int VIRTUAL_WIDTH = 1280;
    private final int VIRTUAL_HEIGHT = 720;

    public EndlessLevelScreen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);

        warningTexture = new Texture(Gdx.files.internal("warning.png"));
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        highestScoreFont = new BitmapFont();
        highestScoreFont.setColor(Color.YELLOW);
        highestScoreFont.getData().setScale(1.5f);

        highestScore = HighScoreDAO.getHighestScore();
    }

    @Override
    protected void setupLevel() {
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        addWall(new Wall2(0,   0, 1280, 50));
        addWall(new Wall2(0, 670, 1280, 50));
        addWall(new Wall2(0,   0,   50, 720));
        addWall(new Wall2(1230,0,   50, 720));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (!isGameOver) {
            handleInput();
            updateGameElements(delta);
        }

        game.batch.begin();

        game.batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        renderWalls();
        playerTank.draw(game.batch);

        for (EnemyTank e : enemies) e.render(game.batch);
        for (Bullet b : bullets)    b.draw(game.batch);

        if (isWarningActive) {
            game.batch.draw(warningTexture, warningX, warningY);
        }

        // Hiển thị điểm và điểm cao
        String s = "Score: " + score;
        GlyphLayout gl = new GlyphLayout(font, s);
        float x = VIRTUAL_WIDTH - gl.width - 10;
        float y = VIRTUAL_HEIGHT - 10;
        font.setColor(Color.BLACK);
        font.draw(game.batch, s, x + 2, y - 2);
        font.setColor(Color.WHITE);
        font.draw(game.batch, s, x, y);

        String hs = "High Score: " + highestScore;
        GlyphLayout hsLayout = new GlyphLayout(highestScoreFont, hs);
        float hsX = x - hsLayout.width - 20;
        float hsY = y - 30;
        highestScoreFont.setColor(Color.BLACK);
        highestScoreFont.draw(game.batch, hs, hsX + 2, hsY - 2);
        highestScoreFont.setColor(Color.YELLOW);
        highestScoreFont.draw(game.batch, hs, hsX, hsY);

        game.batch.end();

        // Nếu game over thì hiện giao diện Game Over và nút thử lại
        if (isGameOver) {
            game.batch.begin();

            // Overlay đen mờ
            Gdx.gl.glEnable(GL20.GL_BLEND);
            game.batch.setColor(0, 0, 0, 0.7f);
            game.batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            game.batch.setColor(1, 1, 1, 1);

            BitmapFont gameOverFont = new BitmapFont();
            gameOverFont.getData().setScale(3f);
            gameOverFont.setColor(Color.RED);
            gameOverFont.draw(game.batch, "Game Over", VIRTUAL_WIDTH / 2f - 150, VIRTUAL_HEIGHT / 2f + 100);

            font.setColor(Color.WHITE);
            font.getData().setScale(2f);
            font.draw(game.batch, "Your Score: " + score, VIRTUAL_WIDTH / 2f - 100, VIRTUAL_HEIGHT / 2f + 40);

            highestScoreFont.draw(game.batch, "Highest Score: " + highestScore, VIRTUAL_WIDTH / 2f - 120, VIRTUAL_HEIGHT / 2f);

            font.draw(game.batch, "Tap to Retry", VIRTUAL_WIDTH / 2f - 90, VIRTUAL_HEIGHT / 2f - 60);
            font.draw(game.batch, "Press B to go to MainMenu", VIRTUAL_WIDTH / 2f - 140, VIRTUAL_HEIGHT / 2f - 100);

            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
            game.batch.end();

            // Kiểm tra tap để thử lại
            if (Gdx.input.justTouched()) {
                retryGame();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    protected void updateGameElements(float delta) {
        if (isGameOver) return;  // Ngưng update khi game over

        int before = enemies.size();
        super.updateGameElements(delta);
        int killed = before - enemies.size();
        if (killed > 0) {
            score += killed;
            Gdx.app.log("DEBUG", "Killed " + killed + " → score=" + score);
        }

        if (playerTank.isDestroyed()) {
            if (!isGameOver) {
                saveScoreToDatabase();
                isGameOver = true;
            }
            return;
        }

        if (!isWarningActive) {
            spawnTimer += delta;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f;
                activateWarning();
            }
        } else {
            warningTimer += delta;
            if (warningTimer >= warningDuration) {
                isWarningActive = false;
                warningTimer = 0f;
                spawnChaserTank();
                spawnInterval = Math.max(minSpawnInterval, spawnInterval - spawnDecreaseRate);
            }
        }
    }

    private void activateWarning() {
        isWarningActive = true;
        warningTimer = 0f;

        // Spawn warning within virtual resolution
        warningX = MathUtils.random(100, VIRTUAL_WIDTH - 100);
        warningY = MathUtils.random(100, VIRTUAL_HEIGHT - 100);
    }

    public void spawnChaserTank() {
        pendingEnemies.add(new ChaserTank(warningX, warningY, playerTank, bullets));
    }

    @Override
    protected void goToUpgradeScreen() {
        // not used
    }

    public void saveScoreToDatabase() {
        HighScoreDAO.insertScore("Player", score);
    }

    private void retryGame() {
        game.setScreen(new EndlessLevelScreen(game, new PlayerTank(100, 100)));
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();
        warningTexture.dispose();
        font.dispose();
        highestScoreFont.dispose();
    }
}
