// TankClient.java
package com.mygdx.tankgame.online;

import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * UDP-based client.
 * Thread 1: receiveLoop() — blocking receive
 * Thread 2: sendLoop() — periodic sends (e.g., handshake or updates)
 */
public class TankClient {
    private final InetSocketAddress hostAddress;
    private DatagramSocket socket;
    private ConnectionListener listener;
    private volatile boolean running = true;

    public TankClient(String hostIp, int port) throws Exception {
        this.hostAddress = new InetSocketAddress(InetAddress.getByName(hostIp), port);
        this.socket = new DatagramSocket();
    }

    /**
     * Start both receive and send threads.
     */
    public void startClient() {
        // Thread 1: receive
        new Thread(this::receiveLoop, "Client-Receive").start();
        // Thread 2: send
        new Thread(this::sendLoop, "Client-Send").start();
    }

    private void receiveLoop() {
        byte[] buf = new byte[512];
        try {
            while (running) {
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                socket.receive(pkt);
                String msg = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.UTF_8);
                System.out.println("[Client] Received: " + msg);
                if (listener != null) listener.onHostMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLoop() {
        try {
            while (running) {
                String hello = "ClientHello: " + System.currentTimeMillis();
                sendToHost(hello);
                Thread.sleep(100); // 10Hz
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendToHost(String message) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket pkt = new DatagramPacket(
            data, data.length,
            hostAddress.getAddress(), hostAddress.getPort()
        );
        try {
            socket.send(pkt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }

    public interface ConnectionListener {
        void onHostMessage(String message);
    }
}
