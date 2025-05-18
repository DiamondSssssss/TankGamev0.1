// TankHost.java
package com.mygdx.tankgame.online;

import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * UDP-based host service.
 * Thread 1: Server startup (invoked externally)
 * Thread 2: receiveLoop() — blocking receive
 * Thread 3: broadcastLoop() — periodic broadcasts
 */
public class TankHost {
    private final int port;
    private DatagramSocket socket;
    private InetSocketAddress clientAddress;
    private ConnectionListener listener;
    private volatile boolean running = true;

    public TankHost(int port) {
        this.port = port;
    }

    /**
     * Call once to start all host threads.
     */
    public void startServer() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("[Host] Listening on UDP port " + port);
            // Thread 2: receive
            new Thread(this::receiveLoop, "Host-Receive").start();
            // Thread 3: broadcast
            new Thread(this::broadcastLoop, "Host-Broadcast").start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveLoop() {
        byte[] buf = new byte[512];
        try {
            while (running) {
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                socket.receive(pkt);
                String msg = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.UTF_8);
                // Learn client address
                if (clientAddress == null) {
                    clientAddress = new InetSocketAddress(pkt.getAddress(), pkt.getPort());
                    System.out.println("[Host] Client connected from " + clientAddress);
                }
                System.out.println("[Host] Received: " + msg);
                if (listener != null) listener.onClientMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastLoop() {
        try {
            while (running) {
                if (clientAddress != null) {
                    String payload = "HostBroadcast: " + System.currentTimeMillis();
                    sendToClient(payload);
                }
                Thread.sleep(100); // 10Hz
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendToClient(String message) {
        if (socket == null || clientAddress == null) return;
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket pkt = new DatagramPacket(
            data, data.length,
            clientAddress.getAddress(), clientAddress.getPort()
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
        void onClientMessage(String message);
    }
}
