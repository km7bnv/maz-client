# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.9** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.9_AND_EARLIER.md`.

## 1.7.18

### Allocation-light ping diagnostics
- Reworked MazClient's Ping HUD sample history from `ArrayDeque` + per-refresh `ArrayList` copying to fixed reusable primitive arrays, eliminating the collection allocation and boxing/sorting churn that previously occurred every time ping statistics refreshed.
- The seven-sample rolling window still preserves chronological jitter calculations, min/max range, median-based quality labels, spike detection, and Stable/Variable/Unstable classification. Only the storage and calculation path changed.
- Median calculation now sorts a reusable scratch array in place after jitter is computed from the chronological ring buffer, keeping the same displayed behavior without allocating temporary collections.
- Disabling the Ping module still clears all sampling state immediately, and invalid latency values remain ignored exactly as before.
- No packets, server polling, background threads, telemetry, gameplay automation, or network behavior were added. The optimization only processes latency data Minecraft already exposes to the client.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient is **1.7.18** and MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.7.17

### HUD render-path optimization
- Reduced avoidable per-frame work in MazClient's main HUD path by caching FPS, CPS, ping, and potion-count display values on the existing 20 Hz fast-refresh cadence instead of rebuilding or resampling them at uncapped render frequency.
- Ping player-info lookup and latency sampling now run at most 20 times per second while the Ping HUD is enabled, rather than once per rendered frame. The display still updates quickly enough for normal network diagnostics while avoiding redundant connection/player-info work at high FPS.
- PotCounter inventory scanning now runs on the same bounded 20 Hz refresh cadence instead of rescanning the player inventory every frame. This removes a particularly unnecessary hot-path inventory walk when the HUD is enabled.
- FPS and CPS strings are refreshed at 20 Hz and reused between draws, reducing short-lived string construction on high-refresh-rate systems without changing the underlying FPS/CPS measurements or module behavior.
- Existing 20 Hz coordinate, movement, direction, item, armor, compass, and potion-effect caches remain intact. No new worker threads, telemetry, packet hooks, background polling, or gameplay automation were introduced.
- FPS Booster itself is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only performance release**. MazClient is **1.7.17** and MazLauncher remains **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.7.16

### Focus-aware Frame Stats
- Made the opt-in **Frame Stats** HUD focus-aware so intentional background rendering throttles are no longer counted as gameplay stutters.
- When Minecraft loses window focus, Frame Stats now stops sampling and resets its active sample window. This prevents Dynamic FPS background limits and ordinary alt-tab pauses from contaminating average frame time, p99 frame time, 1% low FPS, stutter counts, or recent GC deltas.
- Re-focusing the game starts a fresh sampling window instead of carrying stale pre-alt-tab samples forward, and the HUD shows a clear warm-up state until new active-play samples arrive.
- Disabling and re-enabling Frame Stats also starts clean, preventing old statistics from being displayed as if they belonged to the current measurement session.
- Sampling remains render-thread-local and allocation-conscious: the existing fixed-size buffers, 15-frame recalculation cadence, and JVM `GarbageCollectorMXBean` counters are preserved. No telemetry, background worker, packet polling, or server interaction was added.
- This is diagnostics-only. It does not change rendering behavior, graphics settings, Dynamic FPS configuration, JVM settings, networking, gameplay, render distance, or simulation distance.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only** diagnostics/reliability release. MazClient is **1.7.16** and MazLauncher remains **0.6.33**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cloud package path.

## 1.7.15

### Compass ribbon HUD
- Turned the existing **Compass** module toggle into a real HUD element instead of leaving it as a placeholder-only module.
- Added a compact movable compass ribbon showing the current cardinal/intercardinal heading plus exact yaw degrees, with adjacent direction markers for quick orientation while moving or fighting.
- The compass uses MazClient's existing `HudLayout` position and opacity system, so it participates in the same persisted HUD placement workflow as the rest of the client instead of introducing a separate config format.
- Rendering is client-side only and reads local player orientation; it does not automate movement, targeting, combat, packet behavior, or server interaction.
- The new HUD is registered through Fabric's current 26.2 HUD element API and remains disabled unless the existing Compass module is enabled.
- FPS Booster is unchanged and still never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, managed resource packs, and disconnect stability are preserved.

