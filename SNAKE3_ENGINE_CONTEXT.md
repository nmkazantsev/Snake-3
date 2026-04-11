# Snake-3 (Seal Engine 3-M) Migration Context

This repo contains a migrated version of a Processing sketch (`Snake3.pde`) running on **Seal Engine 3-M** (pages + OpenGL rendering).

The intent is to keep gameplay behavior consistent with the original sketch while adapting to the engine’s lifecycle (`GamePageClass`) and rendering/input APIs.

## Project Layout

- `Snake3.pde`
  - Decompiled Processing sketch used as the reference. Note: `Snake.move()` is missing in that file, so the engine port uses a reconstructed implementation.
- `desktop/`
  - Desktop Gradle launcher and packaging (`shadowJar`).
  - Depends on engine jars: `desktop/libs/core.jar`, `desktop/libs/desktop.jar`.
  - Uses shared module `:game`.
- `android/`
  - Android project that reuses `../desktop/game` as module `:game`.
  - Entry point: `android/app/src/main/java/.../MainActivity.java`.

## Runtime Architecture (Engine-Side)

### Start Page

- `com.example.snake_3.game.MainRenderer`
  - Extends `GamePageClass`.
  - Owns:
    - `SnakeGame game` (game state + logic/timers)
    - `SnakeRenderer renderer` (GPU draw)
    - `TouchProcessor[]` for button presses
    - cached GL bridges:
      - `GeneralPlatformBridge gl`
      - `GLConstBridge glc`
  - Lifecycle:
    - `onSurfaceChanged(w,h)`: initializes `Camera`, `SnakeGame`, `SnakeRenderer`, touch processors
    - `draw()`: clears screen, configures GL state for 2D sprites, renders current state, then advances `game.logic()`

### Game State / Logic

- `SnakeGame`
  - Owns dynamic state arrays (similar to the sketch):
    - `Snake[] snakes` (4 allocated, `playingUsers=2` active)
    - `Food[] foods` (dynamic length, can change due to food type 11)
    - `Mine[] mines` with `mineLen`
  - Timing:
    - `detectTimek()` computes `timek` from frame delta (mapped like the sketch)
    - `logic()` runs reset logic, snake movement, food regeneration, reverse-controls timeout, button-revert timeout
  - Playfield clamp:
    - `getPlayfieldRowRange()` computes the allowed grid rows by looking at current button rectangles.
    - `Snake.move()` wraps vertically within this range so snakes never enter button zones.

- `Snake`
  - Contains:
    - direction, speed, score, died flag
    - `SnakeSegment[] segments`
    - `SnakeButton[] buttons`
    - `Explosion explosion`
  - `move(SnakeGame)`:
    - ticked by speed and `game.timek`
    - shifts tail, advances head, wraps X across full grid cols, wraps Y only within playfield row range
    - checks self-collision, food effects, mine/explosion collision
  - Food effects mirror the sketch’s intent (grow/shrink/speed, swap heads/segments, mines, explosions, reverse-controls, dynamic food count, button revert).

- `Food`, `Mine`, `Explosion`, `SnakeSegment`, `SnakeButton`
  - Split into individual files for easier future changes.

## Rendering (GPU, No Full-Frame Redraw)

The current renderer does **not** redraw a full-screen `PImage` each frame. Instead it uses cached `SimplePolygon` textures and positions them via `prepareAndDraw`.

- `SnakeRenderer`
  - Per frame:
    - sync score text (only recreates score polygon when score string changes)
    - sync `controlsReversed` skin for buttons
    - draw mines, snakes (segments), explosions, buttons or winner overlay, score, foods

- `SnakeRenderAssets`
  - Holds cached `SimplePolygon` textures:
    - snake segment tiles (lazy cached by `[snakeId][bright]`)
    - food tiles (white/blue)
    - mine tiles (normal/explosion)
    - explosion tile
    - button tiles (wide/tall per player color)
    - score tile (rotated text)
    - winner tiles (player 0 and pre-rotated 180 for player 1)
  - Texture resolution strategy:
    - UI/FX are generated at lower resolution (`UI_TEX_SCALE`, `FX_TEX_SCALE`) and scaled up when drawn.
    - Mine/explosion base textures use power-of-two sizes (clamped) to avoid NPOT texture edge cases.

### GL State

`MainRenderer.draw()` sets 2D-friendly GL state each frame:

- disables depth test and culling for sprite rendering
- sets alpha blending:
  - `gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)`
  - `engine.enableBlend()`

This is required for transparent sprites (score/winner/explosion) to render correctly.

## Input

The original sketch used `touchStarted()` and tested button rectangles.

Port uses:

- `TouchProcessor` per button (2 players × 4 buttons)
- `checkHitbox` is `SnakeButton.checkTouch(x,y)`
- `touchStartedCallback` calls `Snake.onButtonPressed(...)`

## Android Notes

- Orientation is locked to portrait in `MainActivity` via `setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT)`.

## Build/Run

Desktop:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./desktop/gradlew -p desktop shadowJar --no-daemon
JAVA_HOME=/usr/lib/jvm/java-latest-openjdk /usr/lib/jvm/java-latest-openjdk/bin/java -jar desktop/build/libs/snake-3-1.0.jar
```

Android:

```bash
cd android
./gradlew assembleDebug
```

## Known Gaps / Risks

- `Snake3.pde` does not contain the original `Snake.move()` method body (decompiler stub), so movement behavior is reconstructed.
- GPU sprite approach is fast, but texture sizing must be conservative on mobile GPUs. The current code reduces UI/FX texture resolution to improve startup time and stability.
- Known rendering bug (needs investigation): score and winner text tables may be invisible at runtime even though the polygons are created. Likely causes: GL state (blend/depth), text color state on `PImage`, or incorrect polygon draw sizing/placement when `saveMemory=true`. Repro: run a round until winner should show, observe no visible text.

## Recent Commits

- Latest fix commit hash: `813aa29a10c3a2808d9a41de96562578926195c8`
