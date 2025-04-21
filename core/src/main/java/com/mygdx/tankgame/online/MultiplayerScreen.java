package com.mygdx.tankgame.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.tankgame.TankGame;

public class MultiplayerScreen extends ScreenAdapter {
    private TankGame game;
    private MultiplayerTank localTank, remoteTank;
    private NetworkHandler net;
    private BitmapFont font;
    private boolean isHost;

    public MultiplayerScreen(TankGame game, boolean isHost, String ip) {
        this.game = game;
        this.isHost = isHost;
        font = new BitmapFont();
        localTank = new MultiplayerTank(100, 100);
        remoteTank = new MultiplayerTank(200, 200);

        if (isHost) {
            //TankHost.startInBackground(5555); // Start server in background
            try {
                Thread.sleep(300); // Wait briefly for server to initialize
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            net = new NetworkHandler(ip, 5555); // Connect as client
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void render(float delta) {
        handleInput();

        try {
            // Send data of the local tank
            net.send(localTank.serialize());

            // Receive remote tank's position data
            String data = net.receive();
            if (data != null) {
                remoteTank.updateFromNetwork(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        // Draw tank positions as text (you can use images if you want)
        font.draw(game.batch, "You: (" + localTank.x + "," + localTank.y + ")", 20, 480);
        font.draw(game.batch, "Enemy: (" + remoteTank.x + "," + remoteTank.y + ")", 20, 460);

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            localTank.x += 1;
        }
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
