package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class Mine {
    private long explosionTime = 0L;
    private final int px;
    private final int py;
    private final long setTime;
    private final int id;

    public Mine(int px, int py, int id) {
        this.px = px;
        this.py = py;
        this.id = id;
        this.setTime = Utils.millis();
    }

    public boolean checkMine(SnakeGame game, int cx, int cy) {
        if (Utils.abs(cx - px) > 1 || Utils.abs(cy - py) > 1) return false;
        explosionTime = Utils.millis();
        return true;
    }

    public boolean shouldDelete(SnakeGame game) {
        return explosionTime > 0 && (Utils.millis() - explosionTime > 1000);
    }

    public int getPx() {
        return px;
    }

    public int getPy() {
        return py;
    }

    public int getId() {
        return id;
    }

    public long getExplosionTime() {
        return explosionTime;
    }

    public long getSetTime() {
        return setTime;
    }
}

