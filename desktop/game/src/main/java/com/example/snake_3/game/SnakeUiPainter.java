package com.example.snake_3.game;

import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;

import java.lang.reflect.Method;

final class SnakeUiPainter {
    static final float[] BACKGROUND = {7.0f, 12.0f, 18.0f};
    static final float[] PANEL_BORDER = {130.0f, 190.0f, 255.0f};
    static final float[] TEXT_PRIMARY = {240.0f, 240.0f, 240.0f};
    static final float[] TEXT_MUTED = {160.0f, 175.0f, 160.0f};
    private static final float FALLBACK_ASCENT_RATIO = 0.78f;

    private SnakeUiPainter() {
    }

    enum ArrowDirection {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    static void drawPanel(PImage image, float x, float y, float width, float height, float[] borderColor) {
        float outerStroke = Math.max(2.0f, Math.min(width, height) * 0.012f);
        float innerStroke = Math.max(1.0f, outerStroke * 0.5f);

        image.noStroke();
        image.fill(0.0f, 0.0f, 0.0f, 220.0f);
        image.rect(x, y, width, height);

        image.stroke(borderColor[0], borderColor[1], borderColor[2], 255.0f);
        image.strokeWeight(outerStroke);
        image.fill(0.0f, 0.0f, 0.0f, 0.0f);
        image.rect(x + outerStroke * 0.5f, y + outerStroke * 0.5f, width - outerStroke, height - outerStroke);

        image.stroke(255.0f, 255.0f, 255.0f, 140.0f);
        image.strokeWeight(innerStroke);
        float inset = outerStroke + innerStroke * 0.5f;
        image.rect(x + inset, y + inset, width - inset * 2.0f, height - inset * 2.0f);
    }

    static void drawGameplayButton(PImage image, float x, float y, float width, float height, int red, int green, int blue, boolean controlsReversed) {
        image.fill(red, green, blue);
        image.noStroke();
        image.rect(x, y, width, height);

        float baseStroke = Math.max(2f, Math.min(width, height) * 0.03f);
        image.stroke(255, 255, 255, 255);
        image.strokeWeight(baseStroke);
        image.rect(x + baseStroke * 0.5f, y + baseStroke * 0.5f, width - baseStroke, height - baseStroke);

        if (controlsReversed) {
            float outerStroke = Math.max(6f, Math.min(width, height) * 0.16f);
            image.stroke(255.0f, 0.0f, 0.0f, 255.0f);
            image.strokeWeight(outerStroke);
            image.rect(x + outerStroke * 0.5f, y + outerStroke * 0.5f, width - outerStroke, height - outerStroke);

            float innerStroke = Math.max(4f, Math.min(width, height) * 0.08f);
            image.stroke(255.0f, 90.0f, 90.0f, 255.0f);
            image.strokeWeight(innerStroke);
            image.rect(x + innerStroke * 0.5f, y + innerStroke * 0.5f, width - innerStroke, height - innerStroke);
        }
    }

    static void drawKeyCap(PImage image, float x, float y, float width, float height, float[] accentColor) {
        float frameStroke = Math.max(2f, Math.min(width, height) * 0.06f);

        image.noStroke();
        image.fill(12.0f, 16.0f, 24.0f, 255.0f);
        image.rect(x, y, width, height);

        image.stroke(255.0f, 255.0f, 255.0f, 255.0f);
        image.strokeWeight(frameStroke);
        image.fill(0.0f, 0.0f, 0.0f, 0.0f);
        image.rect(x + frameStroke * 0.5f, y + frameStroke * 0.5f, width - frameStroke, height - frameStroke);

        image.stroke(accentColor[0], accentColor[1], accentColor[2], 255.0f);
        image.strokeWeight(Math.max(1.0f, frameStroke * 0.45f));
        float inset = frameStroke + 2.0f;
        image.rect(x + inset, y + inset, width - inset * 2.0f, height - inset * 2.0f);
    }

    static void drawKeyCapText(PImage image, float x, float y, float width, float height, String label, float[] accentColor, float textSize) {
        drawKeyCap(image, x, y, width, height, accentColor);
        drawCenteredTextInBox(image, label, x, y, width, height, textSize, TEXT_PRIMARY);
    }

    static void drawKeyCapArrow(PImage image, float x, float y, float width, float height, ArrowDirection direction, float[] accentColor) {
        drawKeyCap(image, x, y, width, height, accentColor);
        drawArrowIcon(image, x, y, width, height, direction, TEXT_PRIMARY);
    }

    static void drawCenteredText(PImage image, String text, float centerX, float baselineY, float textSize, float[] color) {
        SnakeUiResources.applyUiFont(image);
        image.setUpperText(true);
        image.textAlign(TextAlign.CENTER);
        image.textSize(textSize);
        image.noStroke();
        image.fill(color[0], color[1], color[2], 255.0f);
        image.text(text, centerX, baselineY);
    }

