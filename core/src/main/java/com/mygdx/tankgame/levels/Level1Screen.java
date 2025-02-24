package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.EnemyTank;
import com.mygdx.tankgame.LevelScreen;
import com.mygdx.tankgame.TankGame;

public class Level1Screen extends LevelScreen {
    public Level1Screen(TankGame game) {
        super(game);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(400, 400, playerTank)); // One enemy
    }

    @Override
    protected void goToNextLevel() {
        game.setScreen(new Level2Screen(game));  // Move to Level 2
    }
}
