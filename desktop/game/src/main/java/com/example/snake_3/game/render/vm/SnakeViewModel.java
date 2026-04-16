package com.example.snake_3.game.render.vm;

import java.util.List;

public record SnakeViewModel(int snakeId, int score, boolean died, List<SegmentViewModel> segments, ExplosionViewModel explosion) {
}
