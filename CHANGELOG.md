# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.10.1** are preserved verbatim in `CHANGELOG_ARCHIVE_1.10.1_AND_EARLIER.md`.

## 1.10.7

### Module sidebar confined to Modules
- Removed the module-category sidebar from the global Minecraft menu theme. The v1.4 category sidebar now appears **only** in `MazMenuScreen`, where Performance, HUD, Combat, Visual and Utility are real interactive module categories.
- Home and Pause remain fully custom Maz screens without a sidebar. Options, Video, Controls, Accessibility, Language, Multiplayer, World Selection, Resource Packs and similar normal menus keep the custom dark Maz panel/button treatment without displaying fake module navigation.
- Corrected the title lifecycle so returning to the title screen after leaving a world restores `MazHomeScreen` after the vanilla title widgets are safely initialized, rather than exposing the vanilla title layout. The existing stability delay remains to avoid panorama/disconnect softlocks.
- Old dark Maz colors, custom buttons, Minecraft utility icons and hover names, `Made by awnkr_par`, menu behavior, focus, narration and config persistence are preserved.
- FPS Booster remains unchanged and does not modify render distance or simulation distance.

### Versioning and distribution
- MazClient advances from **1.10.6** to **1.10.7**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw JARs remain internal.

## 1.10.6

### Totem Counter HUD
- Added an opt-in **Totem Counter** HUD module for Minecraft 26.2 that displays the local player's current Totem of Undying count, including a totem held in the offhand.
- The counter reads only already-loaded local inventory state. It never equips, refills, moves, selects, or automatically uses totems and sends no gameplay packets.
- Inventory counting is cached on a **500 ms telemetry cadence** rather than tied to render FPS or every client tick, so even very high-FPS sessions avoid unnecessary repeated inventory scans.
- The module uses MazClient's persistent HUD layout/opacity system and custom HUD styling, so it can be enabled, repositioned, and retained across sessions like the existing HUD modules.
- Fresh 26.2 research shows passive totem-count displays remain a common lightweight client-side PvP/QoL feature, while several neighboring totem mods include auto-equip/refill/cursor automation. MazClient intentionally implements only the passive counter behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.5** to **1.10.6**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.10.5

### Low air warning
- Added an opt-in **Low Air Warning** utility module for Minecraft 26.2 that samples only the local player's client-visible air supply twice per second.
- The module sends one local MazClient system message at about **5 seconds** of air remaining and escalates once at about **2 seconds**. It stays quiet at the same severity and resets after resurfacing, recovery, death, disconnect, or disable.
- This is warning-only QoL: it sends no packets, changes no movement, selects no items, and performs no automated swimming, breathing, or other gameplay actions.
- Fresh 26.2 research shows numerical oxygen awareness remains a useful client-side HUD/QoL pattern while the existing MazClient stack already covers many common performance optimizers and status displays. This release therefore adds a focused survival warning instead of another overlapping dependency.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.4** to **1.10.5**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.10.4

### Low hunger warning
- Added an opt-in **Low Hunger Warning** utility module for Minecraft 26.2 that samples only the local player's client-visible food level once per second.
- The module sends one local MazClient system message when hunger drops to **6/20**, then escalates once at **2/20**. It stays quiet at the same severity and resets after recovery, death, disconnect, or disable.
- This is warning-only QoL: it sends no packets, selects no food, presses no use key, and performs no automatic eating or other gameplay automation.
- Fresh 26.2 research shows low-hunger alerts remain a common client-side QoL feature, while several current 26.2 mods automate eating. MazClient intentionally implements only the non-cheat reminder behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.3** to **1.10.4**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.10.3

### Low health warning
- Added an opt-in **Low Health Warning** utility module for Minecraft 26.2 that samples only the local player's client-visible health once per second.
- The module sends one local MazClient system message when health crosses **30%** of maximum, then escalates once at **15%**. It does not repeat the same severity every tick and resets cleanly after recovery, death, disconnect, or disable.
- Thresholds scale with modified maximum health rather than assuming ten hearts, so temporary health modifiers remain compatible.
- This feature sends no packets, uses no automated movement or item actions, and performs no gameplay automation. It is client-safe for singleplayer and normal multiplayer.
- Fresh 26.2 research continues to show configurable low-health visual warnings as a common lightweight QoL pattern, while the current performance ecosystem already has broad coverage from Sodium/Lithium/ImmediatelyFast/FerriteCore/culling stacks; this release therefore avoids another overlapping optimizer.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.2** to **1.10.3**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.10.2

### Inventory full warning
- Added an opt-in **Inventory Full Warning** utility module for Minecraft 26.2 that checks only the 36 main inventory slots once per second using client-visible inventory state.
- The module emits one local MazClient system message when the inventory transitions from having free space to completely full, then stays quiet until at least one slot is freed before it can warn again. This avoids repeated chat spam while mining, looting, or farming.
- The check stops and resets cleanly when no player is present or when the module is disabled, preserving disconnect stability and avoiding stale state between worlds or servers.
- This feature sends no packets, moves no items, automates no inputs, and performs no gameplay actions. It is client-safe for singleplayer and normal multiplayer.
- Fresh Minecraft 26.2 research continues to show full-inventory alerts as a common lightweight QoL feature, while current performance packs already converge on Sodium, Lithium, ImmediatelyFast, FerriteCore, culling tools, and similar optimizers; this release therefore avoids stacking another overlapping performance dependency.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed mods, the custom dark v1.4 menu system, and disconnect stability are preserved.

### Versioning and distribution
- MazClient advances from **1.10.1** to **1.10.2**; MazLauncher remains **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.
