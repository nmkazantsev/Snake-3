package com.example.snake_3.game;

import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.utils.Utils;

import java.util.List;
import java.util.function.Function;

/**
 * GPU-side assets: each {@link SimplePolygon} redraws its texture rarely and is then re-used via prepareAndDraw().
 */
public final class SnakeRenderAssets {
    private final GamePageClass page;

    // Fixed texture resolutions (independent from screen size).
    private static final float SEGMENT_TEX_SIZE = 64f;
    private static final float FOOD_TEX_SIZE = 64f;
    private static final float MINE_TEX_SIZE = 128f;
    private static final float BUTTON_WIDE_TEX_W = 256f;
    private static final float BUTTON_WIDE_TEX_H = 110f;
    private static final float BUTTON_TALL_TEX_W = 128f;
    private static final float BUTTON_TALL_TEX_H = 220f;

    private static final int MINE_EXP_FRAMES = 16; // 0..1s in discrete steps
    private static final int EXPLOSION_COLOR_STEPS = 64; // distance gradient quantization
    private static final float EXPLOSION_SQUARE_TEX = 8f; // small solid-color tile

    private float x = 1f;
    private float y = 1f;
    private float kx = 1f;
    private float ky = 1f;
    private float sizx = 1f;
    private float sizy = 1f;

    private boolean controlsReversed = false;

    // Tiles / sprites
    private SimplePolygon[][] segmentTile = new SimplePolygon[2][100]; // [snakeId 0..1][bright 0..99] (lazy)
    private SimplePolygon foodTileWhite;
    private SimplePolygon foodTileBlue;

    private SimplePolygon mineTileNormal;
    private SimplePolygon[] mineTileExplosionFrames = new SimplePolygon[0];
    private SimplePolygon[] explosionSquareTile = new SimplePolygon[0];

    // Buttons: two sizes per player (350x150 and 175x300)
    private SimplePolygon buttonWideP0;
    private SimplePolygon buttonTallP0;
    private SimplePolygon buttonWideP1;
    private SimplePolygon buttonTallP1;

    // Text
    private SimplePolygon scorePoly;
    private String scoreText = "";
    private float scoreDrawW = 1f;
    private float scoreDrawH = 1f;

    private SimplePolygon winnerPoly;
    private float winnerDrawW = 1f;
    private float winnerDrawH = 1f;
    private float winnerPadX = 0f;
    private float winnerBaselineOffsetY = 0f;

    public SnakeRenderAssets(GamePageClass page) {
        this.page = page;
    }

    public void onSurfaceChanged(SnakeGame game) {
        x = Math.max(1f, game.getX());
        y = Math.max(1f, game.getY());
        kx = game.getKx();
        ky = game.getKy();
        sizx = Math.max(1f, game.getSizx());
        sizy = Math.max(1f, game.getSizy());

        // Drop old GPU resources on resize.
        if (foodTileWhite != null) foodTileWhite.delete();
        if (foodTileBlue != null) foodTileBlue.delete();
        if (mineTileNormal != null) mineTileNormal.delete();
        for (SimplePolygon p : mineTileExplosionFrames) {
            if (p != null) p.delete();
        }
        for (SimplePolygon p : explosionSquareTile) {
            if (p != null) p.delete();
        }
        if (buttonWideP0 != null) buttonWideP0.delete();
        if (buttonTallP0 != null) buttonTallP0.delete();
        if (buttonWideP1 != null) buttonWideP1.delete();
        if (buttonTallP1 != null) buttonTallP1.delete();
        if (scorePoly != null) scorePoly.delete();
        scorePoly = null;
        if (winnerPoly != null) winnerPoly.delete();
        winnerPoly = null;

        // Lazy caches: we rebuild on resize by dropping the polygons.
        segmentTile = new SimplePolygon[2][100];
        buildFoodTiles();
        buildMineTiles();
        buildExplosionSquareTiles();
        buildButtonTiles();
        buildWinnerTiles();
    }

