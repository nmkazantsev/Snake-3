package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.FoodType;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.infra.RandomSource;

public final class FoodFactory {
    private final RandomSource random;
    private final PlayfieldSystem playfieldSystem;

    public FoodFactory(RandomSource random, PlayfieldSystem playfieldSystem) {
        this.random = random;
        this.playfieldSystem = playfieldSystem;
    }

    public FoodState create(GameState state) {
        int maxAttempts = Math.max(1, state.metrics.gridCols * state.metrics.gridRows * 2);
        FoodState fallback = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            FoodState candidate = new FoodState(
                    playfieldSystem.getRandomPlayableCol(state.metrics, random),
                    playfieldSystem.getRandomPlayableRow(state.metrics, random),
                    FoodType.fromCode(random.nextTruncatedInt(0.0f, 13.0f))
            );
            if (fallback == null) {
                fallback = candidate;
            }
            if (!isOccupiedByOtherFood(state, candidate)) {
                return candidate;
            }
        }

        return fallback != null
                ? fallback
                : new FoodState(
                playfieldSystem.getRandomPlayableCol(state.metrics, random),
                playfieldSystem.getRandomPlayableRow(state.metrics, random),
                FoodType.fromCode(random.nextTruncatedInt(0.0f, 13.0f))
        );
    }

    public void refillMissing(GameState state) {
        for (int index = 0; index < state.foods.length; index++) {
            if (state.foods[index] == null) {
                state.foods[index] = create(state);
            }
        }
    }

    private boolean isOccupiedByOtherFood(GameState state, FoodState candidate) {
        for (FoodState existingFood : state.foods) {
            if (existingFood == null) {
                continue;
            }
            if (existingFood.px == candidate.px && existingFood.py == candidate.py) {
                return true;
            }
        }
        return false;
    }
}
