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
    private static final float EXPLOSION_TEX_SIZE = 256f;
    private static final float BUTTON_WIDE_TEX_W = 256f;
    private static final float BUTTON_WIDE_TEX_H = 110f;
    private static final float BUTTON_TALL_TEX_W = 128f;
    private static final float BUTTON_TALL_TEX_H = 220f;
    private static final float SCORE_TEX_W = 512f;
    private static final float SCORE_TEX_H = 160f;
    private static final float WINNER_TEX_W = 512f;
    private static final float WINNER_TEX_H = 128f;

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
    private SimplePolygon mineTileExplosion;
    private SimplePolygon explosionTile; // used for snake explosion too

    // Buttons: two sizes per player (350x150 and 175x300)
    private SimplePolygon buttonWideP0;
    private SimplePolygon buttonTallP0;
    private SimplePolygon buttonWideP1;
    private SimplePolygon buttonTallP1;

    // Text
    private SimplePolygon scorePoly;
    private String scoreText = "";
    private float scoreBaseDrawW = 1f;
    private float scoreBaseDrawH = 1f;
    private float scoreDrawW = 1f;
    private float scoreDrawH = 1f;

    private SimplePolygon winnerPoly;
    private float winnerDrawW = 1f;
    private float winnerDrawH = 1f;

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

        // Lazy caches: we rebuild on resize by dropping the polygons.
        segmentTile = new SimplePolygon[2][100];
        buildFoodTiles();
        buildMineTiles();
        buildExplosionTile();
        buildButtonTiles();
        buildWinnerTiles();

        // Score is dynamic; build a blank one.
        setScoreText("");
    }

    public void setControlsReversed(boolean controlsReversed) {
        if (this.controlsReversed == controlsReversed) return;
        this.controlsReversed = controlsReversed;
        // Button textures depend on this flag.
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

        // Deterministic draw size in screen pixels (independent from UI_TEX_SCALE).
        float ts = sizx * 2.0f;
        float baseW = Math.max(8f, ts * 8f);
        float baseH = Math.max(8f, ts * 2.2f);
        scoreBaseDrawW = baseW;
        scoreBaseDrawH = baseH;
        // We rotate the polygon by 90 degrees at draw time => AABB size swaps.
        scoreDrawW = baseH;
        scoreDrawH = baseW;
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
            float s = (sizx * 3.0f) * ((float) (Utils.millis() - mine.getExplosionTime())) / 1000.0f;
            s = Math.max(1f, s);
            mineTileExplosion.prepareAndDraw(cx - s * 0.5f, cy - s * 0.5f, s, s, z);
        }
    }

    public void drawExplosion(Explosion explosion, float z) {
        float cx = (explosion.getPx() * sizx);
        float cy = (explosion.getPy() * sizy);
        float s = Math.max(1f, explosion.getSize() * 2.0f);
        explosionTile.prepareAndDraw(cx - s * 0.5f, cy - s * 0.5f, s, s, z);
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
        // Same placement intent as the old Processing rotate(90)/text: center-ish near the left.
        float centerX = x / 20.0f;
        float centerY = (y / 2.0f) - (sizx / 2.0f);
        float aabbTlx = centerX - scoreDrawW * 0.5f;
        float aabbTly = centerY - scoreDrawH * 0.5f;
        aabbTlx = Math.max(0f, Math.min(aabbTlx, game.getX() - scoreDrawW));
        aabbTly = Math.max(0f, Math.min(aabbTly, game.getY() - scoreDrawH));

        // SimplePolygon.rotate is centered, while our layout math is AABB-based.
        // Convert the desired AABB back into a pre-rotation rect with the same center.
        float aabbCenterX = aabbTlx + scoreDrawW * 0.5f;
        float aabbCenterY = aabbTly + scoreDrawH * 0.5f;
        float rectTlx = aabbCenterX - scoreBaseDrawW * 0.5f;
        float rectTly = aabbCenterY - scoreBaseDrawH * 0.5f;

        // NOTE: SimplePolygon.prepareAndDraw signature is (rot, x, y, w, h, z).
        scorePoly.prepareAndDraw(Utils.radians(90.0f), rectTlx, rectTly, scoreBaseDrawW, scoreBaseDrawH, z);
    }

    public void drawWinner(int snakeId, SnakeGame game, float z) {
        if (winnerPoly == null) return;

        // Original sketch draws text with default LEFT baseline. Convert that intent into a top-left rect.
        float anchorX;
        float baselineY;
        if (snakeId == 0) {
            anchorX = game.getX() - (300.0f * game.getKx());
            baselineY = game.getY() - (100.0f * game.getKy());
        } else {
            anchorX = 300.0f * game.getKx();
            baselineY = 100.0f * game.getKy();
        }

        float tlx = anchorX;
        float tly = baselineY - (winnerDrawH * 0.80f);
        // Clamp into the visible screen to avoid disappearing text due to layout changes.
        tlx = Math.max(0f, Math.min(tlx, game.getX() - winnerDrawW));
        tly = Math.max(0f, Math.min(tly, game.getY() - winnerDrawH));

        float rot = (snakeId == 0) ? 0.0f : Utils.radians(180.0f);
        // NOTE: SimplePolygon.prepareAndDraw signature is (rot, x, y, w, h, z).
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
                img.ellipse(baseSize * 0.5f, baseSize * 0.5f, r, r);
            }
            return img;
        }, true, 0, page);

        mineTileExplosion = new SimplePolygon(unused -> {
            PImage img = new PImage(baseSize, baseSize);
            img.clear();
            img.setAntiAlias(true);
            img.noStroke();
            for (float r = baseSize; r > 0.0f; r -= 1.0f) {
                float t = (r / baseSize);
                img.fill((t * 105.0f) + 155.0f, 100.0f - (t * 100.0f), 0.0f, 255.0f);
                img.ellipse(baseSize * 0.5f, baseSize * 0.5f, r, r);
            }
            return img;
        }, true, 0, page);
    }

    private void buildExplosionTile() {
        final float baseSize = EXPLOSION_TEX_SIZE;
        explosionTile = new SimplePolygon(unused -> {
            PImage img = new PImage(baseSize, baseSize);
            img.clear();
            img.setAntiAlias(true);
            img.noStroke();
            for (float r = baseSize; r > 0.0f; r -= 1.0f) {
                float t = (r / baseSize);
                img.fill((t * 105.0f) + 150.0f, 100.0f - (t * 100.0f), 0.0f, (1.0f - t) * 255.0f);
                img.ellipse(baseSize * 0.5f, baseSize * 0.5f, r, r);
            }
            return img;
        }, true, 0, page);
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
        float ts = sizx * 2.0f;

        float baseW = Math.max(8f, ts * 8f);
        float baseH = Math.max(8f, ts * 2.2f);
        float baseTexW = SCORE_TEX_W;
        float baseTexH = SCORE_TEX_H;
        float rx = baseTexW / baseW;
        float ry = baseTexH / baseH;
        float rxy = Math.min(rx, ry);
        PImage base = new PImage(baseTexW, baseTexH);
        base.clear();
        base.setAntiAlias(true);
        base.setUpperText(false);
        base.noStroke();
        // "Table" background (semi-transparent black)
        base.fill(0, 0, 0, 160);
        float r = Math.max(6f, 12f * rxy);
        base.roundRect(0, 0, baseTexW, baseTexH, r, r);
        // Text color
        base.fill(255, 255, 255, 255);
        base.textAlign(TextAlign.CENTER);
        base.textSize(ts * rxy);
        float th = Math.max(1f, base.getTextHeight(text));
        float baselineY = (baseTexH * 0.5f) + (th * 0.35f);
        base.text(text, baseTexW * 0.5f, baselineY);
        // Rotations are applied at draw time via SimplePolygon.prepareAndDraw(..., rot, z).
        return base;
    }

    private void buildWinnerTiles() {
        float ts = 50.0f * kx;
        winnerDrawW = Math.max(8f, ts * 7.0f);
        winnerDrawH = Math.max(8f, ts * 1.6f);

        float texW = WINNER_TEX_W;
        float texH = WINNER_TEX_H;
        float rx = texW / winnerDrawW;
        float ry = texH / winnerDrawH;
        float rxy = Math.min(rx, ry);
        float texTs = Math.max(8f, ts * rxy);

        winnerPoly = new SimplePolygon(unused -> {
            PImage img = new PImage(texW, texH);
            img.clear();
            img.setAntiAlias(true);
            img.setUpperText(false);
            img.noStroke();
            img.fill(0, 0, 0, 160);
            float r = Math.max(6f, 12f * rxy);
            img.roundRect(0, 0, texW, texH, r, r);
            img.fill(255, 255, 255, 255);
            img.textAlign(TextAlign.CENTER);
            img.textSize(texTs);
            float th = Math.max(1f, img.getTextHeight("WINNER"));
            float baselineY = (texH * 0.5f) + (th * 0.5f);
            img.text("WINNER", texW * 0.5f, baselineY);
            return img;
        }, true, 0, page);
    }
}
