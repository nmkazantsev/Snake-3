package com.example.snake_3.game.core.config;

public final class GameConfig {
    public final boolean desktopPlatform;
    public final boolean touchControlsEnabled;
    public final int maxSquares;
    public final int playingUsers;
    public final int allocatedSnakes;
    public final int initialFoodCount;
    public final int maxMineCount;
    public final int initialSegmentsToAdd;
    public final int maxSegments;
    public final float defaultSpawnRowRatio;
    public final float initialSpeed;
    public final float minSpeed;
    public final long reverseTimeMs;
    public final long buttonsRevertedTimeMs;
    public final long roundResetDelayMs;
    public final long explosionDurationMs;
    public final long mineExplosionDurationMs;
    public final long fixedStepMs;

    private GameConfig(boolean desktopPlatform, int maxSquares) {
        this.desktopPlatform = desktopPlatform;
        this.touchControlsEnabled = !desktopPlatform;
        this.maxSquares = maxSquares;
        this.playingUsers = 2;
        this.allocatedSnakes = 4;
        this.initialFoodCount = 2;
        this.maxMineCount = 50;
        this.initialSegmentsToAdd = 5;
        this.maxSegments = 500;
        this.defaultSpawnRowRatio = 0.28f;
        this.initialSpeed = 5.0f;
        this.minSpeed = 3.0f;
        this.reverseTimeMs = 10_000L;
        this.buttonsRevertedTimeMs = 5_000L;
        this.roundResetDelayMs = 1_000L;
        this.explosionDurationMs = 4_000L;
        this.mineExplosionDurationMs = 1_000L;
        this.fixedStepMs = 1;
    }

    public static GameConfig forPlatform(boolean desktopPlatform) {
        return new GameConfig(desktopPlatform, desktopPlatform ? 80 : 40);
    }
}
