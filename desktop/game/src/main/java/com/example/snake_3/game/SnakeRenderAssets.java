package com.example.snake_3.game;

import com.example.snake_3.game.core.model.FoodType;
import com.example.snake_3.game.render.vm.ButtonViewModel;
import com.example.snake_3.game.render.vm.ExplosionViewModel;
import com.example.snake_3.game.render.vm.FoodViewModel;
import com.example.snake_3.game.render.vm.GameViewModel;
import com.example.snake_3.game.render.vm.MineViewModel;
import com.example.snake_3.game.render.vm.SegmentViewModel;
import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.utils.Utils;

import java.util.List;
import java.util.function.Function;

public final class SnakeRenderAssets {
    private static final String INVERTED_CONTROLS_TEXT = "управление инвертировано";
    private static final String CONTROL_SWAP_TEXT = "смена управления";
    private static final float SEGMENT_TEX_SIZE = 64f;
    private static final float FOOD_TEX_SIZE = 64f;
    private static final float MINE_TEX_SIZE = 128f;
    private static final float BUTTON_WIDE_TEX_W = 256f;
    private static final float BUTTON_WIDE_TEX_H = 110f;
    private static final float BUTTON_TALL_TEX_W = 128f;
    private static final float BUTTON_TALL_TEX_H = 220f;
    private static final int MINE_EXP_FRAMES = 16;
    private static final int EXPLOSION_COLOR_STEPS = 64;
    private static final float EXPLOSION_SQUARE_TEX = 8f;

    private final GamePageClass page;

    private float x = 1f;
    private float y = 1f;
    private float kx = 1f;
    private float ky = 1f;
    private float sizx = 1f;
    private float sizy = 1f;
    private boolean controlsReversed = false;

    private SimplePolygon[][] segmentTile = new SimplePolygon[2][100];
    private SimplePolygon foodTileWhite;
    private SimplePolygon foodTileBlue;
    private SimplePolygon mineTileNormal;
    private SimplePolygon[] mineTileExplosionFrames = new SimplePolygon[0];
    private SimplePolygon[] explosionSquareTile = new SimplePolygon[0];
    private SimplePolygon buttonWideP0;
    private SimplePolygon buttonTallP0;
    private SimplePolygon buttonWideP1;
    private SimplePolygon buttonTallP1;
    private SimplePolygon scorePoly;
    private String scoreText = "";
    private float scoreDrawW = 1f;
    private float scoreDrawH = 1f;
    private SimplePolygon winnerPoly;
    private float winnerDrawW = 1f;
    private float winnerDrawH = 1f;
    private float winnerPadX = 0f;
    private float winnerBaselineOffsetY = 0f;
    private SimplePolygon invertedControlsPoly;
    private float invertedControlsDrawW = 1f;
    private float invertedControlsDrawH = 1f;
    private SimplePolygon controlSwapPoly;
    private float controlSwapDrawW = 1f;
    private float controlSwapDrawH = 1f;

    public SnakeRenderAssets(GamePageClass page) {
        this.page = page;
    }

    public void onSurfaceChanged(GameViewModel viewModel) {
        x = Math.max(1f, viewModel.x());
        y = Math.max(1f, viewModel.y());
        kx = viewModel.kx();
        ky = viewModel.ky();
        sizx = Math.max(1f, viewModel.sizx());
        sizy = Math.max(1f, viewModel.sizy());

        deletePolygon(foodTileWhite);
        deletePolygon(foodTileBlue);
        deletePolygon(mineTileNormal);
        deleteAll(mineTileExplosionFrames);
        deleteAll(explosionSquareTile);
        deletePolygon(buttonWideP0);
        deletePolygon(buttonTallP0);
        deletePolygon(buttonWideP1);
        deletePolygon(buttonTallP1);
        deletePolygon(scorePoly);
        deletePolygon(winnerPoly);
        deletePolygon(invertedControlsPoly);
        deletePolygon(controlSwapPoly);
        scorePoly = null;
        winnerPoly = null;
        invertedControlsPoly = null;
        controlSwapPoly = null;

        segmentTile = new SimplePolygon[2][100];
        buildFoodTiles();
        buildMineTiles();
        buildExplosionSquareTiles();
        buildButtonTiles();
        buildWinnerTiles();
        buildWarningTiles();
    }

