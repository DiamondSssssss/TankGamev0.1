package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.tankgame.buildstuff.Wall2;

import java.util.ArrayList;
import java.util.List;

public class LevelMapLoader {
    public static class EnemyData {
        public String type;
        public float x;
        public float y;

        public EnemyData(String type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public static class MapData {
        public String background;
        public List<Wall2> walls = new ArrayList<>();
        public EnemyData bossData = null;
        public List<EnemyData> enemyData = new ArrayList<>();
    }

    public static MapData load(String mapPath) {
        FileHandle file = Gdx.files.internal(mapPath);
        JsonValue root = new JsonReader().parse(file);

        MapData data = new MapData();
        data.background = root.getString("background");

        // Load walls
        JsonValue wallArray = root.get("walls");
        for (JsonValue wall : wallArray) {
            Wall2 w = new Wall2(
                wall.getFloat("x"),
                wall.getFloat("y"),
                wall.getFloat("width"),
                wall.getFloat("height")
            );
            data.walls.add(w);
        }

        // Load obstacles as walls
        JsonValue obstacleArray = root.get("obstacles");
        for (JsonValue obs : obstacleArray) {
            Wall2 w = new Wall2(
                obs.getFloat("x"),
                obs.getFloat("y"),
                obs.getFloat("width"),
                obs.getFloat("height")
            );
            data.walls.add(w);
        }

        // Load enemies with type and validate position
        JsonValue enemyArray = root.get("enemies");
        if (enemyArray != null) {
            for (JsonValue enemy : enemyArray) {
                String type = enemy.getString("type", "EnemyTank"); // Default to EnemyTank
                float x = enemy.getFloat("x");
                float y = enemy.getFloat("y");
                float width = type.equals("BossTank") ? 128f : 64f; // Assume BossTank is larger
                float height = type.equals("BossTank") ? 128f : 64f;
                float[] validPos = findValidPosition(x, y, width, height, data.walls);
                if (validPos != null) {
                    data.enemyData.add(new EnemyData(type, validPos[0], validPos[1]));
                } else {
                    Gdx.app.log("WARNING", "Enemy " + type + " at (" + x + "," + y + ") is too close to walls");
                }
            }
        }

        // Load boss
        JsonValue boss = root.get("boss");
        if (boss != null) {
            String type = boss.getString("type", "BossTank");
            float x = boss.getFloat("x");
            float y = boss.getFloat("y");
            float[] validPos = findValidPosition(x, y, 128f, 128f, data.walls); // BossTank size
            if (validPos != null) {
                data.bossData = new EnemyData(type, validPos[0], validPos[1]);
            } else {
                Gdx.app.log("WARNING", "Boss " + type + " at (" + x + "," + y + ") is too close to walls");
            }
        }

        return data;
    }

    private static float[] findValidPosition(float x, float y, float width, float height, List<Wall2> walls) {
        float buffer = 10f; // Buffer distance from walls
        // Create enemy rectangle with buffer
        Rectangle enemyRect = new Rectangle(x + buffer / 2, y + buffer / 2, width - buffer, height - buffer);

        // Ensure within level bounds (1280x720 minus walls and buffer)
        if (x < 50 + buffer || x + width > 1230 - buffer || y < 50 + buffer || y + height > 670 - buffer) {
            return null;
        }

        // Check if position is valid (no wall collisions)
        if (!checkCollision(enemyRect, walls)) {
            return new float[]{x, y};
        }

        // Position is too close to walls
        return null;
    }

    private static boolean checkCollision(Rectangle enemyRect, List<Wall2> walls) {
        for (Wall2 wall : walls) {
            if (wall.getBoundingRectangle().overlaps(enemyRect)) {
                return true;
            }
        }
        return false;
    }
}
