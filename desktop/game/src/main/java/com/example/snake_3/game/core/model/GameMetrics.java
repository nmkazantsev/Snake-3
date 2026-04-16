package com.example.snake_3.game.core.model;

public final class GameMetrics {
    public final float x;
    public final float y;
    public final float kx;
    public final float ky;
    public final float sizx;
    public final float sizy;
    public final int gridCols;
    public final int gridRows;
    public final int minPlayableCol;
    public final int maxPlayableCol;
    public int playfieldMinRow;
    public int playfieldMaxRow;

    public GameMetrics(
            float x,
            float y,
            float kx,
            float ky,
            float sizx,
            float sizy,
            int gridCols,
            int gridRows,
            int minPlayableCol,
            int maxPlayableCol
    ) {
        this.x = x;
        this.y = y;
        this.kx = kx;
        this.ky = ky;
        this.sizx = sizx;
        this.sizy = sizy;
        this.gridCols = gridCols;
        this.gridRows = gridRows;
        this.minPlayableCol = minPlayableCol;
        this.maxPlayableCol = maxPlayableCol;
        this.playfieldMinRow = 0;
        this.playfieldMaxRow = Math.max(0, gridRows - 1);
    }
}