    public void setControlsReversed(boolean controlsReversed) {
        if (this.controlsReversed == controlsReversed) {
            return;
        }
        this.controlsReversed = controlsReversed;
        for (SimplePolygon[] polygons : segmentTile) {
            for (SimplePolygon polygon : polygons) {
                if (polygon != null) {
                    polygon.setRedrawNeeded(true);
                    polygon.redrawNow();
                }
            }
        }
        redraw(buttonWideP0);
        redraw(buttonTallP0);
        redraw(buttonWideP1);
        redraw(buttonTallP1);
    }

    public void setScoreText(String newText) {
        if (newText == null) {
            newText = "";
        }
        if (newText.equals(scoreText) && scorePoly != null) {
            return;
        }
        scoreText = newText;
        deletePolygon(scorePoly);
        final String captured = scoreText;
        scorePoly = new SimplePolygon(unused -> redrawScore(captured), true, 0, page);
        scorePoly.redrawNow();
    }

    public void drawSegment(SegmentViewModel segment, float z) {
        int snakeId = segment.snakeId() == 0 ? 0 : 1;
        int brightness = Math.floorMod((segment.segmentIndex() + 1) * 37, 100);
        SimplePolygon polygon = segmentTile[snakeId][brightness];
        if (polygon == null) {
            polygon = createSegmentTile(snakeId, brightness);
            segmentTile[snakeId][brightness] = polygon;
        }
        polygon.prepareAndDraw(segment.px() * sizx, segment.py() * sizy, sizx, sizy, z);
    }

    public void drawFood(FoodViewModel food, float z) {
        SimplePolygon polygon = food.type() == FoodType.TELEPORT_BOOST ? foodTileBlue : foodTileWhite;
        polygon.prepareAndDraw(food.px() * sizx, food.py() * sizy, sizx, sizy, z);
    }

    public void drawMine(MineViewModel mine, long nowMs, float z) {
        float centerX = (mine.px() * sizx) + (sizx * 0.5f);
        float centerY = (mine.py() * sizy) + (sizy * 0.5f);
        if (mine.explosionTimeMs() == 0L) {
            float size = sizx * 3.0f;
            mineTileNormal.prepareAndDraw(centerX - size * 0.5f, centerY - size * 0.5f, size, size, z);
            return;
        }

        float progress = Math.max(0f, Math.min(1f, (float) (nowMs - mine.explosionTimeMs()) / 1000.0f));
        float size = Math.max(1f, (sizx * 3.0f) * progress);
        int frameIndex = Math.max(0, Math.min(mineTileExplosionFrames.length - 1, Math.round(progress * (mineTileExplosionFrames.length - 1))));
        SimplePolygon polygon = mineTileExplosionFrames[frameIndex];
        if (polygon != null) {
            polygon.prepareAndDraw(centerX - size * 0.5f, centerY - size * 0.5f, size, size, z);
        }
    }

