package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.UpgradeScreen;
import com.mygdx.tankgame.buildstuff.Wall;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.playertank.PlayerTank;

public class Level1Screen extends LevelScreen {
    public Level1Screen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        // Load map data from JSON
        LevelMapLoader.MapData mapData = LevelMapLoader.load("map_level1.json");

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal(mapData.background));

        // Add all walls and obstacles
        for (Wall2 wall : mapData.walls) {
            addWall(wall);
        }
        for (float[] pos : mapData.enemyPositions) {
            enemies.add(new ChaserTank(pos[0], pos[1], playerTank, bullets));
        }

    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game, playerTank, 1));
    }
}
