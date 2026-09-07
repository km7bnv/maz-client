# MazLauncher Changelog

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
