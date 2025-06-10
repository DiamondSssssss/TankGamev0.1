package com.mygdx.tankgame.buildstuff;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameAssets {
    public static final AssetManager manager = new AssetManager();

    public static void load() {
        // Load textures
        manager.load("textures/tank.png", Texture.class);
        manager.load("textures/bullet.png", Texture.class);
        manager.load("textures/explosion.png", Texture.class);
        manager.load("textures/background.jpg", Texture.class);
        manager.load("textures/button_play.png", Texture.class);
        manager.load("textures/button_exit.png", Texture.class);

//        // Load sounds
//        manager.load("sounds/explosion.wav", Sound.class);
//        manager.load("sounds/shoot.wav", Sound.class);
//        manager.load("sounds/click.wav", Sound.class);
//
//        // Load music
//        manager.load("music/menu_music.mp3", Music.class);
//        manager.load("music/game_music.mp3", Music.class);

        // Load fonts
        manager.load("fonts/game_font.fnt", BitmapFont.class);
    }

    public static void dispose() {
        manager.dispose();
    }
}
