package com.example.snake_3.game.core.model;

public final class SegmentState {
    public int px;
    public int py;

    public SegmentState(int px, int py) {
        this.px = px;
        this.py = py;
    }

    public SegmentState copy() {
        return new SegmentState(px, py);
    }
}
