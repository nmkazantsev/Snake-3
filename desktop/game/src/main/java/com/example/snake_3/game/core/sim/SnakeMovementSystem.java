package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;

public final class SnakeMovementSystem {
    private final PlayfieldSystem playfieldSystem;

    public SnakeMovementSystem(PlayfieldSystem playfieldSystem) {
        this.playfieldSystem = playfieldSystem;
    }

    public void applyButtonPress(SnakeState snake, int buttonIndex) {
        if (snake.buttonsInverted) {
            if (Math.abs(buttonIndex - snake.chosenDirection) != 2) {
                snake.chosenDirection = buttonIndex;
            }
            if (buttonIndex == 0 && snake.chosenDirection != 0) {
                snake.chosenDirection = 2;
            }
            if (buttonIndex == 2 && snake.chosenDirection != 2) {
                snake.chosenDirection = 0;
            }
            return;
        }

        if (Math.abs(buttonIndex - snake.chosenDirection) != 2) {
            snake.chosenDirection = buttonIndex;
        }
    }

    public int consumeMovementSteps(SnakeState snake, long deltaMs, GameConfig config) {
        snake.moveAccumulatorMs += deltaMs;
        float intervalMs = 1000.0f / Math.max(config.minSpeed, snake.speed);
        int steps = 0;
        while (snake.moveAccumulatorMs >= intervalMs) {
            snake.moveAccumulatorMs -= (long) intervalMs;
            steps++;
        }
        return steps;
    }

    public void moveOneStep(GameState state, SnakeState snake, boolean controlsReversed) {
        int direction = snake.chosenDirection;
        if (controlsReversed) {
            direction = (direction + 2) % 4;
        }

        for (int index = snake.length - 1; index > 0; index--) {
            snake.segments[index].px = snake.segments[index - 1].px;
            snake.segments[index].py = snake.segments[index - 1].py;
        }

        SegmentState head = snake.segments[0];
        if (direction == 0) {
            head.py -= 1;
        } else if (direction == 1) {
            head.px += 1;
        } else if (direction == 2) {
            head.py += 1;
        } else if (direction == 3) {
            head.px -= 1;
        }

        head.px = playfieldSystem.wrapColumn(head.px, state.metrics);
        head.py = playfieldSystem.wrapRow(head.py, state.metrics);
    }
}
