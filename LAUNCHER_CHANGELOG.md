# MazLauncher Changelog

## 0.6.14

### Release pipeline reliability
- Consolidated MazLauncher publishing onto the canonical `client-release.yml` release path and removed the redundant `launcher-installer.yml` workflow that was creating a second release for the same launcher build under a different tag prefix.
- Launcher-only updates now have one authoritative release tag format, `launcher-v<version>`, which is also the format used by MazLauncher's self-updater.
- The surviving release workflow still clean-builds MazClient, publishes and smoke-tests the Windows launcher EXE, bundles the MazClient JAR only inside the installer payload, builds the Inno Setup installer, and verifies the resulting GitHub Release before declaring success.
- Public GitHub Release assets remain restricted to exactly one Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.
- This change does not alter account/config persistence, offline operation, smart caching, managed performance mods, Simple Voice Chat management, resource-pack handling, launch/disconnect behavior, or FPS Booster settings. FPS Booster still never modifies render distance or simulation distance.

### Versioning
- This is a **MazLauncher/release-pipeline-only** reliability update. MazClient remains **1.7.8**.
- MazLauncher assembly and Inno Setup installer metadata are synchronized at **0.6.14**.

## 0.6.13

### Memory optimization
- Added **FerriteCore** as a MazLauncher-managed Fabric optimization for Minecraft 26.2 MazClient installations, complementing the existing Sodium, Lithium, ImmediatelyFast, and Entity Culling performance stack.
- MazLauncher resolves the latest stable Minecraft 26.2/Fabric FerriteCore release from Modrinth during normal launch preparation and offline-cache refresh instead of pinning an experimental build.
- FerriteCore is now part of the smart offline-cache completeness check, so an otherwise current MazClient cache is repaired once when the managed optimization is missing.
- Existing cached FerriteCore JARs remain usable when Modrinth is unavailable, matching the launcher's established offline fallback for managed mods.
- The Mods tab treats FerriteCore as launcher-managed, preventing accidental disable/removal while still leaving user-added mods untouched.
- This change does not alter FPS Booster behavior, render distance, simulation distance, networking, account/config persistence, Simple Voice Chat management, managed resource packs, or disconnect handling.

### Versioning
- This is a **MazLauncher-only** performance update. MazClient remains **1.7.8**.
- MazLauncher assembly and Inno Setup installer metadata are synchronized at **0.6.13**.
- Public GitHub Release assets remain limited to the Windows EXE installer; raw MazClient JARs remain internal to the installer/cloud package path.

## 0.6.12

### Managed resource packs
- Added **Smaller Totem** to the existing MazLauncher-managed Minecraft 26.2 visual-pack set alongside Low Fire and Low Shield PvP.
- Fixed a launch-order race where the background managed-pack warmup could still be running when Minecraft started, causing the Resource Packs screen to miss packs that were downloaded moments later.
- MazLauncher now awaits managed resource-pack preparation before launching the selected MazClient version, ensuring the ZIPs are present and their `resourcePacks` entries are written before Minecraft scans the installation.
- Cached managed packs remain available offline when Modrinth cannot be reached, preserving the existing fallback behavior.

### Version metadata
- Synchronized the MazLauncher assembly and Inno Setup installer metadata at **0.6.12**, correcting the stale installer-script version left behind after 0.6.11.
- This launcher ships with MazClient **1.6.46**; public GitHub Release assets remain limited to the Windows EXE installer and do not expose raw MazClient JARs.
- Existing account/session persistence, smart caching, Simple Voice Chat management, cloud-JAR verification, config migration, and disconnect behavior are unchanged.

## 0.6.11

### Updater integrity
- MazLauncher now reads the SHA-256 digest published by GitHub for the exact versioned Windows installer selected during self-update.
- The updater validates that the release asset exposes a well-formed SHA-256 digest, hashes the downloaded installer locally, and refuses to execute the EXE if the digest does not match.
- Installer filename/version resolution remains tied to the exact `mazlauncher-v<version>` release, while failed or malformed integrity metadata now stops the update safely instead of launching an unverified installer.
- Existing MazClient cloud-JAR SHA-256 verification, offline manifest caching, config persistence, smart caching, Simple Voice Chat management, and launch/disconnect behavior are unchanged.

### Versioning
- This is a **MazLauncher-only** reliability update. MazClient remains **1.6.45**.

