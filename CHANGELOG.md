# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.18** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.18_AND_EARLIER.md`. Notes through **1.7.9** also remain in the earlier archive referenced there.

## 1.8.10

### Chunk Position HUD
- Added an opt-in **Chunk Position** HUD that shows the player's current chunk X/Z coordinates plus the 0-15 X/Z position inside that chunk.
- The HUD derives everything from the already-loaded local player block position using integer floor division/modulo, so negative-world coordinates are handled correctly without querying the server.
- Refresh work is capped at 250 ms and text width is remeasured only when the displayed chunk/in-chunk coordinates change, keeping the render path allocation-light.
- The module uses the normal MazClient HUD position/opacity system and persists through the existing module/config path.
- This is client-safe informational QoL only: no packets, server polling, automated input, targeting, combat logic, movement changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD/QoL release**. MazClient advances from **1.8.9** to **1.8.10**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.9

### Current Block HUD
- Added an opt-in **Current Block** HUD that shows the localized name and coordinates of the block currently under the crosshair.
- Reads only the already-available local client hit result and loaded block state; it does not send packets, poll the server, automate mining, select targets, inject input, or change gameplay behavior.
- Refresh work is capped at 100 ms and the panel width is remeasured only when the displayed text changes, keeping the feature lightweight instead of doing string/width work at uncapped render frequency.
- The module uses the normal MazClient HUD position/opacity system and persists through the existing module/config path.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only HUD/QoL release**. MazClient advances from **1.8.8** to **1.8.9**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.8

### Tightened cached HUD render paths
- Removed the remaining render-time font-width fallback calls from Inventory Space, Offhand Counter, and Durability Status. Each HUD now relies exclusively on the width computed during its existing refresh pass instead of retaining a defensive per-frame measurement path.
- Replaced the three `Long.MIN_VALUE` first-refresh sentinels with an explicit `0L` initial deadline. This keeps the intended immediate first refresh while avoiding sentinel-style edge cases and makes width initialization deterministic before the first visible panel draw.
- Preserved each module's existing 250 ms refresh cadence and the staggered phases introduced in 1.8.0, so inventory/equipment scans remain spread across the interval rather than clustering on the same frame.
- Display text, warning thresholds, positions, opacity behavior, local inventory/equipment reads, and module controls are unchanged.
- This remains client-safe HUD optimization only. No packets, server polling, telemetry, automated input, targeting, combat logic, movement behavior, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance/reliability release**. MazClient advances from **1.8.7** to **1.8.8**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.7

### Bounded Compass HUD width cache
- Replaced the Compass HUD's two uncapped per-frame font-width measurements with bounded lazy caches while preserving per-frame yaw and degree updates.
- Center labels cache their measured widths by the displayed integer degree, so each of the 360 possible center readings is measured at most once per client session instead of once per rendered frame.
- Cardinal/intercardinal labels cache their widths by the existing eight-direction index, so right-side direction alignment is measured at most once per direction per client session.
- Compass text, 1-degree display precision, direction thresholds, panel geometry, positioning, opacity behavior, and render responsiveness are unchanged.
- This remains client-safe local HUD optimization only. No packets, server polling, telemetry, automated input, targeting, combat logic, movement behavior, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient advances from **1.8.6** to **1.8.7**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.6

### Cached vehicle and flight HUD widths
- Cached Mount Health HUD width alongside its existing 250 ms mount-health refresh instead of measuring the same formatted text on every rendered frame.
- Cached Flight Status HUD width alongside its existing 250 ms local flight telemetry refresh, removing another per-frame font-width calculation while preserving speed, elytra durability, rocket count, and warning-state behavior.
- Mount Health now explicitly treats its `Long.MIN_VALUE` timer sentinel as an immediate first refresh, avoiding signed-overflow arithmetic before the cached width is initialized.
- Text content, positions, opacity, refresh cadence, warning thresholds, and local-only data sources are unchanged.
- This remains client-safe HUD optimization only. No packets, server polling, telemetry, automated input, targeting, combat logic, movement behavior, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance/reliability release**. MazClient advances from **1.8.5** to **1.8.6**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.5

### Cached refresh-bound HUD widths
- Cached Light Level HUD width alongside its existing 250 ms local light refresh, removing font-width measurement from the every-frame render path.
- Cached World Time HUD width alongside its existing 500 ms clock/moon refresh, preserving the same displayed day, time, and moon phase behavior.
- Cached XP Progress HUD width alongside its existing 250 ms XP refresh, preserving the same level, percentage, current-XP, and next-level calculations.
- All three HUDs still perform their first refresh immediately through the existing startup-refresh fix, so the cached widths are initialized before rendering visible data.
- Text content, positions, opacity behavior, refresh cadence, local-only data sources, and module controls are unchanged.
- This remains client-safe HUD optimization only. No packets, server polling, telemetry, automated input, targeting, combat logic, movement behavior, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient advances from **1.8.4** to **1.8.5**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.4

