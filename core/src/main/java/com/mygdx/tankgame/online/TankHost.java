package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankHost {
    private int port;
    private ClientConnectListener connectionListener;

    public TankHost(int port) {
        this.port = port;
    }

    public void setConnectionListener(ClientConnectListener listener) {
        this.connectionListener = listener;
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started, waiting for client...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Notify GUI thread via listener
            if (connectionListener != null) {
                connectionListener.onClientConnected();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Server Started, Client Connected!");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                out.println("Server: " + inputLine);
            }

            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
