package com.example.snake_3.game;

import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;

final class SnakeIntroRenderer {
    private static final float[] PLAYER_ONE_COLOR = {220.0f, 90.0f, 90.0f};
    private static final float[] PLAYER_TWO_COLOR = {120.0f, 220.0f, 120.0f};
    private static final String[] RULE_LINES = {
            "Своё тело не убивает.",
            "Через собственные витки можно проходить без штрафа.",
            "Края арены работают как телепорт.",
            "Вышел за границу - вернёшься с другой стороны.",
            "Столкновение с телом соперника сразу проигрывает раунд.",
            "Еда даёт случайные эффекты: длина, скорость, мины, взрывы, обмен позициями и смена управления.",
            "Если погибнешь от опасного бонуса или ловушки, очко получит выживший соперник.",
            "Раунд идёт до одной смерти, после чего матч быстро стартует заново."
    };

    enum Screen {
        CONTROLS,
        RULES
    }

    private final GamePageClass page;
    private final boolean desktopPlatform;

    private float width = 1.0f;
    private float height = 1.0f;
    private Screen screen = Screen.CONTROLS;
    private SimplePolygon screenPolygon;

    SnakeIntroRenderer(GamePageClass page, boolean desktopPlatform) {
        this.page = page;
        this.desktopPlatform = desktopPlatform;
    }

    void onSurfaceChanged(int width, int height) {
        this.width = Math.max(1.0f, width);
        this.height = Math.max(1.0f, height);
        rebuildPolygon();
    }

    void setScreen(Screen screen) {
        if (this.screen == screen) {
            return;
        }
        this.screen = screen;
        redraw();
    }

    void render() {
        if (screenPolygon == null) {
            rebuildPolygon();
        }
        if (screenPolygon != null) {
            screenPolygon.prepareAndDraw(0.0f, 0.0f, width, height, 0.0f);
        }
    }

    void delete() {
        if (screenPolygon != null) {
            screenPolygon.delete();
            screenPolygon = null;
        }
    }

    private void rebuildPolygon() {
        if (screenPolygon != null) {
            screenPolygon.delete();
        }
        screenPolygon = new SimplePolygon(unused -> redrawScreen(), true, 0, page);
        screenPolygon.redrawNow();
    }

    private void redraw() {
        if (screenPolygon != null) {
            screenPolygon.setRedrawNeeded(true);
            screenPolygon.redrawNow();
        }
    }

