package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankHost {
    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ConnectionListener listener;

    public TankHost(int port) {
        this.port = port;
    }

    // Start the server in a new thread
    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started, waiting for client...");

                // Wait for client to connect
                clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Send a welcome message to the client
                out.println("Welcome to the game!");

                // Start reading client messages
                listenToClient();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start(); // Run server logic on a background thread
    }

    private void listenToClient() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from client: " + message);
                if (listener != null) {
                    listener.onClientMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send message to client
    public void sendToClient(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // Set the listener to notify when messages are received
    public void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }

    // Interface for connection listener
    public interface ConnectionListener {
        void onClientMessage(String message);
    }
}
