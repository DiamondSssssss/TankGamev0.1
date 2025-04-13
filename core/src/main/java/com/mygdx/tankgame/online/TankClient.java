package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5555); // Change IP for LAN

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String userText;
        while ((userText = userInput.readLine()) != null) {
            out.println(userText);
            System.out.println("Server: " + in.readLine());
        }

        socket.close();
    }
}
