package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class NetworkHandler {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public NetworkHandler(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error during network initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        try {
            out.println(msg);
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("Error receiving message: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
