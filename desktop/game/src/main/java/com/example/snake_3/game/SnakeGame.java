package com.example.snake_3.game;

import com.nikitos.utils.Utils;

/**
 * Engine-native port of the original Processing Snake3 sketch.
 * <p>
 * Notes:
 * - The original sketch advanced logic after drawing; MainRenderer preserves that order.
 * - Playfield is dynamically clamped to never overlap the button areas.
 */
public final class SnakeGame {
    private static final int MOBILE_MAX_SQUARES = 40;
    private static final int DESKTOP_MAX_SQUARES = 80;
    private static final int INITIAL_FOOD_COUNT = 2;
    private static final float DEFAULT_SPAWN_ROW_RATIO = 0.28f;

    private final int playingUsers = 2;
    private final boolean desktopPlatform;
    private final int maxSquares;

    private boolean initialized = false;

    private float x = 1f;
    private float y = 1f;
    private float kx = 1f;
    private float ky = 1f;
    private float sizx = 1f;
    private float sizy = 1f;

    private float timek = 1f;
    private long prevFrame = 0L;

    private boolean resetting = false;
    private long startReset = 0L;

    private boolean controlsReversed = false;
    private long reverseStarted = 0L;
    private final long revTime = 10_000L;

    private long buttonsReverted = 0L;
    private final long butRevTime = 5_000L;

    private final Snake[] snakes = new Snake[4];
    private Food[] foods = new Food[INITIAL_FOOD_COUNT];
    private final Mine[] mines = new Mine[50];
    private int mineLen = 0;

    public SnakeGame(boolean desktopPlatform) {
        this.desktopPlatform = desktopPlatform;
        this.maxSquares = desktopPlatform ? DESKTOP_MAX_SQUARES : MOBILE_MAX_SQUARES;
    }

    public void onSurfaceChanged(int width, int height) {
        updateMetrics(width, height);

        // IMPORTANT: onSurfaceChanged() can be called many times (window resize, reopen, orientation changes).
        // Gameplay state must not reset here; only size-dependent metrics/resources should.
        if (!initialized) {
            initGame();
            initialized = true;
        } else {
            // Recompute UI layout in the current "mode" (normal vs reverted) using the new metrics.
            if (isTouchControlsEnabled()) {
                applyButtonsLayoutForCurrentState();
            }
            clampEntitiesToPlayfield();
        }
    }

    private void updateMetrics(int width, int height) {
        float ux = Utils.getX();
        float uy = Utils.getY();

        x = (ux > 0) ? ux : Math.max(1f, width);
        y = (uy > 0) ? uy : Math.max(1f, height);
        if (x <= 0) x = 1f;
        if (y <= 0) y = 1f;

        ky = y / 1280.0f;
        kx = x / 720.0f;
        sizx = x / Math.max(1.0f, (float) maxSquares);
        sizy = sizx;
    }

    private void initGame() {
        mineLen = 0;
        foods = new Food[INITIAL_FOOD_COUNT];
        resetting = false;
        controlsReversed = false;
        reverseStarted = 0L;
        buttonsReverted = 0L;
        prevFrame = Utils.millis();
        timek = 1f;

        for (int i = 0; i < snakes.length; i++) {
            snakes[i] = new Snake(i);
            snakes[i].init(this);
        }
        for (int i = 0; i < foods.length; i++) {
            foods[i] = new Food(this);
        }
    }

    private void applyButtonsLayoutForCurrentState() {
        for (Snake s : snakes) {
            if (s == null) continue;
            if (isButtonsRevertedActive()) s.revertButtons(this);
            else s.createButtons(this);
        }
    }