    static void drawLeftText(PImage image, String text, float x, float baselineY, float textSize, float[] color) {
        SnakeUiResources.applyUiFont(image);
        image.setUpperText(true);
        image.textAlign(TextAlign.LEFT);
        image.textSize(textSize);
        image.noStroke();
        image.fill(color[0], color[1], color[2], 255.0f);
        image.text(text, x, baselineY);
    }

    static void drawCenteredTextInBox(PImage image, String text, float x, float y, float width, float height, float textSize, float[] color) {
        float baselineY = computeCenteredBaseline(text, textSize, y, height);
        drawCenteredText(image, text, x + width * 0.5f, baselineY, textSize, color);
    }

    static void drawTopAlignedLeftText(PImage image, String text, float x, float topY, float textSize, float[] color) {
        float baselineY = computeTopAlignedBaseline(text, textSize, topY);
        drawLeftText(image, text, x, baselineY, textSize, color);
    }

    static float measureTextHeight(String text, float textSize) {
        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        SnakeUiResources.applyUiFont(measurer);
        measurer.setUpperText(true);
        measurer.textAlign(TextAlign.LEFT);
        measurer.textSize(textSize);
        return Math.max(1.0f, measurer.getTextHeight(text));
    }

    static float computeCenteredBaseline(String text, float textSize, float boxY, float boxHeight) {
        FontMetricsSnapshot metrics = readFontMetrics();
        if (metrics != null) {
            return boxY + ((boxHeight - metrics.lineHeight()) * 0.5f) - metrics.ascent();
        }

        float textHeight = measureTextHeight(text, textSize);
        return boxY + ((boxHeight - textHeight) * 0.5f) + (textHeight * FALLBACK_ASCENT_RATIO);
    }

    static float computeTopAlignedBaseline(String text, float textSize, float topY) {
        FontMetricsSnapshot metrics = readFontMetrics();
        if (metrics != null) {
            return topY - metrics.ascent();
        }

        float textHeight = measureTextHeight(text, textSize);
        return topY + (textHeight * FALLBACK_ASCENT_RATIO);
    }

    private static void drawArrowIcon(PImage image, float x, float y, float width, float height, ArrowDirection direction, float[] color) {
        int[][] pattern = switch (direction) {
            case UP -> new int[][]{
                    {0, 0, 1, 0, 0},
                    {0, 1, 1, 1, 0},
                    {1, 0, 1, 0, 1},
                    {0, 0, 1, 0, 0},
                    {0, 0, 1, 0, 0}
            };
            case RIGHT -> new int[][]{
                    {0, 0, 1, 0, 0},
                    {0, 0, 1, 1, 0},
                    {1, 1, 1, 1, 1},
                    {0, 0, 1, 1, 0},
                    {0, 0, 1, 0, 0}
            };
            case DOWN -> new int[][]{
                    {0, 0, 1, 0, 0},
                    {0, 0, 1, 0, 0},
                    {1, 0, 1, 0, 1},
                    {0, 1, 1, 1, 0},
                    {0, 0, 1, 0, 0}
            };
            case LEFT -> new int[][]{
                    {0, 0, 1, 0, 0},
                    {0, 1, 1, 0, 0},
                    {1, 1, 1, 1, 1},
                    {0, 1, 1, 0, 0},
                    {0, 0, 1, 0, 0}
            };
        };

        float drawable = Math.min(width, height) * 0.44f;
        float pixel = Math.max(2.0f, drawable / pattern.length);
        float drawW = pattern[0].length * pixel;
        float drawH = pattern.length * pixel;
        float startX = x + ((width - drawW) * 0.5f);
        float startY = y + ((height - drawH) * 0.5f);

        image.noStroke();
        image.fill(color[0], color[1], color[2], 255.0f);
        for (int row = 0; row < pattern.length; row++) {
            for (int column = 0; column < pattern[row].length; column++) {
                if (pattern[row][column] == 0) {
                    continue;
                }
                image.rect(startX + (column * pixel), startY + (row * pixel), pixel, pixel);
            }
        }
    }

    private static FontMetricsSnapshot readFontMetrics() {
        try {
            Object platformFont = SnakeUiResources.getPlatformFont();
            if (platformFont == null) {
                return null;
            }
            Method getMetrics = platformFont.getClass().getMethod("getMetrics");
            Object metrics = getMetrics.invoke(platformFont);
            Method getAscent = metrics.getClass().getMethod("getAscent");
            Method getDescent = metrics.getClass().getMethod("getDescent");
            float ascent = ((Number) getAscent.invoke(metrics)).floatValue();
            float descent = ((Number) getDescent.invoke(metrics)).floatValue();
            return new FontMetricsSnapshot(ascent, descent);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private record FontMetricsSnapshot(float ascent, float descent) {
        float lineHeight() {
            return descent - ascent;
        }
    }
}
