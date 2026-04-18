package com.example.snake_3.game;

import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;

final class SnakeIntroRenderer {
    private static final float OUTER_MARGIN_RATIO = 0.03f;
    private static final float TITLE_ZONE_RATIO = 0.14f;
    private static final float FOOTER_ZONE_RATIO = 0.10f;
    private static final float SECTION_GAP_RATIO = 0.022f;
    private static final float CONTENT_HORIZONTAL_PADDING_RATIO = 0.055f;
    private static final float CONTENT_VERTICAL_PADDING_RATIO = 0.04f;
    private static final float PLAYER_PANEL_GAP_RATIO = 0.045f;
    private static final float TITLE_SIZE_RATIO = 0.055f;
    private static final float SUBTITLE_SIZE_RATIO = 0.022f;
    private static final float FOOTER_TEXT_RATIO = 0.028f;
    private static final float PANEL_TITLE_RATIO = 0.038f;
    private static final float PANEL_NOTE_RATIO = 0.024f;
    private static final float RULE_TEXT_RATIO = 0.022f;

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

        ScreenLayout layout = buildLayout();
        SnakeUiPainter.drawPanel(image, layout.frameX(), layout.frameY(), layout.frameWidth(), layout.frameHeight(), SnakeUiPainter.PANEL_BORDER);
        drawHeader(image, layout);

