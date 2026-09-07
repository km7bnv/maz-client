# MazLauncher Changelog

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
