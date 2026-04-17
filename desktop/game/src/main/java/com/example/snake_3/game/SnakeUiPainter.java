package com.example.snake_3.game;

import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;

final class SnakeUiPainter {
    static final float[] BACKGROUND = {7.0f, 12.0f, 18.0f};
    static final float[] PANEL_BORDER = {130.0f, 190.0f, 255.0f};
    static final float[] TEXT_PRIMARY = {240.0f, 240.0f, 240.0f};
    static final float[] TEXT_MUTED = {160.0f, 175.0f, 160.0f};

    private SnakeUiPainter() {
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

    static void drawKeyCap(PImage image, float x, float y, float width, float height, String label, float[] accentColor, float textSize) {
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

        drawCenteredText(image, label, x + width * 0.5f, y + height * 0.62f, textSize, TEXT_PRIMARY);
    }

    static void drawCenteredText(PImage image, String text, float centerX, float baselineY, float textSize, float[] color) {
        SnakeUiResources.applyUiFont(image);
        image.setUpperText(false);
        image.textAlign(TextAlign.CENTER);
        image.textSize(textSize);
        image.noStroke();
        image.fill(color[0], color[1], color[2], 255.0f);
        image.text(text, centerX, baselineY);
    }

    static void drawLeftText(PImage image, String text, float x, float baselineY, float textSize, float[] color) {
        SnakeUiResources.applyUiFont(image);
        image.setUpperText(false);
        image.textAlign(TextAlign.LEFT);
        image.textSize(textSize);
        image.noStroke();
        image.fill(color[0], color[1], color[2], 255.0f);
        image.text(text, x, baselineY);
    }
}
