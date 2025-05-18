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
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultiplayerScreen extends ScreenAdapter {
    private final TankGame game;
    private final boolean isHost;
    private final String peerIp;

    private final MultiplayerTank localTank, remoteTank;
    private final List<MultiplayerBullet> myBullets    = new ArrayList<>();
    private final List<MultiplayerBullet> theirBullets = new ArrayList<>();

    private final Texture tankTex   = new Texture("tank.png");
    private final Texture bulletTex = new Texture("bullet.jpg");
    private final BitmapFont font   = new BitmapFont();

    private TankHost   hostService;
    private TankClient clientService;

    private final ConcurrentLinkedQueue<String> recvQueue = new ConcurrentLinkedQueue<>();

    public MultiplayerScreen(TankGame game, boolean isHost, String peerIp) {
        this.game   = game;
        this.isHost = isHost;
        this.peerIp = peerIp;
        localTank  = new MultiplayerTank(100, 100);
        remoteTank = new MultiplayerTank(200, 200);
    }

    @Override
    public void show() {
        if (isHost) {
            hostService = new TankHost(5555);
            hostService.setConnectionListener(recvQueue::offer);
            new Thread(hostService::startServer, "Host-Startup").start();
        } else {
            try {
                clientService = new TankClient(peerIp, 5555);
                clientService.setConnectionListener(recvQueue::offer);
                clientService.startClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Send-loop: send our state at ~20Hz
        new Thread(() -> {
            try {
                while (true) {
                    StringBuilder sb = new StringBuilder(localTank.serialize());
                    synchronized (myBullets) {
                        for (MultiplayerBullet b : myBullets) {
                            sb.append(";").append(b.serialize());
                        }
                    }
                    String payload = sb.toString();
                    if (isHost) {
                        hostService.sendToClient(payload);
                    } else {
                        clientService.sendToHost(payload);
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {}
        }, "Send-Loop").start();

        // Receive-loop: parse incoming packets
        new Thread(() -> {
            while (true) {
                String data = recvQueue.poll();
                if (data != null) {
                    String[] parts = data.split(";");
                    remoteTank.updateFromNetwork(parts[0]);
                    synchronized (theirBullets) {
                        theirBullets.clear();
                        for (int i = 1; i < parts.length; i++) {
                            theirBullets.add(MultiplayerBullet.deserialize(parts[i]));
                        }
                    }
                }
            }
        }, "Recv-Loop").start();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateBullets(delta);
        checkWinCondition();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(tankTex, localTank.x, localTank.y);
        game.batch.draw(tankTex, remoteTank.x, remoteTank.y);
        synchronized (myBullets) {
            for (MultiplayerBullet b : myBullets) {
                game.batch.draw(bulletTex, b.x, b.y);
            }
        }
        synchronized (theirBullets) {
            for (MultiplayerBullet b : theirBullets) {
                game.batch.draw(bulletTex, b.x, b.y);
            }
        }
        font.draw(game.batch, "Your HP: " + localTank.health, 20, 460);
        font.draw(game.batch, "Enemy HP: " + remoteTank.health, 20, 440);
        game.batch.end();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) localTank.y += 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) localTank.y -= 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) localTank.x -= 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) localTank.x += 200 * delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            MultiplayerBullet bullet = new MultiplayerBullet(localTank.x + 16, localTank.y + 32, 0, 300);
            synchronized (myBullets) {
                myBullets.add(bullet);
            }
        }
    }

    private void updateBullets(float delta) {
        // Update my bullets
        synchronized (myBullets) {
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
        }
        // Update their bullets
        synchronized (theirBullets) {
            Iterator<MultiplayerBullet> enemyIt = theirBullets.iterator();
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
    }

    private void checkWinCondition() {
        if (localTank.health <= 0 || remoteTank.health <= 0) {
            game.batch.begin();
            String result = localTank.health <= 0 ? "You Lose!" : "You Win!";
            font.draw(game.batch, result,
                Gdx.graphics.getWidth() / 2f - 40,
                Gdx.graphics.getHeight() / 2f);
            game.batch.end();
        }
    }

    private Rectangle getTankRect(MultiplayerTank tank) {
        return new Rectangle(tank.x, tank.y, 32, 32);
    }

    private Rectangle getBulletRect(MultiplayerBullet bullet) {
        return new Rectangle(bullet.x, bullet.y, 8, 8);
    }

    @Override
    public void dispose() {
        tankTex.dispose();
        bulletTex.dispose();
        font.dispose();
    }
}