### Versioning and distribution
- This is a **MazClient-only** feature release. MazClient is **1.7.15** and MazLauncher remains **0.6.22**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 1.7.14

### Full Maz menu shell
- Restored a much stronger MazClient menu presentation across the normal Minecraft menu/settings screen tree without reintroducing automatic `TitleScreen` or `PauseScreen` replacement paths that caused panorama softlocks.
- The title screen now renders a full MazClient shell with a branded sidebar, version/status treatment, accent framing, and a dedicated control area while Minecraft retains ownership of the actual title-screen buttons and lifecycle.
- Normal menu/settings screens now use a full MazClient panel shell with stronger headers, accents, footer identity, and live Maz borders around vanilla widgets so the menus visibly read as MazClient instead of lightly-skinned Vanilla.
- Widget chrome is applied through Fabric's current 26.2 screen extract lifecycle, so existing controls, narration, focus, input, and screen transitions remain intact.
- Gameplay surfaces remain excluded: inventory/container screens, chat/in-bed chat, death, world loading/receiving, and progress screens are untouched.
- The proven 1.7.12 empty-title recovery remains anomaly-only and unchanged; no timed or tick-based screen takeover was restored.
- FPS Booster remains unchanged and never modifies render distance or simulation distance.

### Distribution
- MazLauncher remains at **0.6.21** for this client-only release.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs stay internal to the installer/cloud package path.

## 1.7.13

### Maz menu comeback
- Restored a clearly MazClient-branded title/menu presentation without reintroducing the old automatic `TitleScreen` or `PauseScreen` replacement paths that caused panorama softlocks.
- Vanilla Minecraft still owns the real screen instances, widgets, input, narration, and lifecycle; MazClient supplies the custom dark-blue title card, menu panels, header, accent chrome, titles, and version footer through Fabric screen-rendering hooks instead of swapping screens.
- Normal Minecraft menu/settings flows under `net.minecraft.client.gui.screens.*` receive MazClient chrome, including Title, Pause, Options, Controls, Video Settings, Multiplayer, World Select, Resource Packs, Accessibility, Language, and similar non-gameplay menus.
- Gameplay surfaces are explicitly excluded: inventory/container screens (including chest, crafting, furnace, horse, merchant, beacon, anvil and related screens), chat/in-bed chat, death, level loading/receiving, and progress screens remain untouched.
- The 1.7.12 anomaly-only empty-title-screen recovery remains intact and still rebuilds only a broken vanilla `TitleScreen` with no widgets; healthy title screens are never replaced.

### Shared vanilla settings
- Bundled MazLauncher **0.6.20** adds a filtered shared Minecraft-settings layer between launcher-managed Vanilla and MazClient installations so normal settings such as controls/keybinds, video, sound, language, accessibility, FOV and other `options.txt` preferences carry across modes.
- On launcher startup, the newest managed `options.txt` becomes the shared normal-settings source and is merged into the other managed installations.
- MazClient-only and installation-specific state is deliberately excluded from sharing: `resourcePacks`, `incompatibleResourcePacks`, last-server/address state, mods, worlds, MazClient config, and launcher data remain isolated.
- Settings synchronization is local-only, best-effort, atomic, and never blocks launcher startup or Minecraft launch if a file cannot be read or written.
- This preserves the managed Low Fire, Low Shield PvP, and Smaller Totem entries instead of leaking or deleting them through vanilla settings synchronization.

### Stability and distribution
- FPS Booster remains unchanged and never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, managed performance mods, Simple Voice Chat management, managed resource packs, launcher state, and disconnect stability are preserved.
- MazLauncher is updated to **0.6.20** for the shared-settings integration.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs stay internal to the installer/cloud package path.

## 1.7.12

