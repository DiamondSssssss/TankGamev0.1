package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.UpgradeScreen;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;

public class Level2Screen extends LevelScreen {
    public Level2Screen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        // Load level 2 map data
        LevelMapLoader.MapData mapData = LevelMapLoader.load("map_level2.json");

        // Set background
        backgroundTexture = new Texture(Gdx.files.internal(mapData.background));

        // Add all walls and obstacles
        for (Wall wall : mapData.walls) {
            addWall(wall);
        }

        // Add enemies
        for (float[] pos : mapData.enemyPositions) {
            enemies.add(new EnemyTank(pos[0], pos[1], playerTank, bullets));
        }
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game, playerTank, 2)); // Go to Level 3
    }
}
