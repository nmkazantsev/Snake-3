package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.GameViewModel;
import com.nikitos.GamePageClass;

public final class SnakeRenderer {
    private final SnakeRenderAssets assets;
    private final SnakeFieldRenderer fieldRenderer;
    private final SnakeUiRenderer uiRenderer;
    private boolean assetsReady = false;

    public SnakeRenderer(GamePageClass page, boolean desktopPlatform) {
        this.assets = new SnakeRenderAssets(page);
        this.fieldRenderer = new SnakeFieldRenderer(page, assets);
        this.uiRenderer = desktopPlatform ? new DesktopSnakeUiRenderer(page) : new AndroidSnakeUiRenderer();
    }

    public void onSurfaceChanged(GameViewModel viewModel) {
        assets.onSurfaceChanged(viewModel);
        fieldRenderer.onSurfaceChanged(viewModel);
        uiRenderer.onSurfaceChanged(viewModel, assets);
        assetsReady = true;
    }

    public void render(GameViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        if (!assetsReady) {
            onSurfaceChanged(viewModel);
        }
        fieldRenderer.render(viewModel);
        uiRenderer.render(viewModel, assets);
    }
}
