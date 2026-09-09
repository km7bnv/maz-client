# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

Versioning rule: MazClient patch versions run from `0` through `19`. After `X.Y.19`, the next release rolls over to `X.(Y+1).0` instead of using patch `20` or higher. Historical releases keep their original version numbers.

Historical notes through **1.7.9** are preserved verbatim in `CHANGELOG_ARCHIVE_1.7.9_AND_EARLIER.md`.

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
