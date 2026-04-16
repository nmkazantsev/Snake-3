package com.example.snake_3.game.core.sim;

import com.example.snake_3.game.core.config.GameConfig;
import com.example.snake_3.game.core.model.ExplosionState;
import com.example.snake_3.game.core.model.GameState;
import com.example.snake_3.game.core.model.MineState;
import com.example.snake_3.game.core.model.SegmentState;
import com.example.snake_3.game.core.model.SnakeState;

public final class CollisionSystem {
    public boolean checkSnakeDeath(GameState state, SnakeState snake, long nowMs) {
        if (hitsOtherSnake(state, snake)) {
            return true;
        }
        if (hitsOtherExplosion(state, snake, nowMs)) {
            return true;
        }
        return hitsMine(state, snake, nowMs);
    }

    public boolean headConflictsWithOtherSnake(GameState state, SnakeState snake) {
        SegmentState head = snake.segments[0];
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState otherSnake = state.snakes[snakeId];
            for (int segmentIndex = 0; segmentIndex < otherSnake.length; segmentIndex++) {
                SegmentState otherSegment = otherSnake.segments[segmentIndex];
                if (otherSegment == null) {
                    continue;
                }
                if (snake.id != otherSnake.id && head.px == otherSegment.px && head.py == otherSegment.py) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hitsOtherSnake(GameState state, SnakeState snake) {
        SegmentState head = snake.segments[0];
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState otherSnake = state.snakes[snakeId];
            for (int segmentIndex = 0; segmentIndex < otherSnake.length; segmentIndex++) {
                SegmentState otherSegment = otherSnake.segments[segmentIndex];
                if (otherSegment == null) {
                    continue;
                }
                if (snake.id != otherSnake.id && head.px == otherSegment.px && head.py == otherSegment.py) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hitsOtherExplosion(GameState state, SnakeState snake, long nowMs) {
        GameConfig config = state.config;
        for (int snakeId = 0; snakeId < state.config.playingUsers; snakeId++) {
            SnakeState otherSnake = state.snakes[snakeId];
            ExplosionState explosion = otherSnake.explosion;
            if (otherSnake.id == snake.id || explosion == null) {
                continue;
            }
            if (nowMs - explosion.startTimeMs > config.explosionDurationMs) {
                continue;
            }
            float radius = getExplosionSize(state, explosion, nowMs, config);
            for (int segmentIndex = 0; segmentIndex < snake.length; segmentIndex++) {
                SegmentState segment = snake.segments[segmentIndex];
                if (segment == null) {
                    continue;
                }
                if (isInExplosionRadius(state, explosion, radius, segment.px, segment.py)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hitsMine(GameState state, SnakeState snake, long nowMs) {
        SegmentState head = snake.segments[0];
        for (int index = 0; index < state.mineCount; index++) {
            MineState mine = state.mines[index];
            if (mine == null) {
                continue;
            }
            if (Math.abs(head.px - mine.px) > 1 || Math.abs(head.py - mine.py) > 1) {
                continue;
            }
            mine.explosionTimeMs = nowMs;
            return true;
        }
        return false;
    }

    public float getExplosionSize(GameState state, ExplosionState explosion, long nowMs, GameConfig config) {
        return ((state.metrics.sizx * 15.0f) * (float) (nowMs - explosion.startTimeMs)) / config.explosionDurationMs;
    }

    private boolean isInExplosionRadius(GameState state, ExplosionState explosion, float radius, float cellX, float cellY) {
        float deltaX = (explosion.px * state.metrics.sizx) - ((state.metrics.sizx * cellX) + (state.metrics.sizx / 2.0f));
        float deltaY = (explosion.py * state.metrics.sizy) - ((state.metrics.sizy * cellY) + (state.metrics.sizy / 2.0f));
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY)) < radius;
    }
}
