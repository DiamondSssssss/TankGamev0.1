package com.mygdx.tankgame.online;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.tankgame.TankGame;


public class MultiplayerScreen extends ScreenAdapter {
    private TankGame game;
    private MultiplayerTank localTank, remoteTank;
    private NetworkHandler net;

    public MultiplayerScreen(TankGame game, String ip) {
        this.game = game;
        try {
            net = new NetworkHandler(ip, 5555);
        } catch (Exception e) {
            e.printStackTrace();
        }
        localTank = new MultiplayerTank(100, 100);
        remoteTank = new MultiplayerTank(200, 200);
    }

    @Override
    public void render(float delta) {
        handleInput();

        try {
            net.send(localTank.serialize());
            String data = net.receive();
            if (data != null) {
                remoteTank.updateFromNetwork(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        // draw your tanks here using game.batch.draw(texture, x, y)
        game.font.draw(game.batch, "You: (" + localTank.x + "," + localTank.y + ")", 20, 480);
        game.font.draw(game.batch, "Enemy: (" + remoteTank.x + "," + remoteTank.y + ")", 20, 460);
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            localTank.x += 1;
        }
    }
}
