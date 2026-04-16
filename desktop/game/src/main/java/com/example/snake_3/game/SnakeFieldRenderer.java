package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.ExplosionViewModel;
import com.example.snake_3.game.render.vm.FoodViewModel;
import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.MineViewModel;
import com.example.snake_3.game.render.vm.SegmentViewModel;
import com.example.snake_3.game.render.vm.SnakeViewModel;
import com.nikitos.GamePageClass;
import com.nikitos.main.frameBuffers.FrameBuffer;
import com.nikitos.maths.PVector;

final class SnakeFieldRenderer {
    private static final float FIELD_TEXTURE_Z = 5.00f;
    private static final float MINE_Z = 5.05f;
    private static final float SEGMENT_Z = 5.10f;
    private static final float EXPLOSION_Z = 5.12f;
    private static final float FOOD_Z = 5.20f;

    private final GamePageClass page;
    private final SnakeRenderAssets assets;

    private FrameBuffer fieldFrameBuffer;
    private int fieldFrameBufferWidth = -1;
    private int fieldFrameBufferHeight = -1;

    SnakeFieldRenderer(GamePageClass page, SnakeRenderAssets assets) {
        this.page = page;
        this.assets = assets;
    }

    void onSurfaceChanged(GameViewModel viewModel) {
        rebuildFieldFrameBuffer(viewModel);
    }

    void render(GameViewModel viewModel) {
        ensureFieldFrameBuffer(viewModel);

        if (fieldFrameBuffer == null) {
            drawFieldContents(viewModel);
            return;
        }

        fieldFrameBuffer.apply();
        drawFieldContents(viewModel);
        fieldFrameBuffer.connectDefaultFrameBuffer();
        fieldFrameBuffer.drawTexture(
                new PVector(0.0f, 0.0f, FIELD_TEXTURE_Z),
                new PVector(viewModel.x(), 0.0f, FIELD_TEXTURE_Z),
                new PVector(0.0f, viewModel.y(), FIELD_TEXTURE_Z)
        );
    }

    private void drawFieldContents(GameViewModel viewModel) {
        for (MineViewModel mine : viewModel.mines()) {
            assets.drawMine(mine, viewModel.currentTimeMs(), MINE_Z);
        }

        for (SnakeViewModel snake : viewModel.snakes()) {
            for (SegmentViewModel segment : snake.segments()) {
                assets.drawSegment(segment, SEGMENT_Z);
            }
            ExplosionViewModel explosion = snake.explosion();
            if (explosion != null) {
                assets.drawExplosion(explosion, viewModel, EXPLOSION_Z);
            }
        }

        for (FoodViewModel food : viewModel.foods()) {
            assets.drawFood(food, FOOD_Z);
        }
    }

    private void ensureFieldFrameBuffer(GameViewModel viewModel) {
        int width = Math.max(1, Math.round(viewModel.x()));
        int height = Math.max(1, Math.round(viewModel.y()));
        if (fieldFrameBuffer == null || fieldFrameBufferWidth != width || fieldFrameBufferHeight != height) {
            rebuildFieldFrameBuffer(viewModel);
        }
    }

    private void rebuildFieldFrameBuffer(GameViewModel viewModel) {
        if (fieldFrameBuffer != null) {
            fieldFrameBuffer.delete();
        }

        fieldFrameBufferWidth = Math.max(1, Math.round(viewModel.x()));
        fieldFrameBufferHeight = Math.max(1, Math.round(viewModel.y()));
        fieldFrameBuffer = new FrameBuffer(fieldFrameBufferWidth, fieldFrameBufferHeight, page);
    }
}
