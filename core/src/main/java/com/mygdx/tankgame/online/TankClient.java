package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankClient {
    public static void main(String[] args) {
        try {
            // Replace with the host's IP address if running on LAN (localhost for testing)
            Socket socket = new Socket("localhost", 5555);
            System.out.println("Connected to the host!");

            // Input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Read and print the server's message
            String serverMessage = in.readLine();
            System.out.println("Server: " + serverMessage);

            // Send a message back to the server
            out.println("Hello, Server!");

            // Close the connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
