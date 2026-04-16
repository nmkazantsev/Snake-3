package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.HudViewModel;
import com.nikitos.GamePageClass;

final class DesktopSnakeUiRenderer implements SnakeUiRenderer {
    private static final float WINNER_Z = 5.35f;
    private static final float WARNING_TEXT_Z = 5.39f;
    private static final float SCORE_Z = 5.40f;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;

    DesktopSnakeUiRenderer(GamePageClass page) {
    }

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
        for (int snakeId = 0; snakeId < hud.winners().length; snakeId++) {
            if (hud.winners()[snakeId]) {
                assets.drawWinner(snakeId, viewModel, WINNER_Z);
            }
        }

        float warningY = Math.max(12.0f * viewModel.ky(), 32.0f * viewModel.ky());
        float warningGap = Math.max(8.0f * viewModel.ky(), 10.0f);

        if (hud.buttonsReverted()) {
            warningY += assets.drawControlSwapWarning(viewModel, warningY, WARNING_TEXT_Z) + warningGap;
        }

        if (hud.controlsReversed()) {
            assets.drawInvertedControlsWarning(viewModel, warningY, WARNING_TEXT_Z);
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
