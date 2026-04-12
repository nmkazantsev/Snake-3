package com.example.snake_3.game;

interface SnakeUiRenderer {
    void onSurfaceChanged(SnakeGame game, SnakeRenderAssets assets);

    void render(SnakeGame game, SnakeRenderAssets assets);
}
