package com.example.snake_3.game.app;

import com.example.snake_3.game.core.command.GameCommand;
import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.sim.CollisionSystem;
import com.example.snake_3.game.core.sim.FoodEffectSystem;
import com.example.snake_3.game.core.sim.FoodFactory;
import com.example.snake_3.game.core.sim.GameSimulation;
import com.example.snake_3.game.core.sim.PlayfieldSystem;
import com.example.snake_3.game.core.sim.RoundResetSystem;
import com.example.snake_3.game.core.sim.SnakeMovementSystem;
import com.example.snake_3.game.infra.Clock;
import com.example.snake_3.game.infra.RandomSource;
import com.example.snake_3.game.input.TouchButtonLayout;
import com.example.snake_3.game.input.TouchButtonRect;
import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.GameViewModelMapper;

import java.util.ArrayList;
import java.util.List;

public final class SnakeGameController {
    private final GameConfig config;
    private final Clock clock;
    private final ViewportLayoutAdapter viewportLayoutAdapter;
    private final TouchButtonLayout touchButtonLayout;
    private final PlayfieldSystem playfieldSystem;
    private final GameSimulation simulation;
    private final GameViewModelMapper viewModelMapper;
    private final GameState state;
    private final List<GameCommand> pendingCommands = new ArrayList<>();

    private TouchButtonRect[][] currentButtons = new TouchButtonRect[0][];
    private long lastFrameTimeMs = -1L;
    private long simulationTimeMs = -1L;
    private long accumulatorMs = 0L;
    private GameViewModel currentViewModel;

    public SnakeGameController(boolean desktopPlatform, Clock clock, RandomSource randomSource) {
        this.config = GameConfig.forPlatform(desktopPlatform);
        this.clock = clock;
        this.viewportLayoutAdapter = new ViewportLayoutAdapter();
        this.touchButtonLayout = new TouchButtonLayout();
        this.playfieldSystem = new PlayfieldSystem();

        CollisionSystem collisionSystem = new CollisionSystem();
        FoodFactory foodFactory = new FoodFactory(randomSource, playfieldSystem);
        RoundResetSystem roundResetSystem = new RoundResetSystem(playfieldSystem, foodFactory);
        SnakeMovementSystem snakeMovementSystem = new SnakeMovementSystem(playfieldSystem);
        FoodEffectSystem foodEffectSystem = new FoodEffectSystem(randomSource, playfieldSystem, collisionSystem, foodFactory);
        this.simulation = new GameSimulation(snakeMovementSystem, collisionSystem, foodEffectSystem, roundResetSystem);
        this.viewModelMapper = new GameViewModelMapper();
        this.state = new GameState(config);
    }

    public void onSurfaceChanged(int width, int height) {
        state.metrics = viewportLayoutAdapter.createMetrics(width, height, config);
        updateTouchLayout(clock.nowMillis());
        playfieldSystem.clampEntitiesToPlayfield(state);

        if (!state.roundState.initialized) {
            long nowMs = clock.nowMillis();
            simulation.initialize(state, nowMs);
            simulationTimeMs = nowMs;
            lastFrameTimeMs = nowMs;
        }

        rebuildViewModel(clock.nowMillis());
    }

    public void enqueue(GameCommand command) {
        pendingCommands.add(command);
    }

    public void tickFrame() {
        if (!state.roundState.initialized || state.metrics == null) {
            return;
        }

        long nowMs = clock.nowMillis();
        if (lastFrameTimeMs < 0L) {
            lastFrameTimeMs = nowMs;
        }

        long frameDeltaMs = Math.max(0L, nowMs - lastFrameTimeMs);
        lastFrameTimeMs = nowMs;
        accumulatorMs = Math.min(accumulatorMs + frameDeltaMs, 250L);

        while (accumulatorMs >= config.fixedStepMs) {
            simulationTimeMs += config.fixedStepMs;
            simulation.tick(state, consumePendingCommands(), config.fixedStepMs, simulationTimeMs);
            updateTouchLayout(simulationTimeMs);
            accumulatorMs -= config.fixedStepMs;
        }

        rebuildViewModel(nowMs);
    }

    public GameViewModel getViewModel() {
        return currentViewModel;
    }

    public TouchButtonRect[][] getTouchButtons() {
        return currentButtons;
    }

    public boolean isDesktopPlatform() {
        return config.desktopPlatform;
    }

    private List<GameCommand> consumePendingCommands() {
        List<GameCommand> commands = new ArrayList<>(pendingCommands);
        pendingCommands.clear();
        return commands;
    }

    private void updateTouchLayout(long nowMs) {
        TouchButtonLayout.LayoutResult layoutResult = touchButtonLayout.build(state.metrics, config, state.effectState.isButtonsRevertedActive(nowMs, config));
        currentButtons = layoutResult.buttons();
        state.metrics.playfieldMinRow = layoutResult.playfieldMinRow();
        state.metrics.playfieldMaxRow = layoutResult.playfieldMaxRow();
    }

    private void rebuildViewModel(long renderTimeMs) {
        currentViewModel = viewModelMapper.map(state, currentButtons, renderTimeMs);
    }
}
