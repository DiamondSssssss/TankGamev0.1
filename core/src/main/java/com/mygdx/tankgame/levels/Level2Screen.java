package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.UpgradeScreen;
import com.mygdx.tankgame.enemies.EnemyTank;
import com.mygdx.tankgame.TankGame;

public class Level2Screen extends LevelScreen {
    public Level2Screen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new EnemyTank(300, 300, playerTank,bullets));
        enemies.add(new EnemyTank(500, 500, playerTank,bullets)); // Two enemies
    }

    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new UpgradeScreen(game,playerTank,2));  // Move to Level 3
    }
}
