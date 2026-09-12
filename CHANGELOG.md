# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.8.12** are preserved verbatim in `CHANGELOG_ARCHIVE_1.8.12_AND_EARLIER.md`.

## 1.9.4

### Old Maz menu style restored across vanilla menus
- Restored the compact dark-card visual language from MazClient's original `MazHomeScreen` / `MazPauseScreen` across normal vanilla-owned Minecraft menu and settings screens instead of the newer full-screen shell treatment.
- The global menu theme now uses the original Maz palette (`#090E1A` page background, `#11192A` top field, `#141E31` cards, `#2A3958` borders and `#5865F2` accent), compact bordered cards, the square Maz `M` brand block, muted version/footer treatment, and old-style hover accents.
- Normal screens dynamically size and center their Maz card around their existing visible vanilla widgets, keeping compact screens compact while still accommodating wider settings/list screens. A dark content well sits behind controls without moving or replacing them.
- The title screen keeps Minecraft's real `TitleScreen` and widgets but now receives the old Maz home-card identity, module status treatment, original tagline styling, version footer, and matching compact chrome.
- Vanilla controls, focus, narration, keyboard/mouse input, screen transitions, multiplayer/world selection behavior and accessibility remain owned by Minecraft. MazClient continues to style through Fabric 26.2 `ScreenEvents.afterBackground` / `afterExtract` hooks rather than reintroducing the old automatic title/pause screen takeover that previously caused panorama softlocks.
- Gameplay/container surfaces remain excluded: inventory/container screens, chat/in-bed chat, death, world loading/receiving and progress screens are untouched.
- The anomaly-only empty-title recovery remains unchanged: a healthy title screen is never replaced.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only UI release**. MazClient advances from **1.9.3** to **1.9.4**; MazLauncher remains **0.6.43**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.9.3

### Current Block stable-target caching
- Optimized the opt-in **Current Block** HUD so its existing 100 ms crosshair refresh still checks the currently targeted block, but repeated refreshes on the same block position and block type now reuse the existing display text and measured width instead of resolving the display name and rebuilding the same string again.
- The HUD still reads the local block state on every scheduled refresh, so changing targets or a block changing into a different block type is reflected on the next 100 ms update. Block name, coordinates, HUD position, opacity, styling, and visible formatting are unchanged.
- The target cache is cleared when no block is targeted and when the client has no active level, preventing stale target state from carrying across disconnects/world changes.
- This remains client-safe local HUD optimization only. No packets, server queries, telemetry, automated input, targeting assistance, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.9.2** to **1.9.3**; MazLauncher remains **0.6.40**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.9.2

### Frame Stats stutter-rate diagnostic
- Extended the opt-in **Frame Stats** HUD so its existing rolling stutter count now also shows the percentage of sampled frames at or above the existing 50 ms stutter threshold.
- The percentage is calculated directly from the already-counted stutters and current 180-frame sample size during the existing 200 ms statistics refresh. It adds no new frame sampling, buffer scan, allocation, timer, packet, or server query.
- Existing average frametime, p50, p99, p99 gap, worst-frame, 1% low, raw stutter count, GC diagnostics, health accents, active-window filtering, cached HUD width, module state, position, opacity, and disconnect/background reset behavior are preserved.
- This remains client-safe local diagnostics only. No automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only diagnostics/QoL release**. MazClient advances from **1.9.1** to **1.9.2**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.9.1

### Flight Status stable-width caching
- Reduced redundant font width/layout work in the opt-in **Flight Status** HUD. Its existing 250 ms local telemetry refresh is unchanged, but it now measures text width only when the formatted flight-status string actually changes or the width cache is uninitialized.
- Flight speed, elytra durability, rocket inventory count, warning accents, HUD position/opacity, and visible formatting are unchanged. The optimization only reuses the prior width when the displayed text is identical to the previous refresh.
- The HUD still reads only client state Minecraft already has loaded; no packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.9.0** to **1.9.1**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.9.0

