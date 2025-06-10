package com.mygdx.tankgame.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.Screen.UpgradeScreen;
import com.mygdx.tankgame.buildstuff.Wall2;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EliteEnemyTank;
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

        // Add enemies based on type
        for (LevelMapLoader.EnemyData enemy : mapData.enemyData) {
            switch (enemy.type) {
                case "ChaserTank":
                    enemies.add(new ChaserTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                case "EliteEnemyTank":
                    enemies.add(new EliteEnemyTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                case "EnemyTank":
                    enemies.add(new EnemyTank(enemy.x, enemy.y, playerTank, bullets));
                    break;
                default:
                    Gdx.app.log("ERROR", "Unknown enemy type: " + enemy.type);
                    enemies.add(new ChaserTank(enemy.x, enemy.y, playerTank, bullets)); // Fallback
                    break;
            }
        }
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game, playerTank, 1));
    }
}
