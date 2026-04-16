package com.example.snake_3.game;

import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.HudViewModel;
import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;

final class DesktopSnakeUiRenderer implements SnakeUiRenderer {
    private static final String INVERTED_CONTROLS_TEXT = "управление инвертировано";
    private static final String CONTROL_SWAP_TEXT = "смена управления";
    private static final float WINNER_Z = 5.35f;
    private static final float WARNING_TEXT_Z = 5.39f;
    private static final float SCORE_Z = 5.40f;

    private final GamePageClass page;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;
    private SimplePolygon invertedControlsPoly;
    private float invertedControlsDrawW = 1f;
    private float invertedControlsDrawH = 1f;
    private SimplePolygon controlSwapPoly;
    private float controlSwapDrawW = 1f;
    private float controlSwapDrawH = 1f;

    DesktopSnakeUiRenderer(GamePageClass page) {
        this.page = page;
    }

    @Override
    public void onSurfaceChanged(GameViewModel viewModel, SnakeRenderAssets assets) {
        lastControlsReversed = viewModel.hud().controlsReversed();
        assets.setControlsReversed(lastControlsReversed);

        lastScoreText = viewModel.hud().scoreText();
        assets.setScoreText(lastScoreText);

        rebuildInvertedControlsText(viewModel);
        rebuildControlSwapText(viewModel);
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

        float centerX = viewModel.x() * 0.5f;
        float warningY = Math.max(12.0f * viewModel.ky(), 32.0f * viewModel.ky());
        float warningGap = Math.max(8.0f * viewModel.ky(), 10.0f);

        if (hud.buttonsReverted() && controlSwapPoly != null) {
            float topLeftX = centerX - (controlSwapDrawW * 0.5f);
            controlSwapPoly.prepareAndDraw(topLeftX, warningY, controlSwapDrawW, controlSwapDrawH, WARNING_TEXT_Z);
            warningY += controlSwapDrawH + warningGap;
        }

        if (hud.controlsReversed() && invertedControlsPoly != null) {
            float topLeftX = centerX - (invertedControlsDrawW * 0.5f);
            invertedControlsPoly.prepareAndDraw(topLeftX, warningY, invertedControlsDrawW, invertedControlsDrawH, WARNING_TEXT_Z);
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

    private void rebuildInvertedControlsText(GameViewModel viewModel) {
        if (invertedControlsPoly != null) {
            invertedControlsPoly.delete();
        }

        final float textSize = Math.max(28.0f * viewModel.kx(), 18.0f);
        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.CENTER);
        measurer.textSize(textSize);

        float textWidth = Math.max(1f, measurer.getTextWidth(INVERTED_CONTROLS_TEXT));
        float textHeight = Math.max(1f, measurer.getTextHeight(INVERTED_CONTROLS_TEXT));
        float padX = Math.max(8f, textSize * 0.25f);
        float padY = Math.max(4f, textSize * 0.18f);

        invertedControlsDrawW = (float) Math.ceil(textWidth + (padX * 2.0f));
        invertedControlsDrawH = (float) Math.ceil(textHeight + (padY * 2.0f));

        invertedControlsPoly = new SimplePolygon(unused -> {
            PImage image = new PImage(invertedControlsDrawW, invertedControlsDrawH);
            image.clear();
            image.setAntiAlias(true);
            image.setUpperText(true);
            image.textAlign(TextAlign.CENTER);
            image.textSize(textSize);
            image.noStroke();
            image.fill(255.0f, 100.0f, 100.0f, 255.0f);
            float baselineY = Math.max(0f, Math.min((invertedControlsDrawH * 0.5f) + (textHeight * 0.35f), invertedControlsDrawH));
            image.text(INVERTED_CONTROLS_TEXT, invertedControlsDrawW * 0.5f, baselineY);
            return image;
        }, true, 0, page);
        invertedControlsPoly.redrawNow();
    }

    private void rebuildControlSwapText(GameViewModel viewModel) {
        if (controlSwapPoly != null) {
            controlSwapPoly.delete();
        }

        final float textSize = Math.max(28.0f * viewModel.kx(), 18.0f);
        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.CENTER);
        measurer.textSize(textSize);

        float textWidth = Math.max(1f, measurer.getTextWidth(CONTROL_SWAP_TEXT));
        float textHeight = Math.max(1f, measurer.getTextHeight(CONTROL_SWAP_TEXT));
        float padX = Math.max(8f, textSize * 0.25f);
        float padY = Math.max(4f, textSize * 0.18f);

        controlSwapDrawW = (float) Math.ceil(textWidth + (padX * 2.0f));
        controlSwapDrawH = (float) Math.ceil(textHeight + (padY * 2.0f));

        controlSwapPoly = new SimplePolygon(unused -> {
            PImage image = new PImage(controlSwapDrawW, controlSwapDrawH);
            image.clear();
            image.setAntiAlias(true);
            image.setUpperText(true);
            image.textAlign(TextAlign.CENTER);
            image.textSize(textSize);
            image.noStroke();
            image.fill(255.0f, 220.0f, 120.0f, 255.0f);
            float baselineY = Math.max(0f, Math.min((controlSwapDrawH * 0.5f) + (textHeight * 0.35f), controlSwapDrawH));
            image.text(CONTROL_SWAP_TEXT, controlSwapDrawW * 0.5f, baselineY);
            return image;
        }, true, 0, page);
        controlSwapPoly.redrawNow();
    }
}
