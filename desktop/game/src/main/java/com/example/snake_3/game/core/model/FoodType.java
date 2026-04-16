package com.example.snake_3.game.core.model;

public enum FoodType {
    GROW_SELF(0),
    GROW_OTHERS(1),
    SHRINK_SELF(2),
    SPEED_UP(3),
    SLOW_DOWN(4),
    SWAP_HEADS(5),
    SWAP_BODIES(6),
    DROP_MINE(7),
    CREATE_EXPLOSION(8),
    TELEPORT_BOOST(9),
    REVERSE_CONTROLS(10),
    CHANGE_FOOD_COUNT(11),
    SWAP_CONTROLS(12);

    public final int code;

    FoodType(int code) {
        this.code = code;
    }

    public static FoodType fromCode(int code) {
        for (FoodType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return GROW_SELF;
    }
}
