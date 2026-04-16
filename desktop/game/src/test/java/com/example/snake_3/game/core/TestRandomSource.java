package com.example.snake_3.game.core;

import com.example.snake_3.game.infra.RandomSource;

public final class TestRandomSource implements RandomSource {
    private float[] values;
    private int index = 0;

    public TestRandomSource(float... values) {
        this.values = values;
    }

    public void reset(float... values) {
        this.values = values;
        this.index = 0;
    }

    @Override
    public float nextFloat(float minInclusive, float maxExclusive) {
        float normalized = values.length == 0 ? 0.5f : values[Math.min(index, values.length - 1)];
        index++;
        normalized = Math.max(0.0f, Math.min(0.9999f, normalized));
        return minInclusive + ((maxExclusive - minInclusive) * normalized);
    }
}
