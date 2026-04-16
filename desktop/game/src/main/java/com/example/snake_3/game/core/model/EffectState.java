package com.example.snake_3.game.core.model;

import com.example.snake_3.game.core.config.GameConfig;

public final class EffectState {
    public boolean controlsReversed;
    public long reverseStartedAtMs;
    public long buttonsRevertedAtMs;

    public boolean isControlsReversedActive(long nowMs, GameConfig config) {
        return controlsReversed && nowMs - reverseStartedAtMs <= config.reverseTimeMs;
    }

    public boolean isButtonsRevertedActive(long nowMs, GameConfig config) {
        return buttonsRevertedAtMs > 0L && nowMs - buttonsRevertedAtMs <= config.buttonsRevertedTimeMs;
    }
}
