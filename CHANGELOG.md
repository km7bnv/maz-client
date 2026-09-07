# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

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
