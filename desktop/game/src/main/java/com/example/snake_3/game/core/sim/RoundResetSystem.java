package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;

public final class RoundResetSystem {
    private final PlayfieldSystem playfieldSystem;
    private final FoodFactory foodFactory;

    public RoundResetSystem(PlayfieldSystem playfieldSystem, FoodFactory foodFactory) {
        this.playfieldSystem = playfieldSystem;
        this.foodFactory = foodFactory;
    }

    public void initialize(GameState state, long nowMs) {
        resetSnakes(state, false);
        fillFoods(state);
        state.mineCount = 0;
        state.effectState.controlsReversed = false;
        state.effectState.reverseStartedAtMs = 0L;
        state.effectState.buttonsRevertedAtMs = 0L;
        state.roundState.initialized = true;
        state.roundState.resetting = false;
        state.roundState.resetStartedAtMs = 0L;
        state.simulationTimeMs = nowMs;
    }

    public void update(GameState state, long nowMs) {
        if (countAlive(state) < 2 && !state.roundState.resetting) {
            state.roundState.resetting = true;
            state.roundState.resetStartedAtMs = nowMs;
        }
        if (!state.roundState.resetting) {
            return;
        }
        if (nowMs - state.roundState.resetStartedAtMs <= state.config.roundResetDelayMs) {
            return;
        }
        state.roundState.resetting = false;
        resetRound(state);
    }

    private void resetRound(GameState state) {
        resetSnakes(state, true);
        fillFoods(state);
        state.effectState.controlsReversed = false;
        state.effectState.reverseStartedAtMs = 0L;
    }

    private void resetSnakes(GameState state, boolean awardScore) {
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            if (awardScore && !snake.died) {
                snake.score++;
            }

            snake.chosenDirection = snake.id == 0 ? 2 : 0;
            snake.died = false;
            snake.speed = state.config.initialSpeed;
            snake.moveAccumulatorMs = 0L;
            snake.explosion = null;
            snake.length = 1;
            snake.segments[0] = createSpawnHead(state, snake.id);
            for (int index = 1; index < snake.segments.length; index++) {
                snake.segments[index] = null;
            }
            for (int index = 0; index < state.config.initialSegmentsToAdd; index++) {
                SegmentState tail = snake.segments[snake.length - 1];
                snake.segments[snake.length + index] = new SegmentState(tail.px, tail.py);
            }
            snake.length += state.config.initialSegmentsToAdd;
        }
    }

    private SegmentState createSpawnHead(GameState state, int snakeId) {
        int headX = snakeId == 0
                ? playfieldSystem.getLeftSpawnCol(state.metrics)
                : playfieldSystem.getRightSpawnCol(state.metrics);
        int headY = playfieldSystem.getDefaultSpawnRow(state.metrics, state.config);
        return new SegmentState(headX, headY);
    }

    private int countAlive(GameState state) {
        int alive = 0;
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            if (!state.snakes[snakeId].died) {
                alive++;
            }
        }
        return alive;
    }

    private void fillFoods(GameState state) {
        FoodState[] foods = new FoodState[state.foods.length == 0 ? state.config.initialFoodCount : state.foods.length];
        state.foods = foods;
        for (int index = 0; index < state.foods.length; index++) {
            state.foods[index] = foodFactory.create(state);
        }
    }
}
