package com.example.snake_3.game;

import com.nikitos.GamePageClass;

public final class SnakeRenderer {
    private final SnakeRenderAssets assets;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;

    public SnakeRenderer(GamePageClass page) {
        this.assets = new SnakeRenderAssets(page);
    }

    public void onSurfaceChanged() {
        // No-op; actual initialization happens on the first render when game metrics are available.
    }

    public void render(SnakeGame game) {
        // Assets are created once per surface size (MainRenderer calls game.onSurfaceChanged()).
        if (lastScoreText == null) assets.onSurfaceChanged(game);

        // Sync stateful skins.
        if (lastControlsReversed != game.isControlsReversed()) {
            lastControlsReversed = game.isControlsReversed();
            assets.setControlsReversed(lastControlsReversed);
        }

        // Score texture updates only when score changes.
        String scoreText = game.getSnakes()[1].getScore() + ":" + game.getSnakes()[0].getScore();
        if (!scoreText.equals(lastScoreText)) {
            lastScoreText = scoreText;
            assets.setScoreText(scoreText);
        }

        // Mines
        for (int i = 0; i < game.getMineLen(); i++) {
            Mine m = game.getMines()[i];
            if (m != null) assets.drawMine(m, 0.15f);
        }

        // Snakes (+ their explosions)
        for (int si = 0; si < game.getPlayingUsers(); si++) {
            Snake s = game.getSnakes()[si];
            for (int i = 0; i < s.getLength(); i++) {
                SnakeSegment seg = s.getSegments()[i];
                if (seg != null) assets.drawSegment(si, seg, 0.20f);
            }
            Explosion ex = s.getExplosion();
            if (ex != null) assets.drawExplosion(ex, 0.22f);
        }

        // Buttons + winner overlays (Processing: buttons drawn after snakes)
        for (int si = 0; si < game.getPlayingUsers(); si++) {
            if (!game.isResetting()) {
                assets.drawButtons(game, si, 0.30f);
            } else {
                if (!game.getSnakes()[si].isDied()) {
                    assets.drawWinner(si, game, 0.35f);
                }
            }
        }

        // Score on top
        assets.drawScore(game, 0.40f);

        // Foods
        for (Food f : game.getFoods()) {
            if (f != null) assets.drawFood(f, 0.45f);
        }
    }
}