    public void setControlsReversed(boolean controlsReversed) {
        if (this.controlsReversed == controlsReversed) return;
        this.controlsReversed = controlsReversed;
        // Button and snake-segment textures depend on this flag.
        for (int sid = 0; sid < segmentTile.length; sid++) {
            for (int bi = 0; bi < segmentTile[sid].length; bi++) {
                SimplePolygon poly = segmentTile[sid][bi];
                if (poly != null) {
                    poly.setRedrawNeeded(true);
                    poly.redrawNow();
                }
            }
        }
        if (buttonWideP0 != null) buttonWideP0.setRedrawNeeded(true);
        if (buttonTallP0 != null) buttonTallP0.setRedrawNeeded(true);
        if (buttonWideP1 != null) buttonWideP1.setRedrawNeeded(true);
        if (buttonTallP1 != null) buttonTallP1.setRedrawNeeded(true);
        if (buttonWideP0 != null) buttonWideP0.redrawNow();
        if (buttonTallP0 != null) buttonTallP0.redrawNow();
        if (buttonWideP1 != null) buttonWideP1.redrawNow();
        if (buttonTallP1 != null) buttonTallP1.redrawNow();
    }

    public void setScoreText(String newText) {
        if (newText == null) newText = "no";
        if (newText.equals(scoreText) && scorePoly != null) return;
        scoreText = newText;
        if (scorePoly != null) scorePoly.delete();

        final String captured = scoreText;
        scorePoly = new SimplePolygon(unused -> redrawScore(captured), true, 0, page);
        // Make sure redrawScore() runs at least once so it can compute scoreDrawW/H.
        scorePoly.redrawNow();
    }

    public void drawSegment(int snakeId, SnakeSegment segment, float z) {
        int sid = (snakeId == 0) ? 0 : 1;
        int b = Math.max(0, Math.min(99, segment.getBright()));
        SimplePolygon poly = segmentTile[sid][b];
        if (poly == null) {
            poly = createSegmentTile(sid, b);
            segmentTile[sid][b] = poly;
        }
        poly.prepareAndDraw(segment.getPx() * sizx, segment.getPy() * sizy, sizx, sizy, z);
    }

    public void drawFood(Food food, float z) {
        SimplePolygon poly = (food.getType() == 9) ? foodTileBlue : foodTileWhite;
        poly.prepareAndDraw(food.getPx() * sizx, food.getPy() * sizy, sizx, sizy, z);
    }

    public void drawMine(Mine mine, float z) {
        float cx = (mine.getPx() * sizx) + (sizx * 0.5f);
        float cy = (mine.getPy() * sizy) + (sizy * 0.5f);

        if (mine.getExplosionTime() == 0L) {
            float s = sizx * 3.0f;
            mineTileNormal.prepareAndDraw(cx - s * 0.5f, cy - s * 0.5f, s, s, z);
        } else {
            if (mineTileExplosionFrames.length == 0) return;
            float dt = (float) (Utils.millis() - mine.getExplosionTime());
            float progress = Math.max(0f, Math.min(1f, dt / 1000.0f));
            float s = Math.max(1f, (sizx * 3.0f) * progress);

            int frames = Math.max(1, mineTileExplosionFrames.length);
            int idx = Math.max(0, Math.min(frames - 1, Math.round(progress * (frames - 1))));
            SimplePolygon poly = mineTileExplosionFrames[Math.max(0, Math.min(mineTileExplosionFrames.length - 1, idx))];
            if (poly != null) poly.prepareAndDraw(cx - s * 0.5f, cy - s * 0.5f, s, s, z);
        }
    }

