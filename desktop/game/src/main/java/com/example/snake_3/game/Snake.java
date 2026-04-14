package com.example.snake_3.game;

import com.nikitos.utils.Utils;

public final class Snake {
    private static final float MIN_SPEED = 10.0f;
    private static final float INITIAL_SPEED = 13;

    private final int id;
    private int chosenDirection;
    private long prevMoved = 0L;
    private int score = 0;
    private boolean buttonsInverted = false;
    private int length = 1;
    private final SnakeSegment[] segments = new SnakeSegment[500];
    private final SnakeButton[] buttons = new SnakeButton[4];
    private boolean died = false;
    private float speed = INITIAL_SPEED;
    private Explosion explosion = null;

    public Snake(int id) {
        this.id = id;
    }

    public void init(SnakeGame game) {
        chosenDirection = 0;
        segments[0] = createSpawnHead(game);
        length = 1;
        addSegments(game, 5);
        for (int i = 0; i < buttons.length; i++) buttons[i] = new SnakeButton();
        createButtons(game);

        if (id == 0) {
            chosenDirection = 2;
        }
        if (id == 1) {
            chosenDirection = 0;
            buttonsInverted = true;
        }
    }

    public void onButtonPressed(SnakeGame game, int buttonIndex) {
        if (buttonsInverted) {
            if (Utils.abs(buttonIndex - chosenDirection) != 2) {
                chosenDirection = buttonIndex;
            }
            if (buttonIndex == 0 && chosenDirection != 0) chosenDirection = 2;
            if (buttonIndex == 2 && chosenDirection != 2) chosenDirection = 0;
        } else {
            if (Utils.abs(buttonIndex - chosenDirection) != 2) {
                chosenDirection = buttonIndex;
            }
        }
    }

    public void addSegments(SnakeGame game, int n) {
        for (int i = 0; i < n; i++) {
            SnakeSegment tail = segments[length - 1];
            segments[length + i] = new SnakeSegment(tail.getPx(), tail.getPy());
        }
        length += n;
    }

    public void deleteSegments(int n) {
        if (length - n < 1) n = length - 1;
        for (int i = length - 1; i > length - n; i--) {
            segments[i] = null;
        }
        length -= n;
        length = Math.max(length, 1);
    }

