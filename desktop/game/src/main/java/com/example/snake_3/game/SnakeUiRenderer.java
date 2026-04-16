package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.GameViewModel;

interface SnakeUiRenderer {
    void onSurfaceChanged(GameViewModel viewModel, SnakeRenderAssets assets);

    void render(GameViewModel viewModel, SnakeRenderAssets assets);
}
