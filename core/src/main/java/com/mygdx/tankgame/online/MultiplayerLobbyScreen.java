package com.mygdx.tankgame.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Input;
import com.mygdx.tankgame.TankGame;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MultiplayerLobbyScreen extends ScreenAdapter {
    private final TankGame game;
    private BitmapFont font;
    private String ipAddress;

    {
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    ;
    private boolean typingIp = false;

    private int screenWidth;
    private int screenHeight;

    private TankHost tankHost;

    public MultiplayerLobbyScreen(TankGame game) {
        this.game = game;
        font = new BitmapFont();
        tankHost = new TankHost(5555); // Initialize the server with a specific port
    }

    @Override
    public void show() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        font.getData().setScale(2f);
        font.draw(game.batch, "Multiplayer Lobby", 100, screenHeight - 100);

        font.getData().setScale(1.5f);
        font.draw(game.batch, "[H] Host Game", 100, screenHeight - 180);
        font.draw(game.batch, "[J] Join Game", 100, screenHeight - 220);
        font.draw(game.batch, "IP Address: " + ipAddress + (typingIp ? "|" : ""), 100, screenHeight - 260);
        font.draw(game.batch, "(Press Enter to finish typing IP)", 100, screenHeight - 290);

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            // Host Game, start the server when "H" is pressed
            game.setScreen(new MultiplayerConnectionScreen(game, true, "localhost"));  // Move to connection screen
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            // Join Game, allow typing IP address
            typingIp = true;
        }

        if (typingIp) {
            for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    ipAddress += (char) (key + 32); // lowercase
                }
            }

            for (int key = Input.Keys.NUM_0; key <= Input.Keys.NUM_9; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    ipAddress += (char) (key + 48 - Input.Keys.NUM_0);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
                ipAddress += ".";
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                if (ipAddress.length() > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                typingIp = false;
                // After typing, pass the IP address to the MultiplayerConnectionScreen
                game.setScreen(new MultiplayerConnectionScreen(game, false, ipAddress));
            }
        }
    }

    @Override
    public void hide() {
        font.dispose();
    }
}