## 0.6.10

### Auto RAM
- Added **Automatically allocate RAM** to Settings and enabled it by default for new launcher settings.
- MazLauncher now detects installed physical memory on Windows and calculates Minecraft's maximum heap while reserving **4 GB** outside the Minecraft heap for Windows and other applications.
- Auto RAM applies to both Vanilla and MazClient launches through the existing shared memory settings path.
- The Memory section shows detected physical RAM, the 4 GB system reserve, and the resulting Minecraft maximum; manual minimum/maximum RAM selectors remain available when Auto RAM is disabled.
- Very low-memory systems fall back conservatively instead of trying to reserve more RAM than the machine has, and launcher allocations remain bounded to the supported range.

### Updater reliability
- Fixed launcher upgrades returning GitHub **404** after a newer MazClient release changed the client-version portion of the installer filename.
- MazLauncher now resolves the Windows installer from the exact `mazlauncher-v<version>` GitHub Release assets instead of guessing an installer filename from the current cloud MazClient version.
- Future MazClient-only releases can therefore advance independently without breaking an already-published MazLauncher upgrade URL.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.27**.

## 0.6.9

### Release verification reliability
- Fixed a false CI failure after a correctly published GitHub Release when GitHub normalized release-body line endings differently from the local changelog text.
- Replaced wildcard-based changelog-body matching with a literal ordinal substring check so Markdown characters such as `*` and `[` cannot be interpreted as wildcard syntax during verification.
- Both expected and published notes now normalize CRLF/CR line endings to LF before comparison, while the release still must contain the exact matching changelog content.
- Existing safeguards remain unchanged: the public release must target the current merged commit and contain exactly one matching Windows EXE installer, with raw MazClient JARs kept out of public GitHub Release assets.

### Versioning
- This is a **MazLauncher/release-pipeline-only** reliability update. MazClient remains **1.6.27**.

## 0.6.8

### Theme transition
- Added a smooth **Dark ↔ Light** launcher theme transition instead of instantly snapping the full interface between palettes.
- The launcher now fades the main UI down briefly, swaps the complete theme palette at the midpoint, then eases back to full opacity for a cleaner two-way transition.
- Repeated theme-toggle clicks are locked out while the transition is running so overlapping animations cannot leave the UI in a half-faded state.
- The existing **Reduce motion and visual effects** preference skips the animation and applies the theme immediately.
- Theme choice is still saved before the transition finishes, so the selected Dark/Light mode remains persistent across launcher restarts.

### Versioning
- This is a **MazLauncher-only** interface update. MazClient remains **1.6.26**.

## 0.6.7

### Release publishing
- Launcher-only updates now publish to a fresh `launcher-v<version>` GitHub Release instead of rewriting the existing `v<MazClient version>` release.
- Launcher releases take their public notes from the exact matching `LAUNCHER_CHANGELOG.md` section, while new MazClient releases continue to use the matching `CHANGELOG.md` section and canonical `v*` tags used by the launcher version catalog.
- Public release verification now requires exactly one matching Windows EXE installer, checks that the release targets the current merged commit, and confirms that the published body contains the matching changelog notes.
- Raw MazClient JARs remain confined to the internal installer payload/cloud channel and are never uploaded as public release assets.

### Versioning
- This is a **MazLauncher-only** release reliability update. MazClient remains **1.6.26**.

## 0.6.6

### Theme build fix
- Fixed the ComboBox item styling compile failure by using WPF's explicit four-side `Thickness` constructor for the intended 10 px horizontal / 7 px vertical padding.
- Preserves the 0.6.4 theme-aware dropdown colors and the 0.6.5 unified launcher initialization path without changing account, cache, update, mod, resource-pack, or launch behavior.

### Versioning
- This is a **MazLauncher-only** build reliability fix. MazClient remains **1.6.26**.

## 0.6.5

### Release reliability
- Fixed the Windows release build failure caused by two `MainWindow` partial classes defining the same `OnContentRendered` lifecycle override and `FindAncestor` helper.
- Launcher logging, persistence migration, the offline startup watchdog, Settings extras, resource-pack warmup, and UI animations now share one post-render initialization path instead of competing lifecycle overrides.
- Kept the LOG button-activity lookup separate from the animation helper so future partial-class features cannot silently create another duplicate-member compile failure.

