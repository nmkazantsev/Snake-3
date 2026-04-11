package processing.test.snake3;

import processing.core.PApplet;

/* loaded from: classes.dex */
public class Snake3 extends PApplet {
    long buttonsReverted;
    boolean controlsReversed;
    int foodsOnField;
    float kx;
    float ky;
    long prevFoodCreated;
    long prevFrame;
    boolean resetting;
    long reverseStarted;
    float sizx;
    float sizy;
    long startReset;
    float timek;
    float x;
    float y;
    int playingUsers = 2;
    Snake[] snakes = new Snake[4];
    Food[] foods = new Food[1];
    Mine[] mines = new Mine[50];
    int mineLen = 0;
    long revTime = 10000;
    long butRevTime = 5000;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Explosion {
        float px;
        float py;
        long startTime;
        final long explTime = 4000;
        float size = 0.0f;

        public Explosion(float f, float f2) {
            this.startTime = Snake3.this.millis();
            this.px = f;
            this.py = f2;
        }

        public boolean checkFinished() {
            return ((long) Snake3.this.millis()) - this.startTime > 4000;
        }

        public boolean checkHitbox(float f, float f2) {
            return Snake3.sqrt(Snake3.sq((this.px * Snake3.this.sizx) - ((Snake3.this.sizx * f) + (Snake3.this.sizx / 2.0f))) + Snake3.sq((this.py * Snake3.this.sizy) - ((Snake3.this.sizy * f2) + (Snake3.this.sizy / 2.0f)))) < this.size - (0.0f * Snake3.this.sizx);
        }

        public void drawExpl() {
            this.size = ((Snake3.this.sizx * 15.0f) * ((float) (Snake3.this.millis() - this.startTime))) / 4000.0f;
            for (int i = 0; i < (Snake3.this.x / Snake3.this.sizx) * 2.0f; i++) {
                for (int i2 = 0; i2 < (Snake3.this.y / Snake3.this.sizy) * 2.0f; i2++) {
                    if (checkHitbox(i / 2.0f, i2 / 2.0f)) {
                        float sqrt = Snake3.sqrt(Snake3.sq((((i * Snake3.this.sizx) / 2.0f) - (this.px * Snake3.this.sizx)) + (Snake3.this.sizx / 2.0f)) + Snake3.sq((((i2 * Snake3.this.sizy) / 2.0f) - (this.py * Snake3.this.sizy)) + (Snake3.this.sizy / 2.0f)));
                        Snake3.this.fill((((sqrt / Snake3.this.sizx) / 15.0f) * 105.0f) + 150.0f, 100.0f - (((sqrt / Snake3.this.sizx) / 15.0f) * 100.0f), 0.0f);
                        Snake3.this.noStroke();
                        Snake3.this.rect((i * Snake3.this.sizx) / 2.0f, (i2 * Snake3.this.sizy) / 2.0f, Snake3.this.sizx / 2.0f, Snake3.this.sizy / 2.0f);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Food {
        int px;
        int py;
        int type;

        Food() {
            this.px = PApplet.parseInt(Snake3.this.random(1.0f, 39.0f));
            this.py = PApplet.parseInt(Snake3.this.random(21.0f, 62.0f));
            this.type = PApplet.parseInt(Snake3.this.random(0.0f, 13.0f));
        }

        public void drawFood() {
            Snake3.this.fill(255);
            Snake3.this.stroke(255);
            Snake3.this.strokeWeight(3.0f * Snake3.this.kx);
            if (this.type == 9) {
                Snake3.this.fill(0.0f, 0.0f, 255.0f);
            }
            Snake3.this.rect(this.px * Snake3.this.sizx, this.py * Snake3.this.sizy, Snake3.this.sizx, Snake3.this.sizy);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Mine {
        long explosionTime;
        int id;
        int px;
        int py;
        long setTime;

        private Mine(int i, int i2, int i3) {
            this.explosionTime = 0L;
            this.px = i;
            this.py = i2;
            this.setTime = Snake3.this.millis();
            this.id = i3;
        }

        /* synthetic */ Mine(Snake3 snake3, int i, int i2, int i3, Mine mine) {
            this(i, i2, i3);
        }

        public boolean checkMine(int i, int i2) {
            if (Snake3.abs(i - this.px) > 1 || Snake3.abs(i2 - this.py) > 1) {
                return false;
            }
            this.explosionTime = Snake3.this.millis();
            return true;
        }

        public void drawMine() {
            Snake3.this.noStroke();
            if (this.explosionTime == 0) {
                for (float f = Snake3.this.sizx * 3.0f; f > 0.0f; f -= 1.0f) {
                    Snake3.this.fill(150.0f - (((f / Snake3.this.sizx) / 3.0f) * 150.0f));
                    Snake3.this.ellipse((this.px * Snake3.this.sizx) + (Snake3.this.sizx / 2.0f), (this.py * Snake3.this.sizy) + (Snake3.this.sizy / 2.0f), f, f);
                }
            } else {
                for (float millis = ((Snake3.this.sizx * 3.0f) * ((float) (Snake3.this.millis() - this.explosionTime))) / 1000.0f; millis > 0.0f; millis -= 1.0f) {
                    Snake3.this.fill((((millis / Snake3.this.sizx) / 3.0f) * 105.0f) + 155.0f, 100.0f - (((millis / Snake3.this.sizx) / 3.0f) * 100.0f), 0.0f);
                    Snake3.this.ellipse((this.px * Snake3.this.sizx) + (Snake3.this.sizx / 2.0f), (this.py * Snake3.this.sizy) + (Snake3.this.sizy / 2.0f), millis, millis);
                }
            }
            if (Snake3.this.millis() - this.explosionTime <= 1000 || this.explosionTime <= 0) {
                return;
            }
            Snake3.this.deleteMine(this.id);
        }
    }

    /* loaded from: classes.dex */
    class Rocket {
        int px;
        int py;

        Rocket() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class Snake {
        int chosenDirection;
        Explosion explosion;
        int id;
        long prevMoved;
        final /* synthetic */ Snake3 this$0;
        int score = 0;
        boolean buttonsInverted = false;
        int length = 1;
        Segment[] segments = new Segment[500];
        Button[] buttons = new Button[4];
        boolean died = false;
        float speed = 10.0f;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public class Button {
            float px;
            float py;
            float sizx;
            float sizy;

            Button() {
            }

            public boolean checkTouch(float f, float f2) {
                return f > this.px && f < this.px + this.sizx && f2 > this.py && f2 < this.py + this.sizy;
            }

            public void drawButton(int i, int i2, int i3) {
                Snake.this.this$0.fill(i, i2, i3);
                Snake.this.this$0.stroke(255);
                Snake.this.this$0.strokeWeight(3.0f * Snake.this.this$0.kx);
                if (Snake.this.this$0.controlsReversed) {
                    Snake.this.this$0.stroke(255.0f, 0.0f, 0.0f);
                    Snake.this.this$0.strokeWeight(10.0f * Snake.this.this$0.kx);
                }
                Snake.this.this$0.rect(this.px, this.py, this.sizx, this.sizy);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public class Segment {
            int bright;
            int px;
            int py;
            int sid;

            Segment(int i, int i2, int i3) {
                this.sid = i;
                this.px = i2;
                this.py = i3;
                this.bright = (int) (this.bright + Snake.this.this$0.random(0.0f, 100.0f));
            }

            public void drawSegment() {
                Snake.this.this$0.stroke(0);
                Snake.this.this$0.strokeWeight(1.0f * Snake.this.this$0.kx);
                if (Snake.this.id == 0) {
                    Snake.this.this$0.fill(this.bright + 150, 0.0f, 0.0f);
                }
                if (Snake.this.id == 1) {
                    Snake.this.this$0.fill(0.0f, this.bright + 150, 0.0f);
                }
                Snake.this.this$0.rect(this.px * Snake.this.this$0.sizx, this.py * Snake.this.this$0.sizy, Snake.this.this$0.sizx, Snake.this.this$0.sizy);
            }
        }

        Snake(Snake3 snake3, int i) {
            int i2;
            int i3;
            int i4 = 20;
            this.this$0 = snake3;
            this.chosenDirection = 0;
            this.id = i;
            if (i == 0) {
                this.chosenDirection = 2;
                i2 = 20;
                i3 = 1;
            } else {
                i2 = 0;
                i3 = 0;
            }
            if (i == 1) {
                this.chosenDirection = 0;
                i3 = 39;
            } else {
                i4 = i2;
            }
            this.segments[0] = new Segment(0, i3, i4);
            addSegments(5);
            for (int i5 = 0; i5 < this.buttons.length; i5++) {
                this.buttons[i5] = new Button();
            }
            createButtons();
        }

        public void addSegments(int i) {
            for (int i2 = 0; i2 < i; i2++) {
                this.segments[this.length + i2] = new Segment(this.length + i2, this.segments[this.length - 1].px, this.segments[this.length - 1].py);
            }
            this.length += i;
        }

        public void checkButtons(float f, float f2) {
            for (int i = 0; i < this.buttons.length; i++) {
                if (this.buttons[i].checkTouch(f, f2)) {
                    if (this.buttonsInverted) {
                        if (Snake3.abs(i - this.chosenDirection) != 2) {
                            this.chosenDirection = i;
                        }
                        if (i == 0 && this.chosenDirection != 0) {
                            this.chosenDirection = 2;
                        }
                        if (i == 2 && this.chosenDirection != 2) {
                            this.chosenDirection = 0;
                        }
                    } else if (Snake3.abs(i - this.chosenDirection) != 2) {
                        this.chosenDirection = i;
                    }
                }
            }
        }

        public boolean checkDied() {
            for (int i = 0; i < this.this$0.playingUsers; i++) {
                for (int i2 = 0; i2 < this.this$0.snakes[i].length; i2++) {
                    if (i != this.id && this.segments[0].px == this.this$0.snakes[i].segments[i2].px && this.segments[0].py == this.this$0.snakes[i].segments[i2].py) {
                        return true;
                    }
                }
                for (int i3 = 0; i3 < this.length; i3++) {
                    if (this.this$0.snakes[i].explosion != null && i != this.id && this.this$0.snakes[i].explosion.checkHitbox(this.segments[i3].px, this.segments[i3].py)) {
                        return true;
                    }
                }
            }
            return this.this$0.checkAllMines(this.segments[0].px, this.segments[0].py);
        }

        public void checkFood() {
            for (int i = 0; i < this.this$0.foods.length; i++) {
                Food food = this.this$0.foods[i];
                if (food != null && this.segments[0].px == food.px && this.segments[0].py == food.py) {
                    Snake3.println(food.type);
                    if (food.type == 0) {
                        addSegments(PApplet.parseInt(this.this$0.random(2.0f, 6.0f)));
                    }
                    if (food.type == 1) {
                        this.this$0.addSegmentsToOthers(this, PApplet.parseInt(this.this$0.random(8.0f, 18.0f)));
                    }
                    if (food.type == 2) {
                        deleteSegments(PApplet.parseInt(this.this$0.random(2.0f, 4.0f)));
                    }
                    if (food.type == 3) {
                        this.speed += this.this$0.random(1.0f, 5.0f);
                    }
                    if (food.type == 4) {
                        this.speed -= this.this$0.random(1.0f, 5.0f);
                    }
                    if (food.type == 5) {
                        int i2 = this.chosenDirection;
                        this.chosenDirection = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].chosenDirection;
                        this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].chosenDirection = i2;
                        int i3 = this.segments[0].px;
                        int i4 = this.segments[0].py;
                        this.segments[0].px = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[0].px;
                        this.segments[0].py = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[0].py;
                        this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[0].px = i3;
                        this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[0].py = i4;
                    }
                    if (food.type == 6) {
                        int i5 = this.chosenDirection;
                        this.chosenDirection = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].chosenDirection;
                        this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].chosenDirection = i5;
                        for (int i6 = 0; i6 < Snake3.min(this.length, this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].length); i6++) {
                            int i7 = this.segments[i6].px;
                            int i8 = this.segments[i6].py;
                            this.segments[i6].px = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[i6].px;
                            this.segments[i6].py = this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[i6].py;
                            this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[i6].px = i7;
                            this.this$0.snakes[(this.id + 1) % this.this$0.snakes.length].segments[i6].py = i8;
                        }
                    }
                    if (food.type == 7) {
                        this.this$0.addMine(this.segments[0].px, this.segments[0].py);
                        do {
                            this.segments[0].px = (int) this.this$0.random(this.this$0.x / this.this$0.sizx);
                            this.segments[0].py = (int) this.this$0.random(this.this$0.y / this.this$0.sizy);
                        } while (checkSegment());
                    }
                    if (food.type == 8) {
                        this.explosion = new Explosion(this.segments[0].px + 0.5f, this.segments[0].py + 0.5f);
                    }
                    if (food.type == 9) {
                        this.segments[0].px = (int) this.this$0.random(this.this$0.x / this.this$0.sizx);
                        this.segments[0].py = (int) this.this$0.random(this.this$0.y / this.this$0.sizy);
                        addSegments(20);
                        this.speed += 2.0f;
                    }
                    if (food.type == 10) {
                        this.this$0.controlsReversed = true;
                        this.this$0.reverseStarted = this.this$0.millis();
                    }
                    if (food.type == 11) {
                        this.this$0.foods = new Food[Snake3.max(1, this.this$0.foods.length + PApplet.parseInt(this.this$0.random(-2.0f, 2.0f)))];
                        for (int i9 = 0; i9 < this.this$0.foods.length; i9++) {
                            this.this$0.foods[i9] = new Food();
                        }
                    }
                    if (food.type == 12) {
                        this.this$0.buttonsReverted = this.this$0.millis();
                        for (int i10 = 0; i10 < this.this$0.snakes.length; i10++) {
                            this.this$0.snakes[i10].revertButtons();
                        }
                    }
                    this.this$0.foods[i] = null;
                }
            }
        }

        public boolean checkSegment() {
            for (int i = 0; i < this.this$0.playingUsers; i++) {
                for (int i2 = 0; i2 < this.this$0.snakes[i].length; i2++) {
                    if (i != this.id && this.segments[0].px == this.this$0.snakes[i].segments[i2].px && this.segments[0].py == this.this$0.snakes[i].segments[i2].py) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void createButtons() {
            if (this.id == 0) {
                setButtonSet((this.this$0.x / 2.0f) - (this.this$0.kx * 350.0f), this.this$0.y - (300.0f * this.this$0.ky));
            }
            if (this.id == 1) {
                setButtonSet((this.this$0.x / 2.0f) - (this.this$0.kx * 350.0f), 5.0f * this.this$0.ky);
            }
        }

        public void deleteSegments(int i) {
            if (this.length - i < 1) {
                i = this.length - 1;
            }
            int i2 = this.length;
            while (true) {
                i2--;
                if (i2 <= this.length - i) {
                    this.length -= i;
                    this.length = Snake3.max(this.length, 1);
                    return;
                }
                this.segments[i2] = null;
            }
        }

        public void drawSnake() {
            for (int i = 0; i < this.length; i++) {
                this.segments[i].drawSegment();
            }
            if (this.explosion != null) {
                this.explosion.drawExpl();
                if (this.explosion.checkFinished()) {
                    this.explosion = null;
                }
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:17:0x003d, code lost:
        
            if (r12.chosenDirection == 3) goto L19;
         */
        /* JADX WARN: Code restructure failed: missing block: B:40:0x00ba, code lost:
        
            r1 = r3;
         */
        /* JADX WARN: Code restructure failed: missing block: B:53:0x00b8, code lost:
        
            if (r12.chosenDirection != 1) goto L49;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void move() {
            /*
                Method dump skipped, instructions count: 229
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: processing.test.snake3.Snake3.Snake.move():void");
        }

        public void reset() {
            int i;
            int i2;
            int i3 = 20;
            if (this.id == 0) {
                this.chosenDirection = 2;
                i = 20;
                i2 = 1;
            } else {
                i = 0;
                i2 = 0;
            }
            if (this.id == 1) {
                i2 = 39;
                this.chosenDirection = 0;
            } else {
                i3 = i;
            }
            this.segments[0] = new Segment(0, i2, i3);
            for (int i4 = 1; i4 < this.segments.length; i4++) {
                this.segments[i4] = null;
            }
            this.length = 1;
            addSegments(5);
            if (!this.died) {
                this.score++;
            }
            this.died = false;
            this.speed = 10.0f;
        }

        public void revertButtons() {
            if (this.id == 1) {
                setButtonSet((this.this$0.x / 2.0f) - (this.this$0.kx * 350.0f), this.this$0.y - (300.0f * this.this$0.ky));
            }
            if (this.id == 0) {
                setButtonSet((this.this$0.x / 2.0f) - (this.this$0.kx * 350.0f), 5.0f * this.this$0.ky);
            }
        }

        public void setButtonSet(float f, float f2) {
            this.buttons[0].sizx = this.this$0.kx * 350.0f;
            this.buttons[0].sizy = this.this$0.ky * 150.0f;
            this.buttons[0].px = (this.buttons[0].sizx / 2.0f) + f;
            this.buttons[0].py = f2;
            this.buttons[1].sizx = this.this$0.kx * 175.0f;
            this.buttons[1].sizy = this.this$0.ky * 300.0f;
            this.buttons[1].px = (525.0f * this.this$0.kx) + f;
            this.buttons[1].py = f2;
            this.buttons[2].sizx = this.this$0.kx * 350.0f;
            this.buttons[2].sizy = this.this$0.ky * 150.0f;
            this.buttons[2].px = (this.buttons[2].sizx / 2.0f) + f;
            this.buttons[2].py = (this.this$0.ky * 150.0f) + f2;
            this.buttons[3].sizx = this.this$0.kx * 175.0f;
            this.buttons[3].sizy = this.this$0.ky * 300.0f;
            this.buttons[3].px = f;
            this.buttons[3].py = f2;
        }

        public void showWiner() {
            this.this$0.fill(255);
            this.this$0.textSize(50.0f * this.this$0.kx);
            if (this.id == 0) {
                this.this$0.text("WINNER", this.this$0.x - (300.0f * this.this$0.kx), this.this$0.y - (100.0f * this.this$0.ky));
            }
            if (this.id == 1) {
                this.this$0.rotate(Snake3.radians(180.0f));
                this.this$0.text("WINNER", (-300.0f) * this.this$0.kx, (-100.0f) * this.this$0.ky);
            }
        }
    }

    public static void main(String[] strArr) {
        String[] strArr2 = {"Snake3"};
        if (strArr != null) {
            PApplet.main(concat(strArr2, strArr));
        } else {
            PApplet.main(strArr2);
        }
    }

    public void addMine(int i, int i2) {
        if (this.mineLen < this.mines.length - 1) {
            this.mines[this.mineLen] = new Mine(this, i, i2, this.mineLen, null);
            this.mineLen++;
        }
    }

    public void addSegmentsToOthers(Snake snake, int i) {
        for (int i2 = 0; i2 < this.snakes.length; i2++) {
            if (this.snakes[i2] != snake) {
                this.snakes[i2].addSegments(i);
            }
        }
    }

    public boolean checkAllMines(int i, int i2) {
        for (int i3 = 0; i3 < this.mineLen; i3++) {
            if (this.mines[i3].checkMine(i, i2)) {
                return true;
            }
        }
        return false;
    }

    public void checkReset() {
        if (countAlive() < 2 && !this.resetting) {
            this.startReset = millis();
            this.resetting = true;
        }
        if (millis() - this.startReset <= 1000 || !this.resetting) {
            return;
        }
        this.resetting = false;
        reset();
    }

    public int countAlive() {
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i;
            if (i2 >= this.playingUsers) {
                return i3;
            }
            i = !this.snakes[i2].died ? i3 + 1 : i3;
            i2++;
        }
    }

    public void deleteMine(int i) {
        while (i < this.mineLen) {
            this.mines[i] = this.mines[i + 1];
            i++;
        }
        this.mineLen--;
    }

    public void detectTimek() {
        this.timek = map((float) (millis() - this.prevFrame), 17.0f, 34.0f, 1.0f, 2.0f);
        background(0);
        textSize(20.0f);
        fill(250);
        this.prevFrame = millis();
    }

    @Override // processing.core.PApplet
    public void draw() {
        detectTimek();
        drawMines();
        drawSnakes();
        drawButtons();
        drawField();
        for (int i = 0; i < this.foods.length; i++) {
            this.foods[i].drawFood();
        }
        logic();
    }

    public void drawButtons() {
        for (int i = 0; i < this.playingUsers; i++) {
            if (!this.resetting) {
                for (int i2 = 0; i2 < 4; i2++) {
                    if (i == 0) {
                        this.snakes[i].buttons[i2].drawButton(50, 0, 0);
                    }
                    if (i == 1) {
                        this.snakes[i].buttons[i2].drawButton(0, 50, 0);
                    }
                }
            }
            if (this.resetting && !this.snakes[i].died) {
                this.snakes[i].showWiner();
            }
        }
    }

    public void drawField() {
        fill(255);
        rotate(radians(90.0f));
        textSize(this.sizx * 2.0f);
        textAlign(3, 3);
        text(String.valueOf(String.valueOf(this.snakes[1].score)) + ":" + String.valueOf(this.snakes[0].score), (this.y / 2.0f) - (this.sizx / 2.0f), -(this.x / 20.0f));
        rotate(radians(270.0f));
    }

    public void drawMines() {
        for (int i = 0; i < this.mineLen; i++) {
            this.mines[i].drawMine();
        }
    }

    public void drawSnakes() {
        for (int i = 0; i < this.playingUsers; i++) {
            this.snakes[i].drawSnake();
        }
    }

    public void logic() {
        checkReset();
        if (!this.resetting) {
            for (int i = 0; i < this.playingUsers; i++) {
                this.snakes[i].move();
            }
        }
        for (int i2 = 0; i2 < this.foods.length; i2++) {
            if (this.foods[i2] == null) {
                this.foods[i2] = new Food();
            }
        }
        if (millis() - this.reverseStarted > this.revTime) {
            this.controlsReversed = false;
        }
        if (millis() - this.buttonsReverted > this.butRevTime) {
            for (int i3 = 0; i3 < this.snakes.length; i3++) {
                this.snakes[i3].createButtons();
            }
        }
    }

    public void reset() {
        for (int i = 0; i < this.playingUsers; i++) {
            this.snakes[i].reset();
        }
        for (int i2 = 0; i2 < this.foods.length; i2++) {
            this.foods[i2] = new Food();
        }
        this.controlsReversed = false;
    }

    @Override // processing.core.PApplet
    public void settings() {
        fullScreen();
    }

    @Override // processing.core.PApplet
    public void setup() {
        this.x = this.displayWidth;
        this.y = this.displayHeight;
        this.ky = this.y / 1280.0f;
        this.kx = this.x / 720.0f;
        this.sizx = this.x / 40.0f;
        this.sizy = this.sizx;
        for (int i = 0; i < this.snakes.length; i++) {
            this.snakes[i] = new Snake(this, i);
        }
        for (int i2 = 0; i2 < this.foods.length; i2++) {
            this.foods[i2] = new Food();
        }
    }

    @Override // processing.core.PApplet
    public void touchStarted() {
        for (int i = 0; i < this.touches.length; i++) {
            for (int i2 = 0; i2 < this.playingUsers; i2++) {
                this.snakes[i2].checkButtons(this.touches[i].x, this.touches[i].y);
            }
        }
    }
}
