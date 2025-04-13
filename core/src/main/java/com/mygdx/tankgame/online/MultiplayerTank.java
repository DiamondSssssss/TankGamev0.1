package com.mygdx.tankgame.online;


public class MultiplayerTank {
    public float x, y;

    public MultiplayerTank(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void updateFromNetwork(String data) {
        String[] parts = data.split(",");
        x = Float.parseFloat(parts[0]);
        y = Float.parseFloat(parts[1]);
    }

    public String serialize() {
        return x + "," + y;
    }
}
