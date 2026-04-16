package com.example.snake_3.game.infra;

import com.nikitos.utils.Utils;

public final class UtilsRandomSource implements RandomSource {
    @Override
    public float nextFloat(float minInclusive, float maxExclusive) {
        return Utils.random(minInclusive, maxExclusive);
    }
}
