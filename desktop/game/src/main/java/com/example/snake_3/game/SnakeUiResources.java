package com.example.snake_3.game;

import com.nikitos.main.images.PFont;
import com.nikitos.main.images.PImage;

final class SnakeUiResources {
    static final String UI_FONT_ASSET = "win_font.otf";

    private static PFont sharedUiFont;
    private static boolean fontLoadAttempted;

    private SnakeUiResources() {
    }

    static synchronized PFont getSharedUiFont() {
        if (!fontLoadAttempted) {
            fontLoadAttempted = true;
            try {
                sharedUiFont = PFont.fromAsset(UI_FONT_ASSET);
            } catch (RuntimeException ignored) {
                sharedUiFont = null;
            }
        }
        return sharedUiFont;
    }

    static void applyUiFont(PImage image) {
        if (image == null) {
            return;
        }
        PFont font = getSharedUiFont();
        if (font != null && font.isLoaded()) {
            image.setFont(font);
        }
    }
}
