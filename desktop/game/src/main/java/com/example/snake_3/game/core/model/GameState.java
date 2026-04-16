package com.example.snake_3.game.core.model;

import com.example.snake_3.game.core.config.GameConfig;

public final class GameState {
    public final GameConfig config;
    public GameMetrics metrics;
    public final RoundState roundState;
    public final EffectState effectState;
    public final SnakeState[] snakes;
    public FoodState[] foods;
    public final MineState[] mines;
    public int mineCount;
    public long simulationTimeMs;

    public GameState(GameConfig config) {
        this.config = config;
        this.roundState = new RoundState();
        this.effectState = new EffectState();
        this.snakes = new SnakeState[config.allocatedSnakes];
        for (int index = 0; index < snakes.length; index++) {
            snakes[index] = new SnakeState(index, index == 1, config.maxSegments);
        }
        this.foods = new FoodState[config.initialFoodCount];
        this.mines = new MineState[config.maxMineCount];
        this.mineCount = 0;
        this.simulationTimeMs = 0L;
    }
}
