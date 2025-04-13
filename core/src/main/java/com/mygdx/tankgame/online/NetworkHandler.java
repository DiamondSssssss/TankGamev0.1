package com.mygdx.tankgame.online;
import java.io.*;
import java.net.*;

public class NetworkHandler {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public NetworkHandler(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        out.println(msg);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        socket.close();
    }
}
