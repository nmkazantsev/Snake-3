package com.example.snake_3.game.core.model;

public final class FoodState {
    public int px;
    public int py;
    public FoodType type;

    public FoodState(int px, int py, FoodType type) {
        this.px = px;
        this.py = py;
        this.type = type;
    }
}
