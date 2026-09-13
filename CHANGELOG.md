# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.10.1** are preserved verbatim in `CHANGELOG_ARCHIVE_1.10.1_AND_EARLIER.md`.

## 1.10.2

### Inventory full warning
- Added an opt-in **Inventory Full Warning** utility module for Minecraft 26.2 that checks only the 36 main player inventory slots once per second using client-visible inventory state.
- The module emits one local MazClient system message when the inventory transitions from having free space to completely full, then stays quiet until at least one slot is freed before it can warn again. This avoids repeated chat spam while mining, looting, or farming.
- The check stops and resets cleanly when no player is present or when the module is disabled, preserving disconnect stability and avoiding stale state between worlds or servers.
- This feature sends no packets, moves no items, automates no inputs, and performs no gameplay actions. It is client-safe for singleplayer and normal multiplayer.
- Fresh Minecraft 26.2 research continues to show full-inventory alerts as a common lightweight QoL feature, while current performance packs already converge on Sodium, Lithium, ImmediatelyFast, FerriteCore, culling tools, and similar optimizers; this release therefore avoids stacking another overlapping performance dependency.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.1** to **1.10.2**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.
