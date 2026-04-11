package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class Food {
    private final int px;
    private final int py;
    private final int type;

    public Food(SnakeGame game) {
        int cols = game.getGridCols();
        int[] rr = game.getPlayfieldRowRange();
        int minRow = rr[0];
        int maxRow = rr[1];

        // Original: px in [1..39], py in [21..62], type in [0..12].
        // Port: constrain to current playfield so food never spawns under the buttons.
        int minX = 1;
        int maxX = Math.max(minX + 1, cols - 1);
        this.px = Utils.parseInt(Utils.random((float) minX, (float) maxX));

        int minY = Math.max(0, minRow);
        int maxY = Math.max(minY + 1, maxRow);
        this.py = Utils.parseInt(Utils.random((float) minY, (float) maxY));

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
}

