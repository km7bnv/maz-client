# MazLauncher Changelog

Historical notes through **0.6.40** are preserved verbatim in `LAUNCHER_CHANGELOG_ARCHIVE_0.6.40_AND_EARLIER.md`.

## 0.6.41

### SmoothHud removal
- Removed **SmoothHud** from MazLauncher's managed Minecraft 26.2 Fabric stack. MazLauncher no longer resolves, downloads, validates, caches, or launches SmoothHud.
- Deleted the dedicated `SmoothHudService` integration and removed SmoothHud from managed-stack initialization.
- Expanded retired-mod cleanup so existing MazClient installations automatically delete legacy SmoothHud JARs and the `.maz-smoothhud` marker on launcher startup. This covers previously managed cached installs so upgrading users do not keep the mod after the integration is removed.
- BetterF3 retirement cleanup remains intact.
- Existing MazClient config, HUD setup, performance mods, resource packs, account cache, offline behavior, smart caching, Simple Voice Chat management, and disconnect stability remain untouched.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** removal/cleanup release. MazClient remains unchanged.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.41**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.
