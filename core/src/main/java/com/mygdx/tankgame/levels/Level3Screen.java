package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.VictoryScreen;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.playertank.PlayerTank;

public class Level3Screen extends LevelScreen {
    public Level3Screen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        // Load map data from JSON
        LevelMapLoader.MapData mapData = LevelMapLoader.load("map_level3.json");

        // Set background
        backgroundTexture = new Texture(Gdx.files.internal(mapData.background));

        // Add all walls and obstacles
        for (Wall wall : mapData.walls) {
            addWall(wall);
        }

        // Spawn boss (if bossPosition is set)
        if (mapData.bossPosition != null) {
            float[] pos = mapData.bossPosition;
            enemies.add(new BossTank(pos[0], pos[1], playerTank, game, bullets));
        }
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game)); // End of game
    }
}
