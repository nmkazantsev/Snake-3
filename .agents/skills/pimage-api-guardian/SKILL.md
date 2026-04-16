---
name: pimage-api-guardian
description: Use this whenever code calls, wraps, or refactors the custom Seal Engine 3-M PImage API. This skill prevents invented methods, wrong overload assumptions, accidental Processing-style guesses, and unsafe behavior changes in text, drawing, color, image, and font calls.
---

You are protecting correct usage of the custom `PImage` API.

## Main principle

`PImage` in this project is a real project-specific API. Never treat it as generic Processing, Android Canvas, Bitmap, Java2D, or Skija unless the project explicitly does so.

## Required workflow

Before changing call sites:

1. Read the actual `PImage` API.
2. Read `AbstractImage` and `PFont` docs/source if relevant.
3. Verify overloads, parameter meaning, return types, side effects, and ownership.
4. Search for existing real call sites in the codebase.
5. Only then modify the code.

## What to protect against

Never:

- invent a `PImage` method
- assume an overload exists without checking
- silently change color-channel assumptions
- silently change alpha behavior
- change text alignment or uppercase behavior by guesswork
- replace `PImage` with a different abstraction without explicit instruction

## Areas that need special care

- `fill(...)`
- `background(...)`
- `stroke(...)`
- `strokeWeight(...)`
- `text(...)`
- `textSize(...)`
- `textAlign(...)`
- `setUpperText(...)`
- `setFont(...)`
- `getTextWidth(...)`
- `getTextHeight(...)`
- `setAntiAlias(...)`
- `clear()`
- `image(...)`
- `rotImage(...)`
- `drawSector(...)`

## Output expectations

When you propose a change, state:

- what the real API supports
- what the code incorrectly assumed
- the minimal compatible fix
- what must be re-tested visually

## Constraint

Prefer compatibility and behavioral preservation over "cleaner" rewrites.
