package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class SnakeSegment {
    private int px;
    private int py;
    private final int bright; // [0..99]

    public SnakeSegment(int px, int py) {
        this.px = px;
        this.py = py;
        this.bright = Utils.parseInt(Utils.random(0.0f, 100.0f));
    }

    public int getPx() {
        return px;
    }

    public int getPy() {
        return py;
    }

    public int getBright() {
        return bright;
    }

    public void setPx(int px) {
        this.px = px;
    }

    public void setPy(int py) {
        this.py = py;
    }
}

