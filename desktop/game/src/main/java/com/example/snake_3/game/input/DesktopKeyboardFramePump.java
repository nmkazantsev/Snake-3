package com.example.snake_3.game.input;

import com.nikitos.main.keyboard.KeyboardProcessor;

public final class DesktopKeyboardFramePump {
    private DesktopKeyboardFramePump() {
    }

    public static void flushPendingEvents() {
        KeyboardProcessor.processKeys();
    }
}
