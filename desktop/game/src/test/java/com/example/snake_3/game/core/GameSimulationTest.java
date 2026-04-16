package com.example.snake_3.game.core;

import com.example.snake_3.game.app.ViewportLayoutAdapter;
import com.example.snake_3.game.core.command.TurnSnakeCommand;
import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.FoodState;
import com.example.snake_3.game.core.model.FoodType;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;
import com.example.snake_3.game.core.sim.CollisionSystem;
import com.example.snake_3.game.core.sim.FoodEffectSystem;
import com.example.snake_3.game.core.sim.FoodFactory;
import com.example.snake_3.game.core.sim.GameSimulation;
import com.example.snake_3.game.core.sim.PlayfieldSystem;
import com.example.snake_3.game.core.sim.RoundResetSystem;
import com.example.snake_3.game.core.sim.SnakeMovementSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSimulationTest {
    @Test
    void initializeCreatesTwoFoodsAndInitialSpeed() {
        Harness harness = createHarness();

        assertEquals(2, harness.state.foods.length);
        assertNotNull(harness.state.foods[0]);
        assertNotNull(harness.state.foods[1]);
        assertFalse(
                harness.state.foods[0].px == harness.state.foods[1].px
                        && harness.state.foods[0].py == harness.state.foods[1].py
        );
        assertEquals(20.0f, harness.state.snakes[0].speed);
        assertEquals(20.0f, harness.state.snakes[1].speed);
    }

    @Test
    void foodFactoryAvoidsSpawningOnExistingFoodCell() {
        Harness harness = createHarness();
        harness.state.foods = new FoodState[]{
                new FoodState(10, 10, FoodType.GROW_SELF),
                null
        };
        harness.random.reset(
                normalizedForCol(harness, 10), normalizedForRow(harness, 10), 0.0f,
                normalizedForCol(harness, 12), normalizedForRow(harness, 14), 0.0f
        );

        harness.foodFactory.refillMissing(harness.state);

        assertNotNull(harness.state.foods[1]);
        assertEquals(12, harness.state.foods[1].px);
        assertEquals(14, harness.state.foods[1].py);
    }

    @Test
    void movementUsesFixedStepInsteadOfFrameRate() {
        Harness harness = createHarness();
        SnakeState snake = harness.state.snakes[0];
        int startY = snake.segments[0].py;

        harness.simulation.tick(harness.state, List.of(), 16L, 16L);
        harness.simulation.tick(harness.state, List.of(), 16L, 32L);
        harness.simulation.tick(harness.state, List.of(), 16L, 48L);
        assertEquals(startY, snake.segments[0].py);

        harness.simulation.tick(harness.state, List.of(), 16L, 64L);
        assertEquals(startY + 1, snake.segments[0].py);
    }

    @Test
    void foodTypeGrowSelfAddsSegments() {
        Harness harness = createHarness(0.0f);
        SnakeState snake = harness.state.snakes[0];
        int initialLength = snake.length;

        consumeFood(harness, snake, FoodType.GROW_SELF, 0);

        assertEquals(initialLength + 2, snake.length);
        assertNull(harness.state.foods[0]);
    }

    @Test
    void foodTypeGrowOthersAffectsOnlyActiveOpponent() {
        Harness harness = createHarness(0.0f);
        SnakeState snake = harness.state.snakes[0];
        int ownLength = snake.length;
        int opponentLength = harness.state.snakes[1].length;

        consumeFood(harness, snake, FoodType.GROW_OTHERS, 0);

        assertEquals(ownLength, snake.length);
        assertEquals(opponentLength + 8, harness.state.snakes[1].length);
        assertEquals(0, harness.state.snakes[2].length);
        assertEquals(0, harness.state.snakes[3].length);
    }

    @Test
    void foodTypeShrinkSelfNeverDropsBelowOneSegment() {
        Harness harness = createHarness(0.99f);
        SnakeState snake = harness.state.snakes[0];
        snake.length = 1;
        snake.segments[1] = null;

        consumeFood(harness, snake, FoodType.SHRINK_SELF, 0);

        assertEquals(1, snake.length);
        assertNotNull(snake.segments[0]);
    }

    @Test
    void foodTypeSpeedUpIncreasesSpeed() {
        Harness harness = createHarness(0.0f);
        SnakeState snake = harness.state.snakes[0];

        consumeFood(harness, snake, FoodType.SPEED_UP, 0);

        assertEquals(21.0f, snake.speed);
    }

    @Test
    void foodTypeSlowDownCannotDropBelowMinimum() {
        Harness harness = createHarness(0.99f);
        SnakeState snake = harness.state.snakes[0];
        snake.speed = harness.config.minSpeed;

        consumeFood(harness, snake, FoodType.SLOW_DOWN, 0);

        assertEquals(harness.config.minSpeed, snake.speed);
    }

    @Test
    void foodTypeSwapHeadsSwapsDirectionsAndHeadPositions() {
        Harness harness = createHarness();
        SnakeState snake = harness.state.snakes[0];
        SnakeState otherSnake = harness.state.snakes[1];
        snake.chosenDirection = 3;
        otherSnake.chosenDirection = 1;
        snake.segments[0].px = 5;
        snake.segments[0].py = 6;
        otherSnake.segments[0].px = 33;
        otherSnake.segments[0].py = 34;

        consumeFood(harness, snake, FoodType.SWAP_HEADS, 0);

        assertEquals(1, snake.chosenDirection);
        assertEquals(3, otherSnake.chosenDirection);
        assertEquals(33, snake.segments[0].px);
        assertEquals(34, snake.segments[0].py);
        assertEquals(5, otherSnake.segments[0].px);
        assertEquals(6, otherSnake.segments[0].py);
    }

    @Test
    void foodTypeSwapBodiesSwapsSegmentsAndDirections() {
        Harness harness = createHarness();
        SnakeState snake = harness.state.snakes[0];
        SnakeState otherSnake = harness.state.snakes[1];
        snake.chosenDirection = 3;
        otherSnake.chosenDirection = 1;
        snake.segments[0] = new SegmentState(2, 3);
        snake.segments[1] = new SegmentState(2, 4);
        otherSnake.segments[0] = new SegmentState(30, 31);
        otherSnake.segments[1] = new SegmentState(30, 32);

        consumeFood(harness, snake, FoodType.SWAP_BODIES, 0);

        assertEquals(1, snake.chosenDirection);
        assertEquals(3, otherSnake.chosenDirection);
        assertEquals(30, snake.segments[0].px);
        assertEquals(31, snake.segments[0].py);
        assertEquals(30, snake.segments[1].px);
        assertEquals(32, snake.segments[1].py);
        assertEquals(2, otherSnake.segments[0].px);
        assertEquals(3, otherSnake.segments[0].py);
    }

    @Test
    void foodTypeDropMineAddsMineAndTeleportsHead() {
        Harness harness = createHarness(0.8f, 0.7f);
        SnakeState snake = harness.state.snakes[0];
        int oldX = snake.segments[0].px;
        int oldY = snake.segments[0].py;

        consumeFood(harness, snake, FoodType.DROP_MINE, 0);

        assertEquals(1, harness.state.mineCount);
        assertEquals(oldX, harness.state.mines[0].px);
        assertEquals(oldY, harness.state.mines[0].py);
        assertNotEquals(oldX, snake.segments[0].px);
    }

    @Test
    void foodTypeCreateExplosionCreatesExplosionAtHeadCenter() {
        Harness harness = createHarness();
        SnakeState snake = harness.state.snakes[0];
        snake.segments[0].px = 8;
        snake.segments[0].py = 9;

        consumeFood(harness, snake, FoodType.CREATE_EXPLOSION, 123L, 0);

        assertNotNull(snake.explosion);
        assertEquals(8.5f, snake.explosion.px);
        assertEquals(9.5f, snake.explosion.py);
        assertEquals(123L, snake.explosion.startTimeMs);
    }

    @Test
    void foodTypeTeleportBoostTeleportsGrowsAndSpeedsUp() {
        Harness harness = createHarness(0.8f, 0.7f);
        SnakeState snake = harness.state.snakes[0];
        int startLength = snake.length;
        float startSpeed = snake.speed;
        int oldX = snake.segments[0].px;
        int oldY = snake.segments[0].py;

        consumeFood(harness, snake, FoodType.TELEPORT_BOOST, 0);

        assertEquals(startLength + 20, snake.length);
        assertEquals(startSpeed + 2.0f, snake.speed);
        assertTrue(snake.segments[0].px != oldX || snake.segments[0].py != oldY);
    }

    @Test
    void foodTypeReverseControlsActivatesFlag() {
        Harness harness = createHarness();

        consumeFood(harness, harness.state.snakes[0], FoodType.REVERSE_CONTROLS, 555L, 0);

        assertTrue(harness.state.effectState.controlsReversed);
        assertEquals(555L, harness.state.effectState.reverseStartedAtMs);
    }

    @Test
    void foodTypeChangeFoodCountCanShrinkSafelyFromLastIndex() {
        Harness harness = createHarness(0.25f, 0.1f, 0.2f, 0.3f);
        SnakeState snake = harness.state.snakes[0];
        harness.state.foods = new FoodState[]{
                new FoodState(99, 99, FoodType.GROW_SELF),
                new FoodState(snake.segments[0].px, snake.segments[0].py, FoodType.CHANGE_FOOD_COUNT)
        };

        harness.foodEffectSystem.applyFoodAtHead(harness.state, snake, 100L);

        assertEquals(1, harness.state.foods.length);
        assertNotNull(harness.state.foods[0]);
    }

    @Test
    void foodTypeSwapControlsStartsTimer() {
        Harness harness = createHarness();

        consumeFood(harness, harness.state.snakes[0], FoodType.SWAP_CONTROLS, 777L, 0);

        assertEquals(777L, harness.state.effectState.buttonsRevertedAtMs);
    }

    @Test
    void reverseControlsChangesMovementDirectionInCore() {
        Harness harness = createHarness();
        SnakeState snake = harness.state.snakes[0];
        int startY = snake.segments[0].py;
        harness.state.effectState.controlsReversed = true;
        harness.state.effectState.reverseStartedAtMs = 0L;

        harness.simulation.tick(harness.state, List.of(), 64L, 64L);

        assertEquals(startY - 1, snake.segments[0].py);
    }

    @Test
    void buttonSwapRoutesCommandsToOpponentInCore() {
        Harness harness = createHarness();
        harness.state.effectState.buttonsRevertedAtMs = 100L;
        harness.state.snakes[0].chosenDirection = 2;
        harness.state.snakes[1].chosenDirection = 0;

        harness.simulation.tick(harness.state, List.of(new TurnSnakeCommand(0, 1)), 0L, 200L);

        assertEquals(2, harness.state.snakes[0].chosenDirection);
        assertEquals(1, harness.state.snakes[1].chosenDirection);
    }

    @Test
    void resetRoundAfterDelayRespawnsFoodAndClearsReverseEffect() {
        Harness harness = createHarness();
        harness.state.effectState.controlsReversed = true;
        harness.state.effectState.reverseStartedAtMs = 0L;
        harness.state.snakes[1].died = true;

        harness.simulation.tick(harness.state, List.of(), 0L, 10L);
        assertTrue(harness.state.roundState.resetting);

        harness.simulation.tick(harness.state, List.of(), 0L, 1200L);

        assertFalse(harness.state.roundState.resetting);
        assertFalse(harness.state.snakes[0].died);
        assertFalse(harness.state.snakes[1].died);
        assertEquals(1, harness.state.snakes[0].score);
        assertFalse(harness.state.effectState.controlsReversed);
        assertEquals(2, harness.state.foods.length);
        assertNotNull(harness.state.foods[0]);
        assertNotNull(harness.state.foods[1]);
    }

    private void consumeFood(Harness harness, SnakeState snake, FoodType foodType, int foodIndex) {
        consumeFood(harness, snake, foodType, 100L, foodIndex);
    }

    private void consumeFood(Harness harness, SnakeState snake, FoodType foodType, long nowMs, int foodIndex) {
        for (int index = 0; index < harness.state.foods.length; index++) {
            harness.state.foods[index] = null;
        }
        harness.state.foods[foodIndex] = new FoodState(snake.segments[0].px, snake.segments[0].py, foodType);
        harness.foodEffectSystem.applyFoodAtHead(harness.state, snake, nowMs);
    }

    private Harness createHarness(float... randomValues) {
        GameConfig config = GameConfig.forPlatform(true);
        GameState state = new GameState(config);
        state.metrics = new ViewportLayoutAdapter().createMetrics(720, 1280, config);
        state.metrics.playfieldMinRow = 0;
        state.metrics.playfieldMaxRow = state.metrics.gridRows - 1;

        PlayfieldSystem playfieldSystem = new PlayfieldSystem();
        TestRandomSource random = new TestRandomSource(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f);
        FoodFactory foodFactory = new FoodFactory(random, playfieldSystem);
        CollisionSystem collisionSystem = new CollisionSystem();
        RoundResetSystem roundResetSystem = new RoundResetSystem(playfieldSystem, foodFactory);
        SnakeMovementSystem snakeMovementSystem = new SnakeMovementSystem(playfieldSystem);
        FoodEffectSystem foodEffectSystem = new FoodEffectSystem(random, playfieldSystem, collisionSystem, foodFactory);
        GameSimulation simulation = new GameSimulation(snakeMovementSystem, collisionSystem, foodEffectSystem, roundResetSystem);
        simulation.initialize(state, 0L);
        random.reset(randomValues);
        return new Harness(config, state, simulation, foodEffectSystem, random, foodFactory);
    }

    private float normalizedForCol(Harness harness, int col) {
        return (col - harness.state.metrics.minPlayableCol) / (float) ((harness.state.metrics.maxPlayableCol + 1) - harness.state.metrics.minPlayableCol);
    }

    private float normalizedForRow(Harness harness, int row) {
        return (row - harness.state.metrics.playfieldMinRow) / (float) ((harness.state.metrics.playfieldMaxRow + 1) - harness.state.metrics.playfieldMinRow);
    }

    private record Harness(
            GameConfig config,
            GameState state,
            GameSimulation simulation,
            FoodEffectSystem foodEffectSystem,
            TestRandomSource random,
            FoodFactory foodFactory
    ) {
    }
}
