# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.9** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.9_AND_EARLIER.md`.

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
