package com.example.snake_3.game.infra;

public interface RandomSource {
    float nextFloat(float minInclusive, float maxExclusive);

    default int nextTruncatedInt(float minInclusive, float maxExclusive) {
        return (int) nextFloat(minInclusive, maxExclusive);
    }
}
