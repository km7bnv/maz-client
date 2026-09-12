# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.8.12** are preserved verbatim in `CHANGELOG_ARCHIVE_1.8.12_AND_EARLIER.md`.

## 1.8.15

### Stable HUD width-cache refresh
- Reduced repeated text-width work in the opt-in **Biome**, **Dimension**, and **Light Level** HUDs. Their existing 250 ms/500 ms data refreshes now measure text width only when the displayed string actually changes (or when the cache is first initialized).
- This removes redundant font layout/measurement while a player remains in the same biome/dimension or while the sampled block/sky light values remain unchanged, without lowering data refresh frequency or changing what the HUDs display.
- The optimization is intentionally local and conservative: no packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.8.14** to **1.8.15**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.14

### Frame Stats p50 frametime diagnostic
- Extended the opt-in **Frame Stats** HUD with p50 (median) frametime so players can compare typical frame pacing against the existing average, p99, worst-frame, 1% low, stutter-count, and GC diagnostics.
- Reuses the already-sorted 180-frame buffer during the existing 200 ms statistics refresh. The median is read directly from that buffer, so the feature adds no extra sampling pass, allocation, timer, server query, or per-frame collection work.
- The p50 value follows the same active-window reset behavior as the existing diagnostics, so background/alt-tab throttling cannot leave stale median data on screen.
- Existing 50 ms stutter detection, health accents, one-second GC polling, cached HUD width, module state, position, opacity, and background-window filtering are preserved.
- This remains client-safe local diagnostics only. No packets, server polling, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only diagnostics/QoL release**. MazClient advances from **1.8.13** to **1.8.14**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.13

### Rolling worst-frame diagnostic
- Extended the opt-in **Frame Stats** HUD with the worst frame time observed in its existing 180-frame rolling sample window, making isolated hitch severity visible alongside average frame time, p99, 1% low FPS, stutter count, and GC deltas.
- Reuses the already-sorted frame-time buffer from the existing 200 ms statistics refresh, so the new value is read from the final sorted sample instead of adding another scan, allocation, timer, or per-frame measurement.
- The worst-frame value resets with the same active-window sampling reset used by the rest of Frame Stats, so alt-tab/background throttling does not leave stale hitch data on screen.
- Existing 50 ms stutter detection, health accents, 1-second GC polling, cached HUD width, module state, position, opacity, and background-window filtering are preserved.
- This remains client-safe local diagnostics only. No packets, server polling, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only diagnostics/QoL release**. MazClient advances from **1.8.12** to **1.8.13**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.
