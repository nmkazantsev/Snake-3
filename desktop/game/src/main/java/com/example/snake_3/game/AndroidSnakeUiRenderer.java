package com.example.snake_3.game;

final class AndroidSnakeUiRenderer implements SnakeUiRenderer {
    private static final float BUTTON_Z = 5.30f;
    private static final float WINNER_Z = 5.35f;
    private static final float SCORE_Z = 5.40f;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;

    @Override
    public void onSurfaceChanged(SnakeGame game, SnakeRenderAssets assets) {
        lastControlsReversed = game.isControlsReversed();
        assets.setControlsReversed(lastControlsReversed);

        lastScoreText = buildScoreText(game);
        assets.setScoreText(lastScoreText);
    }

    @Override
    public void render(SnakeGame game, SnakeRenderAssets assets) {
        syncUiState(game, assets);

        for (int si = 0; si < game.getPlayingUsers(); si++) {
            if (!game.isResetting()) {
                assets.drawButtons(game, si, BUTTON_Z);
            } else {
                if (!game.getSnakes()[si].isDied()) {
                    assets.drawWinner(si, game, WINNER_Z);
                }
            }
        }

        assets.drawScore(game, SCORE_Z);
    }

    private void syncUiState(SnakeGame game, SnakeRenderAssets assets) {
        if (lastControlsReversed != game.isControlsReversed()) {
            lastControlsReversed = game.isControlsReversed();
            assets.setControlsReversed(lastControlsReversed);
        }

        String scoreText = buildScoreText(game);
        if (!scoreText.equals(lastScoreText)) {
            lastScoreText = scoreText;
            assets.setScoreText(scoreText);
        }
    }

    private String buildScoreText(SnakeGame game) {
        return game.getSnakes()[1].getScore() + ":" + game.getSnakes()[0].getScore();
    }
}
