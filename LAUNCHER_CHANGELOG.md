# MazLauncher Changelog

## 0.6.23

### Better Block Entities optimization
- Added **Better Block Entities** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations.
- MazLauncher resolves the latest stable Fabric release for Minecraft 26.2 from Modrinth and caches it per MazClient installation instead of pinning an experimental build.
- Better Block Entities complements Sodium by optimizing supported block-entity rendering such as chests, signs, beds, bells, banners, decorated pots, shulker boxes, and related objects without changing gameplay logic.
- A marker-backed last-known-good cache is preserved for offline use. If Modrinth cannot be reached, an already cached valid JAR remains usable instead of blocking launch.
- The optimization is prepared when the selected MazClient version becomes active in the launcher and is refreshed independently from MazClient's own JAR, preserving the existing smart-cache model.
- This change does not modify Vanilla installations and does not alter MazClient gameplay, config persistence, account/session persistence, Simple Voice Chat management, resource packs, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.23**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.22

### Managed resource-pack health and priority repair
- Fixed the managed **Low Fire**, **Low Shield PvP**, and **Smaller Totem** packs appearing enabled while having no visible effect when another selected pack outranked them.
- MazLauncher now writes its managed overlay entries to the highest-priority end of Minecraft's `resourcePacks` list while preserving every unrelated user-selected resource pack and its relative order.
- Managed pack validation now checks real Minecraft resource-pack structure instead of accepting any readable ZIP: a cached/downloaded pack must contain root `pack.mcmeta` metadata and an `assets/` tree before MazLauncher marks it healthy.
- Invalid cached packs are discarded and refreshed online; if refresh fails, MazLauncher falls back only to a previously cached pack that passes the same Minecraft-structure validation.
- Pack download retries, marker-based last-known-good fallback, offline behavior, and launch-blocking repair errors remain intact.
- The change is isolated to MazClient-managed installation directories and does not modify Vanilla resource packs.
- FPS Booster remains unchanged and still never modifies render distance or simulation distance. Account/session persistence, launcher preferences, smart caching, Simple Voice Chat, managed performance mods, MazClient config persistence, and disconnect stability remain preserved.

### Versioning and distribution
- This is a **MazLauncher-only** resource-pack reliability release. MazClient remains **1.7.14**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.22**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.21

### Vanilla performance isolation
- Tightened shared `options.txt` synchronization so MazClient can no longer copy video/render/performance settings into launcher-managed Vanilla installations.
- Replaced the broad exclusion-list model from 0.6.20 with an explicit allow-list limited to non-performance preferences such as keybinds, mouse controls, language, sound categories, chat preferences, model-part toggles, narrator/subtitles, and accessibility-adjacent input/display preferences.
- Unknown future Minecraft options are isolated by default. A new option is not shared unless MazLauncher explicitly classifies it as safe and non-performance-affecting.
- Existing 0.6.20 shared-settings snapshots are sanitized on startup so stale video/performance keys already written to `%AppData%\MazLauncher\shared\minecraft-options.txt` cannot be applied after upgrading.
- Vanilla and MazClient already use separate game directories, and MazLauncher's managed Fabric/API/Sodium/Lithium/ImmediatelyFast/Entity Culling/FerriteCore/MazClient mod stack continues to be installed only inside MazClient directories. This release hardens the remaining settings-sharing boundary.
- This release does not intentionally lower Vanilla settings or modify a user's render distance, simulation distance, graphics mode, particles, entity distance, biome blend, clouds, mipmaps, VSync, FPS limit, or any other rendering/performance preference in either mode.
- FPS Booster remains unchanged and still never modifies render distance or simulation distance. Offline operation, smart caching, Simple Voice Chat, managed resource packs, account/session persistence, launcher preferences, and disconnect stability remain preserved.

### Versioning and distribution
- This is a **MazLauncher-only** isolation/reliability release. MazClient remains **1.7.13**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.21**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.20

### Shared Vanilla/MazClient settings
- Added a filtered shared Minecraft-settings layer so launcher-managed Vanilla and MazClient installations can reuse normal `options.txt` preferences such as controls/keybinds, video, sound, language, accessibility, FOV, mouse and other standard client settings.
- On MazLauncher startup, the newest managed installation `options.txt` is treated as the current normal-settings source and merged into the other managed installations using local atomic writes.
- Installation-specific state stays isolated: `resourcePacks`, `incompatibleResourcePacks`, last-server/address state, mods, worlds, MazClient config, launcher settings, account/session data, and caches are never shared through this layer.
- Excluding the resource-pack keys preserves MazLauncher's managed Low Fire, Low Shield PvP, and Smaller Totem configuration instead of leaking those entries into Vanilla or allowing Vanilla settings to wipe them.
- Sync is local-only and fail-safe. Read/write errors are ignored so settings sharing can never block launcher startup or Minecraft launch.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.20**.
- This release is bundled with MazClient **1.7.13**, which restores stronger MazClient menu chrome while preserving the fixed vanilla-owned screen lifecycle.
- FPS Booster remains unchanged and never modifies render distance or simulation distance. Offline operation, smart caching, Simple Voice Chat, managed performance mods, managed resource packs, account/session persistence, and disconnect stability remain preserved.

