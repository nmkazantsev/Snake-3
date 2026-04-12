package com.example.snake_3.game;

import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;

final class DesktopSnakeUiRenderer implements SnakeUiRenderer {
    private static final String INVERTED_CONTROLS_TEXT = "управление инвертировано";
    private static final float WINNER_Z = 5.35f;
    private static final float WARNING_TEXT_Z = 5.39f;
    private static final float SCORE_Z = 5.40f;

    private final GamePageClass page;

    private String lastScoreText = null;
    private boolean lastControlsReversed = false;
    private SimplePolygon invertedControlsPoly;
    private float invertedControlsDrawW = 1f;
    private float invertedControlsDrawH = 1f;

    DesktopSnakeUiRenderer(GamePageClass page) {
        this.page = page;
    }

     @Override
    public void onSurfaceChanged(SnakeGame game, SnakeRenderAssets assets) {
        lastControlsReversed = game.isControlsReversed();
        assets.setControlsReversed(lastControlsReversed);

        lastScoreText = buildScoreText(game);
        assets.setScoreText(lastScoreText);

        rebuildInvertedControlsText(game);
    }

    @Override
    public void render(SnakeGame game, SnakeRenderAssets assets) {
        syncControlsState(game, assets);
        syncScore(game, assets);

        for (int si = 0; si < game.getPlayingUsers(); si++) {
            if (game.isResetting() && !game.getSnakes()[si].isDied()) {
                assets.drawWinner(si, game, WINNER_Z);
            }
        }

        if (game.isControlsReversed() && invertedControlsPoly != null) {
            float centerX = game.getX() * 0.5f;
            float topY = Math.max(12.0f * game.getKy(), 32.0f * game.getKy());
            float tlx = centerX - (invertedControlsDrawW * 0.5f);
            invertedControlsPoly.prepareAndDraw(tlx, topY, invertedControlsDrawW, invertedControlsDrawH, WARNING_TEXT_Z);
        }

        assets.drawScore(game, SCORE_Z);
    }

    private void syncControlsState(SnakeGame game, SnakeRenderAssets assets) {
        if (lastControlsReversed != game.isControlsReversed()) {
            lastControlsReversed = game.isControlsReversed();
            assets.setControlsReversed(lastControlsReversed);
        }
    }

    private void syncScore(SnakeGame game, SnakeRenderAssets assets) {
        String scoreText = buildScoreText(game);
        if (!scoreText.equals(lastScoreText)) {
            lastScoreText = scoreText;
            assets.setScoreText(scoreText);
        }
    }

    private String buildScoreText(SnakeGame game) {
        return game.getSnakes()[1].getScore() + ":" + game.getSnakes()[0].getScore();
    }

    private void rebuildInvertedControlsText(SnakeGame game) {
        if (invertedControlsPoly != null) {
            invertedControlsPoly.delete();
        }

        final float textSize = Math.max(28.0f * game.getKx(), 18.0f);

        PImage meas = new PImage(1, 1);
        meas.setAntiAlias(true);
        meas.setUpperText(true);
        meas.textAlign(TextAlign.CENTER);
        meas.textSize(textSize);

        float textW = Math.max(1f, meas.getTextWidth(INVERTED_CONTROLS_TEXT));
        float textH = Math.max(1f, meas.getTextHeight(INVERTED_CONTROLS_TEXT));
        float padX = Math.max(8f, textSize * 0.25f);
        float padY = Math.max(4f, textSize * 0.18f);

        invertedControlsDrawW = (float) Math.ceil(textW + (padX * 2.0f));
        invertedControlsDrawH = (float) Math.ceil(textH + (padY * 2.0f));

        invertedControlsPoly = new SimplePolygon(unused -> {
            PImage img = new PImage(invertedControlsDrawW, invertedControlsDrawH);
            img.clear();
            img.setAntiAlias(true);
            img.setUpperText(true);
            img.textAlign(TextAlign.CENTER);
            img.textSize(textSize);
            img.noStroke();
            img.fill(255.0f, 100.0f, 100.0f, 255.0f);

            float baselineY = (invertedControlsDrawH * 0.5f) + (textH * 0.35f);
            baselineY = Math.max(0f, Math.min(baselineY, invertedControlsDrawH));
            img.text(INVERTED_CONTROLS_TEXT, invertedControlsDrawW * 0.5f, baselineY);
            return img;
        }, true, 0, page);
        invertedControlsPoly.redrawNow();
    }
}