### Versioning
- This is a **MazLauncher-only** reliability fix. MazClient remains **1.6.26**.

## 0.6.4

### Interface animations
- Added subtle launcher startup, tab-content, button hover/press, and Settings overlay animations for a smoother WPF interface.
- The existing **Reduce motion and visual effects** preference disables the added motion so accessibility behavior remains user-controlled.

### Settings
- Added a real **Memory** section to Settings with persistent minimum and maximum Java RAM selectors.
- Memory choices are saved under the existing MazLauncher settings file and are applied to both Vanilla and MazClient launches on the next start.
- Minimum memory is kept at or below maximum memory automatically, with bounded choices from 512 MB up to 32 GB.
- Added an **About** section showing the running MazLauncher version, selected MazClient version, Minecraft version, Fabric Loader version, and a direct GitHub project button.

### Theme fixes
- Fixed dark-mode dropdown selectors using Windows default white dropdown styling, which could produce white text on a white background.
- ComboBox entries now use MazLauncher theme resources for normal, hovered, and selected states so Dark, Light, and High Contrast remain readable.

### Managed resource packs
- Added **Low Fire** as a MazLauncher-managed Minecraft 26.2 resource pack using the MIT-licensed `lower-fire` project from Modrinth.
- Added **Low Shield PvP** as a MazLauncher-managed Minecraft 26.2 resource pack using the MIT-licensed `low-shield-pvp` project from Modrinth.
- MazLauncher downloads the latest compatible release, keeps the last cached ZIP when the network is unavailable, replaces superseded managed pack files, and records the managed filename with local marker files.
- Both packs are automatically inserted into the MazClient installation's `resourcePacks` list so they are enabled without manual Resource Packs menu setup.
- Resource-pack warmup runs after the selected MazClient version becomes available and logs success or fallback behavior in the launcher LOG.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.26**.

## 0.6.3

### Rendering Performance
- Added **ImmediatelyFast** as a MazLauncher-managed Fabric optimization for MazClient installations.
- Added **Entity Culling** as a MazLauncher-managed Fabric optimization for MazClient installations.
- MazLauncher selects the latest compatible Minecraft 26.2/Fabric release for both mods from Modrinth during launch preparation and offline-cache refresh.
- ImmediatelyFast and Entity Culling are now part of MazLauncher's offline-cache completeness check, so a cache is only marked current when both optimizations are present.
- Existing cached copies are kept when Modrinth is unavailable, matching the launcher's offline fallback behavior for other managed mods.
- The Mods tab protects both optimization mods from accidental disable/removal because MazLauncher owns their versions.
- Managed-mod detection is now case-insensitive so mixed-case filenames such as ImmediatelyFast are handled reliably.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.19**.

## 0.6.2

### Smart Offline Cache
- Fixed MazLauncher re-running full offline cache preparation every time the launcher started even when the cached Vanilla and MazClient versions were already current.
- MazLauncher now checks the stored cache markers and verifies that the expected installation files still exist before doing expensive cache work.
- Vanilla is skipped when its cached version is current; MazClient is skipped when its cached version, client JAR, Fabric profile, and managed voice-chat mod are all present.
- If only one side is outdated or incomplete, MazLauncher refreshes only that component instead of rebuilding both caches.
- Cache markers are written only after a successful preparation, so interrupted or incomplete caches are repaired on the next run.
- The live LOG/status output explicitly reports `cache already current — skipping` or `cache needs refresh` for each component.

### Simple Voice Chat
- Added **Simple Voice Chat** as a MazLauncher-managed Fabric mod for MazClient installations.
- MazLauncher selects the latest compatible release from Modrinth for Minecraft 26.2/Fabric, downloads it automatically when missing, and keeps the last cached copy available offline.
- Simple Voice Chat is checked during MazClient launch preparation and offline-cache preparation.
- The Mods tab treats Simple Voice Chat as a managed core dependency so it cannot be accidentally disabled or removed.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.14**.

## 0.6.1

### Persistence
- Fixed MazClient appearing factory-reset after moving to a newly released MazClient version.
- MazLauncher now detects the newest previous MazClient installation and carries the user's data forward the first time a new MazClient version is selected.
- Migration preserves Minecraft `options.txt`, server list data, hotbar data, MazClient/Fabric config files, resource packs, shader packs, screenshots, and singleplayer saves.
- Managed MazClient/Fabric/Sodium/Lithium binaries remain version-isolated and are not copied forward, so user data persists without mixing managed runtime files between versions.
- Migration only fills a brand-new target installation and never overwrites settings that already exist in that version.
- The LOG tab records the source and destination MazClient versions and whether migration completed or failed.
### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.11**.

