package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.command.GameCommand;
import com.example.snake_3.game.core.command.TurnSnakeCommand;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.SnakeState;

import java.util.List;

public final class GameSimulation {
    private final SnakeMovementSystem snakeMovementSystem;
    private final CollisionSystem collisionSystem;
    private final FoodEffectSystem foodEffectSystem;
    private final RoundResetSystem roundResetSystem;

    public GameSimulation(
            SnakeMovementSystem snakeMovementSystem,
            CollisionSystem collisionSystem,
            FoodEffectSystem foodEffectSystem,
            RoundResetSystem roundResetSystem
    ) {
        this.snakeMovementSystem = snakeMovementSystem;
        this.collisionSystem = collisionSystem;
        this.foodEffectSystem = foodEffectSystem;
        this.roundResetSystem = roundResetSystem;
    }

    public void initialize(GameState state, long nowMs) {
        roundResetSystem.initialize(state, nowMs);
    }

    public void tick(GameState state, List<GameCommand> commands, long deltaMs, long nowMs) {
        state.simulationTimeMs = nowMs;
        applyCommands(state, commands, nowMs);
        expireTimedEffects(state, nowMs);
        expireExplosions(state, nowMs);

        roundResetSystem.update(state, nowMs);
        if (!state.roundState.resetting) {
            updateSnakes(state, deltaMs, nowMs);
        }

        deleteExpiredMines(state, nowMs);
        foodEffectSystem.refillMissingFoods(state);
    }

    private void applyCommands(GameState state, List<GameCommand> commands, long nowMs) {
        boolean buttonsRevertedActive = state.effectState.isButtonsRevertedActive(nowMs, state.config);
        for (GameCommand command : commands) {
            if (!(command instanceof TurnSnakeCommand turnSnakeCommand)) {
                continue;
            }
            int targetSnakeId = turnSnakeCommand.snakeId();
            if (buttonsRevertedActive) {
                targetSnakeId = (targetSnakeId + 1) % state.config.playingUsers;
            }
            SnakeState targetSnake = state.snakes[targetSnakeId];
            snakeMovementSystem.applyButtonPress(targetSnake, turnSnakeCommand.buttonIndex());
        }
    }

    private void updateSnakes(GameState state, long deltaMs, long nowMs) {
        boolean controlsReversed = state.effectState.isControlsReversedActive(nowMs, state.config);
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            if (snake.died) {
                continue;
            }
            int steps = snakeMovementSystem.consumeMovementSteps(snake, deltaMs, state.config);
            for (int step = 0; step < steps; step++) {
                snakeMovementSystem.moveOneStep(state, snake, controlsReversed);
                foodEffectSystem.applyFoodAtHead(state, snake, nowMs);
                if (collisionSystem.checkSnakeDeath(state, snake, nowMs)) {
                    snake.died = true;
                    break;
                }
            }
        }
    }

    private void expireTimedEffects(GameState state, long nowMs) {
        if (state.effectState.controlsReversed && !state.effectState.isControlsReversedActive(nowMs, state.config)) {
            state.effectState.controlsReversed = false;
            state.effectState.reverseStartedAtMs = 0L;
        }
        if (state.effectState.buttonsRevertedAtMs > 0L && !state.effectState.isButtonsRevertedActive(nowMs, state.config)) {
            state.effectState.buttonsRevertedAtMs = 0L;
        }
    }

    private void expireExplosions(GameState state, long nowMs) {
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            if (snake.explosion != null && nowMs - snake.explosion.startTimeMs > state.config.explosionDurationMs) {
                snake.explosion = null;
            }
        }
    }

    private void deleteExpiredMines(GameState state, long nowMs) {
        for (int mineIndex = 0; mineIndex < state.mineCount; mineIndex++) {
            if (state.mines[mineIndex] == null) {
                continue;
            }
            long explosionTime = state.mines[mineIndex].explosionTimeMs;
            if (explosionTime <= 0L || nowMs - explosionTime <= state.config.mineExplosionDurationMs) {
                continue;
            }
            deleteMine(state, mineIndex);
            mineIndex--;
        }
    }

    private void deleteMine(GameState state, int mineIndex) {
        for (int index = mineIndex; index < state.mineCount - 1; index++) {
            state.mines[index] = state.mines[index + 1];
            if (state.mines[index] != null) {
                state.mines[index].id = index;
            }
        }
        state.mines[state.mineCount - 1] = null;
        state.mineCount--;
    }
}