    public void drawExplosion(ExplosionViewModel explosion, GameViewModel viewModel, float z) {
        if (explosionSquareTile.length == 0) {
            return;
        }

        float radiusPx = ((sizx * 15.0f) * ((float) (viewModel.currentTimeMs() - explosion.startTimeMs()))) / 4000.0f;
        if (radiusPx <= 0.0f) {
            return;
        }

        int cols2 = Math.max(1, viewModel.gridCols() * 2);
        int rows2 = Math.max(1, viewModel.gridRows() * 2);
        float stepX = sizx * 0.5f;
        float stepY = sizy * 0.5f;
        float centerPxX = explosion.px() * sizx;
        float centerPxY = explosion.py() * sizy;
        float pointOffsetX = sizx * 0.5f;
        float pointOffsetY = sizy * 0.5f;

        int iMin = Math.max(0, Math.min(cols2 - 1, (int) Math.floor((centerPxX - radiusPx - pointOffsetX) / stepX) - 1));
        int iMax = Math.max(0, Math.min(cols2 - 1, (int) Math.ceil((centerPxX + radiusPx - pointOffsetX) / stepX) + 1));
        int jMin = Math.max(0, Math.min(rows2 - 1, (int) Math.floor((centerPxY - radiusPx - pointOffsetY) / stepY) - 1));
        int jMax = Math.max(0, Math.min(rows2 - 1, (int) Math.ceil((centerPxY + radiusPx - pointOffsetY) / stepY) + 1));

        for (int column = iMin; column <= iMax; column++) {
            float rectX = column * stepX;
            float pointX = rectX + pointOffsetX;
            float deltaX = pointX - centerPxX;
            for (int row = jMin; row <= jMax; row++) {
                float rectY = row * stepY;
                float pointY = rectY + pointOffsetY;
                float deltaY = pointY - centerPxY;
                float distance = (float) Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                if (distance >= radiusPx) {
                    continue;
                }
                float gradient = Math.max(0f, Math.min(1f, (distance / sizx) / 15.0f));
                int colorIndex = Math.max(0, Math.min(EXPLOSION_COLOR_STEPS - 1, Math.round(gradient * (EXPLOSION_COLOR_STEPS - 1))));
                explosionSquareTile[colorIndex].prepareAndDraw(rectX, rectY, stepX, stepY, z);
            }
        }
    }

    public void drawButtons(List<ButtonViewModel> buttons, float z) {
        for (ButtonViewModel button : buttons) {
            SimplePolygon polygon;
            if (button.snakeId() == 0) {
                polygon = button.wide() ? buttonWideP0 : buttonTallP0;
            } else {
                polygon = button.wide() ? buttonWideP1 : buttonTallP1;
            }
            polygon.prepareAndDraw(button.px(), button.py(), button.width(), button.height(), z);
        }
    }

    public void drawScore(GameViewModel viewModel, float z) {
        if (scorePoly == null) {
            return;
        }
        float centerX = x / 20.0f;
        float centerY = (y / 2.0f) - (sizx / 2.0f);
        float topLeftX = centerX - (scoreDrawW * 0.5f);
        float topLeftY = centerY - (scoreDrawH * 0.5f);
        scorePoly.prepareAndDraw(Utils.radians(90.0f), topLeftX, topLeftY, scoreDrawW, scoreDrawH, z);
    }

    public void drawWinner(int snakeId, GameViewModel viewModel, float z) {
        if (winnerPoly == null) {
            return;
        }

        float anchorX;
        float baselineY;
        if (snakeId == 0) {
            anchorX = viewModel.x() - (300.0f * viewModel.kx());
            baselineY = viewModel.y() - (100.0f * viewModel.ky());
        } else {
            anchorX = 300.0f * viewModel.kx();
            baselineY = 100.0f * viewModel.ky();
        }

        float rotation = snakeId == 0 ? 0.0f : Utils.radians(180.0f);
        float topLeftX;
        float topLeftY;
        if (snakeId == 0) {
            topLeftX = anchorX - winnerPadX;
            topLeftY = baselineY - winnerBaselineOffsetY;
        } else {
            float rotatedAnchorX = winnerDrawW - winnerPadX;
            float rotatedAnchorY = winnerDrawH - winnerBaselineOffsetY;
            topLeftX = anchorX - rotatedAnchorX;
            topLeftY = baselineY - rotatedAnchorY;
        }

        winnerPoly.prepareAndDraw(rotation, topLeftX, topLeftY, winnerDrawW, winnerDrawH, z);
    }

