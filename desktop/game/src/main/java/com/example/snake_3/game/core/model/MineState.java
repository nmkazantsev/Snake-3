package com.example.snake_3.game.core.model;

public final class MineState {
    public int id;
    public int px;
    public int py;
    public final long setTimeMs;
    public long explosionTimeMs;

    public MineState(int id, int px, int py, long setTimeMs) {
        this.id = id;
        this.px = px;
        this.py = py;
        this.setTimeMs = setTimeMs;
        this.explosionTimeMs = 0L;
    }
}
