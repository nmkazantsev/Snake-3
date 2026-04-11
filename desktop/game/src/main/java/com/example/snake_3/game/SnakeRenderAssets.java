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

    // Reduce heavy UI/FX texture allocations. Polygons are scaled up at draw time.
    private static final float UI_TEX_SCALE = 0.5f;
    private static final float FX_TEX_SCALE = 0.5f;
    private static final int FX_TEX_MAX = 512;

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
    private float scoreDrawW = 1f;
    private float scoreDrawH = 1f;

    private SimplePolygon winnerPoly0;
    private SimplePolygon winnerPoly1;
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
        if (newText == null) newText = "";
        if (newText.equals(scoreText) && scorePoly != null) return;
        scoreText = newText;
        if (scorePoly != null) scorePoly.delete();

        final String captured = scoreText;
        scorePoly = new SimplePolygon(unused -> redrawScore(captured), false, 0, page);

        float ts = sizx * 2.0f;
        float baseW = Math.max(8f, ts * 8f);
        float baseH = Math.max(8f, ts * 2.2f);
        scoreDrawW = baseH; // rotated 90deg
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
        float tlx = centerX - scoreDrawW * 0.5f;
        float tly = centerY - scoreDrawH * 0.5f;
        scorePoly.prepareAndDraw(tlx, tly, scoreDrawW, scoreDrawH, z);
    }

    public void drawWinner(int snakeId, SnakeGame game, float z) {
        SimplePolygon poly = (snakeId == 0) ? winnerPoly0 : winnerPoly1;
        if (poly == null) return;

        // Keep placement close to the original Processing calls:
        // id0: text at (x - 300*kx, y - 100*ky)
        // id1: rotate(180) then text at (-300*kx, -100*ky) => screen anchor around (300*kx, 100*ky)
        float anchorX = (snakeId == 0) ? (game.getX() - (300.0f * game.getKx())) : (300.0f * game.getKx());
        float anchorY = (snakeId == 0) ? (game.getY() - (100.0f * game.getKy())) : (100.0f * game.getKy());

        // Draw with a small offset so the text isn't half off-screen.
        poly.prepareAndDraw(anchorX, anchorY - winnerDrawH, winnerDrawW, winnerDrawH, z);
    }

    private SimplePolygon createSegmentTile(int snakeId, int bright) {
        return new SimplePolygon(unused -> {
            PImage img = new PImage(sizx, sizy);
            img.clear();
            img.setAntiAlias(true);

            img.stroke(0);
            img.strokeWeight(1.0f * kx);
            if (snakeId == 0) {
                img.fill(bright + 150, 0.0f, 0.0f);
            } else {
                img.fill(0.0f, bright + 150, 0.0f);
            }
            img.rect(0, 0, sizx, sizy);
            return img;
        }, false, 0, page);
    }

    private void buildFoodTiles() {
        foodTileWhite = new SimplePolygon(unused -> {
            PImage img = new PImage(sizx, sizy);
            img.clear();
            img.setAntiAlias(true);
            img.fill(255);
            img.stroke(255);
            img.strokeWeight(3.0f * kx);
            img.rect(0, 0, sizx, sizy);
            return img;
        }, false, 0, page);

        foodTileBlue = new SimplePolygon(unused -> {
            PImage img = new PImage(sizx, sizy);
            img.clear();
            img.setAntiAlias(true);
            img.fill(0.0f, 0.0f, 255.0f);
            img.stroke(255);
            img.strokeWeight(3.0f * kx);
            img.rect(0, 0, sizx, sizy);
            return img;
        }, false, 0, page);
    }

    private void buildMineTiles() {
        final float baseSize = pow2Clamped(sizx * 3.0f * FX_TEX_SCALE, FX_TEX_MAX);
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
        }, false, 0, page);

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
        }, false, 0, page);
    }

    private void buildExplosionTile() {
        // Large surfaces can make sizx very large; keep the base texture bounded and scale at draw time.
        final float baseSize = pow2Clamped(sizx * 30.0f * FX_TEX_SCALE, FX_TEX_MAX);
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
        }, false, 0, page);
    }

    private void buildButtonTiles() {
        buttonWideP0 = new SimplePolygon(redrawButtonWide(50, 0, 0), false, 0, page);
        buttonTallP0 = new SimplePolygon(redrawButtonTall(50, 0, 0), false, 0, page);
        buttonWideP1 = new SimplePolygon(redrawButtonWide(0, 50, 0), false, 0, page);
        buttonTallP1 = new SimplePolygon(redrawButtonTall(0, 50, 0), false, 0, page);
    }

    private Function<List<Object>, PImage> redrawButtonWide(int r, int g, int b) {
        return unused -> {
            float w = Math.max(1f, kx * 350.0f * UI_TEX_SCALE);
            float h = Math.max(1f, ky * 150.0f * UI_TEX_SCALE);
            PImage img = new PImage(w, h);
            img.clear();
            img.setAntiAlias(true);
            img.fill(r, g, b);
            img.stroke(255);
            img.strokeWeight(3.0f * kx * UI_TEX_SCALE);
            if (controlsReversed) {
                img.stroke(255.0f, 0.0f, 0.0f);
                img.strokeWeight(10.0f * kx * UI_TEX_SCALE);
            }
            img.rect(0, 0, w, h);
            return img;
        };
    }

    private Function<List<Object>, PImage> redrawButtonTall(int r, int g, int b) {
        return unused -> {
            float w = Math.max(1f, kx * 175.0f * UI_TEX_SCALE);
            float h = Math.max(1f, ky * 300.0f * UI_TEX_SCALE);
            PImage img = new PImage(w, h);
            img.clear();
            img.setAntiAlias(true);
            img.fill(r, g, b);
            img.stroke(255);
            img.strokeWeight(3.0f * kx * UI_TEX_SCALE);
            if (controlsReversed) {
                img.stroke(255.0f, 0.0f, 0.0f);
                img.strokeWeight(10.0f * kx * UI_TEX_SCALE);
            }
            img.rect(0, 0, w, h);
            return img;
        };
    }

    private PImage redrawScore(String text) {
        float ts = sizx * 2.0f;

        float baseW = Math.max(8f, ts * 8f);
        float baseH = Math.max(8f, ts * 2.2f);
        float baseTexW = Math.max(8f, baseW * UI_TEX_SCALE);
        float baseTexH = Math.max(8f, baseH * UI_TEX_SCALE);
        PImage base = new PImage(baseTexW, baseTexH);
        base.clear();
        base.setAntiAlias(true);
        base.setUpperText(false);
        base.noStroke();
        base.fill(255);
        base.textAlign(TextAlign.CENTER);
        base.textSize(ts * UI_TEX_SCALE);
        base.text(text, baseTexW * 0.5f, baseTexH * 0.75f);

        // Rotate 90 degrees into a new bitmap to match the original rotate(90)/text() behavior.
        float rotW = baseTexH;
        float rotH = baseTexW;
        PImage rot = new PImage(Math.max(8f, rotW), Math.max(8f, rotH));
        rot.clear();
        rot.setAntiAlias(true);
        rot.rotImage(base, (rot.getWidth() * 0.5f), (rot.getHeight() * 0.5f), 1.0f, Utils.radians(90.0f));
        return rot;
    }

    private void buildWinnerTiles() {
        float ts = 50.0f * kx;
        winnerDrawW = Math.max(8f, ts * 7.0f);
        winnerDrawH = Math.max(8f, ts * 1.6f);

        float texW = Math.max(8f, winnerDrawW * UI_TEX_SCALE);
        float texH = Math.max(8f, winnerDrawH * UI_TEX_SCALE);
        float texTs = ts * UI_TEX_SCALE;

        winnerPoly0 = new SimplePolygon(unused -> {
            PImage img = new PImage(texW, texH);
            img.clear();
            img.setAntiAlias(true);
            img.setUpperText(false);
            img.noStroke();
            img.fill(255);
            img.textAlign(TextAlign.CENTER);
            img.textSize(texTs);
            img.text("WINNER", texW * 0.5f, texH * 0.75f);
            return img;
        }, false, 0, page);

        winnerPoly1 = new SimplePolygon(unused -> {
            // Pre-rotate 180 degrees.
            PImage base = new PImage(texW, texH);
            base.clear();
            base.setAntiAlias(true);
            base.setUpperText(false);
            base.noStroke();
            base.fill(255);
            base.textAlign(TextAlign.CENTER);
            base.textSize(texTs);
            base.text("WINNER", texW * 0.5f, texH * 0.75f);

            PImage rot = new PImage(texW, texH);
            rot.clear();
            rot.setAntiAlias(true);
            rot.rotImage(base, texW * 0.5f, texH * 0.5f, 1.0f, Utils.radians(180.0f));
            return rot;
        }, false, 0, page);
    }

    private static float pow2Clamped(float desired, int max) {
        int v = (int) Math.ceil(desired);
        if (v < 1) v = 1;
        int p = 1;
        while (p < v && p < max) p <<= 1;
        if (p > max) p = max;
        return (float) p;
    }
}
