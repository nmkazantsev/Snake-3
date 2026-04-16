package com.example.snake_3.game.render.vm;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.MineState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;
import com.example.snake_3.game.input.TouchButtonRect;

import java.util.ArrayList;
import java.util.List;

public final class GameViewModelMapper {
    public GameViewModel map(GameState state, TouchButtonRect[][] touchButtons, long renderTimeMs) {
        GameConfig config = state.config;
        List<SnakeViewModel> snakes = new ArrayList<>();
        List<FoodViewModel> foods = new ArrayList<>();
        List<MineViewModel> mines = new ArrayList<>();
        List<ButtonViewModel> buttons = new ArrayList<>();

        for (int snakeId = 0; snakeId < config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            List<SegmentViewModel> segments = new ArrayList<>(snake.length);
            for (int segmentIndex = 0; segmentIndex < snake.length; segmentIndex++) {
                SegmentState segment = snake.segments[segmentIndex];
                if (segment != null) {
                    segments.add(new SegmentViewModel(snakeId, segmentIndex, segment.px, segment.py));
                }
            }
            ExplosionViewModel explosion = snake.explosion == null
                    ? null
                    : new ExplosionViewModel(snake.explosion.px, snake.explosion.py, snake.explosion.startTimeMs);
            snakes.add(new SnakeViewModel(snakeId, snake.score, snake.died, segments, explosion));
        }

        for (FoodState food : state.foods) {
            if (food != null) {
                foods.add(new FoodViewModel(food.px, food.py, food.type));
            }
        }

        for (int index = 0; index < state.mineCount; index++) {
            MineState mine = state.mines[index];
            if (mine != null) {
                mines.add(new MineViewModel(mine.id, mine.px, mine.py, mine.explosionTimeMs));
            }
        }

        if (!config.desktopPlatform && touchButtons != null) {
            for (int snakeId = 0; snakeId < Math.min(touchButtons.length, config.playingUsers); snakeId++) {
                for (TouchButtonRect button : touchButtons[snakeId]) {
                    buttons.add(new ButtonViewModel(button.snakeId(), button.buttonIndex(), button.wide(), button.px(), button.py(), button.width(), button.height()));
                }
            }
        }

        boolean[] winners = new boolean[config.playingUsers];
        if (state.roundState.resetting) {
            for (int snakeId = 0; snakeId < config.playingUsers; snakeId++) {
                winners[snakeId] = !state.snakes[snakeId].died;
            }
        }

        boolean controlsReversed = state.effectState.isControlsReversedActive(renderTimeMs, config);
        boolean buttonsReverted = state.effectState.isButtonsRevertedActive(renderTimeMs, config);
        HudViewModel hud = new HudViewModel(
                state.snakes[1].score + ":" + state.snakes[0].score,
                controlsReversed,
                buttonsReverted,
                state.roundState.resetting,
                winners
        );

        return new GameViewModel(
                state.metrics.x,
                state.metrics.y,
                state.metrics.kx,
                state.metrics.ky,
                state.metrics.sizx,
                state.metrics.sizy,
                state.metrics.gridCols,
                state.metrics.gridRows,
                renderTimeMs,
                config.desktopPlatform,
                snakes,
                foods,
                mines,
                buttons,
                hud
        );
    }
}
