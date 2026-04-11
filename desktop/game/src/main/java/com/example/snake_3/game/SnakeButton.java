package com.example.snake_3.game;

public final class SnakeButton {
    private float px;
    private float py;
    private float sizx;
    private float sizy;

    public boolean checkTouch(float tx, float ty) {
        return tx > px && tx < px + sizx && ty > py && ty < py + sizy;
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

