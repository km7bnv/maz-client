# MazLauncher Changelog

## 0.4.2

### Fixed
- Explicitly applies the MazClient purple **M** icon to the WPF window and taskbar instead of relying on Windows to inherit the executable icon.
- Keeps the same icon embedded in `MazLauncher.exe`, the installer, Start Menu/Desktop shortcuts, and uninstall entry.
- Generates the icon before WPF resource preparation so both the application executable and XAML window resource receive the same icon.
- Hardened launcher self-update with a full backup and automatic rollback if the freshly patched launcher exits immediately after replacement.
- Added a Windows launcher smoke test before cloud or installer publishing so an executable that instantly exits cannot be released.

### Versioning
- This is a **MazLauncher-only** release. MazClient remains at **1.6.0**.
- Launcher-only releases now use MazLauncher versioning instead of creating a fake MazClient version bump.