    private void clampEntitiesToPlayfield() {
        int cols = getGridCols();
        int[] rr = getPlayfieldRowRange();
        int minRow = rr[0];
        int maxRow = rr[1];

        for (int si = 0; si < playingUsers; si++) {
            Snake s = snakes[si];
            if (s == null) continue;
            SnakeSegment[] segs = s.getSegments();
            int len = s.getLength();
            for (int i = 0; i < len; i++) {
                SnakeSegment seg = segs[i];
                if (seg == null) continue;
                int px = seg.getPx();
                int py = seg.getPy();
                if (px < 0) px = cols - 1;
                if (px >= cols) px = 0;
                if (py < minRow) py = maxRow;
                if (py > maxRow) py = minRow;
                seg.setPx(px);
                seg.setPy(py);
            }
        }

        for (int i = 0; i < foods.length; i++) {
            Food f = foods[i];
            if (f == null) continue;
            int px = f.getPx();
            int py = f.getPy();
            if (px < 0) px = cols - 1;
            if (px >= cols) px = 0;
            if (py < minRow) py = minRow;
            if (py > maxRow) py = maxRow;
            f.setPx(px);
            f.setPy(py);
        }

        for (int i = 0; i < mineLen; i++) {
            Mine m = mines[i];
            if (m == null) continue;
            int px = m.getPx();
            int py = m.getPy();
            if (px < 0) px = cols - 1;
            if (px >= cols) px = 0;
            if (py < minRow) py = minRow;
            if (py > maxRow) py = maxRow;
            m.setPx(px);
            m.setPy(py);
        }
    }

    public void detectTimek() {
        long now = Utils.millis();
        timek = Utils.map((float) (now - prevFrame), 17.0f, 34.0f, 1.0f, 2.0f);
        prevFrame = now;
    }

    public void logic() {
        checkReset();
        if (!resetting) {
            for (int i = 0; i < playingUsers; i++) {
                snakes[i].move(this);
            }
        }

        // Mine lifecycle: original code deleted mines from draw() after 1 second of explosion.
        // We preserve that behavior in logic() since we no longer redraw mines on CPU each frame.
        for (int i = 0; i < mineLen; i++) {
            Mine m = mines[i];
            if (m != null && m.shouldDelete(this)) {
                deleteMine(m.getId());
                // Array has shifted; restart scan to keep behavior stable.
                i = -1;
            }
        }

        for (int i = 0; i < foods.length; i++) {
            if (foods[i] == null) foods[i] = new Food(this);
        }
        if (Utils.millis() - reverseStarted > revTime) {
            controlsReversed = false;
        }
        if (isTouchControlsEnabled() && buttonsReverted > 0L && !isButtonsRevertedActive()) {
            for (Snake s : snakes) {
                if (s != null) s.createButtons(this);
            }
            buttonsReverted = 0L;
        }
    }

    private void checkReset() {
        if (countAlive() < 2 && !resetting) {
            startReset = Utils.millis();
            resetting = true;
        }
        if (!resetting) return;
        if (Utils.millis() - startReset <= 1000) return;
        resetting = false;
        resetRound();
    }

    private int countAlive() {
        int alive = 0;
        for (int i = 0; i < playingUsers; i++) {
            if (!snakes[i].isDied()) alive++;
        }
        return alive;
    }

    private void resetRound() {
        for (int i = 0; i < playingUsers; i++) {
            snakes[i].reset(this);
        }
        for (int i = 0; i < foods.length; i++) {
            foods[i] = new Food(this);
        }
        controlsReversed = false;
    }

    public void addMine(int px, int py) {
        if (mineLen < mines.length - 1) {
            mines[mineLen] = new Mine(px, py, mineLen);
            mineLen++;
        }
    }

    public void deleteMine(int id) {
        if (mineLen <= 0) return;
        if (id < 0) id = 0;
        if (id >= mineLen) id = mineLen - 1;
        int i = id;
        while (i < mineLen) {
            mines[i] = mines[i + 1];
            i++;
        }
        mineLen--;
    }

    public boolean checkAllMines(int px, int py) {
        for (int i = 0; i < mineLen; i++) {
            if (mines[i].checkMine(this, px, py)) {
                return true;
            }
        }
        return false;
    }

    public void addSegmentsToOthers(Snake snake, int n) {
        for (Snake s : snakes) {
            if (s != null && s != snake) s.addSegments(this, n);
        }
    }

