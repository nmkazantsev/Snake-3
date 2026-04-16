---
name: engine-architecture-explorer
description: Use this when exploring the architecture of Seal Engine 3-M or a game built on it. Use it to map modules, ownership boundaries, lifecycle, rendering flow, page graph, platform bridge responsibilities, and risky hotspots before making changes.
---

You are analyzing the architecture of a Seal Engine 3-M codebase.

## Goals

Build a reliable mental model before proposing changes.

## Reading order

1. `README.md`
2. `PROJECT_MAP_FOR_CODEX.md`
3. `ENGINE_INTERNALS_MAP_FOR_CODEX.md` only if engine-source reading is truly required
4. Other root-level markdown files
5. Then relevant Java code

## Required workflow

1. Identify whether the current task is:
   - application work using the engine as JAR/AAR
   - engine-source work inside Seal Engine 3-M itself
2. Classify the code into these buckets where applicable:
   - engine core
   - rendering
   - game logic
   - physics
   - UI/UX
   - platform bridge
   - assets/resources
   - debugging tools
3. Map page ownership and lifecycle:
   - `GamePageClass`
   - `Engine.startNewPage(...)`
   - page-owned resources
   - page-scoped cleanup behavior
4. Identify where initialization actually happens:
   - constructor / preload phase
   - `onSurfaceChanged(...)`
   - `draw()`
   - platform bootstrap
5. Call out risky hotspots and cross-platform boundaries.

## Output expectations

When reporting architecture, include:

- subsystem list
- key classes and responsibilities
- ownership/lifecycle boundaries
- rendering path summary
- platform differences
- safest places to modify for the current task
- dangerous files/classes to avoid unless necessary

## Important constraints

- Do not start editing code before the architecture map is clear.
- Do not assume engine internals are available in app repositories.
- For app tasks, prefer app code and assets before engine-source investigation.
- Keep the explanation grounded in the actual docs and code, not in generic engine assumptions.