    public float drawControlSwapWarning(GameViewModel viewModel, float topY, float z) {
        if (controlSwapPoly == null) {
            return 0f;
        }
        float topLeftX = (viewModel.x() * 0.5f) - (controlSwapDrawW * 0.5f);
        controlSwapPoly.prepareAndDraw(topLeftX, topY, controlSwapDrawW, controlSwapDrawH, z);
        return controlSwapDrawH;
    }

    public float drawInvertedControlsWarning(GameViewModel viewModel, float topY, float z) {
        if (invertedControlsPoly == null) {
            return 0f;
        }
        float topLeftX = (viewModel.x() * 0.5f) - (invertedControlsDrawW * 0.5f);
        invertedControlsPoly.prepareAndDraw(topLeftX, topY, invertedControlsDrawW, invertedControlsDrawH, z);
        return invertedControlsDrawH;
    }

    private SimplePolygon createSegmentTile(int snakeId, int brightness) {
        return new SimplePolygon(unused -> {
            PImage image = new PImage(SEGMENT_TEX_SIZE, SEGMENT_TEX_SIZE);
            image.clear();
            image.setAntiAlias(true);
            image.stroke(0);
            image.strokeWeight(1.0f);
            if (snakeId == 0) {
                image.fill(brightness + 150, 0.0f, 0.0f);
            } else {
                image.fill(0.0f, brightness + 150, 0.0f);
            }
            image.rect(0, 0, SEGMENT_TEX_SIZE, SEGMENT_TEX_SIZE);

            if (controlsReversed) {
                float outerStroke = Math.max(3f, SEGMENT_TEX_SIZE * 0.11f);
                image.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                image.strokeWeight(outerStroke);
                image.rect(outerStroke * 0.5f, outerStroke * 0.5f, SEGMENT_TEX_SIZE - outerStroke, SEGMENT_TEX_SIZE - outerStroke);

                float innerStroke = Math.max(2f, SEGMENT_TEX_SIZE * 0.05f);
                image.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                image.strokeWeight(innerStroke);
                image.rect(innerStroke * 0.5f, innerStroke * 0.5f, SEGMENT_TEX_SIZE - innerStroke, SEGMENT_TEX_SIZE - innerStroke);
            }
            return image;
        }, true, 0, page);
    }

    private void buildFoodTiles() {
        foodTileWhite = new SimplePolygon(unused -> {
            PImage image = new PImage(FOOD_TEX_SIZE, FOOD_TEX_SIZE);
            image.clear();
            image.setAntiAlias(true);
            image.fill(255);
            image.stroke(255);
            image.strokeWeight(3.0f);
            image.rect(0, 0, FOOD_TEX_SIZE, FOOD_TEX_SIZE);
            return image;
        }, true, 0, page);

        foodTileBlue = new SimplePolygon(unused -> {
            PImage image = new PImage(FOOD_TEX_SIZE, FOOD_TEX_SIZE);
            image.clear();
            image.setAntiAlias(true);
            image.fill(0.0f, 0.0f, 255.0f);
            image.stroke(255);
            image.strokeWeight(3.0f);
            image.rect(0, 0, FOOD_TEX_SIZE, FOOD_TEX_SIZE);
            return image;
        }, true, 0, page);
    }

