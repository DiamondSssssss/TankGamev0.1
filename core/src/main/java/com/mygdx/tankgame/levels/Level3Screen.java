package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.VictoryScreen;

public class Level3Screen extends LevelScreen {
    public Level3Screen(TankGame game) {
        super(game);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(200, 200, playerTank));
        enemies.add(new EnemyTank(600, 600, playerTank));
        enemies.add(new EnemyTank(400, 400, playerTank)); // Three enemies
    }

    @Override
    protected void goToNextLevel() {
        game.setScreen(new VictoryScreen(game));
    }
}
