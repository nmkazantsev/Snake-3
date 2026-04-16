package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.HudViewModel;

final class AndroidSnakeUiRenderer implements SnakeUiRenderer {
    private static final float BUTTON_Z = 5.30f;
    private static final float WINNER_Z = 5.35f;
    private static final float SCORE_Z = 5.40f;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;

    @Override
    public void onSurfaceChanged(GameViewModel viewModel, SnakeRenderAssets assets) {
        lastControlsReversed = viewModel.hud().controlsReversed();
        assets.setControlsReversed(lastControlsReversed);

        lastScoreText = viewModel.hud().scoreText();
        assets.setScoreText(lastScoreText);
    }

    @Override
    public void render(GameViewModel viewModel, SnakeRenderAssets assets) {
        syncUiState(viewModel, assets);

        HudViewModel hud = viewModel.hud();
        if (!hud.resetting()) {
            assets.drawButtons(viewModel.buttons(), BUTTON_Z);
        } else {
            for (int snakeId = 0; snakeId < hud.winners().length; snakeId++) {
                if (hud.winners()[snakeId]) {
                    assets.drawWinner(snakeId, viewModel, WINNER_Z);
                }
            }
        }

        assets.drawScore(viewModel, SCORE_Z);
    }

    private void syncUiState(GameViewModel viewModel, SnakeRenderAssets assets) {
        if (lastControlsReversed != viewModel.hud().controlsReversed()) {
            lastControlsReversed = viewModel.hud().controlsReversed();
            assets.setControlsReversed(lastControlsReversed);
        }
        if (!viewModel.hud().scoreText().equals(lastScoreText)) {
            lastScoreText = viewModel.hud().scoreText();
            assets.setScoreText(lastScoreText);
        }
    }
}