    private void buildMineTiles() {
        mineTileNormal = new SimplePolygon(unused -> {
            PImage image = new PImage(MINE_TEX_SIZE, MINE_TEX_SIZE);
            image.clear();
            image.setAntiAlias(true);
            image.noStroke();
            for (float radius = MINE_TEX_SIZE; radius > 0.0f; radius -= 1.0f) {
                float color = 150.0f - ((radius / MINE_TEX_SIZE) * 150.0f);
                image.fill(color, color, color, 255.0f);
                image.ellipse(MINE_TEX_SIZE * 0.5f, MINE_TEX_SIZE * 0.5f, radius * 0.5f, radius * 0.5f);
            }
            return image;
        }, true, 0, page);

        mineTileExplosionFrames = new SimplePolygon[MINE_EXP_FRAMES];
        for (int frameIndex = 0; frameIndex < MINE_EXP_FRAMES; frameIndex++) {
            final float progress = MINE_EXP_FRAMES <= 1 ? 1.0f : frameIndex / (float) (MINE_EXP_FRAMES - 1);
            mineTileExplosionFrames[frameIndex] = new SimplePolygon(unused -> {
                PImage image = new PImage(MINE_TEX_SIZE, MINE_TEX_SIZE);
                image.clear();
                image.setAntiAlias(true);
                image.noStroke();
                for (float diameter = MINE_TEX_SIZE; diameter > 0.0f; diameter -= 1.0f) {
                    float gradient = Math.max(0f, Math.min(1f, (diameter / MINE_TEX_SIZE) * progress));
                    image.fill((gradient * 105.0f) + 155.0f, 100.0f - (gradient * 100.0f), 0.0f, 255.0f);
                    image.ellipse(MINE_TEX_SIZE * 0.5f, MINE_TEX_SIZE * 0.5f, diameter * 0.5f, diameter * 0.5f);
                }
                return image;
            }, true, 0, page);
        }
    }

    private void buildExplosionSquareTiles() {
        explosionSquareTile = new SimplePolygon[EXPLOSION_COLOR_STEPS];
        for (int index = 0; index < EXPLOSION_COLOR_STEPS; index++) {
            final float gradient = EXPLOSION_COLOR_STEPS <= 1 ? 0.0f : index / (float) (EXPLOSION_COLOR_STEPS - 1);
            final float red = (gradient * 105.0f) + 150.0f;
            final float green = 100.0f - (gradient * 100.0f);
            explosionSquareTile[index] = new SimplePolygon(unused -> {
                PImage image = new PImage(EXPLOSION_SQUARE_TEX, EXPLOSION_SQUARE_TEX);
                image.clear();
                image.setAntiAlias(false);
                image.noStroke();
                image.fill(red, green, 0.0f, 255.0f);
                image.rect(0, 0, EXPLOSION_SQUARE_TEX, EXPLOSION_SQUARE_TEX);
                return image;
            }, true, 0, page);
        }
    }

    private void buildButtonTiles() {
        buttonWideP0 = new SimplePolygon(redrawButtonWide(50, 0, 0), true, 0, page);
        buttonTallP0 = new SimplePolygon(redrawButtonTall(50, 0, 0), true, 0, page);
        buttonWideP1 = new SimplePolygon(redrawButtonWide(0, 50, 0), true, 0, page);
        buttonTallP1 = new SimplePolygon(redrawButtonTall(0, 50, 0), true, 0, page);
    }

    private Function<List<Object>, PImage> redrawButtonWide(int red, int green, int blue) {
        return unused -> {
            PImage image = new PImage(BUTTON_WIDE_TEX_W, BUTTON_WIDE_TEX_H);
            image.clear();
            image.setAntiAlias(true);
            image.fill(red, green, blue);
            image.noStroke();
            image.rect(0, 0, BUTTON_WIDE_TEX_W, BUTTON_WIDE_TEX_H);

            float baseStroke = Math.max(2f, Math.min(BUTTON_WIDE_TEX_W, BUTTON_WIDE_TEX_H) * 0.03f);
            image.stroke(255, 255, 255, 255);
            image.strokeWeight(baseStroke);
            image.rect(baseStroke * 0.5f, baseStroke * 0.5f, BUTTON_WIDE_TEX_W - baseStroke, BUTTON_WIDE_TEX_H - baseStroke);

            if (controlsReversed) {
                float outerStroke = Math.max(6f, Math.min(BUTTON_WIDE_TEX_W, BUTTON_WIDE_TEX_H) * 0.16f);
                image.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                image.strokeWeight(outerStroke);
                image.rect(outerStroke * 0.5f, outerStroke * 0.5f, BUTTON_WIDE_TEX_W - outerStroke, BUTTON_WIDE_TEX_H - outerStroke);

                float innerStroke = Math.max(4f, Math.min(BUTTON_WIDE_TEX_W, BUTTON_WIDE_TEX_H) * 0.08f);
                image.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                image.strokeWeight(innerStroke);
                image.rect(innerStroke * 0.5f, innerStroke * 0.5f, BUTTON_WIDE_TEX_W - innerStroke, BUTTON_WIDE_TEX_H - innerStroke);
            }
            return image;
        };
    }

