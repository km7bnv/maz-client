# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.18** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.18_AND_EARLIER.md`. Notes through **1.7.9** also remain in the earlier archive referenced there.

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