    public void drawExplosion(Explosion explosion, SnakeGame game, float z) {
        // Processing Explosion.drawExpl(): half-cell squares across the whole screen, colored by distance.
        if (explosionSquareTile.length == 0) return;

        float radiusPx = explosion.getSize();
        if (radiusPx <= 0.0f) return;

        int cols2 = Math.max(1, game.getGridCols() * 2);
        int rows2 = Math.max(1, game.getGridRows() * 2);

        float stepX = sizx * 0.5f;
        float stepY = sizy * 0.5f;

        float centerPxX = explosion.getPx() * sizx;
        float centerPxY = explosion.getPy() * sizy;

        // Limit scan to a bounding box around the radius (in the same "checkHitbox point" space as the sketch).
        float pointOffsetX = sizx * 0.5f;
        float pointOffsetY = sizy * 0.5f;
        float minPointX = centerPxX - radiusPx;
        float maxPointX = centerPxX + radiusPx;
        float minPointY = centerPxY - radiusPx;
        float maxPointY = centerPxY + radiusPx;

        int iMin = (int) Math.floor((minPointX - pointOffsetX) / stepX) - 1;
        int iMax = (int) Math.ceil((maxPointX - pointOffsetX) / stepX) + 1;
        int jMin = (int) Math.floor((minPointY - pointOffsetY) / stepY) - 1;
        int jMax = (int) Math.ceil((maxPointY - pointOffsetY) / stepY) + 1;

        iMin = Math.max(0, Math.min(cols2 - 1, iMin));
        iMax = Math.max(0, Math.min(cols2 - 1, iMax));
        jMin = Math.max(0, Math.min(rows2 - 1, jMin));
        jMax = Math.max(0, Math.min(rows2 - 1, jMax));

        for (int i = iMin; i <= iMax; i++) {
            float rectX = i * stepX;
            float pointX = rectX + pointOffsetX;
            float dx = pointX - centerPxX;
            for (int j = jMin; j <= jMax; j++) {
                float rectY = j * stepY;
                float pointY = rectY + pointOffsetY;
                float dy = pointY - centerPxY;

                float dist = Utils.sqrt(Utils.sq(dx) + Utils.sq(dy));
                if (dist >= radiusPx) continue;

                float t = (dist / sizx) / 15.0f; // 0..1 while radius grows (matches sketch's color math)
                t = Math.max(0f, Math.min(1f, t));
                int idx = Math.max(0, Math.min(EXPLOSION_COLOR_STEPS - 1, Math.round(t * (EXPLOSION_COLOR_STEPS - 1))));
                SimplePolygon tile = explosionSquareTile[idx];
                if (tile != null) tile.prepareAndDraw(rectX, rectY, stepX, stepY, z);
            }
        }
    }

    public void drawButtons(SnakeGame game, int snakeId, float z) {
        SnakeButton[] btns = game.getSnakes()[snakeId].getButtons();
        for (int i = 0; i < btns.length; i++) {
            SnakeButton b = btns[i];
            boolean wide = (i == 0 || i == 2);
            SimplePolygon poly;
            if (snakeId == 0) {
                poly = wide ? buttonWideP0 : buttonTallP0;
            } else {
                poly = wide ? buttonWideP1 : buttonTallP1;
            }
            poly.prepareAndDraw(b.getPx(), b.getPy(), b.getSizx(), b.getSizy(), z);
        }
    }

    public void drawScore(SnakeGame game, float z) {
        if (scorePoly == null) return;

        // Processing drawField():
        // rotate(90); textAlign(CENTER, CENTER);
        // text(score, (y/2)-(sizx/2), -(x/20)); rotate(270);
        // => in screen coords the text center ends up at (x/20, (y/2)-(sizx/2)), rotated 90 degrees.
        float centerX = x / 20.0f;
        float centerY = (y / 2.0f) - (sizx / 2.0f);
        float tlx = centerX - (scoreDrawW * 0.5f);
        float tly = centerY - (scoreDrawH * 0.5f);
        scorePoly.prepareAndDraw(Utils.radians(90.0f), tlx, tly, scoreDrawW, scoreDrawH, z);
    }