## 0.6.0

### Offline Reliability
- Fixed an offline-startup softlock where MazLauncher could remain in its busy state indefinitely if version, update, or authentication network work stalled after the internet connection disappeared.
- Added an 8-second startup watchdog that releases the launcher UI into cached/offline mode instead of allowing a dead network request to keep controls disabled forever.
- When a cached session is already available, the watchdog restores the launch controls and reports `Ready — Offline / cached mode`.
- When account restoration is still pending, the launcher restores Settings, update, sign-in, theme, and navigation access while the background operation finishes instead of freezing the whole application.
- Cloud manifest/update HTTP requests now fail over after 5 seconds and reuse the locally cached cloud manifest when available.
- The LOG tab records when startup exceeds the timeout and when MazLauncher switches into cached/offline mode, making future offline failures diagnosable.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.11**.

## 0.5.9

### Log
- Added a live **LOG** tab to MazLauncher that records launcher activity with millisecond timestamps.
- Status changes, cache/download progress, update checks, launch preparation, UI button actions, startup, shutdown, and unhandled launcher errors are written to the live log.
- The log auto-scrolls as new activity arrives and uses a monospace console-style view.
- Activity is also persisted to `%AppData%\MazLauncher\logs\launcher.log` so recent launcher behavior can be reviewed after a restart.
- Logging is fail-safe: a log-file write problem never blocks or crashes the launcher.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.11**.

## 0.5.8

### Interface
- The bottom-right Light/Dark theme switch now stays enabled while MazLauncher is busy.
- Theme switching remains available during cache warming, version loading, update checks, sign-in/sign-out work, and game preparation instead of being locked together with launch controls.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.10**.

## 0.5.7

### Offline cache
- MazLauncher now automatically prepares the latest **Vanilla Minecraft** and latest **MazClient** installation after a cached account is restored or after Microsoft sign-in completes.
- The warm cache includes Minecraft game files, the Fabric profile, MazClient, Fabric API, Sodium, and Lithium so the prepared installs can be reused without re-downloading them.
- Vanilla and MazClient version catalogs are cached under MazLauncher app data and are used as fallbacks when the online catalogs cannot be reached.
- The cloud `latest.json` manifest is cached locally and reused when the update/package server is unavailable.
- Managed Modrinth dependencies now keep the last successfully cached compatible JAR when the network is unavailable instead of blocking launch just because Modrinth cannot be reached.
- Cache state survives launcher upgrades because it remains under `%AppData%\MazLauncher`.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.10**.

## 0.5.6

### Skins
- Replaced the browser redirect in the **Skins** tab with an in-launcher Minecraft skin importer.
- Users can choose a local PNG, preview it inside MazLauncher, select **Classic (Steve)** or **Slim (Alex)**, and apply it directly to the currently signed-in Minecraft account.
- Skin uploads use the Minecraft access token from the existing Microsoft/Minecraft session instead of requiring another sign-in.
- Added validation for standard 64x64 skins and legacy 64x32 PNGs, plus clear upload status/error messages.

### Interface
- Removed the `Versioned Vanilla + MazClient installations` subtitle from the launcher header for a cleaner top bar.
- Keeps the Light/Dark switch, repaired High Contrast mode, and CHANGELOG tab from 0.5.5.

### Versioning
- This launcher ships alongside **MazClient 1.6.9**.

## 0.5.5

### Fixed
- Fixed MazClient 1.6.8 launch failures caused by MazLauncher still requesting the removed public `maz-client-1.6.8.jar` release asset.
- The current MazClient version now downloads from the dedicated `cloud/maz-client.jar` package channel and uses the cloud manifest SHA-256 verification path.
- Legacy versioned JAR URLs are only used as a compatibility fallback for older MazClient releases that still have those assets.
- Launch failures now show the useful error message instead of dumping the full .NET stack trace into the dialog.
- High Contrast now overrides the full shared theme resource palette, including backgrounds, inputs, borders, text, muted text, and accent contrast.

