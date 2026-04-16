package com.example.snake_3.game.core.model;

public final class SnakeState {
    public final int id;
    public final boolean buttonsInverted;
    public int chosenDirection;
    public int score;
    public boolean died;
    public float speed;
    public long moveAccumulatorMs;
    public final SegmentState[] segments;
    public int length;
    public ExplosionState explosion;

    public SnakeState(int id, boolean buttonsInverted, int maxSegments) {
        this.id = id;
        this.buttonsInverted = buttonsInverted;
        this.chosenDirection = 0;
        this.score = 0;
        this.died = false;
        this.speed = 0.0f;
        this.moveAccumulatorMs = 0L;
        this.segments = new SegmentState[maxSegments];
        this.length = 0;
        this.explosion = null;
    }
}
