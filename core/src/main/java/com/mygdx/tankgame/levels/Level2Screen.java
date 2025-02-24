package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;

public class Level2Screen extends LevelScreen {
    public Level2Screen(TankGame game) {
        super(game);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(300, 300, playerTank));
        enemies.add(new EnemyTank(500, 500, playerTank)); // Two enemies
    }

    @Override
    protected void goToNextLevel() {
        game.setScreen(new Level3Screen(game));  // Move to Level 3
    }
}
