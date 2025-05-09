package com.mygdx.tankgame.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.tankgame.TankGame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiplayerScreen extends ScreenAdapter {
    private final TankGame game;
    private final boolean isHost;
    private final MultiplayerTank localTank, remoteTank;
    private final BitmapFont font;
    private final List<MultiplayerBullet> myBullets = new ArrayList<>();
    private final List<MultiplayerBullet> enemyBullets = new ArrayList<>();
    private Texture tankTexture;
    private Texture bulletTexture;
    private NetworkHandler net;

    public MultiplayerScreen(TankGame game, boolean isHost, String ip) {
        this.game = game;
        this.isHost = isHost;
        font = new BitmapFont();

        // Load images
        tankTexture = new Texture("tank.png");
        bulletTexture = new Texture("bullet.jpg");

        localTank = new MultiplayerTank(100, 100);
        remoteTank = new MultiplayerTank(200, 200);

        if (isHost) {
            try {
                Thread.sleep(300); // Give time to set up server
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            net = new NetworkHandler(ip, 5555);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        handleInput();
        updateBullets(delta);
        checkWinCondition();

        try {
            // Send data
            StringBuilder dataToSend = new StringBuilder(localTank.serialize());
            for (MultiplayerBullet b : myBullets) {
                dataToSend.append(";").append(b.serialize());
            }
            net.send(dataToSend.toString());

            // Receive data
            String data = net.receive();
            if (data != null) {
                String[] parts = data.split(";");
                remoteTank.updateFromNetwork(parts[0]);

                enemyBullets.clear();
                for (int i = 1; i < parts.length; i++) {
                    enemyBullets.add(MultiplayerBullet.deserialize(parts[i]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // Draw tanks
        game.batch.draw(tankTexture, localTank.x, localTank.y);
        game.batch.draw(tankTexture, remoteTank.x, remoteTank.y);

        // Draw bullets
        for (MultiplayerBullet b : myBullets) {
            game.batch.draw(bulletTexture, b.x, b.y);
        }
        for (MultiplayerBullet b : enemyBullets) {
            game.batch.draw(bulletTexture, b.x, b.y);
        }

        // HUD
        font.draw(game.batch, "Your HP: " + localTank.health, 20, 440);
        font.draw(game.batch, "Enemy HP: " + remoteTank.health, 20, 420);

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) localTank.y += 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) localTank.y -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) localTank.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) localTank.x += 200 * Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            MultiplayerBullet bullet = new MultiplayerBullet(localTank.x + 16, localTank.y + 32, 0, 300);
            myBullets.add(bullet);
        }
    }

    private void updateBullets(float delta) {
        // Update my bullets
        Iterator<MultiplayerBullet> myIt = myBullets.iterator();
        while (myIt.hasNext()) {
            MultiplayerBullet b = myIt.next();
            b.update(delta);
            if (getBulletRect(b).overlaps(getTankRect(remoteTank))) {
                remoteTank.health -= 10;
                myIt.remove();
            } else if (b.y > Gdx.graphics.getHeight()) {
                myIt.remove();
            }
        }

        // Update enemy bullets
        Iterator<MultiplayerBullet> enemyIt = enemyBullets.iterator();
        while (enemyIt.hasNext()) {
            MultiplayerBullet b = enemyIt.next();
            b.update(delta);
            if (getBulletRect(b).overlaps(getTankRect(localTank))) {
                localTank.health -= 10;
                enemyIt.remove();
            } else if (b.y > Gdx.graphics.getHeight()) {
                enemyIt.remove();
            }
        }
    }

    private void checkWinCondition() {
        if (localTank.health <= 0 || remoteTank.health <= 0) {
            game.batch.begin();
            String result = localTank.health <= 0 ? "You Lose!" : "You Win!";
            font.draw(game.batch, result, Gdx.graphics.getWidth() / 2f - 40, Gdx.graphics.getHeight() / 2f);
            game.batch.end();
        }
    }

    private Rectangle getTankRect(MultiplayerTank tank) {
        return new Rectangle(tank.x, tank.y, 32, 32); // size of the tank texture
    }

    private Rectangle getBulletRect(MultiplayerBullet bullet) {
        return new Rectangle(bullet.x, bullet.y, 8, 8); // size of the bullet texture
    }

    @Override
    public void dispose() {
        font.dispose();
        tankTexture.dispose();
        bulletTexture.dispose();
    }
}