### Interface
- Added persistent Light and Dark launcher themes based on the supplied slate/white/indigo palette.
- Added a bottom-right Light/Dark theme button.
- Added a **CHANGELOG** tab that loads the current `CHANGELOG.md` directly inside MazLauncher.
- Converted launcher surfaces and text to dynamic theme resources so Light, Dark, and High Contrast repaint consistently.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.8**.

## 0.5.4

### Fixed
- Fixed a startup crash introduced by the Settings accessibility preferences.
- High Contrast now replaces WPF resource brushes instead of mutating shared brushes that may be frozen/read-only at runtime.
- Keeps the 0.5.3 tab-width fix and the 0.5.2 Settings account relocation.

### Versioning
- This is a **MazLauncher-only** runtime stability fix. MazClient remains **1.6.7**.

## 0.5.3

### Interface
- Replaced the default WPF tab header layout with a MazLauncher-owned tab control template.
- Play, Installations, Mods, and Skins now share the available width evenly instead of overflowing to the right.
- Reduced tab padding and removed the old header-panel sizing behavior that could clip the right edge of tab buttons.
- Kept the rounded MazLauncher tab visuals and selected/hover states from 0.5.1.

### Versioning
- This is a **MazLauncher-only** layout fix. MazClient remains **1.6.7**.

## 0.5.2

### Settings
- Added a real top-right **☰ Settings** menu.
- Removed the account card from the main launcher screen and moved Microsoft/Xbox account controls into **Settings → Account**.
- Added persistent launcher preferences stored under the MazLauncher app-data folder.
- Added familiar launcher behavior options for keeping the launcher open after Minecraft starts and checking for updates on startup.

### Accessibility
- Added **High Contrast** mode.
- Added **Larger launcher text**.
- Added **Stronger keyboard focus indicators**.
- Added **Reduced motion and visual effects** preference for accessibility-aware UI behavior.

### Interface
- Settings now opens as a dedicated overlay instead of adding another main navigation tab.
- The main launcher screen is cleaner and keeps account management out of the primary Play/Installations/Mods/Skins flow.

### Versioning
- This is a **MazLauncher-only** update. MazClient remains **1.6.7**.

## 0.5.1

### Interface
- Replaced default WPF tab chrome with custom rounded MazLauncher tabs, hover states, and a purple selected state.
- Restyled sign-in and sign-out controls into compact launcher-native buttons instead of default Windows buttons.
- Standardized launcher button heights, padding, corner radii, and spacing so controls no longer look stretched or randomly proportioned.
- Reduced the oversized Play cards and tightened Installations, Mods, Skins, and update controls for a denser layout.
- Added consistent dark surfaces, borders, and hover feedback across launcher controls.

### Versioning
- This is a **MazLauncher-only** visual update. MazClient remains **1.6.7**.

## 0.5.0

### Installations
- Added a dedicated **Installations** tab with separate selectors for **Vanilla Minecraft versions** and **MazClient versions**.
- Vanilla versions are loaded from Minecraft's version catalog through CmlLib instead of being hardcoded to Minecraft 26.2.
- MazClient versions are loaded from versioned `v*` GitHub releases and can be launched independently instead of always forcing cloud-latest.
- Vanilla and MazClient installations use version-isolated game directories so caches, configs, and mods do not all collide.

### Mods
- Added a **Mods** tab for MazClient installations.
- Mods can be added from local JAR files, enabled/disabled, removed, or opened directly in the installation's mods folder.
- MazClient, Fabric API, Sodium, and Lithium remain launcher-managed core mods and are protected from removal/disable through the Mods tab.

### Skins
- Added a **Skins** tab with a direct route to the signed-in Minecraft account's official skin manager.
- In-launcher saved skin slots and previews remain a future layer; 0.5.0 does not fake local skin uploads that are not applied to the Microsoft/Minecraft account.

### Interface
- Reworked the launcher into Play, Installations, Mods, and Skins tabs and increased the window size so controls are no longer squeezed into oversized two-button cards.

### Versioning
- MazLauncher is now **0.5.0** and is bundled with the current MazClient release line.

## 0.4.9

### Fixed
- Removed the launcher self-overwrite/livepatch updater that could leave MazLauncher partially replaced or missing after an update.
- Launcher updates now download the versioned MazLauncher installer, close the current launcher, let the installer replace the old installation, then automatically relaunch the new MazLauncher.
- Silent updater installs now explicitly relaunch MazLauncher when setup finishes.
- MazClient JAR cloud updates remain live and separate from launcher installer updates.