    public void drawWinner(int snakeId, SnakeGame game, float z) {
        if (winnerPoly == null) return;

        // Processing Snake.showWiner(): default LEFT + baseline text.
        float anchorX;
        float baselineY;
        if (snakeId == 0) {
            anchorX = game.getX() - (300.0f * game.getKx());
            baselineY = game.getY() - (100.0f * game.getKy());
        } else {
            // The sketch does rotate(180) then draws at (-300*kx, -100*ky),
            // which maps to (300*kx, 100*ky) in screen space.
            anchorX = 300.0f * game.getKx();
            baselineY = 100.0f * game.getKy();
        }

        float rot = (snakeId == 0) ? 0.0f : Utils.radians(180.0f);

        // We render the word into a texture with a known (padX, baselineOffsetY) point.
        // Place that baseline-left point at the same screen coordinates as in the sketch.
        float tlx;
        float tly;
        if (snakeId == 0) {
            tlx = anchorX - winnerPadX;
            tly = baselineY - winnerBaselineOffsetY;
        } else {
            // 180deg rotation around rect center mirrors the baseline point inside the rect:
            // (ax, ay) -> (w-ax, h-ay)
            float axRot = winnerDrawW - winnerPadX;
            float ayRot = winnerDrawH - winnerBaselineOffsetY;
            tlx = anchorX - axRot;
            tly = baselineY - ayRot;
        }

        winnerPoly.prepareAndDraw(rot, tlx, tly, winnerDrawW, winnerDrawH, z);
    }

    private SimplePolygon createSegmentTile(int snakeId, int bright) {
        return new SimplePolygon(unused -> {
            float tw = SEGMENT_TEX_SIZE;
            float th = SEGMENT_TEX_SIZE;
            PImage img = new PImage(tw, th);
            img.clear();
            img.setAntiAlias(true);

            img.stroke(0);
            img.strokeWeight(1.0f);
            if (snakeId == 0) {
                img.fill(bright + 150, 0.0f, 0.0f);
            } else {
                img.fill(0.0f, bright + 150, 0.0f);
            }
            img.rect(0, 0, tw, th);

            if (controlsReversed) {
                float outerSw = Math.max(3f, Math.min(tw, th) * 0.11f);
                img.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                img.strokeWeight(outerSw);
                float outerInset = outerSw * 0.5f;
                img.rect(outerInset, outerInset, tw - outerSw, th - outerSw);

                float innerSw = Math.max(2f, Math.min(tw, th) * 0.05f);
                img.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                img.strokeWeight(innerSw);
                float innerInset = innerSw * 0.5f;
                img.rect(innerInset, innerInset, tw - innerSw, th - innerSw);
            }
            return img;
        }, true, 0, page);
    }

    private void buildFoodTiles() {
        foodTileWhite = new SimplePolygon(unused -> {
            float tw = FOOD_TEX_SIZE;
            float th = FOOD_TEX_SIZE;
            PImage img = new PImage(tw, th);
            img.clear();
            img.setAntiAlias(true);
            img.fill(255);
            img.stroke(255);
            img.strokeWeight(3.0f);
            img.rect(0, 0, tw, th);
            return img;
        }, true, 0, page);

        foodTileBlue = new SimplePolygon(unused -> {
            float tw = FOOD_TEX_SIZE;
            float th = FOOD_TEX_SIZE;
            PImage img = new PImage(tw, th);
            img.clear();
            img.setAntiAlias(true);
            img.fill(0.0f, 0.0f, 255.0f);
            img.stroke(255);
            img.strokeWeight(3.0f);
            img.rect(0, 0, tw, th);
            return img;
        }, true, 0, page);
    }

    private void buildMineTiles() {
        final float baseSize = MINE_TEX_SIZE;
        mineTileNormal = new SimplePolygon(unused -> {
            PImage img = new PImage(baseSize, baseSize);
            img.clear();
            img.setAntiAlias(true);
            img.noStroke();
            for (float r = baseSize; r > 0.0f; r -= 1.0f) {
                float t = (r / baseSize); // 1..0
                float c = 150.0f - (t * 150.0f);
                img.fill(c, c, c, 255.0f);
                // Engine ellipse() uses radii, while Processing uses diameters.
                img.ellipse(baseSize * 0.5f, baseSize * 0.5f, r * 0.5f, r * 0.5f);
            }
            return img;
        }, true, 0, page);

        mineTileExplosionFrames = new SimplePolygon[MINE_EXP_FRAMES];
        for (int fi = 0; fi < MINE_EXP_FRAMES; fi++) {
            final float progress = (MINE_EXP_FRAMES <= 1) ? 1.0f : (fi / (float) (MINE_EXP_FRAMES - 1));
            mineTileExplosionFrames[fi] = new SimplePolygon(unused -> {
                PImage img = new PImage(baseSize, baseSize);
                img.clear();
                img.setAntiAlias(true);
                img.noStroke();
                // Match Processing Mine.drawMine() explosion branch:
                // outer color intensity ramps up with explosion progress (0..1 sec).
                for (float d = baseSize; d > 0.0f; d -= 1.0f) {
                    float u = (d / baseSize); // 1..0 (diameter fraction inside the current max diameter)
                    float t = Math.max(0f, Math.min(1f, u * progress));
                    img.fill((t * 105.0f) + 155.0f, 100.0f - (t * 100.0f), 0.0f, 255.0f);
                    // Engine ellipse() uses radii, while Processing uses diameters.
                    img.ellipse(baseSize * 0.5f, baseSize * 0.5f, d * 0.5f, d * 0.5f);
                }
                return img;
            }, true, 0, page);
        }
    }