    /**
     * Returns the inclusive [minRow, maxRow] where the snakes/food are allowed to exist.
     * Clamped so the playfield never overlaps the current button areas.
     */
    public int[] getPlayfieldRowRange() {
        int cols = Math.max(1, Utils.parseInt(x / sizx));
        int rows = Math.max(1, Utils.parseInt(y / sizy));

        if (!isTouchControlsEnabled()) {
            return new int[]{0, rows - 1};
        }

        // Determine top/bottom reserved areas from the current button rectangles.
        float reservedTopMax = 0f;
        float reservedBottomMin = y;
        float midY = y * 0.5f;

        for (int si = 0; si < playingUsers; si++) {
            SnakeButton[] btns = snakes[si].getButtons();
            for (int bi = 0; bi < btns.length; bi++) {
                SnakeButton b = btns[bi];
                if (b == null) continue;
                if (b.getPy() < midY) {
                    reservedTopMax = Math.max(reservedTopMax, b.getPy() + b.getSizy());
                } else {
                    reservedBottomMin = Math.min(reservedBottomMin, b.getPy());
                }
            }
        }

        int minRow = (int) Math.ceil(reservedTopMax / sizy);
        int maxRow = (int) Math.floor((reservedBottomMin - sizy) / sizy);

        minRow = Math.max(0, Math.min(minRow, rows - 1));
        maxRow = Math.max(0, Math.min(maxRow, rows - 1));

        // If something goes wrong (overlap), fall back to the whole screen.
        if (maxRow < minRow) {
            minRow = 0;
            maxRow = rows - 1;
        }

        // Keep at least one row.
        if (maxRow == minRow) {
            maxRow = Math.min(rows - 1, minRow);
        }

        // Keep at least one column (used by callers).
        // cols is kept for symmetry; it affects food spawns only.
        if (cols < 1) cols = 1;

        return new int[]{minRow, maxRow};
    }

    public int getMinPlayableCol() {
        return (getGridCols() > 1) ? 1 : 0;
    }

    public int getMaxPlayableCol() {
        return Math.max(getMinPlayableCol(), getGridCols() - 1);
    }

    public int getDefaultSpawnRow() {
        int[] rowRange = getPlayfieldRowRange();
        int playableRows = Math.max(0, rowRange[1] - rowRange[0]);
        return rowRange[0] + Math.round(playableRows * DEFAULT_SPAWN_ROW_RATIO);
    }

    public int getLeftSpawnCol() {
        return Math.min(getMinPlayableCol() + 1, getMaxPlayableCol());
    }

    public int getRightSpawnCol() {
        return getMaxPlayableCol();
    }

    public int getRandomPlayableCol() {
        int minCol = getMinPlayableCol();
        int maxCol = getMaxPlayableCol();
        if (maxCol <= minCol) return minCol;
        return Utils.parseInt(Utils.random((float) minCol, (float) (maxCol + 1)));
    }

    public int getRandomPlayableRow() {
        int[] rowRange = getPlayfieldRowRange();
        int minRow = rowRange[0];
        int maxRow = rowRange[1];
        if (maxRow <= minRow) return minRow;
        return Utils.parseInt(Utils.random((float) minRow, (float) (maxRow + 1)));
    }

    public boolean isTouchControlsEnabled() {
        return !desktopPlatform;
    }

    public int getMaxSquares() {
        return maxSquares;
    }

    public int getGridCols() {
        return Math.max(1, Utils.parseInt(x / sizx));
    }

    public int getGridRows() {
        return Math.max(1, Utils.parseInt(y / sizy));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getKx() {
        return kx;
    }

    public float getKy() {
        return ky;
    }

    public float getSizx() {
        return sizx;
    }

    public float getSizy() {
        return sizy;
    }

    public float getTimek() {
        return timek;
    }

    public int getPlayingUsers() {
        return playingUsers;
    }

    public Snake[] getSnakes() {
        return snakes;
    }

    public Food[] getFoods() {
        return foods;
    }

    public void setFoods(Food[] foods) {
        this.foods = foods;
    }

    public Mine[] getMines() {
        return mines;
    }

    public int getMineLen() {
        return mineLen;
    }

    public boolean isResetting() {
        return resetting;
    }

    public boolean isControlsReversed() {
        return controlsReversed;
    }

    public void setControlsReversed(boolean controlsReversed) {
        this.controlsReversed = controlsReversed;
    }

    public void setReverseStarted(long reverseStarted) {
        this.reverseStarted = reverseStarted;
    }

    public long getButtonsReverted() {
        return buttonsReverted;
    }

    public void setButtonsReverted(long buttonsReverted) {
        this.buttonsReverted = buttonsReverted;
    }

    public boolean isButtonsRevertedActive() {
        return buttonsReverted > 0L && Utils.millis() - buttonsReverted <= butRevTime;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
