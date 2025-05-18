package com.mygdx.tankgame.online;

import java.io.*;
import java.net.*;

public class TankClient {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // Replace with the host's IP address if running on LAN (localhost for testing)
            socket = new Socket("localhost", 5555);
            System.out.println("Connected to the host!");

            try {
                // Input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read and print the server's message
                String serverMessage = in.readLine();
                if (serverMessage != null) {
                    System.out.println("Server: " + serverMessage);
                } else {
                    System.out.println("Server did not send any message.");
                }

                // Send a message back to the server
                out.println("Hello, Server!");
            } catch (IOException e) {
                System.out.println("I/O Error during communication: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the connection and resources in the finally block to ensure they get closed
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
