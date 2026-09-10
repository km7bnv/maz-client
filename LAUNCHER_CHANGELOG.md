# MazLauncher Changelog

## 0.6.30

### Debugify managed vanilla bug-fix integration
- Added **Debugify** to MazLauncher's managed Minecraft 26.2 Fabric stack. Debugify is a mature client/server-compatible bug-fix mod focused on Mojang-tracked vanilla bugs rather than gameplay automation, and its upstream project explicitly permits modpack inclusion.
- MazLauncher resolves a compatible Minecraft 26.2 Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when necessary; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `debugify` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Debugify JAR.
- Debugify refreshes independently from Better Block Entities, BadOptimizations, Dynamic FPS, FerriteCore, Krypton, and ImmediatelyFast so an optional bug-fix refresh failure cannot disable the performance stack or block a usable cached launch.
- Debugify's upstream multiplayer-sensitive gameplay fixes remain governed by Debugify's own safe defaults; MazLauncher does not force-enable gameplay fixes or modify Debugify configuration.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, account/session persistence, MazClient config, resource packs, Simple Voice Chat management, smart caching, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** bug-fix/reliability release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.30**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.29

### ImmediatelyFast managed rendering optimization
- Added **ImmediatelyFast** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations. ImmediatelyFast accelerates immediate-mode rendering paths used by entities, block entities, particles, text, GUI/HUD, maps, and other compatible client rendering without adding gameplay automation.
- The integration uses the current Minecraft 26.2 Fabric channel from Modrinth, preferring stable releases and falling back to compatible beta builds only when necessary; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `immediatelyfast` mod id. Corrupt, truncated, or wrong-mod files are discarded rather than trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated ImmediatelyFast JAR.
- ImmediatelyFast refreshes independently from Better Block Entities, BadOptimizations, Dynamic FPS, FerriteCore, and Krypton, so a failure in one optional optimization cannot block a usable cached launch or disable the rest of the stack.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, account/session persistence, MazClient config, resource packs, Simple Voice Chat management, smart caching, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.29**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.28

### Krypton managed network optimization
- Added **Krypton** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations. Krypton optimizes Minecraft's networking stack on the client without adding gameplay automation or requiring server-side installation.
- MazLauncher resolves a compatible Minecraft 26.2 Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when no stable build exists; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `krypton` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Krypton JAR.
- Better Block Entities, BadOptimizations, Dynamic FPS, FerriteCore, and Krypton refresh independently, so one optional optimization failing cannot disable the rest of the managed stack or block a usable cached launch.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, account/session persistence, MazClient config, resource packs, Simple Voice Chat management, smart caching, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.28**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.27

### FerriteCore managed memory optimization
- Added **FerriteCore** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations. FerriteCore reduces Minecraft memory usage through well-established memory optimizations and complements Sodium, Better Block Entities, BadOptimizations, and Dynamic FPS without changing gameplay behavior.
- MazLauncher resolves a compatible Minecraft 26.2 Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when no stable build exists; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `ferritecore` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated FerriteCore JAR.
- Better Block Entities, BadOptimizations, Dynamic FPS, and FerriteCore refresh independently, so one optional optimization failing cannot disable the rest of the managed stack or block a usable cached launch.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, account/session persistence, MazClient config, resource packs, Simple Voice Chat management, smart caching, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.27**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.26

### Dynamic FPS managed performance integration
- Added **Dynamic FPS** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations. Dynamic FPS is client-side and reduces wasted CPU/GPU work when Minecraft is unfocused, idle, obscured, or running on battery without changing active-game render distance or simulation distance.
- MazLauncher resolves a compatible Minecraft 26.2 Fabric build from Modrinth, preferring a stable release and falling back to a compatible beta only when no stable build exists; alpha builds are never selected.
- Downloads and cached copies are validated before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `dynamic_fps` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated Dynamic FPS JAR.
- Better Block Entities, BadOptimizations, and Dynamic FPS refresh independently so one optional optimization failing does not disable the rest of the managed stack or block a usable cached launch.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, account/session persistence, MazClient config, resource packs, Simple Voice Chat management, smart caching, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.26**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.25

### BadOptimizations managed performance integration
- Added **BadOptimizations** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations. The mod is client-side, has no required dependencies, and complements Sodium/Better Block Entities by reducing avoidable client work outside the renderer, including unnecessary lightmap updates and dormant debug-renderer logic.
- MazLauncher resolves a compatible Fabric build for Minecraft 26.2 from Modrinth, preferring a stable release and falling back to a compatible beta only when no stable build exists. Alpha builds are never selected.
- Downloads and cached copies are validated as the expected Fabric mod before use: the JAR must be readable, contain root `fabric.mod.json`, parse successfully, and report the exact `badoptimizations` mod id. Corrupt, truncated, or wrong-mod files are discarded instead of being trusted.
- A marker-backed last-known-good cache preserves offline launches. If Modrinth is unavailable, MazLauncher reuses only a previously validated BadOptimizations JAR and does not disturb a working cache.
- Better Block Entities and BadOptimizations are refreshed independently so one optional optimization failing to refresh does not discard or disable the other. The launcher still reaches a usable ready state and logs which optional optimization could not be refreshed.
- The integration is limited to launcher-managed MazClient installations and does not modify Vanilla installations, user resource packs, MazClient config, account/session persistence, Simple Voice Chat management, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** performance release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.25**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.24

### Better Block Entities compatibility and self-healing
- Fixed the Minecraft 26.2 Better Block Entities resolver introduced in 0.6.23: MazLauncher previously accepted only Modrinth entries marked `release`, while the current compatible Fabric 26.2 build is published as a `beta`.
- MazLauncher now prefers a stable Fabric release when one exists and falls back to the newest compatible Fabric beta when no stable 26.2 release is published. Alpha builds are never selected.
- Managed Better Block Entities downloads are now validated as real Fabric mod JARs before they enter the cache. The archive must be readable, contain root `fabric.mod.json`, parse as valid JSON, and expose a non-empty Fabric mod id.
- Existing cached Better Block Entities files are checked with the same validator. Corrupt, truncated, wrong-loader, or otherwise invalid cached files are discarded and repaired instead of being trusted because of file size alone.
- Offline fallback now uses only a previously cached Better Block Entities JAR that passes the same Fabric metadata validation, preserving the last-known-good smart-cache behavior without allowing a damaged cache to poison future launches.
- The repair is limited to MazClient-managed installations and does not change Vanilla installations, MazClient config persistence, account/session persistence, resource packs, Simple Voice Chat management, or disconnect behavior.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** reliability release. MazClient remains **1.7.15**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.24**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.23

### Better Block Entities optimization
- Added **Better Block Entities** to MazLauncher's managed Minecraft 26.2 Fabric performance stack for MazClient installations.
- MazLauncher resolves a compatible Fabric build for Minecraft 26.2 from Modrinth and caches it per MazClient installation.
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
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.18**.
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
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.17**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## Historical launcher releases
The complete 0.6.16-and-earlier launcher history is preserved in `LAUNCHER_CHANGELOG_ARCHIVE.md`.
