package com.example.snake_3.game;

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

    public void onSurfaceChanged(SnakeGame game) {
        // Surface resize/recreate: rebuild size-dependent GPU assets, but do not touch gameplay state.
        assets.onSurfaceChanged(game);
        fieldRenderer.onSurfaceChanged(game);
        uiRenderer.onSurfaceChanged(game, assets);
        assetsReady = true;
    }

    public void render(SnakeGame game) {
        // Lazy init fallback (should usually be triggered from MainRenderer.onSurfaceChanged()).
        if (!assetsReady) {
            assets.onSurfaceChanged(game);
            fieldRenderer.onSurfaceChanged(game);
            uiRenderer.onSurfaceChanged(game, assets);
            assetsReady = true;
        }

        fieldRenderer.render(game);
        uiRenderer.render(game, assets);
    }
}
