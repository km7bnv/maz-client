# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.18** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.18_AND_EARLIER.md`. Notes through **1.7.9** also remain in the earlier archive referenced there.

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
