package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.Tank;
import com.mygdx.tankgame.enemies.ChaserTank;
import com.mygdx.tankgame.enemies.EliteEnemyTank;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.VictoryScreen;

public class Level3Screen extends LevelScreen {
    public Level3Screen(TankGame game, Tank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new ChaserTank(500, 500, playerTank));
        enemies.add(new ChaserTank(500, 200, playerTank));
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game));  // Move to Level 3
    }
}
