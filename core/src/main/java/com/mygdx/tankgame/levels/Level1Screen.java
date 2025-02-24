package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.Tank;
import com.mygdx.tankgame.UpgradeScreen;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;

public class Level1Screen extends LevelScreen {
    public Level1Screen(TankGame game, Tank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(400, 400, playerTank)); // One enemy
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game, playerTank, 1)); // Go to UpgradeScreen before Level 2
    }
}
