package com.example.snake_3.game.render.vm;

import java.util.List;

public record GameViewModel(
        float x,
        float y,
        float kx,
        float ky,
        float sizx,
        float sizy,
        int gridCols,
        int gridRows,
        long currentTimeMs,
        boolean desktopPlatform,
        List<SnakeViewModel> snakes,
        List<FoodViewModel> foods,
        List<MineViewModel> mines,
        List<ButtonViewModel> buttons,
        HudViewModel hud
) {
}
