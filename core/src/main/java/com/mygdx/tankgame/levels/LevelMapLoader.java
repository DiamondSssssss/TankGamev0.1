package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.buildstuff.Wall;

import java.util.ArrayList;
import java.util.List;

public class LevelMapLoader {
    public static class MapData {
        public String background;
        public List<Wall> walls = new ArrayList<>();
    }

    public static MapData load(String mapPath) {
        FileHandle file = Gdx.files.internal(mapPath);
        JsonValue root = new JsonReader().parse(file);

        MapData data = new MapData();
        data.background = root.getString("background");

        JsonValue wallArray = root.get("walls");
        for (JsonValue wall : wallArray) {
            Wall w = new Wall(
                wall.getFloat("x"),
                wall.getFloat("y"),
                wall.getFloat("width"),
                wall.getFloat("height")
            );
            data.walls.add(w);
        }

        JsonValue obstacleArray = root.get("obstacles");
        for (JsonValue obs : obstacleArray) {
            Wall w = new Wall(
                obs.getFloat("x"),
                obs.getFloat("y"),
                obs.getFloat("width"),
                obs.getFloat("height")
            );
            data.walls.add(w);
        }

        return data;
    }
}
