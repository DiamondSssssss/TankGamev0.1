package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.Tank;
import com.mygdx.tankgame.UpgradeScreen;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;

public class Level2Screen extends LevelScreen {
    public Level2Screen(TankGame game, Tank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(300, 300, playerTank));
        enemies.add(new EnemyTank(500, 500, playerTank)); // Two enemies
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game,playerTank,2));  // Move to Level 3
    }
}
