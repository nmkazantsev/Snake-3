package com.example.snake_3.desktop;

import com.nikitos.platform.DesktopLauncher;
import com.nikitos.platformBridge.LauncherParams;
import com.example.snake_3.game.SnakeIntroPage;

public class Main {
    public static void main(String[] args) {
        LauncherParams launcherParams = new LauncherParams()
                .setWindowTitle("Snake-3")
                .setFullScreen(true)
                .setDebug(false)
                .setStartPage(unused -> new SnakeIntroPage());
        DesktopLauncher desktopLauncher = new DesktopLauncher(launcherParams);
        desktopLauncher.run();
    }
}