    private PImage redrawScreen() {
        PImage image = new PImage(width, height);
        image.clear();
        image.setAntiAlias(true);
        image.background(SnakeUiPainter.BACKGROUND[0], SnakeUiPainter.BACKGROUND[1], SnakeUiPainter.BACKGROUND[2], 255.0f);
        SnakeUiResources.applyUiFont(image);

        float outerMargin = Math.max(18.0f, Math.min(width, height) * 0.03f);
        SnakeUiPainter.drawPanel(image, outerMargin, outerMargin, width - outerMargin * 2.0f, height - outerMargin * 2.0f, SnakeUiPainter.PANEL_BORDER);

        float titleSize = Math.max(30.0f, Math.min(width, height) * 0.055f);
        float subtitleSize = Math.max(18.0f, titleSize * 0.42f);
        float titleY = outerMargin + titleSize * 1.25f;
        float subtitleY = titleY + subtitleSize * 1.35f;

        SnakeUiPainter.drawCenteredText(image, "SNAKE-3", width * 0.5f, titleY, titleSize, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredText(
                image,
                screen == Screen.CONTROLS ? "ПОДГОТОВКА К РАУНДУ" : "ПРАВИЛА МАТЧА",
                width * 0.5f,
                subtitleY,
                subtitleSize,
                SnakeUiPainter.TEXT_MUTED
        );

        if (screen == Screen.CONTROLS) {
            if (desktopPlatform) {
                drawDesktopControlsScreen(image, subtitleY + subtitleSize * 0.7f);
            } else {
                drawMobileControlsScreen(image, subtitleY + subtitleSize * 0.7f);
            }
        } else {
            drawRulesScreen(image, subtitleY + subtitleSize * 0.7f);
        }
        return image;
    }

    private void drawDesktopControlsScreen(PImage image, float topY) {
        float panelGap = width * 0.04f;
        float panelWidth = (width - panelGap - (width * 0.16f)) * 0.5f;
        float panelHeight = height * 0.48f;
        float panelY = Math.max(topY + height * 0.03f, height * 0.22f);
        float leftX = width * 0.08f;
        float rightX = leftX + panelWidth + panelGap;

        drawKeyboardPanel(image, leftX, panelY, panelWidth, panelHeight, PLAYER_ONE_COLOR, "ИГРОК 1", "W A S D", new String[]{"W", "A", "S", "D"});
        drawKeyboardPanel(image, rightX, panelY, panelWidth, panelHeight, PLAYER_TWO_COLOR, "ИГРОК 2", "стрелки", new String[]{"^", "<", "v", ">"});

        float promptSize = Math.max(22.0f, Math.min(width, height) * 0.03f);
        SnakeUiPainter.drawCenteredText(
                image,
                "нажмите enter чтобы продолжить",
                width * 0.5f,
                height - Math.max(36.0f, height * 0.07f),
                promptSize,
                SnakeUiPainter.TEXT_PRIMARY
        );
    }

    private void drawKeyboardPanel(PImage image, float x, float y, float width, float height, float[] color, String title, String note, String[] labels) {
        SnakeUiPainter.drawPanel(image, x, y, width, height, color);

        float titleSize = Math.max(22.0f, height * 0.09f);
        float noteSize = Math.max(16.0f, titleSize * 0.55f);
        SnakeUiPainter.drawCenteredText(image, title, x + width * 0.5f, y + titleSize * 1.35f, titleSize, color);
        SnakeUiPainter.drawCenteredText(image, note, x + width * 0.5f, y + titleSize * 2.25f, noteSize, SnakeUiPainter.TEXT_MUTED);

        float keySize = Math.min(width * 0.18f, height * 0.22f);
        float gap = keySize * 0.16f;
        float clusterWidth = (keySize * 3.0f) + (gap * 2.0f);
        float clusterX = x + (width - clusterWidth) * 0.5f;
        float clusterY = y + height * 0.34f;
        float topKeyX = x + (width - keySize) * 0.5f;
        float topKeyY = clusterY;
        float rowY = topKeyY + keySize + gap;
        float keyTextSize = Math.max(20.0f, keySize * 0.38f);

        SnakeUiPainter.drawKeyCap(image, topKeyX, topKeyY, keySize, keySize, labels[0], color, keyTextSize);
        SnakeUiPainter.drawKeyCap(image, clusterX, rowY, keySize, keySize, labels[1], color, keyTextSize);
        SnakeUiPainter.drawKeyCap(image, clusterX + keySize + gap, rowY, keySize, keySize, labels[2], color, keyTextSize);
        SnakeUiPainter.drawKeyCap(image, clusterX + (keySize + gap) * 2.0f, rowY, keySize, keySize, labels[3], color, keyTextSize);
    }

    private void drawMobileControlsScreen(PImage image, float topY) {
        float panelMarginX = width * 0.10f;
        float panelGap = height * 0.035f;
        float panelWidth = width - panelMarginX * 2.0f;
        float panelHeight = height * 0.24f;
        float firstPanelY = Math.max(topY + height * 0.025f, height * 0.20f);
        float secondPanelY = firstPanelY + panelHeight + panelGap;

        drawTouchPanel(image, panelMarginX, firstPanelY, panelWidth, panelHeight, PLAYER_ONE_COLOR, "ИГРОК 1");
        drawTouchPanel(image, panelMarginX, secondPanelY, panelWidth, panelHeight, PLAYER_TWO_COLOR, "ИГРОК 2");

        float promptSize = Math.max(18.0f, Math.min(width, height) * 0.027f);
        SnakeUiPainter.drawCenteredText(
                image,
                "нажмите на экран, чтобы продолжить",
                width * 0.5f,
                height - Math.max(34.0f, height * 0.06f),
                promptSize,
                SnakeUiPainter.TEXT_PRIMARY
        );
    }

    private void drawTouchPanel(PImage image, float x, float y, float width, float height, float[] color, String title) {
        SnakeUiPainter.drawPanel(image, x, y, width, height, color);

        float titleSize = Math.max(18.0f, height * 0.14f);
        SnakeUiPainter.drawCenteredText(image, title, x + width * 0.5f, y + titleSize * 1.35f, titleSize, color);

        float clusterWidth = width * 0.58f;
        float clusterHeight = height * 0.52f;
        float clusterX = x + (width - clusterWidth) * 0.5f;
        float clusterY = y + height * 0.28f;
        float tallWidth = clusterWidth * 0.25f;
        float wideWidth = clusterWidth * 0.50f;
        float tallHeight = clusterHeight;
        float wideHeight = clusterHeight * 0.50f;

        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth, clusterY, wideWidth, wideHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth + wideWidth, clusterY, tallWidth, tallHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth, clusterY + wideHeight, wideWidth, wideHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX, clusterY, tallWidth, tallHeight, (int) color[0], (int) color[1], (int) color[2], false);

        float textSizeWide = Math.max(16.0f, wideHeight * 0.26f);
        float textSizeTall = Math.max(14.0f, tallWidth * 0.18f);
        SnakeUiPainter.drawCenteredText(image, "вверх", clusterX + tallWidth + wideWidth * 0.5f, clusterY + wideHeight * 0.60f, textSizeWide, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredText(image, "вправо", clusterX + tallWidth + wideWidth + tallWidth * 0.5f, clusterY + tallHeight * 0.56f, textSizeTall, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredText(image, "вниз", clusterX + tallWidth + wideWidth * 0.5f, clusterY + wideHeight + wideHeight * 0.60f, textSizeWide, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredText(image, "влево", clusterX + tallWidth * 0.5f, clusterY + tallHeight * 0.56f, textSizeTall, SnakeUiPainter.TEXT_PRIMARY);
    }

    private void drawRulesScreen(PImage image, float topY) {
        float panelX = width * 0.08f;
        float panelY = Math.max(topY + height * 0.03f, height * 0.20f);
        float panelWidth = width * 0.84f;
        float panelHeight = height * 0.58f;

        SnakeUiPainter.drawPanel(image, panelX, panelY, panelWidth, panelHeight, SnakeUiPainter.PANEL_BORDER);

        float bodyTextSize = Math.max(16.0f, Math.min(width, height) * 0.023f);
        float lineHeight = measureLineHeight(bodyTextSize) * 1.22f;
        float baselineY = panelY + bodyTextSize * 2.0f;
        float textX = panelX + panelWidth * 0.06f;
        float bulletX = panelX + panelWidth * 0.025f;

        for (String line : RULE_LINES) {
            SnakeUiPainter.drawLeftText(image, ">", bulletX, baselineY, bodyTextSize, SnakeUiPainter.TEXT_MUTED);
            SnakeUiPainter.drawLeftText(image, line, textX, baselineY, bodyTextSize, SnakeUiPainter.TEXT_PRIMARY);
            baselineY += lineHeight;
        }

        float hintSize = Math.max(18.0f, Math.min(width, height) * 0.027f);
        SnakeUiPainter.drawCenteredText(
                image,
                desktopPlatform ? "нажмите enter чтобы начать матч" : "нажмите на экран, чтобы начать матч",
                width * 0.5f,
                height - Math.max(34.0f, height * 0.06f),
                hintSize,
                SnakeUiPainter.TEXT_PRIMARY
        );
    }

    private float measureLineHeight(float textSize) {
        PImage measurer = new PImage(1, 1);
        measurer.setAntiAlias(true);
        SnakeUiResources.applyUiFont(measurer);
        measurer.setUpperText(false);
        measurer.textAlign(TextAlign.LEFT);
        measurer.textSize(textSize);
        return Math.max(1.0f, measurer.getTextHeight("Ag"));
    }
}
