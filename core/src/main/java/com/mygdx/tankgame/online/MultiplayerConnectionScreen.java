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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiplayerConnectionScreen extends ScreenAdapter {
    private final TankGame game;
    private BitmapFont font;
    private boolean isHost;
    private boolean isClientConnected = false;
    private boolean gameStarted = false;

    private final ShapeRenderer shapeRenderer;
    private final float buttonX = 100f;
    private final float buttonY = 200f;
    private final float buttonWidth = 200f;
    private final float buttonHeight = 100f;
    private String ipAddress;

    private TankHost tankHost;  // Declare the TankHost to start the server

    public MultiplayerConnectionScreen(TankGame game, boolean isHost, String ipAddress) {
        this.game = game;
        this.isHost = isHost;
        this.ipAddress = ipAddress;
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();

        if (isHost) {
            tankHost = new TankHost(5555);
            tankHost.setConnectionListener((String message) -> {
                // This runs in the server thread, so make sure we safely update LibGDX state
                Gdx.app.postRunnable(() -> {
                    isClientConnected = true;
                    System.out.println("Received message: " + message); // You can log or process the message as needed
                });
            });
        }
    }

    @Override
    public void show() {
        if (isHost) {
            // Host mode
            new Thread(() -> {
                tankHost.startServer();  // Start the server in a new thread
            }).start();
        } else {
            // Client mode
            new Thread(() -> {
                try {
                    Socket socket = new Socket(ipAddress, 5555);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // Read welcome message from server
                    String message = in.readLine();
                    System.out.println("Server: " + message);

                    // Send a greeting message to server
                    out.println("Hello from Client!");

                    // Flag that client is connected
                    isClientConnected = true;

                    // You may want to keep the socket open or store it for gameplay
                    // For now, just keep it connected in the background
                } catch (IOException e) {
                    Gdx.app.postRunnable(() -> {
                        System.out.println("Error in client connection: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        font.getData().setScale(2f);
        font.draw(game.batch, "Host Room", 100, Gdx.graphics.getHeight() - 100);

        font.getData().setScale(1.5f);

        // Slot 1: Host
        font.draw(game.batch, "Host (You)", 100, Gdx.graphics.getHeight() - 180);

        // Slot 2: Client (empty or connected)
        if (isClientConnected) {
            font.draw(game.batch, "Client (Joined)", 100, Gdx.graphics.getHeight() - 220);
        } else {
            font.draw(game.batch, "Client (Waiting...)", 100, Gdx.graphics.getHeight() - 220);
        }

        game.batch.end();

        // Draw "Start Game" button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        // Draw the text on top of the button using GlyphLayout
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "Start Game");

        game.batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);

        // Calculate position to center the text
        float textX = buttonX + (buttonWidth - layout.width) / 2;
        float textY = buttonY + (buttonHeight + layout.height) / 2;

        // Draw the text centered in the button
        font.draw(game.batch, layout, textX, textY);
        game.batch.end();

        // Start Game logic
        if (isHost && isClientConnected) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                // Start the game if both host and client are connected
                gameStarted = true;
                game.setScreen(new MultiplayerScreen(game, true, "localhost"));
            }
        }
    }

    @Override
    public void hide() {
        font.dispose();
    }
}
