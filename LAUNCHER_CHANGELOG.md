# MazLauncher Changelog

## 0.6.21

### Vanilla Purity Guard
- Added a Vanilla launch guard that checks launcher-managed Vanilla installations for MazClient-managed mod artifacts before Minecraft starts.
- If known MazClient-managed JARs are found in a Vanilla installation, MazLauncher removes only those managed artifacts and leaves unrelated user files untouched.
- The guard runs only on launcher-managed Vanilla game directories and does not modify MazClient installations.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.21**.
- MazClient remains **1.7.13**.

### Distribution
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
