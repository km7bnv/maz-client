# MazLauncher Changelog

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
- Keeps the 0.5.3 tab-width fix and the 0.5.2 Settings menu/account relocation.

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
