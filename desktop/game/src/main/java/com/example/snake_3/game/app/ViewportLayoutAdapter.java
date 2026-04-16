package com.example.snake_3.game.app;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.GameMetrics;

public final class ViewportLayoutAdapter {
    public GameMetrics createMetrics(int width, int height, GameConfig config) {
        float x = Math.max(1f, width);
        float y = Math.max(1f, height);
        float ky = y / 1280.0f;
        float kx = x / 720.0f;
        float sizx = x / Math.max(1.0f, (float) config.maxSquares);
        float sizy = sizx;
        int gridCols = Math.max(1, (int) (x / sizx));
        int gridRows = Math.max(1, (int) (y / sizy));
        int minPlayableCol = gridCols > 1 ? 1 : 0;
        int maxPlayableCol = Math.max(minPlayableCol, gridCols - 1);
        return new GameMetrics(x, y, kx, ky, sizx, sizy, gridCols, gridRows, minPlayableCol, maxPlayableCol);
    }
}
