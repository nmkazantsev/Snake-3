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
        return new FoodState(
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
}
