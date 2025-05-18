package com.mygdx.tankgame.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.tankgame.TankGame;

public class MultiplayerConnectionScreen extends ScreenAdapter {
    private final TankGame game;
    private BitmapFont font;
    private boolean isHost;
    private boolean isClientConnected = false;
    private boolean gameStarted = false;

    private final ShapeRenderer shapeRenderer;
    private final float buttonX = 100f;
    private final float buttonY = 200f;
    private final float buttonWidth = 250f;
    private final float buttonHeight = 100f;

    private String ipAddress;

    private TankHost tankHost;
    private TankClient tankClient;

    public MultiplayerConnectionScreen(TankGame game, boolean isHost, String ipAddress) {
        this.game = game;
        this.isHost = isHost;
        this.ipAddress = ipAddress;
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        if (isHost) {
            tankHost = new TankHost(5555);
            tankHost.setConnectionListener((String message) -> {
                Gdx.app.postRunnable(() -> {
                    isClientConnected = true;
                    System.out.println("[Host] Client message: " + message);
                });
            });
            new Thread(tankHost::startServer).start();
        } else {
            try {
                tankClient = new TankClient(ipAddress, 5555);
                tankClient.setConnectionListener((String message) -> {
                    Gdx.app.postRunnable(() -> {
                        System.out.println("[Client] Host message: " + message);
                        isClientConnected = true; // Not strictly needed, but if host replies, we know it's alive
                    });
                });
                tankClient.startClient(); // Sends "ClientHello"
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        font.getData().setScale(2f);
        font.draw(game.batch, isHost ? "Hosting Room" : "Joining Room", 100, Gdx.graphics.getHeight() - 100);

        font.getData().setScale(1.5f);
        if (isHost) {
            font.draw(game.batch, "You are the Host", 100, Gdx.graphics.getHeight() - 180);
            font.draw(game.batch, isClientConnected ? "Client Connected!" : "Waiting for Client...", 100, Gdx.graphics.getHeight() - 220);
        } else {
            font.draw(game.batch, "You are the Client", 100, Gdx.graphics.getHeight() - 180);
            font.draw(game.batch, isClientConnected ? "Connected to Host!" : "Connecting to Host...", 100, Gdx.graphics.getHeight() - 220);
        }

        game.batch.end();

        // Only host sees "Start Game" button
        if (isHost && isClientConnected) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
            shapeRenderer.end();

            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, "Start Game");

            game.batch.begin();
            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);

            float textX = buttonX + (buttonWidth - layout.width) / 2;
            float textY = buttonY + (buttonHeight + layout.height) / 2;

            font.draw(game.batch, layout, textX, textY);
            game.batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                gameStarted = true;
                game.setScreen(new MultiplayerScreen(game, true, "localhost")); // Host always uses localhost
            }
        }
    }

    @Override
    public void hide() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