    private Function<List<Object>, PImage> redrawButtonTall(int red, int green, int blue) {
        return unused -> {
            PImage image = new PImage(BUTTON_TALL_TEX_W, BUTTON_TALL_TEX_H);
            image.clear();
            image.setAntiAlias(true);
            image.fill(red, green, blue);
            image.noStroke();
            image.rect(0, 0, BUTTON_TALL_TEX_W, BUTTON_TALL_TEX_H);

            float baseStroke = Math.max(2f, Math.min(BUTTON_TALL_TEX_W, BUTTON_TALL_TEX_H) * 0.03f);
            image.stroke(255, 255, 255, 255);
            image.strokeWeight(baseStroke);
            image.rect(baseStroke * 0.5f, baseStroke * 0.5f, BUTTON_TALL_TEX_W - baseStroke, BUTTON_TALL_TEX_H - baseStroke);

            if (controlsReversed) {
                float outerStroke = Math.max(6f, Math.min(BUTTON_TALL_TEX_W, BUTTON_TALL_TEX_H) * 0.16f);
                image.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                image.strokeWeight(outerStroke);
                image.rect(outerStroke * 0.5f, outerStroke * 0.5f, BUTTON_TALL_TEX_W - outerStroke, BUTTON_TALL_TEX_H - outerStroke);

                float innerStroke = Math.max(4f, Math.min(BUTTON_TALL_TEX_W, BUTTON_TALL_TEX_H) * 0.08f);
                image.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                image.strokeWeight(innerStroke);
                image.rect(innerStroke * 0.5f, innerStroke * 0.5f, BUTTON_TALL_TEX_W - innerStroke, BUTTON_TALL_TEX_H - innerStroke);
            }
            return image;
        };
    }

    private PImage redrawScore(String text) {
        float textSize = sizx * 2.0f;
        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.CENTER);
        measurer.textSize(textSize);
        float textWidth = Math.max(1f, measurer.getTextWidth(text));
        float textHeight = Math.max(1f, measurer.getTextHeight(text));

        float padding = Math.max(2f, textSize * 0.15f);
        scoreDrawW = (float) Math.ceil(textWidth + padding * 2.0f);
        scoreDrawH = (float) Math.ceil(textHeight + padding * 2.0f);

