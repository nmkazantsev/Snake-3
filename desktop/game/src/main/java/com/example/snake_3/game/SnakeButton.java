package com.example.snake_3.game;

public final class SnakeButton {
    private float px;
    private float py;
    private float sizx;
    private float sizy;

    public boolean checkTouch(float tx, float ty) {
        // Processing sketch used strict comparisons, but in the engine port float math + adjacent buttons
        // can create thin "dead" seams. Be inclusive (with a tiny epsilon) so borders remain clickable.
        final float eps = 0.0001f;
        return tx >= px - eps && tx <= px + sizx + eps && ty >= py - eps && ty <= py + sizy + eps;
    }

    public float getPx() {
        return px;
    }

    public float getPy() {
        return py;
    }

    public float getSizx() {
        return sizx;
    }

    public float getSizy() {
        return sizy;
    }

    public void setPx(float px) {
        this.px = px;
    }

    public void setPy(float py) {
        this.py = py;
    }

    public void setSizx(float sizx) {
        this.sizx = sizx;
    }

    public void setSizy(float sizy) {
        this.sizy = sizy;
    }
}