### Stable inventory/status HUD width caching
- Reduced redundant font width/layout work in the opt-in **Inventory Space**, **Offhand Counter**, and **Durability Status** HUDs. Their existing 250 ms local-state refresh cadence is unchanged, but each HUD now re-measures text width only when its formatted display string actually changes or its width cache is uninitialized.
- Inventory Space still scans the same 36 storage slots and preserves free-slot, partial-stack, occupancy, and warning thresholds; an unchanged inventory summary now reuses its cached width.
- Offhand Counter still counts matching inventory items and preserves durability/name output; unchanged offhand state no longer triggers another font-width measurement four times per second.
- Durability Status still samples the main-hand item plus armor slots and preserves weakest-item selection and warning accents; unchanged durability text now reuses its cached width.
- This remains local client-side HUD optimization only. No packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. Per the patch rollover rule, MazClient advances from **1.8.19** to **1.9.0**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.19

### Recent Gains single-pass inventory sampling
- Optimized the opt-in **Recent Gains** HUD so each scheduled inventory sample now collects both aggregate item counts and display names in the same pass over the already-loaded local inventory.
- Removed the previous per-gained-item inventory rescan used only to recover display names. A refresh that detects several newly gained item types therefore no longer performs one extra full inventory walk for each gained type.
- The existing 250 ms refresh cadence, positive-delta logic, first-snapshot baseline, one-second merge window, four-entry cap, six-second expiry, cached width/layout behavior, and visible text are preserved.
- The temporary display-name cache is cleared and rebuilt on each scheduled sample and is also cleared on reset, so it cannot leak stale world/player state across disconnects or player changes.
- This remains local client-side HUD optimization only. No packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.8.18** to **1.8.19**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.18

### Frame Stats p99 gap diagnostic
- Extended the opt-in **Frame Stats** HUD with a **p99 gap** value, calculated as p99 frametime minus p50 frametime. This makes frame-pacing spread visible at a glance: a small gap means typical and slow frames are close together, while a large gap highlights uneven pacing even when average FPS still looks healthy.
- Reuses the already-sorted 180-frame sample buffer and existing 200 ms statistics refresh. The gap is computed directly from the existing p50 and p99 values, so there is no extra sample pass, allocation, timer, server query, or per-frame collection work.
- The new value follows the same active-window reset behavior as the rest of Frame Stats, so background/alt-tab throttling cannot leave stale data on screen.
- Existing average frametime, p50, p99, worst-frame, 1% low, stutter-count, GC diagnostics, health accents, one-second GC polling, cached HUD width, module state, position, opacity, and background-window filtering are preserved.
- This remains client-safe local diagnostics only. No packets, server polling, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only diagnostics/QoL release**. MazClient advances from **1.8.17** to **1.8.18**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.17

### Stable Mount Health and World Time HUD width caching
- Reduced repeated font width/layout work in the opt-in **Mount Health** and **World Time** HUDs. Their existing gameplay-data refresh intervals remain unchanged, but each HUD now re-measures text width only when its formatted display string actually changes or its width cache is uninitialized.
- Mount Health still samples the active mount every 250 ms and keeps the same health-percentage accent thresholds; an unchanged mount name/health string no longer triggers another width measurement on every refresh.
- World Time still samples the world clock every 500 ms and keeps the same day, clock, and moon-phase display; refreshes that produce the same formatted minute/day/moon text now reuse the cached width.
- This is local client-side HUD work only. No packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.8.16** to **1.8.17**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.16

### XP HUD width refresh optimization
- Reduced redundant font width/layout work in the opt-in **XP Progress** HUD. Its existing 250 ms gameplay-data refresh is unchanged, but the HUD now re-measures text width only when the formatted XP string actually changes or the width cache has not yet been initialized.
- Players who are not gaining or spending XP no longer trigger repeated width measurements four times per second while the HUD is enabled; XP level/progress values still refresh at the same cadence and visible changes update immediately on the next scheduled refresh.
- This is a local render-path optimization only. No packets, server queries, telemetry, automated input, targeting, combat behavior, movement changes, graphics-setting changes, render-distance changes, simulation-distance changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD performance release**. MazClient advances from **1.8.15** to **1.8.16**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

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
- Extended the opt-in **Frame Stats** HUD with p50 (median) frametime so players can compare typical frame pacing against the existing average, p99, worst-frame, 1% low FPS, stutter count, and GC diagnostics.
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