### Title-screen health recovery
- Added a conservative health check at Fabric's normal post-initialization point for Minecraft's vanilla `TitleScreen`. A healthy title screen always exposes interactive buttons after initialization; if a rare panorama-only state reaches `AFTER_INIT` with no title controls, MazClient now queues one vanilla title-screen rebuild on the client thread.
- Recovery is anomaly-only: MazClient does not replace a healthy title screen, does not time or poll normal startup/disconnect transitions, and does not reinstate the old custom `TitleScreen` or `PauseScreen` takeover paths removed in 1.7.10 and 1.7.11.
- MazClient's custom menu appearance remains enabled through the existing global background/theme hook, so normal Title, Options, Multiplayer, World Select, and other vanilla-owned menus retain MazClient chrome while Minecraft keeps ownership of their real controls and lifecycle.
- The guard rechecks that the same title-screen instance is still active and still has no buttons before repairing it, preventing a queued recovery from overwriting a newer screen transition.
- Updated the compile-time Fabric API dependency from **0.156.0+26.2** to the current stable **0.160.0+26.2** release for Minecraft 26.2. Launcher-managed Fabric API remains resolved through MazLauncher's existing stable Modrinth path.
- FPS Booster remains unchanged and never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, managed performance mods, Simple Voice Chat management, managed resource packs, launcher state, and normal disconnect behavior are preserved.

### Distribution
- MazLauncher remains at **0.6.19** because no launcher code changed in this release.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs stay internal to the installer/cloud package path.

## 1.7.11

### Vanilla-owned custom menu lifecycle
- Removed MazClient's remaining automatic replacement of Minecraft's in-game `PauseScreen`. Minecraft now owns the real pause, save, disconnect-progress, and return-to-title screen instances from creation through teardown.
- MazClient's custom visual identity is preserved through the existing global screen-theme hooks, which render MazClient backgrounds, panels, headers, accent chrome, titles, and version footer around vanilla-owned menu controls instead of swapping the underlying screen object.
- Together with 1.7.10's removal of the timed custom title-screen takeover, this eliminates MazClient's automatic `TitleScreen` and `PauseScreen` instance swaps from the startup/disconnect path while keeping custom-styled menus across normal Minecraft screens.
- Right Shift still opens MazClient's dedicated module screen, and dedicated MazClient configuration/HUD screens remain unchanged because they are explicitly opened by the player rather than injected into Minecraft lifecycle transitions.
- The fix specifically targets the persistent case where a panorama keeps rendering after a disconnect/menu transition but usable menu state is lost. No panorama asset, networking, save behavior, account/session state, or gameplay logic is changed.
- FPS Booster remains unchanged and never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, managed performance mods, Simple Voice Chat management, resource packs, launcher state, and normal disconnect behavior are preserved.

### Distribution
- MazLauncher remains at **0.6.18** because no launcher code changed in this release.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs stay internal to the installer/cloud package path.

## 1.7.10

### Title-screen softlock root fix
- Removed MazClient's remaining timed startup takeover of Minecraft's vanilla `TitleScreen`. MazClient no longer waits a small number of client ticks and then calls `setScreen(new MazHomeScreen())` while the title screen is active.
- Vanilla Minecraft now owns the title-screen lifecycle completely during both clean startup and disconnect/return-to-title transitions, eliminating MazClient's last automatic screen swap in the panorama path.
- Removed the startup title stability counter and the `wasInWorld` / `homeShownOnce` bookkeeping that existed only to schedule or suppress that automatic title-screen replacement.
- The existing in-game pause-screen replacement remains isolated to `PauseScreen` and is unchanged; Right Shift continues to open the MazClient module menu normally.
- This is a lifecycle/stability fix, not a panorama asset change. It intentionally targets the repeated symptom where the panorama continues rendering but the title UI becomes unavailable or non-interactive.
- FPS Booster remains unchanged and never modifies render distance or simulation distance. Config persistence, offline operation, smart caching, managed performance mods, Simple Voice Chat management, resource packs, launcher state, and disconnect handling outside the removed title takeover remain preserved.

### Distribution
- MazLauncher remains at **0.6.17** because no launcher code changed in this release.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs stay internal to the installer/cloud package path.