    private void buildExplosionSquareTiles() {
        explosionSquareTile = new SimplePolygon[EXPLOSION_COLOR_STEPS];
        for (int i = 0; i < EXPLOSION_COLOR_STEPS; i++) {
            final float t = (EXPLOSION_COLOR_STEPS <= 1) ? 0.0f : (i / (float) (EXPLOSION_COLOR_STEPS - 1));
            final float r = (t * 105.0f) + 150.0f;
            final float g = 100.0f - (t * 100.0f);
            explosionSquareTile[i] = new SimplePolygon(unused -> {
                PImage img = new PImage(EXPLOSION_SQUARE_TEX, EXPLOSION_SQUARE_TEX);
                img.clear();
                img.setAntiAlias(false);
                img.noStroke();
                img.fill(r, g, 0.0f, 255.0f);
                img.rect(0, 0, EXPLOSION_SQUARE_TEX, EXPLOSION_SQUARE_TEX);
                return img;
            }, true, 0, page);
        }
    }

    private void buildButtonTiles() {
        buttonWideP0 = new SimplePolygon(redrawButtonWide(50, 0, 0), true, 0, page);
        buttonTallP0 = new SimplePolygon(redrawButtonTall(50, 0, 0), true, 0, page);
        buttonWideP1 = new SimplePolygon(redrawButtonWide(0, 50, 0), true, 0, page);
        buttonTallP1 = new SimplePolygon(redrawButtonTall(0, 50, 0), true, 0, page);
    }

    private Function<List<Object>, PImage> redrawButtonWide(int r, int g, int b) {
        return unused -> {
            float w = BUTTON_WIDE_TEX_W;
            float h = BUTTON_WIDE_TEX_H;
            PImage img = new PImage(w, h);
            img.clear();
            img.setAntiAlias(true);
            img.fill(r, g, b);
            img.noStroke();
            img.rect(0, 0, w, h);

            float baseSw = Math.max(2f, Math.min(w, h) * 0.03f);
            img.stroke(255, 255, 255, 255);
            img.strokeWeight(baseSw);
            float inset = baseSw * 0.5f;
            img.rect(inset, inset, w - baseSw, h - baseSw);

            if (controlsReversed) {
                float outerSw = Math.max(6f, Math.min(w, h) * 0.16f);
                img.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                img.strokeWeight(outerSw);
                float outerInset = outerSw * 0.5f;
                img.rect(outerInset, outerInset, w - outerSw, h - outerSw);

                float innerSw = Math.max(4f, Math.min(w, h) * 0.08f);
                img.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                img.strokeWeight(innerSw);
                float innerInset = innerSw * 0.5f;
                img.rect(innerInset, innerInset, w - innerSw, h - innerSw);
            }
            return img;
        };
    }