        PImage image = new PImage(scoreDrawW, scoreDrawH);
        image.clear();
        image.setAntiAlias(true);
        image.setUpperText(true);
        image.noStroke();
        image.fill(255, 255, 255, 255);
        image.textAlign(TextAlign.CENTER);
        image.textSize(textSize);
        float baselineY = Math.max(0f, Math.min((scoreDrawH * 0.5f) + (textHeight * 0.35f), scoreDrawH));
        image.text(text, scoreDrawW * 0.5f, baselineY);
        return image;
    }

    private void buildWinnerTiles() {
        final String winnerText = "WINNER";
        float textSize = 50.0f * kx;

        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.LEFT);
        measurer.textSize(textSize);
        float textWidth = Math.max(1f, measurer.getTextWidth(winnerText));
        float textHeight = Math.max(1f, measurer.getTextHeight(winnerText));

        float padX = Math.max(1f, textSize * 0.05f);
        float padY = Math.max(1f, textSize * 0.08f);
        winnerPadX = padX;
        winnerBaselineOffsetY = padY + textHeight;
        winnerDrawW = (float) Math.ceil(textWidth + padX * 2.0f);
        winnerDrawH = (float) Math.ceil(textHeight + padY * 2.0f);

        winnerPoly = new SimplePolygon(unused -> {
            PImage image = new PImage(winnerDrawW, winnerDrawH);
            image.clear();
            image.setAntiAlias(true);
            image.setUpperText(true);
            image.noStroke();
            image.fill(255, 255, 255, 255);
            image.textAlign(TextAlign.LEFT);
            image.textSize(textSize);
            image.text(winnerText, winnerPadX, Math.max(0f, Math.min(winnerBaselineOffsetY, winnerDrawH)));
            return image;
        }, true, 0, page);
        winnerPoly.redrawNow();
    }

    private void buildWarningTiles() {
        WarningTile inverted = buildWarningTile(INVERTED_CONTROLS_TEXT, 255.0f, 120.0f, 120.0f);
        invertedControlsPoly = inverted.polygon();
        invertedControlsDrawW = inverted.width();
        invertedControlsDrawH = inverted.height();

        WarningTile swapped = buildWarningTile(CONTROL_SWAP_TEXT, 255.0f, 220.0f, 120.0f);
        controlSwapPoly = swapped.polygon();
        controlSwapDrawW = swapped.width();
        controlSwapDrawH = swapped.height();
    }

    private WarningTile buildWarningTile(String text, float red, float green, float blue) {
        final float textSize = Math.max(28.0f * kx, 18.0f);

        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.CENTER);
        measurer.textSize(textSize);

        float textWidth = Math.max(1f, measurer.getTextWidth(text));
        float textHeight = Math.max(1f, measurer.getTextHeight(text));
        float padX = Math.max(12f, textSize * 0.35f);
        float padY = Math.max(6f, textSize * 0.24f);
        float drawW = (float) Math.ceil(textWidth + (padX * 2.0f));
        float drawH = (float) Math.ceil(textHeight + (padY * 2.0f));

        SimplePolygon polygon = new SimplePolygon(unused -> {
            PImage image = new PImage(drawW, drawH);
            image.clear();
            image.setAntiAlias(true);
            image.setUpperText(true);
            image.textAlign(TextAlign.CENTER);
            image.textSize(textSize);

            image.noStroke();
            image.fill(0.0f, 0.0f, 0.0f, 170.0f);
            image.rect(0, 0, drawW, drawH);

            float borderStroke = Math.max(2f, textSize * 0.08f);
            image.stroke(red, green, blue, 255.0f);
            image.strokeWeight(borderStroke);
            image.fill(0.0f, 0.0f, 0.0f, 0.0f);
            image.rect(borderStroke * 0.5f, borderStroke * 0.5f, drawW - borderStroke, drawH - borderStroke);

            image.noStroke();
            image.fill(red, green, blue, 255.0f);
            float baselineY = Math.max(0f, Math.min((drawH * 0.5f) + (textHeight * 0.35f), drawH));
            image.text(text, drawW * 0.5f, baselineY);
            return image;
        }, true, 0, page);
        polygon.redrawNow();
        return new WarningTile(polygon, drawW, drawH);
    }

    private record WarningTile(SimplePolygon polygon, float width, float height) {
    }

    private void deletePolygon(SimplePolygon polygon) {
        if (polygon != null) {
            polygon.delete();
        }
    }

    private void deleteAll(SimplePolygon[] polygons) {
        for (SimplePolygon polygon : polygons) {
            deletePolygon(polygon);
        }
    }

    private void redraw(SimplePolygon polygon) {
        if (polygon != null) {
            polygon.setRedrawNeeded(true);
            polygon.redrawNow();
        }
    }
}
