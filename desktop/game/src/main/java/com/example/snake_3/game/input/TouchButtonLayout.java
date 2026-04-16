package com.example.snake_3.game.input;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.GameMetrics;

public final class TouchButtonLayout {
    public LayoutResult build(GameMetrics metrics, GameConfig config, boolean buttonsRevertedActive) {
        if (!config.touchControlsEnabled) {
            return new LayoutResult(new TouchButtonRect[config.playingUsers][0], 0, Math.max(0, metrics.gridRows - 1));
        }

        TouchButtonRect[][] buttons = new TouchButtonRect[config.playingUsers][4];
        float reservedTopMax = 0f;
        float reservedBottomMin = metrics.y;
        float midY = metrics.y * 0.5f;

        for (int snakeId = 0; snakeId < config.playingUsers; snakeId++) {
            buttons[snakeId] = buildButtonsForSnake(metrics, snakeId, buttonsRevertedActive);
            for (TouchButtonRect button : buttons[snakeId]) {
                if (button.py() < midY) {
                    reservedTopMax = Math.max(reservedTopMax, button.py() + button.height());
                } else {
                    reservedBottomMin = Math.min(reservedBottomMin, button.py());
                }
            }
        }

        int minRow = (int) Math.ceil(reservedTopMax / metrics.sizy);
        int maxRow = (int) Math.floor((reservedBottomMin - metrics.sizy) / metrics.sizy);

        minRow = Math.max(0, Math.min(minRow, metrics.gridRows - 1));
        maxRow = Math.max(0, Math.min(maxRow, metrics.gridRows - 1));
        if (maxRow < minRow) {
            minRow = 0;
            maxRow = Math.max(0, metrics.gridRows - 1);
        }

        return new LayoutResult(buttons, minRow, maxRow);
    }

    private TouchButtonRect[] buildButtonsForSnake(GameMetrics metrics, int snakeId, boolean buttonsRevertedActive) {
        float originY;
        if ((snakeId == 0 && !buttonsRevertedActive) || (snakeId == 1 && buttonsRevertedActive)) {
            originY = metrics.y - (300.0f * metrics.ky);
        } else {
            originY = 5.0f * metrics.ky;
        }

        float fullWidth = metrics.x;
        float tallWidth = fullWidth * 0.25f;
        float wideWidth = fullWidth * 0.50f;
        float wideHeight = metrics.ky * 150.0f;
        float tallHeight = metrics.ky * 300.0f;

        return new TouchButtonRect[]{
                new TouchButtonRect(snakeId, 0, true, tallWidth, originY, wideWidth, wideHeight),
                new TouchButtonRect(snakeId, 1, false, tallWidth + wideWidth, originY, tallWidth, tallHeight),
                new TouchButtonRect(snakeId, 2, true, tallWidth, originY + wideHeight, wideWidth, wideHeight),
                new TouchButtonRect(snakeId, 3, false, 0.0f, originY, tallWidth, tallHeight)
        };
    }

    public record LayoutResult(TouchButtonRect[][] buttons, int playfieldMinRow, int playfieldMaxRow) {
    }
}