        if (screen == Screen.CONTROLS) {
            if (desktopPlatform) {
                drawDesktopControlsScreen(image, layout);
            } else {
                drawMobileControlsScreen(image, layout);
            }
        } else {
            drawRulesScreen(image, layout);
        }
        drawFooter(image, layout);
        return image;
    }

    private void drawHeader(PImage image, ScreenLayout layout) {
        float titleSize = Math.max(30.0f, Math.min(width, height) * TITLE_SIZE_RATIO);
        float subtitleSize = Math.max(16.0f, Math.min(width, height) * SUBTITLE_SIZE_RATIO);
        float titleBoxHeight = layout.titleHeight() * 0.56f;
        float subtitleBoxHeight = layout.titleHeight() - titleBoxHeight;

        SnakeUiPainter.drawCenteredText(
                image,
                "SNAKE-3",
                width * 0.5f,
                SnakeUiPainter.computeCenteredBaseline("SNAKE-3", titleSize, layout.titleY(), titleBoxHeight),
                titleSize,
                SnakeUiPainter.TEXT_PRIMARY
        );

        SnakeUiPainter.drawCenteredTextInBox(
                image,
                screen == Screen.CONTROLS ? "ПОДГОТОВКА К РАУНДУ" : "ПРАВИЛА МАТЧА",
                layout.frameX(),
                layout.titleY() + titleBoxHeight,
                layout.frameWidth(),
                subtitleBoxHeight,
                subtitleSize,
                SnakeUiPainter.TEXT_MUTED
        );
    }

    private void drawFooter(PImage image, ScreenLayout layout) {
        float promptSize = Math.max(18.0f, Math.min(width, height) * FOOTER_TEXT_RATIO);
        String promptText;
        if (screen == Screen.CONTROLS) {
            promptText = desktopPlatform ? "нажмите enter чтобы продолжить" : "нажмите на экран, чтобы продолжить";
        } else {
            promptText = desktopPlatform ? "нажмите enter чтобы начать матч" : "нажмите на экран, чтобы начать матч";
        }

        SnakeUiPainter.drawCenteredTextInBox(
                image,
                promptText,
                layout.frameX(),
                layout.footerY(),
                layout.frameWidth(),
                layout.footerHeight(),
                promptSize,
                SnakeUiPainter.TEXT_PRIMARY
        );
    }

    private void drawDesktopControlsScreen(PImage image, ScreenLayout layout) {
        float panelGap = layout.contentWidth() * PLAYER_PANEL_GAP_RATIO;
        float panelWidth = (layout.contentWidth() - panelGap) * 0.5f;
        float panelHeight = layout.contentHeight();
        float leftX = layout.contentX();
        float rightX = leftX + panelWidth + panelGap;

        drawKeyboardPanel(image, leftX, layout.contentY(), panelWidth, panelHeight, PLAYER_ONE_COLOR, "ИГРОК 1", "W A S D", true);
        drawKeyboardPanel(image, rightX, layout.contentY(), panelWidth, panelHeight, PLAYER_TWO_COLOR, "ИГРОК 2", "стрелки", false);
    }

    private void drawKeyboardPanel(PImage image, float x, float y, float width, float height, float[] color, String title, String note, boolean letterKeys) {
        SnakeUiPainter.drawPanel(image, x, y, width, height, color);

        float panelPaddingX = width * 0.08f;
        float panelPaddingY = height * 0.08f;
        float headerHeight = height * 0.20f;
        float clusterAreaY = y + panelPaddingY + headerHeight;
        float clusterAreaHeight = height - (panelPaddingY * 2.0f) - headerHeight;
        float titleSize = Math.max(22.0f, Math.min(width, height) * PANEL_TITLE_RATIO);
        float noteSize = Math.max(16.0f, Math.min(width, height) * PANEL_NOTE_RATIO);

        SnakeUiPainter.drawCenteredTextInBox(image, title, x + panelPaddingX, y + panelPaddingY, width - panelPaddingX * 2.0f, headerHeight * 0.56f, titleSize, color);
        SnakeUiPainter.drawCenteredTextInBox(image, note, x + panelPaddingX, y + panelPaddingY + (headerHeight * 0.56f), width - panelPaddingX * 2.0f, headerHeight * 0.44f, noteSize, SnakeUiPainter.TEXT_MUTED);

        float keySize = Math.min(width * 0.18f, clusterAreaHeight * 0.32f);
        float gap = keySize * 0.16f;
        float clusterWidth = (keySize * 3.0f) + (gap * 2.0f);
        float clusterHeight = (keySize * 2.0f) + gap;
        float clusterX = x + (width - clusterWidth) * 0.5f;
        float clusterY = clusterAreaY + ((clusterAreaHeight - clusterHeight) * 0.5f);
        float topKeyX = x + (width - keySize) * 0.5f;
        float topKeyY = clusterY;
        float rowY = topKeyY + keySize + gap;
        float keyTextSize = Math.max(18.0f, keySize * 0.34f);

        if (letterKeys) {
            SnakeUiPainter.drawKeyCapText(image, topKeyX, topKeyY, keySize, keySize, "W", color, keyTextSize);
            SnakeUiPainter.drawKeyCapText(image, clusterX, rowY, keySize, keySize, "A", color, keyTextSize);
            SnakeUiPainter.drawKeyCapText(image, clusterX + keySize + gap, rowY, keySize, keySize, "S", color, keyTextSize);
            SnakeUiPainter.drawKeyCapText(image, clusterX + (keySize + gap) * 2.0f, rowY, keySize, keySize, "D", color, keyTextSize);
            return;
        }

        SnakeUiPainter.drawKeyCapArrow(image, topKeyX, topKeyY, keySize, keySize, SnakeUiPainter.ArrowDirection.UP, color);
        SnakeUiPainter.drawKeyCapArrow(image, clusterX, rowY, keySize, keySize, SnakeUiPainter.ArrowDirection.LEFT, color);
        SnakeUiPainter.drawKeyCapArrow(image, clusterX + keySize + gap, rowY, keySize, keySize, SnakeUiPainter.ArrowDirection.DOWN, color);
        SnakeUiPainter.drawKeyCapArrow(image, clusterX + (keySize + gap) * 2.0f, rowY, keySize, keySize, SnakeUiPainter.ArrowDirection.RIGHT, color);
    }

    private void drawMobileControlsScreen(PImage image, ScreenLayout layout) {
        float panelMarginX = layout.contentWidth() * 0.02f;
        float panelGap = layout.contentHeight() * 0.07f;
        float panelWidth = layout.contentWidth() - panelMarginX * 2.0f;
        float panelHeight = (layout.contentHeight() - panelGap) * 0.5f;
        float firstPanelY = layout.contentY();
        float secondPanelY = firstPanelY + panelHeight + panelGap;

        drawTouchPanel(image, layout.contentX() + panelMarginX, firstPanelY, panelWidth, panelHeight, PLAYER_ONE_COLOR, "ИГРОК 1");
        drawTouchPanel(image, layout.contentX() + panelMarginX, secondPanelY, panelWidth, panelHeight, PLAYER_TWO_COLOR, "ИГРОК 2");
    }

    private void drawTouchPanel(PImage image, float x, float y, float width, float height, float[] color, String title) {
        SnakeUiPainter.drawPanel(image, x, y, width, height, color);

        float titleSize = Math.max(18.0f, height * 0.14f);
        float titleBoxHeight = height * 0.20f;
        SnakeUiPainter.drawCenteredTextInBox(image, title, x, y + (height * 0.06f), width, titleBoxHeight, titleSize, color);

        float clusterWidth = width * 0.58f;
        float clusterHeight = height * 0.44f;
        float clusterX = x + (width - clusterWidth) * 0.5f;
        float clusterY = y + height * 0.34f;
        float tallWidth = clusterWidth * 0.25f;
        float wideWidth = clusterWidth * 0.50f;
        float tallHeight = clusterHeight;
        float wideHeight = clusterHeight * 0.50f;

        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth, clusterY, wideWidth, wideHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth + wideWidth, clusterY, tallWidth, tallHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX + tallWidth, clusterY + wideHeight, wideWidth, wideHeight, (int) color[0], (int) color[1], (int) color[2], false);
        SnakeUiPainter.drawGameplayButton(image, clusterX, clusterY, tallWidth, tallHeight, (int) color[0], (int) color[1], (int) color[2], false);

        float textSizeWide = Math.max(16.0f, wideHeight * 0.20f);
        float textSizeTall = Math.max(14.0f, tallWidth * 0.16f);
        SnakeUiPainter.drawCenteredTextInBox(image, "вверх", clusterX + tallWidth, clusterY, wideWidth, wideHeight, textSizeWide, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredTextInBox(image, "вправо", clusterX + tallWidth + wideWidth, clusterY, tallWidth, tallHeight, textSizeTall, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredTextInBox(image, "вниз", clusterX + tallWidth, clusterY + wideHeight, wideWidth, wideHeight, textSizeWide, SnakeUiPainter.TEXT_PRIMARY);
        SnakeUiPainter.drawCenteredTextInBox(image, "влево", clusterX, clusterY, tallWidth, tallHeight, textSizeTall, SnakeUiPainter.TEXT_PRIMARY);
    }

    private void drawRulesScreen(PImage image, ScreenLayout layout) {
        float panelX = layout.contentX();
        float panelY = layout.contentY();
        float panelWidth = layout.contentWidth();
        float panelHeight = layout.contentHeight();

        SnakeUiPainter.drawPanel(image, panelX, panelY, panelWidth, panelHeight, SnakeUiPainter.PANEL_BORDER);

        float bodyTextSize = Math.max(16.0f, Math.min(width, height) * RULE_TEXT_RATIO);
        float lineHeight = SnakeUiPainter.measureTextHeight("Аg", bodyTextSize) * 1.28f;
        float contentPaddingX = panelWidth * 0.055f;
        float contentPaddingTop = panelHeight * 0.08f;
        float textX = panelX + contentPaddingX;
        float bulletX = panelX + (contentPaddingX * 0.34f);
        float lineTopY = panelY + contentPaddingTop;

        for (String line : RULE_LINES) {
            SnakeUiPainter.drawTopAlignedLeftText(image, ">", bulletX, lineTopY, bodyTextSize, SnakeUiPainter.TEXT_MUTED);
            SnakeUiPainter.drawTopAlignedLeftText(image, line, textX, lineTopY, bodyTextSize, SnakeUiPainter.TEXT_PRIMARY);
            lineTopY += lineHeight;
        }
    }

    private ScreenLayout buildLayout() {
        float outerMargin = Math.max(18.0f, Math.min(width, height) * OUTER_MARGIN_RATIO);
        float sectionGap = Math.max(12.0f, Math.min(width, height) * SECTION_GAP_RATIO);
        float frameX = outerMargin;
        float frameY = outerMargin;
        float frameWidth = width - outerMargin * 2.0f;
        float frameHeight = height - outerMargin * 2.0f;
        float titleHeight = Math.max(84.0f, frameHeight * TITLE_ZONE_RATIO);
        float footerHeight = Math.max(62.0f, frameHeight * FOOTER_ZONE_RATIO);
        float contentX = frameX + Math.max(20.0f, frameWidth * CONTENT_HORIZONTAL_PADDING_RATIO);
        float contentY = frameY + titleHeight + sectionGap;
        float contentWidth = frameWidth - (contentX - frameX) * 2.0f;
        float contentHeight = frameHeight - titleHeight - footerHeight - sectionGap * 2.0f - Math.max(14.0f, frameHeight * CONTENT_VERTICAL_PADDING_RATIO);
        float footerY = frameY + frameHeight - footerHeight - Math.max(8.0f, frameHeight * 0.01f);
        return new ScreenLayout(frameX, frameY, frameWidth, frameHeight, frameY + Math.max(8.0f, frameHeight * 0.01f), titleHeight, contentX, contentY, contentWidth, Math.max(1.0f, contentHeight), footerY, footerHeight);
    }

    private record ScreenLayout(
            float frameX,
            float frameY,
            float frameWidth,
            float frameHeight,
            float titleY,
            float titleHeight,
            float contentX,
            float contentY,
            float contentWidth,
            float contentHeight,
            float footerY,
            float footerHeight
    ) {
    }
}
