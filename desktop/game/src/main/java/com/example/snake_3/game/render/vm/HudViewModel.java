package com.example.snake_3.game.render.vm;

public record HudViewModel(
        String scoreText,
        boolean controlsReversed,
        boolean buttonsReverted,
        boolean resetting,
        boolean[] winners
) {
}
