package com.mygdx.tankgame.levels;

import com.mygdx.tankgame.playertank.PlayerTank;
import com.mygdx.tankgame.enemies.BossTank;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.VictoryScreen;

public class Level3Screen extends LevelScreen {
    public Level3Screen(TankGame game, PlayerTank playerTank) {
        super(game, playerTank);
    }

    @Override
    protected void setupLevel() {
        enemies.add(new BossTank(500, 500, playerTank,game,bullets));
        //enemies.add(new EnemyTank(300, 300, playerTank,bullets));
    }


    @Override
    protected void goToUpgradeScreen() {
        game.setScreen(new VictoryScreen(game));  // Move to Level 3
    }
}
