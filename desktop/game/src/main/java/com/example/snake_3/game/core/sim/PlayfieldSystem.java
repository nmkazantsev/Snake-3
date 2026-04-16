package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.GameMetrics;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.MineState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;
import com.example.snake_3.game.infra.RandomSource;

public final class PlayfieldSystem {
    public int getDefaultSpawnRow(GameMetrics metrics, GameConfig config) {
        int playableRows = Math.max(0, metrics.playfieldMaxRow - metrics.playfieldMinRow);
        return metrics.playfieldMinRow + Math.round(playableRows * config.defaultSpawnRowRatio);
    }

    public int getLeftSpawnCol(GameMetrics metrics) {
        return Math.min(metrics.minPlayableCol + 1, metrics.maxPlayableCol);
    }

    public int getRightSpawnCol(GameMetrics metrics) {
        return metrics.maxPlayableCol;
    }

    public int getRandomPlayableCol(GameMetrics metrics, RandomSource random) {
        if (metrics.maxPlayableCol <= metrics.minPlayableCol) {
            return metrics.minPlayableCol;
        }
        return random.nextTruncatedInt(metrics.minPlayableCol, metrics.maxPlayableCol + 1);
    }

    public int getRandomPlayableRow(GameMetrics metrics, RandomSource random) {
        if (metrics.playfieldMaxRow <= metrics.playfieldMinRow) {
            return metrics.playfieldMinRow;
        }
        return random.nextTruncatedInt(metrics.playfieldMinRow, metrics.playfieldMaxRow + 1);
    }

    public void clampEntitiesToPlayfield(GameState state) {
        GameMetrics metrics = state.metrics;
        if (metrics == null) {
            return;
        }

        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            for (int index = 0; index < snake.length; index++) {
                SegmentState segment = snake.segments[index];
                if (segment == null) {
                    continue;
                }
                segment.px = wrapColumn(segment.px, metrics);
                segment.py = wrapRow(segment.py, metrics);
            }
        }

        for (FoodState food : state.foods) {
            if (food == null) {
                continue;
            }
            food.px = wrapColumn(food.px, metrics);
            food.py = clampRow(food.py, metrics);
        }

        for (int index = 0; index < state.mineCount; index++) {
            MineState mine = state.mines[index];
            if (mine == null) {
                continue;
            }
            mine.px = wrapColumn(mine.px, metrics);
            mine.py = clampRow(mine.py, metrics);
        }
    }

    public int wrapColumn(int column, GameMetrics metrics) {
        if (column < 0) {
            return metrics.gridCols - 1;
        }
        if (column >= metrics.gridCols) {
            return 0;
        }
        return column;
    }

    public int wrapRow(int row, GameMetrics metrics) {
        if (row < metrics.playfieldMinRow) {
            return metrics.playfieldMaxRow;
        }
        if (row > metrics.playfieldMaxRow) {
            return metrics.playfieldMinRow;
        }
        return row;
    }

    public int clampRow(int row, GameMetrics metrics) {
        if (row < metrics.playfieldMinRow) {
            return metrics.playfieldMinRow;
        }
        if (row > metrics.playfieldMaxRow) {
            return metrics.playfieldMaxRow;
        }
        return row;
    }
}
