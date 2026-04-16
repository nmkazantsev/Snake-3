package com.example.snake_3.game.input;

public record TouchButtonRect(int snakeId, int buttonIndex, boolean wide, float px, float py, float width, float height) {
    public boolean contains(float touchX, float touchY) {
        float epsilon = 0.0001f;
        return touchX >= px - epsilon
                && touchX <= px + width + epsilon
                && touchY >= py - epsilon
                && touchY <= py + height + epsilon;
    }
}