### Versioning
- This is a **MazLauncher-only** update path fix bundled with **MazClient 1.6.2**.

## 0.4.8

### Fixed
- Desktop and Start Menu shortcuts now point directly to a physical installed `Assets\MazLauncher.ico` file instead of relying on Windows Explorer to extract the icon from `MazLauncher.exe`.
- The installer now installs the multi-resolution MazLauncher icon into the application `Assets` directory and uses that file for shortcut and uninstall-entry icons.
- Keeps the 0.4.7 taskbar/AppUserModelID icon fix while addressing stale or generic shortcut icons separately.

### Versioning
- This release is bundled with **MazClient 1.6.2**.

## 0.4.7

### Fixed
- Rebuilt the Windows application icon as a true multi-resolution ICO containing 16, 24, 32, 48, 64, 128, and 256 pixel images so the taskbar, Start menu, desktop shortcut, Explorer, and executable use a crisp MazLauncher icon instead of a fallback or badly scaled icon.
- Added an explicit Windows AppUserModelID (`MazClient.MazLauncher`) so taskbar grouping and icon identity stay consistent.
- The installer, Start menu shortcut, desktop shortcut, executable, uninstall entry, and WPF window all continue to reference the same MazLauncher icon.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.1**.

## 0.4.6

### Changed
- Installer download filenames now always put the MazClient version first and the MazLauncher version second.
- New format: `MazClient-<client-version>-MazLauncher-<launcher-version>-Setup.exe`.
- GitHub Actions artifact names use the same client-first, launcher-second version order.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.1**.

## 0.4.5

### Fixed
- The installer now always checks for an existing MazLauncher installation before installing.
- Any running MazLauncher process is closed before upgrade removal begins.
- The installer runs the previous MazLauncher uninstaller silently when it is available, with a fallback to the standard per-user uninstall path.
- Leftover files in `%LocalAppData%\Programs\MazLauncher` are removed before the new version is installed so stale launcher binaries cannot survive an upgrade.
- MazLauncher account/config/cache data under `%AppData%\MazLauncher` is preserved across upgrades.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.1**.

## 0.4.4

### Fixed
- Fixed the GitHub release publishing step so it no longer tries to copy `MazLauncher-Setup-<version>.exe` over the exact same file path.
- Keeps the successful Windows publish, EXE smoke test, installer build, icon generation, update button, sign-out confirmation, resizable/minimize/maximize window, and updater rollback behavior from 0.4.3.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.1**.

## 0.4.3

### Added
- Added a **CHECK FOR UPDATES** button that immediately checks the cloud channel and applies a newer MazLauncher build when one is available.
- The update button reports the current MazLauncher version and MazClient cloud version when everything is already current.
- Added a **Sign out** button for the cached Microsoft/Xbox account.
- Added a confirmation dialog before sign-out so the cached account is not removed accidentally.
- MazLauncher can now be resized, minimized, and maximized using the normal Windows controls.

### Fixed
- Fixed the Windows build ordering so `Assets/MazLauncher.ico` is generated before `dotnet publish`, preventing WPF from failing because the icon file does not exist yet.
- Fixed the self-updater rollback PowerShell raw string so the Windows launcher build compiles successfully.
- Keeps the explicit purple **M** icon on the WPF window/taskbar and in the executable, installer, shortcuts, and uninstall entry.
- Keeps the self-update backup/rollback protection and Windows EXE smoke test introduced for the icon/updater hardening work.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.1**.

## 0.4.2

### Fixed
- Explicitly applies the MazClient purple **M** icon to the WPF window and taskbar instead of relying on Windows to inherit the executable icon.
- Keeps the same icon embedded in `MazLauncher.exe`, the installer, Start Menu/Desktop shortcuts, and uninstall entry.
- Generates the icon before WPF resource preparation so both the application executable and XAML window resource receive the same icon.
- Hardened launcher self-update with a full backup and automatic rollback if the freshly patched launcher exits immediately after replacement.
- Added a Windows launcher smoke test before cloud or installer publishing so an executable that instantly exits cannot be released.

### Versioning
- This is a **MazLauncher-only** release built alongside MazClient **1.6.1**.
- Launcher-only releases now use MazLauncher versioning instead of creating a fake MazClient version bump.