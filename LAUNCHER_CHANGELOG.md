# MazLauncher Changelog

Historical notes through **0.6.33** are preserved verbatim in `LAUNCHER_CHANGELOG_ARCHIVE_0.6.33_AND_EARLIER.md`.

## 0.6.39

### SmoothHud managed hotbar QoL
- Added **SmoothHud** to MazLauncher's managed Minecraft 26.2 Fabric stack. SmoothHud animates only the selected hotbar-slot highlight when the player changes slots, providing a small visual polish feature without automating inventory actions, combat, movement, or networking.
- MazLauncher resolves a compatible Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloaded and cached JARs are validated before use: the archive must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact upstream `smoothhud` mod id. Corrupt, truncated, or wrong-mod files are discarded.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated SmoothHud JAR.
- SmoothHud refreshes independently from the rest of the managed stack so an optional refresh failure cannot block launch or disable existing performance, bug-fix, or QoL mods.
- Upstream SmoothHud settings are left untouched. The integration does not automate gameplay, modify packets, force graphics options, or change render distance or simulation distance.
- Config persistence, offline operation, smart caching, Simple Voice Chat management, managed resource packs, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** QoL/reliability release. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.39**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.38

### Mod Menu managed configuration hub
- Added **Mod Menu** to MazLauncher's managed Minecraft 26.2 Fabric stack. It gives players a searchable installed-mod list and direct access to configuration screens exposed by supported managed mods, making the growing performance/QoL stack easier to inspect and tune without editing files by hand.
- MazLauncher resolves a compatible Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloaded and cached JARs are validated before use: the archive must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact upstream `modmenu` mod id. Corrupt, truncated, or wrong-mod files are discarded.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Mod Menu JAR.
- Mod Menu refreshes independently from the rest of the managed stack so an optional refresh failure cannot block launch or disable existing performance, bug-fix, or QoL mods.
- Upstream Mod Menu settings are left untouched. The integration does not automate gameplay, modify packets, force graphics options, or change render distance or simulation distance.
- Config persistence, offline operation, smart caching, Simple Voice Chat management, managed resource packs, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** QoL/reliability release. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.38**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.37

### BetterF3 managed QoL integration
- Added **BetterF3** to MazLauncher's managed Minecraft 26.2 Fabric stack. BetterF3 replaces the vanilla debug screen with a more readable, customizable client-side HUD without automating gameplay or changing packet behavior.
- MazLauncher resolves a compatible Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloaded and cached JARs are validated before use: the archive must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact upstream `betterf3` mod id. Corrupt, truncated, or wrong-mod files are discarded.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated BetterF3 JAR.
- BetterF3 refreshes independently from the rest of the managed stack so an optional refresh failure cannot block launch or disable existing performance, bug-fix, or QoL mods.
- BetterF3's upstream configuration is left untouched. No gameplay automation, packet behavior, graphics settings, render distance, or simulation distance are modified.
- Config persistence, offline operation, smart caching, Simple Voice Chat management, managed resource packs, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** QoL release. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.37**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.36

### Language Reload managed QoL integration
- Added **Language Reload** to MazLauncher's managed Minecraft 26.2 Fabric stack. It reduces language-switch reload overhead and adds language search/fallback QoL while remaining entirely client-side.
- MazLauncher resolves a compatible Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloaded and cached JARs are validated before use: the archive must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact upstream `language-reload` mod id. Corrupt, truncated, or wrong-mod files are discarded.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Language Reload JAR.
- Language Reload refreshes independently from the rest of the managed stack so an optional refresh failure cannot block launch or disable existing performance, bug-fix, or QoL mods.
- Upstream settings and defaults are left untouched. No gameplay automation, packet behavior, graphics settings, render distance, or simulation distance are modified.
- Config persistence, offline operation, smart caching, Simple Voice Chat management, managed resource packs, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** QoL/reliability release. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.36**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.35

### Reese's Sodium Options managed QoL integration
- Added **Reese's Sodium Options** to MazLauncher's managed Minecraft 26.2 Fabric stack to make Sodium's settings easier to navigate, search, and configure without changing gameplay behavior.
- MazLauncher resolves a compatible Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact upstream `reeses-sodium-options` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Reese's Sodium Options JAR.
- Reese's Sodium Options refreshes independently from the rest of the managed stack so an optional refresh failure cannot block launch or disable the existing performance and bug-fix mods.
- Upstream settings and defaults are left untouched. The integration does not force graphics settings, alter Sodium configuration values, or change render distance or simulation distance.
- Config persistence, offline operation, smart caching, Simple Voice Chat management, managed resource packs, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** QoL/reliability release. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.35**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.34

### Version consistency CI guard
- Added a dedicated GitHub Actions guard that verifies MazLauncher version metadata stays synchronized before launcher changes merge or release.
- The check reads `MazLauncher.csproj` and requires `Version`, `AssemblyVersion`, and `FileVersion` to agree; it separately verifies the Inno Setup `MyAppVersion` matches the same launcher version.
- The check also requires the current launcher version to have a matching section in `LAUNCHER_CHANGELOG.md`, preventing release jobs from reaching publication with missing launcher notes.
- This closes a reliability gap in the release chain: assembly metadata, installer metadata, and launcher release notes can no longer silently drift apart while the ordinary client/launcher build remains green.
- Existing release verification remains authoritative for publication and still enforces exactly one public Windows EXE asset with no raw MazClient or third-party JARs.
- Runtime behavior is unchanged: config persistence, offline operation, smart caching, Simple Voice Chat management, managed performance mods, and disconnect stability are preserved.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** release-engineering reliability update. MazClient remains **1.7.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.34**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.