### Distribution
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.19

### Managed resource-pack reliability
- Hardened MazLauncher's managed **Low Fire**, **Low Shield PvP**, and **Smaller Totem** preparation so the launcher no longer silently continues when a required pack cannot be downloaded and no valid cached copy exists.
- Each managed pack now gets up to three online refresh attempts with a longer per-request timeout before MazLauncher falls back to its existing offline cache.
- Downloaded and cached resource-pack files are validated as readable ZIP archives before they are accepted. Invalid or truncated cached files are discarded and repaired instead of being left in the installation as apparently valid packs.
- Pack markers are only refreshed after the selected file has passed validation, preserving the last-known-good offline fallback when Modrinth is temporarily unavailable.
- Once all three packs are available, MazLauncher rewrites their `file/<name>.zip` entries into the selected MazClient installation's `options.txt` before Minecraft launches. If any required pack is still missing after retries and cache fallback, launch stops with a clear repair message instead of claiming the packs are ready.
- Current Modrinth project slugs remain unchanged because fresh compatibility checks confirm `lower-fire`, `low-shield-pvp`, and `small-low-totem` still publish Minecraft 26.2-compatible resource packs.
- This change does not alter MazClient gameplay behavior, account/session persistence, config migration, smart caching semantics, Simple Voice Chat management, managed performance mods, disconnect handling, or FPS Booster. FPS Booster still never modifies render distance or simulation distance.

### Versioning
- This is a **MazLauncher-only** reliability release. MazClient remains **1.7.11**.
- MazLauncher assembly and Inno Setup installer metadata are synchronized at **0.6.19**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.18

### Self-update release discovery repair
- Fixed MazLauncher's installer resolver to use the authoritative `launcher-v<version>` GitHub Release tag format introduced by the 0.6.14 release-pipeline consolidation instead of still requesting the removed `mazlauncher-v<version>` publisher.
- Update checks and installer application are now aligned: when the cloud manifest reports a newer launcher, MazLauncher resolves that same version from `launcher-v<version>`, verifies the release asset's GitHub SHA-256 digest, downloads the Windows installer, verifies the downloaded bytes again, and only then starts the silent installer.
- The resolver keeps a read-only fallback to historical `mazlauncher-v<version>` tags so older archived releases remain diagnosable without restoring the duplicate publisher for future releases.
- Because 0.6.17 contains the broken old-tag resolver, the 0.6.18 release pipeline publishes a one-time `mazlauncher-v0.6.18` compatibility bridge carrying the same verified Windows installer. This bootstrap alias exists only for 0.6.18 so installed 0.6.17 launchers can update themselves; 0.6.18 and later use the canonical tag path.
- This change does not alter account/session persistence, MazClient config migration, offline operation, smart caching, managed performance mods, Simple Voice Chat management, resource packs, launch/disconnect behavior, or FPS Booster. FPS Booster still never modifies render distance or simulation distance.

### Versioning
- This is a **MazLauncher-only** updater reliability release. MazClient remains **1.7.10**.
- MazLauncher assembly and Inno Setup installer metadata are synchronized at **0.6.18**.
- Every public release created by this cycle exposes only the matching Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.17

### Preference recovery diagnostics
- Expanded `%AppData%\MazLauncher\diagnostics\latest.txt` with explicit launcher preference-recovery state so support can distinguish a healthy primary settings file from a recoverable backup/interrupted-save situation without reading or exposing any setting values.
- Diagnostics now report whether `settings.json`, the last-known-good `settings.json.bak`, and the interrupted-save `settings.json.tmp` exist, plus one sanitized `SettingsRecoveryState` value: `PrimaryPresent`, `BackupAvailable`, `InterruptedSaveAvailable`, or `NoSettingsFiles`.
- Recovery-state reporting is local-only and inspects file existence only; it does not deserialize settings, record account/session data, expose user paths, upload telemetry, or make any network request.
- The diagnostics writer remains fail-safe and atomic: any filesystem error is ignored so diagnostics can never block MazLauncher startup, and the snapshot is still written through a temporary file before replacement.
- Account/session handling, MazClient config migration, offline operation, smart caching, managed performance mods, Simple Voice Chat management, resource packs, launch/disconnect behavior, and FPS Booster are unchanged. FPS Booster still never modifies render distance or simulation distance.

### Versioning
- This is a **MazLauncher-only** reliability update. MazClient remains **1.7.9**.
- MazLauncher assembly and Inno Setup installer metadata are synchronized at **0.6.17**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## Historical launcher releases
The complete 0.6.16-and-earlier launcher history is preserved in `LAUNCHER_CHANGELOG_ARCHIVE.md`.
