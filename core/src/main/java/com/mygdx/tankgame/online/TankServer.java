package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5555);
        System.out.println("Server started, waiting for client...");

        Socket socket = serverSocket.accept();
        System.out.println("Client connected!");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Client: " + inputLine);
            // Echo back (can replace with logic)
            out.println(inputLine);
        }

        socket.close();
        serverSocket.close();
    }
}
