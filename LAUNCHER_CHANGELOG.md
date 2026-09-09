# MazLauncher Changelog

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
