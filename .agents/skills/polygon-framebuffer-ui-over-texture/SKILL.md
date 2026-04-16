---
name: polygon-framebuffer-ui-over-texture
description: Use this when debugging or refactoring UI, HUD, menus, text, or dynamically generated textures rendered through PImage, Polygon, SimplePolygon, or FrameBuffer in Seal Engine 3-M. This includes UI drawn into textures, framebuffer-to-screen pipelines, post-effect overlays, wrong texture coordinates, blurry or stretched UI, and confusion between 2D UI rendering and ordinary 3D geometry rendering.
---

You are working on the Polygon / PImage / FrameBuffer rendering path in Seal Engine 3-M.

## Main principle

Treat UI-over-texture issues as a pipeline problem, not as an isolated drawing bug.

The real chain is often:

- UI drawing code -> `PImage`
- `PImage` uploaded as texture
- `Polygon` / `SimplePolygon` / texture-backed geometry
- optional `FrameBuffer`
- `FrameBuffer.drawTexture(...)` or direct polygon draw
- final screen output

Do not collapse these stages mentally into "just draw UI".

## Reading order

1. `README.md`
2. `PROJECT_MAP_FOR_CODEX.md`
3. `ENGINE_INTERNALS_MAP_FOR_CODEX.md` only if engine-level tracing is required
4. The owning `GamePageClass`
5. The `PImage` generation code
6. The `Polygon` / `SimplePolygon` setup
7. The `FrameBuffer` usage site
8. Relevant shader / adaptor code only if needed

## What this skill is for

Use this skill for cases such as:

- UI drawn into `PImage` appears stretched, blurred, mirrored, clipped, or offset
- text is correct inside `PImage` but wrong when shown on screen
- `Polygon.prepareData(...)` or `prepareAndDraw(...)` uses wrong geometry or UVs
- `FrameBuffer.apply()` / `connectDefaultFrameBuffer()` order is wrong
- `FrameBuffer.drawTexture(...)` draws correctly only with unusual vertex order
- resize breaks HUD, menus, or overlays rendered through textures
- Codex is tempted to rewrite UI as ordinary 3D rendering or vice versa
- `saveMemory`, `mipMap`, redraw timing, or `redrawNow()` behavior is relevant

## Required investigation steps

Always identify all of the following before proposing a fix:

1. Where the texture content is created:
   - direct `PImage` drawing
   - `Polygon` redraw function
   - framebuffer pass
2. Who owns the resource:
   - which `GamePageClass`
   - constructor vs `onSurfaceChanged(...)`
3. Whether the problem is in:
   - the 2D drawing into `PImage`
   - texture upload / redraw timing
   - geometry or UV preparation
   - framebuffer routing
   - final screen draw
4. Whether resize or page change invalidates the resource.

## Always check

- whether heavy texture-generation objects were created in the right lifecycle stage
- whether framebuffer dimensions are recreated on resolution change
- whether UI hitboxes are computed dynamically instead of cached
- whether `Polygon.image` / redraw output matches the final screen result
- whether `saveMemory=true` removes the source `PImage` earlier than expected
- whether `mipMap` causes UI softness or unexpected sampling artifacts
- whether wrong `PVector a, b, d` ordering changes rectangle orientation
- whether UV parameters are swapped or mirrored
- whether the final draw path uses `FrameBuffer.drawTexture(...)` with the intended vertex order
- whether the shader/adaptor being used matches textured 2D geometry expectations
- whether the issue is really texture generation, and not camera/projection state leaking from 3D rendering

## Constraints

- Do not replace the existing `PImage -> Polygon -> FrameBuffer` pipeline with a different rendering architecture unless explicitly asked.
- Do not rewrite UI into raw engine internals when the issue can be fixed at the current abstraction level.
- Do not move game-state objects into `onSurfaceChanged(...)` unless they are truly resolution-dependent.
- Preserve behavior except for the visual defect being fixed.

## Reporting expectations

Explain:

- where the UI texture is created
- where it is uploaded or redrawn
- how it reaches the screen
- whether the bug lives in `PImage`, `Polygon`, `FrameBuffer`, shader/adaptor, or lifecycle code
- the smallest safe fix
- what must be manually tested after resize, page switch, and on both desktop and Android when relevant
