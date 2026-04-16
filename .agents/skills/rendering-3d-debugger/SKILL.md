---
name: rendering-3d-debugger
description: Use this when debugging 2D or 3D rendering issues in Seal Engine 3-M, including wrong transforms, bad projection, shader/adaptor mismatches, broken UVs, framebuffer problems, resize-related bugs, and incorrect separation between rendering state and gameplay state.
---

You are debugging rendering in Seal Engine 3-M.

## Main principle

Treat rendering bugs as pipeline bugs until proven otherwise.

## Reading order

1. `README.md`
2. `PROJECT_MAP_FOR_CODEX.md`
3. `ENGINE_INTERNALS_MAP_FOR_CODEX.md` if engine-level tracing is required
4. Relevant page class
5. Relevant shader/adaptor/vertex/framebuffer code

## Required debugging path

Trace the full chain:

- current `GamePageClass`
- resource creation site
- `onSurfaceChanged(...)`
- `draw()`
- camera setup
- projection setup
- matrix composition
- active shader and adaptor
- geometry source (`Shape`, `Polygon`, `SimplePolygon`, `FrameBuffer`, etc.)
- texture and UV path
- framebuffer / default framebuffer switching

## Always check

- matrix multiplication order
- world/view/projection confusion
- forgotten camera or projection apply step
- stale resize-dependent state
- wrong UV coordinates
- flipped or mirrored axes
- incorrect draw order
- framebuffer source/target confusion
- asset-path mistakes disguised as rendering bugs
- hidden coupling between screen size and game logic

## Reporting expectations

Explain:

- visible symptom
- probable root cause
- exact code path involved
- smallest safe fix
- manual verification checklist

## Constraints

- Do not change gameplay logic while fixing rendering unless the rendering bug directly depends on it.
- Do not move stateful game systems into `onSurfaceChanged(...)` unless they are truly resolution-dependent.
- Preserve behavior except for the rendering defect being fixed.
