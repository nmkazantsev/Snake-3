package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.model.ExplosionState;
import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.FoodType;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.MineState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;
import com.example.snake_3.game.infra.RandomSource;

public final class FoodEffectSystem {
    private final RandomSource random;
    private final PlayfieldSystem playfieldSystem;
    private final CollisionSystem collisionSystem;
    private final FoodFactory foodFactory;

    public FoodEffectSystem(RandomSource random, PlayfieldSystem playfieldSystem, CollisionSystem collisionSystem, FoodFactory foodFactory) {
        this.random = random;
        this.playfieldSystem = playfieldSystem;
        this.collisionSystem = collisionSystem;
        this.foodFactory = foodFactory;
    }

    public void applyFoodAtHead(GameState state, SnakeState snake, long nowMs) {
        if (snake == null || snake.length <= 0 || snake.segments[0] == null) {
            return;
        }
        SegmentState head = snake.segments[0];
        FoodState[] originalFoods = state.foods;
        for (int foodIndex = 0; foodIndex < originalFoods.length; foodIndex++) {
            FoodState food = originalFoods[foodIndex];
            if (food == null || head.px != food.px || head.py != food.py) {
                continue;
            }

            applyEffect(state, snake, food.type, nowMs);
            if (state.foods == originalFoods && foodIndex >= 0 && foodIndex < state.foods.length) {
                state.foods[foodIndex] = null;
            }
        }
    }

    public void refillMissingFoods(GameState state) {
        foodFactory.refillMissing(state);
    }

    private void applyEffect(GameState state, SnakeState snake, FoodType foodType, long nowMs) {
        switch (foodType) {
            case GROW_SELF -> addSegments(snake, random.nextTruncatedInt(2.0f, 6.0f));
            case GROW_OTHERS -> addSegmentsToOthers(state, snake, random.nextTruncatedInt(8.0f, 18.0f));
            case SHRINK_SELF -> deleteSegments(snake, random.nextTruncatedInt(2.0f, 4.0f));
            case SPEED_UP -> snake.speed += random.nextFloat(1.0f, 5.0f);
            case SLOW_DOWN -> snake.speed = Math.max(state.config.minSpeed, snake.speed - random.nextFloat(1.0f, 5.0f));
            case SWAP_HEADS -> swapHeads(state, snake);
            case SWAP_BODIES -> swapBodies(state, snake);
            case DROP_MINE -> dropMine(state, snake, nowMs);
            case CREATE_EXPLOSION -> snake.explosion = new ExplosionState(snake.segments[0].px + 0.5f, snake.segments[0].py + 0.5f, nowMs);
            case TELEPORT_BOOST -> teleportBoost(state, snake);
            case REVERSE_CONTROLS -> {
                state.effectState.controlsReversed = true;
                state.effectState.reverseStartedAtMs = nowMs;
            }
            case CHANGE_FOOD_COUNT -> changeFoodCount(state);
            case SWAP_CONTROLS -> state.effectState.buttonsRevertedAtMs = nowMs;
        }
    }

    private void addSegmentsToOthers(GameState state, SnakeState ownerSnake, int count) {
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState snake = state.snakes[snakeId];
            if (snake != null && snake.id != ownerSnake.id) {
                addSegments(snake, count);
            }
        }
    }

    private void addSegments(SnakeState snake, int count) {
        if (snake == null || snake.length <= 0 || snake.segments[snake.length - 1] == null) {
            return;
        }
        int safeCount = Math.max(0, Math.min(count, snake.segments.length - snake.length));
        for (int iteration = 0; iteration < safeCount; iteration++) {
            SegmentState tail = snake.segments[snake.length - 1];
            snake.segments[snake.length + iteration] = new SegmentState(tail.px, tail.py);
        }
        snake.length += safeCount;
    }

    private void deleteSegments(SnakeState snake, int count) {
        if (snake == null || snake.length <= 1) {
            return;
        }
        if (snake.length - count < 1) {
            count = snake.length - 1;
        }
        for (int index = snake.length - 1; index > snake.length - count; index--) {
            snake.segments[index] = null;
        }
        snake.length = Math.max(1, snake.length - count);
    }

    private void swapHeads(GameState state, SnakeState snake) {
        SnakeState otherSnake = getOpponentSnake(state, snake);
        if (otherSnake == null || snake.segments[0] == null || otherSnake.segments[0] == null) {
            return;
        }
        int previousDirection = snake.chosenDirection;
        snake.chosenDirection = otherSnake.chosenDirection;
        otherSnake.chosenDirection = previousDirection;

        int previousX = snake.segments[0].px;
        int previousY = snake.segments[0].py;
        snake.segments[0].px = otherSnake.segments[0].px;
        snake.segments[0].py = otherSnake.segments[0].py;
        otherSnake.segments[0].px = previousX;
        otherSnake.segments[0].py = previousY;
    }

    private void swapBodies(GameState state, SnakeState snake) {
        SnakeState otherSnake = getOpponentSnake(state, snake);
        if (otherSnake == null) {
            return;
        }
        int previousDirection = snake.chosenDirection;
        snake.chosenDirection = otherSnake.chosenDirection;
        otherSnake.chosenDirection = previousDirection;

        int limit = Math.min(snake.length, otherSnake.length);
        for (int index = 0; index < limit; index++) {
            if (snake.segments[index] == null || otherSnake.segments[index] == null) {
                break;
            }
            int temporaryX = snake.segments[index].px;
            int temporaryY = snake.segments[index].py;
            snake.segments[index].px = otherSnake.segments[index].px;
            snake.segments[index].py = otherSnake.segments[index].py;
            otherSnake.segments[index].px = temporaryX;
            otherSnake.segments[index].py = temporaryY;
        }
    }

    private void dropMine(GameState state, SnakeState snake, long nowMs) {
        if (state.mineCount < state.config.maxMineCount - 1) {
            state.mines[state.mineCount] = new MineState(state.mineCount, snake.segments[0].px, snake.segments[0].py, nowMs);
            state.mineCount++;
        }

        int attempts = 0;
        do {
            snake.segments[0].px = playfieldSystem.getRandomPlayableCol(state.metrics, random);
            snake.segments[0].py = playfieldSystem.getRandomPlayableRow(state.metrics, random);
            attempts++;
        } while (collisionSystem.headConflictsWithOtherSnake(state, snake) && attempts < 64);
    }

    private void teleportBoost(GameState state, SnakeState snake) {
        snake.segments[0].px = playfieldSystem.getRandomPlayableCol(state.metrics, random);
        snake.segments[0].py = playfieldSystem.getRandomPlayableRow(state.metrics, random);
        addSegments(snake, 20);
        snake.speed += 2.0f;
    }

    private void changeFoodCount(GameState state) {
        int delta = random.nextTruncatedInt(-2.0f, 2.0f);
        int newLength = Math.max(1, state.foods.length + delta);
        FoodState[] newFoods = new FoodState[newLength];
        for (int index = 0; index < newFoods.length; index++) {
            newFoods[index] = foodFactory.create(state);
        }
        state.foods = newFoods;
    }

    private SnakeState getOpponentSnake(GameState state, SnakeState snake) {
        if (state.config.playingUsers <= 1) {
            return null;
        }
        int opponentId = (snake.id + 1) % state.config.playingUsers;
        return state.snakes[opponentId];
    }
}
