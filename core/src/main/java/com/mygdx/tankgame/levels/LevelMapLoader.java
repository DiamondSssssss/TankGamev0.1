package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.tankgame.buildstuff.Wall2; // ✅ Changed import from Wall to Wall2

import java.util.ArrayList;
import java.util.List;

public class LevelMapLoader {
    public static class MapData {
        public String background;
        public List<Wall2> walls = new ArrayList<>(); // ✅ Changed to Wall2
        public float[] bossPosition = null;
        public List<float[]> enemyPositions = new ArrayList<>();
    }

    public static MapData load(String mapPath) {
        FileHandle file = Gdx.files.internal(mapPath);
        JsonValue root = new JsonReader().parse(file);

        MapData data = new MapData();
        data.background = root.getString("background");

        JsonValue wallArray = root.get("walls");
        for (JsonValue wall : wallArray) {
            Wall2 w = new Wall2( // ✅ Changed to Wall2
                wall.getFloat("x"),
                wall.getFloat("y"),
                wall.getFloat("width"),
                wall.getFloat("height")
            );
            data.walls.add(w);
        }

        JsonValue obstacleArray = root.get("obstacles");
        for (JsonValue obs : obstacleArray) {
            Wall2 w = new Wall2( // ✅ Changed to Wall2
                obs.getFloat("x"),
                obs.getFloat("y"),
                obs.getFloat("width"),
                obs.getFloat("height")
            );
            data.walls.add(w);
        }

        JsonValue enemyArray = root.get("enemies");
        if (enemyArray != null) {
            for (JsonValue enemy : enemyArray) {
                float[] pos = new float[] {
                    enemy.getFloat("x"),
                    enemy.getFloat("y")
                };
                data.enemyPositions.add(pos);
            }
        }

        JsonValue boss = root.get("boss");
        if (boss != null) {
            data.bossPosition = new float[] {
                boss.getFloat("x"),
                boss.getFloat("y")
            };
        }

        return data;
    }
}
