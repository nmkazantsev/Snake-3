package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class Explosion {
    private final float px;
    private final float py;
    private final long startTime;
    private float size = 0.0f;

    public Explosion(float px, float py) {
        this.px = px;
        this.py = py;
        this.startTime = Utils.millis();
    }

    public boolean checkFinished() {
        return Utils.millis() - startTime > 4000L;
    }

    public void updateSize(SnakeGame game) {
        size = ((game.getSizx() * 15.0f) * ((float) (Utils.millis() - startTime))) / 4000.0f;
    }

    public boolean checkHitbox(SnakeGame game, float cellX, float cellY) {
        float dx = (px * game.getSizx()) - ((game.getSizx() * cellX) + (game.getSizx() / 2.0f));
        float dy = (py * game.getSizy()) - ((game.getSizy() * cellY) + (game.getSizy() / 2.0f));
        return Utils.sqrt(Utils.sq(dx) + Utils.sq(dy)) < size;
    }

    public float getPx() {
        return px;
    }

    public float getPy() {
        return py;
    }

    public long getStartTime() {
        return startTime;
    }

    public float getSize() {
        return size;
    }
}

