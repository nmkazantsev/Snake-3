package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class Food {
    private int px;
    private int py;
    private final int type;

    public Food(SnakeGame game) {
        this.px = game.getRandomPlayableCol();
        this.py = game.getRandomPlayableRow();
        this.type = Utils.parseInt(Utils.random(0.0f, 13.0f)); // [0..12]
    }

    public int getPx() {
        return px;
    }

    public int getPy() {
        return py;
    }

    public int getType() {
        return type;
    }

    public void setPx(int px) {
        this.px = px;
    }

    public void setPy(int py) {
        this.py = py;
    }
}