    private Function<List<Object>, PImage> redrawButtonTall(int r, int g, int b) {
        return unused -> {
            float w = BUTTON_TALL_TEX_W;
            float h = BUTTON_TALL_TEX_H;
            PImage img = new PImage(w, h);
            img.clear();
            img.setAntiAlias(true);
            img.fill(r, g, b);
            img.noStroke();
            img.rect(0, 0, w, h);

            float baseSw = Math.max(2f, Math.min(w, h) * 0.03f);
            img.stroke(255, 255, 255, 255);
            img.strokeWeight(baseSw);
            float inset = baseSw * 0.5f;
            img.rect(inset, inset, w - baseSw, h - baseSw);

            if (controlsReversed) {
                float outerSw = Math.max(6f, Math.min(w, h) * 0.16f);
                img.stroke(255.0f, 0.0f, 0.0f, 255.0f);
                img.strokeWeight(outerSw);
                float outerInset = outerSw * 0.5f;
                img.rect(outerInset, outerInset, w - outerSw, h - outerSw);

                float innerSw = Math.max(4f, Math.min(w, h) * 0.08f);
                img.stroke(255.0f, 90.0f, 90.0f, 255.0f);
                img.strokeWeight(innerSw);
                float innerInset = innerSw * 0.5f;
                img.rect(innerInset, innerInset, w - innerSw, h - innerSw);
            }
            return img;
        };
    }

    private PImage redrawScore(String text) {
        // Processing drawField(): white text on transparent background (no table/box).
        float ts = sizx * 2.0f;

        // Measure text bounds.
        PImage meas = new PImage(1, 1);
        meas.setAntiAlias(true);
        // Desktop impl uses this flag to decide whether y is treated as a baseline or a top-offset.
        // We want baseline-like behavior to match Processing positioning.
        meas.setUpperText(true);
        meas.textAlign(TextAlign.CENTER);
        meas.textSize(ts);
        float tw = Math.max(1f, meas.getTextWidth(text));
        float th = Math.max(1f, meas.getTextHeight(text));

        float pad = Math.max(2f, ts * 0.15f);
        scoreDrawW = (float) Math.ceil(tw + pad * 2.0f);
        scoreDrawH = (float) Math.ceil(th + pad * 2.0f);

        PImage img = new PImage(scoreDrawW, scoreDrawH);
        img.clear();
        img.setAntiAlias(true);
        img.setUpperText(true);
        img.noStroke();
        img.fill(255, 255, 255, 255);
        img.textAlign(TextAlign.CENTER);
        img.textSize(ts);

        // Empirical baseline similar to the previous implementation, but with baseline-style y semantics.
        float baselineY = (scoreDrawH * 0.5f) + (th * 0.35f);
        baselineY = Math.max(0f, Math.min(baselineY, scoreDrawH));
        img.text(text, scoreDrawW * 0.5f, baselineY);
        return img;
    }

    private void buildWinnerTiles() {
        // Processing showWiner(): just white text (no background), default LEFT baseline.
        final String winnerText = "WINNER";
        float ts = 50.0f * kx;

        PImage meas = new PImage(1, 1);
        meas.setAntiAlias(true);
        meas.setUpperText(true);
        meas.textAlign(TextAlign.LEFT);
        meas.textSize(ts);
        float tw = Math.max(1f, meas.getTextWidth(winnerText));
        float th = Math.max(1f, meas.getTextHeight(winnerText));

        float padX = Math.max(1f, ts * 0.05f);
        float padY = Math.max(1f, ts * 0.08f);
        winnerPadX = padX;
        // Baseline position inside the texture (baseline-style y semantics).
        // Keep enough room for ascent above the baseline.
        winnerBaselineOffsetY = padY + th;

        winnerDrawW = (float) Math.ceil(tw + padX * 2.0f);
        winnerDrawH = (float) Math.ceil(th + padY * 2.0f);

        winnerPoly = new SimplePolygon(unused -> {
            PImage img = new PImage(winnerDrawW, winnerDrawH);
            img.clear();
            img.setAntiAlias(true);
            img.setUpperText(true);
            img.noStroke();
            img.fill(255, 255, 255, 255);
            img.textAlign(TextAlign.LEFT);
            img.textSize(ts);
            float by = Math.max(0f, Math.min(winnerBaselineOffsetY, winnerDrawH));
            img.text(winnerText, winnerPadX, by);
            return img;
        }, true, 0, page);
        winnerPoly.redrawNow();
    }
}