### Cached standalone HUD widths
- Cached the rendered widths for Biome and Dimension alongside their existing 250 ms / 500 ms text refreshes, removing repeated font-width measurement from their every-frame render paths.
- Cached Last Death width when a new death location is recorded. Persisted Last Death data loaded before the font is available performs one lazy width measurement on its first visible render, then reuses it thereafter.
- Text content, positions, opacity behavior, refresh cadence, persistence format, and local-only data sources are unchanged.
- This remains client-safe HUD optimization only. No packets, server polling, telemetry, automated input, targeting, combat logic, movement behavior, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient advances from **1.8.3** to **1.8.4**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.3

### Immediate HUD refresh reliability
- Fixed an overflow-prone first-refresh sentinel shared by the core MazHud cache timers and several standalone HUDs. Subtracting `Long.MIN_VALUE` from the current epoch time overflowed a signed `long`, which could leave newly enabled HUDs stuck on their placeholder text instead of performing the intended initial refresh.
- Core FPS, ping, CPS, potion count, coordinates, speed, direction, compass, item/armor status, potion effects, RAM, clock, and session text now enter their existing fast/slow refresh cadence immediately on startup.
- Biome, Dimension, Light Level, World Time, XP Progress, Flight Status, and Recent Gains now perform their first data refresh immediately instead of being blocked by the sentinel overflow.
- Recent Gains still treats its first inventory sample as a baseline, so existing inventory is not reported as newly gained; only the broken initial scheduling sentinel changed.
- Existing refresh intervals, rendering, module configuration, HUD positions, opacity, local-only data access, and feature behavior are otherwise unchanged.
- This remains client-safe local HUD work only. No packets, server polling, telemetry, automated input, targeting, combat behavior, movement changes, or gameplay automation were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only reliability release**. MazClient advances from **1.8.2** to **1.8.3**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.2

### Bounded Frame Stats refresh work
- Kept frame-time sampling on every active rendered frame so the existing p99, 1% low, stutter-count, and average-frame-time diagnostics still observe the full rolling sample window.
- Replaced the old "recalculate every 15 frames" trigger with a fixed 200 ms statistics refresh interval. This caps sorting, GC-counter polling, formatting, and health-state recomputation at about 5 Hz instead of letting that work scale upward with FPS.
- High-FPS systems therefore avoid needlessly running the diagnostics recomputation dozens of times per second while the visible HUD remains responsive.
- Cached the Frame Stats HUD width and remeasures it only when the formatted diagnostics text changes, removing another font-width calculation from the every-frame render path.
- Background/unfocused-window filtering, the 180-frame rolling sample, 50 ms stutter threshold, p99 calculation, 1% low calculation, GC deltas, and Stable/Warning/Stutter accent behavior are preserved.
- This remains local diagnostics only. No packets, server polling, telemetry, automated input, targeting, combat behavior, or gameplay mechanics were added or changed.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient advances from **1.8.1** to **1.8.2**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.1

### Allocation-light Recent Gains HUD
- Reused the Recent Gains current-inventory aggregation map instead of allocating a fresh `HashMap` every 250 ms while the HUD is enabled.
- Deferred item hover-name resolution until a positive gain is actually detected, removing steady-state display-name string work from ordinary no-change inventory scans.
- Cached each gain entry's display string when the gain is recorded or merged, eliminating repeated `+amount item` string creation on every rendered frame.
- Cached the HUD width and recalculates it only when entries are added, merged, expired, cleared, or reset instead of remeasuring every visible line every frame.
- Preserved the existing 250 ms local inventory sampling cadence, six-second entry lifetime, four-entry cap, one-second same-item merge window, and first-snapshot baseline behavior.
- The HUD still reads only the already-loaded local player inventory. No packets, server polling, telemetry, background workers, input automation, targeting, combat behavior, or gameplay mechanics were added or changed.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient advances from **1.8.0** to **1.8.1**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.8.0

### Staggered HUD refresh scheduling
- Staggered the cached refresh phases for Inventory Space, Offhand Counter, and Durability Status so their periodic inventory/equipment scans no longer repeatedly land on the same render frame after the initial warm-up refresh.
- The three HUDs keep the same 250 ms refresh cadence, but their recurring work is phase-separated across that interval to reduce clustered main-thread spikes when several modules are enabled together.
- Cached display widths are now recalculated only when each HUD's display text refreshes instead of calling font-width measurement every rendered frame.
- The first visible refresh remains immediate when a HUD is enabled, so there is no delayed or blank startup state; only subsequent periodic work is staggered.
- All three HUDs still read only already-loaded local client state. No packets, server polling, background workers, gameplay automation, targeting, input injection, or combat behavior were added.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. The version rollover rule advances MazClient from **1.7.19** to **1.8.0**; MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.7.19

### Allocation-light CPS tracking
- Replaced the CPS module's `ArrayDeque<Long>` click histories with primitive `long[]` timestamp queues, removing per-click `Long` boxing and deque-node allocation during normal play.
- Left and right CPS still use the same rolling one-second window and are pruned against the current client clock before values are read.
- The primitive queues begin with a small fixed capacity and grow only if the player produces more clicks inside the one-second window than the current buffer can hold, so normal play stays allocation-free without imposing an artificial CPS cap.
- Queue growth preserves chronological ordering, and empty queues reset their head index so long sessions do not accumulate stale indexing state.
- This is client-side HUD accounting only. No click injection, input automation, packet behavior, combat logic, targeting, server polling, telemetry, or gameplay mechanics were added or changed.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient is **1.7.19** and MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.
