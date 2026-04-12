package com.example.snake_3.game;

import com.nikitos.GamePageClass;
import com.nikitos.main.frameBuffers.FrameBuffer;
import com.nikitos.maths.PVector;

final class SnakeFieldRenderer {
    // Engine 2D camera uses eyeZ=20 with near/far=10..20, so world z=0 lands on the far clip plane.
    // Keep all 2D content comfortably inside that slab to avoid driver-dependent clipping.
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

    void onSurfaceChanged(SnakeGame game) {
        rebuildFieldFrameBuffer(game);
    }

    void render(SnakeGame game) {
        ensureFieldFrameBuffer(game);

        if (fieldFrameBuffer == null) {
            drawFieldContents(game);
            return;
        }

        fieldFrameBuffer.apply();
        drawFieldContents(game);
        fieldFrameBuffer.connectDefaultFrameBuffer();
        fieldFrameBuffer.drawTexture(
                new PVector(0.0f, 0.0f, FIELD_TEXTURE_Z),
                new PVector(game.getX(), 0.0f, FIELD_TEXTURE_Z),
                new PVector(0.0f, game.getY(), FIELD_TEXTURE_Z)
        );
    }

    private void drawFieldContents(SnakeGame game) {
        for (int i = 0; i < game.getMineLen(); i++) {
            Mine m = game.getMines()[i];
            if (m != null) assets.drawMine(m, MINE_Z);
        }

        for (int si = 0; si < game.getPlayingUsers(); si++) {
            Snake s = game.getSnakes()[si];
            for (int i = 0; i < s.getLength(); i++) {
                SnakeSegment seg = s.getSegments()[i];
                if (seg != null) assets.drawSegment(si, seg, SEGMENT_Z);
            }
            Explosion ex = s.getExplosion();
            if (ex != null) assets.drawExplosion(ex, game, EXPLOSION_Z);
        }

        for (Food f : game.getFoods()) {
            if (f != null) assets.drawFood(f, FOOD_Z);
        }
    }

    private void ensureFieldFrameBuffer(SnakeGame game) {
        int width = Math.max(1, Math.round(game.getX()));
        int height = Math.max(1, Math.round(game.getY()));
        if (fieldFrameBuffer == null || fieldFrameBufferWidth != width || fieldFrameBufferHeight != height) {
            rebuildFieldFrameBuffer(game);
        }
    }

    private void rebuildFieldFrameBuffer(SnakeGame game) {
        if (fieldFrameBuffer != null) {
            fieldFrameBuffer.delete();
        }

        fieldFrameBufferWidth = Math.max(1, Math.round(game.getX()));
        fieldFrameBufferHeight = Math.max(1, Math.round(game.getY()));
        fieldFrameBuffer = new FrameBuffer(fieldFrameBufferWidth, fieldFrameBufferHeight, page);
    }
}
