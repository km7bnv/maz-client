# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.4.1

### Added
- Added **Item Counter**, which counts the selected item across your inventory.
- Added **Armor Durability**, which shows the combined durability percentage of equipped armor.
- Added **Compass**, with 8-direction headings and live yaw degrees.
- Added a **global module search bar** to the Right Shift menu. Search results can match modules across categories.

### Launcher
- Added the new MazLauncher app icon: the simple purple square with the white **M** used by the MazClient menu.
- Added a visible purple **M** logo to the MazLauncher header itself.
- Applied the icon to the Windows executable, taskbar/window identity, installer, Start Menu/Desktop shortcuts, and uninstall entry.
- Bumped MazLauncher to **0.4.0** so existing installs can receive the new launcher/icon build through cloud updates.
- Updated the installer so a newer MazLauncher installer automatically detects and removes the previous installed version before installing the new one.
- Preserves `%APPDATA%\MazLauncher` user data during launcher replacement.

### Included
- Includes **Target Health**, which shows the live health and maximum health of the player or mob currently under your crosshair.
- Keeps the saturation overlay on the vanilla hunger bar.
- Keeps MazLauncher's automatic Fabric API, Sodium, and Lithium installation and the Modrinth file-lock crash fix from 1.3.2.

## 1.4.0

### Added
- Added **Target Health**, which shows the live health and maximum health of the player or mob currently under your crosshair.
- Added **Item Counter**, which counts the selected item across your inventory.
- Added **Armor Durability**, which shows the combined durability percentage of equipped armor.
- Added **Compass**, with 8-direction headings and live yaw degrees.
- Added a **global module search bar** to the Right Shift menu. Search results can match modules across categories.

### Changed
- Replaced the old **Health Display** HUD module, which only duplicated your own vanilla heart bar, with the more useful Target Health module.
- Updated HUD editor previews for the new HUD modules.

### Launcher
- Added the new MazLauncher app icon: the simple purple square with the white **M** used by the MazClient menu.
- Added a visible purple **M** logo to the MazLauncher header itself.
- The icon is applied to the Windows executable, taskbar/window identity, installer, Start Menu/Desktop shortcuts, and uninstall entry.
- Bumped MazLauncher to **0.4.0** so existing installs can actually receive the new launcher/icon build through cloud updates.
- New installer builds automatically detect and remove the previous MazLauncher installation before installing the new version while preserving `%APPDATA%\MazLauncher` user data.

### Included
- Keeps the saturation overlay on the vanilla hunger bar.
- Keeps MazLauncher's automatic Fabric API, Sodium, and Lithium installation and the Modrinth file-lock crash fix from 1.3.2.

## 1.3.2

### Fixed
- Fixed MazLauncher crashing while downloading Fabric API, Sodium, or Lithium because the temporary JAR was moved before its file stream was closed.
- Changed Modrinth downloads to use unique temporary files and clean them up safely after each download.

### Launcher
- Keeps automatic Fabric API, Sodium, and Lithium installation for the MazClient instance.
- Includes the safer launcher self-update behavior so a protected install directory does not cause an immediate close loop.

### MazClient
- Includes the saturation display on the vanilla hunger bar from 1.3.1.

## 1.3.1

### Fixed
- Reworked Saturation so it is shown directly on the vanilla hunger bar instead of as a separate draggable HUD text box.

### Changed
- Saturation is locked to the vanilla hunger bar and is no longer shown in the HUD editor.

## 1.3.0

### Added
- Added a Saturation HUD feature.
- Added automatic Sodium installation to MazLauncher for the MazClient instance.
- Added automatic Lithium installation to MazLauncher for the MazClient instance.

### Launcher
- Added SHA256 verification for MazClient cloud downloads.
- Added safer launcher startup handling so cloud-update failures do not block cached account restoration.
- Changed new launcher installs to a per-user location to reduce updater permission problems.

## 1.2.2

### Fixed
- Forced a clean MazClient cloud refresh after an old 1.0.0 JAR was accidentally published as the current client.
- Changed cloud publishing to select the exact versioned MazClient JAR instead of an arbitrary JAR from build output.
- Added SHA256 information to the cloud manifest.
