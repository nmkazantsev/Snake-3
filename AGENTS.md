# AGENTS.md

This repository uses Seal Engine 3-M. Read this file before doing any work.

## Main goal

Work carefully in this codebase. Prefer understanding the architecture and the real engine API before changing code. Keep behavior stable unless the task explicitly asks for behavior changes.

## Reading order

Always start in this order:

1. `README.md`
2. `PROJECT_MAP_FOR_CODEX.md`
3. `ENGINE_INTERNALS_MAP_FOR_CODEX.md` only when engine-source work is actually required
4. Other root-level markdown files
5. Only then inspect Java code

For application work that uses the engine as JAR/AAR, prefer app code and app assets first. Do not jump into engine source unless the bug clearly requires it.

## Seal Engine mental model

- The project is page-based. Screens are implemented as `GamePageClass`.
- Page transitions are hard lifecycle boundaries.
- Many engine objects are page-owned through a `GamePageClass creator`.
- Do not casually move stateful objects across lifecycle boundaries.
- Treat `Engine.startNewPage(...)` as a resource boundary.

## Rendering and lifecycle rules

- Heavy assets such as meshes, images, shaders, and fonts are typically created in the constructor or before first use.
- Resolution-dependent objects such as cameras, frame buffers, and other screen-size-dependent resources belong in `onSurfaceChanged(...)`.
- In `draw()`, ensure shader, projection, camera, and draw calls are applied in the correct order.
- Do not make game logic depend on screen size or graphics initialization.
- Do not cache touch hitboxes if they depend on current screen geometry; compute them dynamically.

## PImage rules

`PImage` is a custom project API. It is not standard Processing, not Android Bitmap API, and not Skija API.

Before changing any code that uses `PImage`:

1. Read the real `PImage` API in this project.
2. Read `AbstractImage` and related image/font classes.
3. Check desktop and Android implementations if behavior matters.
4. Verify method names, overloads, and parameter semantics from source or docs.

Never:

- invent missing `PImage` methods
- assume Processing semantics by memory
- replace the custom abstraction with another graphics API without explicit instruction
- silently change text metrics, font loading, anti-aliasing, alpha semantics, or resource ownership

When fixing `PImage` issues, preserve behavior and prefer the smallest compatible change.

## 3D rendering rules

When debugging 3D rendering:

1. Trace the full path: page -> camera -> matrices -> shader -> adaptor -> mesh/polygon -> framebuffer/texture.
2. Check matrix order, projection settings, UVs, shader bindings, and resize-dependent state.
3. Separate rendering bugs from gameplay bugs.
4. Prefer minimal fixes with a clear root-cause explanation.

## UI-over-texture rules

When working on UI, HUD, text, or overlays rendered through textures:

1. First determine whether the UI is drawn directly into `PImage`, then shown via `Polygon` / `SimplePolygon`, or routed through `FrameBuffer.drawTexture(...)`.
2. Do not treat a UI-over-texture issue as a pure 3D bug until the `PImage -> texture -> polygon/framebuffer -> screen` chain is traced.
3. Check lifecycle ownership carefully: texture content may be created in the constructor, while framebuffer and other resolution-dependent resources belong in `onSurfaceChanged(...)`.
4. Be careful with `saveMemory`, `mipMap`, redraw timing, UV ordering, and framebuffer vertex order.
5. Do not replace the existing UI-over-texture pipeline with a different architecture unless explicitly requested.

## Architecture work rules

When asked to explore or map the project:

- build the architecture map first
- classify code into engine core, rendering, game logic, physics, UI/UX, platform bridge, resources/assets, and debug tooling
- identify ownership boundaries and risky hotspots before proposing refactors
- do not start patching code before the map is clear

## Cross-platform rules

Seal Engine is cross-platform. Be careful with:

- Android vs desktop asset loading
- GL context recreation and reload paths
- font loading and text metrics
- package-name differences between desktop and Android code
- platform-specific implementations hidden behind bridge interfaces

A fix that works only on one platform is usually incomplete.

## Change policy

- Preserve behavior unless the user explicitly requests behavioral changes.
- Prefer app-level fixes for app tasks.
- Prefer engine-level fixes only for real engine bugs.
- Avoid broad rewrites when a local fix is enough.
- Keep public APIs stable unless explicitly instructed otherwise.

## Reporting expectations

When you finish a task, report:

- root cause
- exact files/classes involved
- what was changed
- why the change is safe
- what should be manually tested on desktop and Android

## Risky hotspots

Treat these areas as high-blast-radius and change them only when needed:

- `CoreRenderer`
- `Engine`
- `platformBridge/*`
- `VRAMobject`
- shader/adaptor registries
- image/font bridge code
- page-scoped global registries

## Useful local conventions

- `vertex_bueffer` is a legacy package typo; do not "fix" it unless requested.
- Android package roots may differ from desktop/core package roots.
- Some APIs may be partial or legacy-shaped. Do not normalize names blindly.
