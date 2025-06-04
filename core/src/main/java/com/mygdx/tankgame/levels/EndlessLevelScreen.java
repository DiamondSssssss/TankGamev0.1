package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.tankgame.MainMenuScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.buildstuff.Wall;
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
        // 1) clear
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) input
        handleInput();

        // 3) update everything (scoring happens in super call)a
        updateGameElements(delta);

        // 4) draw all in one batch
        game.batch.begin();

        // background
        game.batch.draw(backgroundTexture, 0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // level walls
        renderWalls();

        // player
        playerTank.draw(game.batch);

        // enemies & bullets
        for (EnemyTank e : enemies) e.render(game.batch);
        for (Bullet b : bullets)    b.draw(game.batch);

        // warning icon
        if (isWarningActive) {
            game.batch.draw(warningTexture, warningX, warningY);
        }

        // score top‑right
        String s = "Score: " + score;
        GlyphLayout gl = new GlyphLayout(font, s);
        float x = Gdx.graphics.getWidth() - gl.width - 10;
        float y = Gdx.graphics.getHeight() - 10;
        font.setColor(Color.BLACK);
        font.draw(game.batch, s, x + 2, y - 2);
        font.setColor(Color.WHITE);
        font.draw(game.batch, s, x, y);
        String hs = "High Score: " + highestScore;
        GlyphLayout hsLayout = new GlyphLayout(highestScoreFont, hs);
        float hsX = x - 100; // move left by 100 pixels
        float hsY = y - 30;

        highestScoreFont.setColor(Color.BLACK);
        highestScoreFont.draw(game.batch, hs, hsX + 2, hsY - 2);
        highestScoreFont.setColor(Color.YELLOW);
        highestScoreFont.draw(game.batch, hs, hsX, hsY);
        game.batch.end();
    }

    @Override
    protected void updateGameElements(float delta) {
        int before = enemies.size();
        super.updateGameElements(delta);
        int killed = before - enemies.size();
        if (killed > 0) {
            score += killed;
            Gdx.app.log("DEBUG", "Killed " + killed + " → score=" + score);
        }

        // kiểm tra nếu người chơi chết
        if (playerTank.isDestroyed()) {
            saveScoreToDatabase();
            game.setScreen(new MainMenuScreen(game)); // hoặc MainMenuScreen
            dispose(); // giải phóng tài nguyên
            return;
        }

        // spawn logic
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
                spawnInterval = Math.max(minSpawnInterval,
                    spawnInterval - spawnDecreaseRate);
            }
        }
    }


    private void activateWarning() {
        isWarningActive = true;
        warningTimer = 0f;
        warningX = MathUtils.random(100, 1100);
        warningY = MathUtils.random(100, 600);
    }

    public void spawnChaserTank() {
        pendingEnemies.add(new ChaserTank(
            warningX, warningY, playerTank, bullets));
    }

    @Override
    protected void goToUpgradeScreen() {
        // never used in endless
    }
    public void saveScoreToDatabase() {
        HighScoreDAO.insertScore("Player", score);
    }

    @Override
    public void dispose() {
        super.dispose();
        warningTexture.dispose();
        font.dispose();
        highestScoreFont.dispose();
    }
}
