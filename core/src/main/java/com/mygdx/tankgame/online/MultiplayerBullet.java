package com.mygdx.tankgame.online;

public class MultiplayerBullet {
    public float x, y;
    public float vx, vy;
    public boolean isAlive = true;

    public MultiplayerBullet(float x, float y, float vx, float vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void update(float delta) {
        x += vx * delta;
        y += vy * delta;
    }

    public String serialize() {
        return x + "," + y + "," + vx + "," + vy;
    }

    public static MultiplayerBullet deserialize(String data) {
        String[] parts = data.split(",");
        return new MultiplayerBullet(
            Float.parseFloat(parts[0]),
            Float.parseFloat(parts[1]),
            Float.parseFloat(parts[2]),
            Float.parseFloat(parts[3])
        );
    }
}