    private boolean checkSegmentVsOthers(SnakeGame game) {
        Snake[] snakes = game.getSnakes();
        for (int i = 0; i < game.getPlayingUsers(); i++) {
            for (int j = 0; j < snakes[i].length; j++) {
                if (i != id && segments[0].getPx() == snakes[i].segments[j].getPx() && segments[0].getPy() == snakes[i].segments[j].getPy()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkDied(SnakeGame game) {
        Snake[] snakes = game.getSnakes();
        for (int i = 0; i < game.getPlayingUsers(); i++) {
            for (int j = 0; j < snakes[i].length; j++) {
                if (i != id && segments[0].getPx() == snakes[i].segments[j].getPx() && segments[0].getPy() == snakes[i].segments[j].getPy()) {
                    return true;
                }
            }
            for (int k = 0; k < length; k++) {
                Explosion otherExpl = snakes[i].explosion;
                if (otherExpl != null && i != id && otherExpl.checkHitbox(game, segments[k].getPx(), segments[k].getPy())) {
                    return true;
                }
            }
        }
        return game.checkAllMines(segments[0].getPx(), segments[0].getPy());
    }

    public void checkFood(SnakeGame game) {
        for (int i = 0; i < game.getFoods().length; i++) {
            Food food = game.getFoods()[i];
            if (food != null && segments[0].getPx() == food.getPx() && segments[0].getPy() == food.getPy()) {
                int type = food.getType();

                if (type == 0) {
                    addSegments(game, Utils.parseInt(Utils.random(2.0f, 6.0f)));
                }
                if (type == 1) {
                    game.addSegmentsToOthers(this, Utils.parseInt(Utils.random(8.0f, 18.0f)));
                }
                if (type == 2) {
                    deleteSegments(Utils.parseInt(Utils.random(2.0f, 4.0f)));
                }
                if (type == 3) {
                    increaseSpeed(Utils.random(1.0f, 5.0f));
                }
                if (type == 4) {
                    decreaseSpeed(Utils.random(1.0f, 5.0f));
                }
                if (type == 5) {
                    Snake other = game.getSnakes()[(id + 1) % game.getSnakes().length];
                    int cd = chosenDirection;
                    chosenDirection = other.chosenDirection;
                    other.chosenDirection = cd;

                    int hx = segments[0].getPx();
                    int hy = segments[0].getPy();
                    segments[0].setPx(other.segments[0].getPx());
                    segments[0].setPy(other.segments[0].getPy());
                    other.segments[0].setPx(hx);
                    other.segments[0].setPy(hy);
                }
                if (type == 6) {
                    Snake other = game.getSnakes()[(id + 1) % game.getSnakes().length];
                    int cd = chosenDirection;
                    chosenDirection = other.chosenDirection;
                    other.chosenDirection = cd;

                    int limit = Math.min(length, other.length);
                    for (int j = 0; j < limit; j++) {
                        int tx = segments[j].getPx();
                        int ty = segments[j].getPy();
                        segments[j].setPx(other.segments[j].getPx());
                        segments[j].setPy(other.segments[j].getPy());
                        other.segments[j].setPx(tx);
                        other.segments[j].setPy(ty);
                    }
                }
                if (type == 7) {
                    game.addMine(segments[0].getPx(), segments[0].getPy());
                    do {
                        segments[0].setPx(game.getRandomPlayableCol());
                        segments[0].setPy(game.getRandomPlayableRow());
                    } while (checkSegmentVsOthers(game));
                }
                if (type == 8) {
                    explosion = new Explosion(segments[0].getPx() + 0.5f, segments[0].getPy() + 0.5f);
                }
                if (type == 9) {
                    segments[0].setPx(game.getRandomPlayableCol());
                    segments[0].setPy(game.getRandomPlayableRow());
                    addSegments(game, 20);
                    increaseSpeed(2.0f);
                }
                if (type == 10) {
                    game.setControlsReversed(true);
                    game.setReverseStarted(Utils.millis());
                }
                if (type == 11) {
                    int delta = Utils.parseInt(Utils.random(-2.0f, 2.0f));
                    int newLen = Math.max(1, game.getFoods().length + delta);
                    Food[] newFoods = new Food[newLen];
                    for (int j = 0; j < newFoods.length; j++) newFoods[j] = new Food(game);
                    game.setFoods(newFoods);
                }
                if (type == 12) {
                    game.setButtonsReverted(Utils.millis());
                    for (Snake s : game.getSnakes()) {
                        if (s != null) s.revertButtons(game);
                    }
                }

                if (i >= 0 && i < game.getFoods().length) {
                    game.getFoods()[i] = null;
                }
            }
        }
    }

    public void createButtons(SnakeGame game) {
        if (!game.isTouchControlsEnabled()) return;
        if (id == 0) {
            setButtonSet(game, game.getY() - (300.0f * game.getKy()));
        }
        if (id == 1) {
            setButtonSet(game, 5.0f * game.getKy());
        }
    }

    public void revertButtons(SnakeGame game) {
        if (!game.isTouchControlsEnabled()) return;
        if (id == 1) {
            setButtonSet(game, game.getY() - (300.0f * game.getKy()));
        }
        if (id == 0) {
            setButtonSet(game, 5.0f * game.getKy());
        }
    }

    private void setButtonSet(SnakeGame game, float oy) {
        // Processing layout is based on a 720px reference and leaves small side margins.
        // In the engine port we want edge-to-edge buttons so there are no unreachable gaps.
        float fullW = game.getX();
        float tallW = fullW * 0.25f; // 175 / (175+350+175)
        float wideW = fullW * 0.50f; // 350 / (175+350+175)
        float wideH = game.getKy() * 150.0f;
        float tallH = game.getKy() * 300.0f;

        SnakeButton b0 = buttons[0];
        b0.setSizx(wideW);
        b0.setSizy(wideH);
        b0.setPx(tallW);
        b0.setPy(oy);

        SnakeButton b1 = buttons[1];
        b1.setSizx(tallW);
        b1.setSizy(tallH);
        b1.setPx(tallW + wideW);
        b1.setPy(oy);

        SnakeButton b2 = buttons[2];
        b2.setSizx(wideW);
        b2.setSizy(wideH);
        b2.setPx(tallW);
        b2.setPy(wideH + oy);

        SnakeButton b3 = buttons[3];
        b3.setSizx(tallW);
        b3.setSizy(tallH);
        b3.setPx(0.0f);
        b3.setPy(oy);
    }

    public void reset(SnakeGame game) {
        if (id == 0) {
            chosenDirection = 2;
        }
        if (id == 1) {
            chosenDirection = 0;
        }

        segments[0] = createSpawnHead(game);
        for (int i = 1; i < segments.length; i++) segments[i] = null;
        length = 1;
        addSegments(game, 5);
        if (!died) score++;
        died = false;
        speed = INITIAL_SPEED;
        explosion = null;
    }

    public void move(SnakeGame game) {
        if (died) return;

        long now = Utils.millis();
        float interval = 1000.0f / Math.max(MIN_SPEED, speed);
        if (now - prevMoved < interval) return;
        prevMoved = now;

        int dir = chosenDirection;
        if (game.isControlsReversed()) dir = (dir + 2) % 4;

        // Shift tail
        for (int i = length - 1; i > 0; i--) {
            segments[i].setPx(segments[i - 1].getPx());
            segments[i].setPy(segments[i - 1].getPy());
        }

        // Head step
        if (dir == 0) segments[0].setPy(segments[0].getPy() - 1);
        if (dir == 1) segments[0].setPx(segments[0].getPx() + 1);
        if (dir == 2) segments[0].setPy(segments[0].getPy() + 1);
        if (dir == 3) segments[0].setPx(segments[0].getPx() - 1);

        // Horizontal wrap (same behavior as before).
        int cols = game.getGridCols();
        if (segments[0].getPx() < 0) segments[0].setPx(cols - 1);
        if (segments[0].getPx() >= cols) segments[0].setPx(0);

        // Vertical wrap but clamped to playfield range (never under button zones).
        int[] rr = game.getPlayfieldRowRange();
        int minRow = rr[0];
        int maxRow = rr[1];
        if (segments[0].getPy() < minRow) segments[0].setPy(maxRow);
        if (segments[0].getPy() > maxRow) segments[0].setPy(minRow);

        // Self-crossing is allowed in this variant; only enemy collisions (and other hazards) can kill.

        checkFood(game);
        if (checkDied(game)) died = true;

        if (explosion != null) {
            explosion.updateSize(game);
            if (explosion.checkFinished()) explosion = null;
        }
    }

    private SnakeSegment createSpawnHead(SnakeGame game) {
        int headX = (id == 0) ? game.getLeftSpawnCol() : game.getRightSpawnCol();
        int headY = game.getDefaultSpawnRow();
        return new SnakeSegment(headX, headY);
    }

    private void increaseSpeed(float delta) {
        speed += delta;
    }

    private void decreaseSpeed(float delta) {
        speed = Math.max(MIN_SPEED, speed - delta);
    }

    public int getId() {
        return id;
    }

    public int getChosenDirection() {
        return chosenDirection;
    }

    public int getScore() {
        return score;
    }

    public int getLength() {
        return length;
    }

    public SnakeSegment[] getSegments() {
        return segments;
    }

    public SnakeButton[] getButtons() {
        return buttons;
    }

    public boolean isDied() {
        return died;
    }

    public Explosion getExplosion() {
        return explosion;
    }
}
