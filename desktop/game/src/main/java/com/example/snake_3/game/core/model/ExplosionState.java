package com.example.snake_3.game.core.model;

public final class ExplosionState {
    public final float px;
    public final float py;
    public final long startTimeMs;

    public ExplosionState(float px, float py, long startTimeMs) {
        this.px = px;
        this.py = py;
        this.startTimeMs = startTimeMs;
    }
}
