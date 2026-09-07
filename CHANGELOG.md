# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.6.2

### Modules
- Global module search now stays global across every category instead of being cleared when a category is clicked.
- Search matches module names, descriptions, and category names.
- Clicking a module row now opens a dedicated details screen with the full description and module controls.
- FPS Booster exposes its existing tuning screen from the module details view.
- Action modules expose an explicit **RUN ACTION** control in the details view.

### Safety / Interaction
- Normal module rows no longer toggle when the row itself is clicked.
- A module can only be enabled or disabled by explicitly clicking its slider/toggle hitbox.
- Clicking the rest of a module row is reserved for details/settings navigation.

### Included Launcher
- Bundled with MazLauncher **0.4.8**, which fixes the Windows desktop and Start Menu shortcut icon by pointing shortcuts directly at the installed multi-resolution ICO file.

## 1.6.1

### Launcher
- Bumped MazLauncher to **0.4.1** so existing 0.4.0 installs detect the icon build through the cloud updater instead of requiring a reinstall.
- The cloud updater replaces the launcher executable after MazLauncher closes, restarts the updated launcher, and preserves the existing MazLauncher cache/config directory.
- Keeps the MazClient purple **M** application icon embedded in the rebuilt Windows executable, installer, shortcuts, and uninstall entry.

## 1.6.0

### Performance
- Added a dedicated **FPS Booster Tuning** screen with Balanced, Aggressive, Extreme, and Custom profiles.
- Added live controls for render distance, simulation distance, entity render distance, particle density, and entity shadows.
- FPS Booster settings now persist across restarts and re-apply live while the booster is enabled.
- Particle filtering is now configurable instead of being hardcoded to one fixed reduction level.

### Interface
- Clicking **FPS Booster** opens its dedicated tuning screen, which contains the booster enable/disable control.
- **Client Settings** on both the MazClient home screen and pause menu now opens Minecraft's real vanilla Options screen.
- **Modules** remains the dedicated MazClient module menu, removing the duplicate-settings behavior.

### Fixed
- Fixed **Clear Chat** so it acts as an instant one-shot action instead of switching on and immediately back off like a broken toggle.

## 1.5.2

### Fixed
- Fixed the MazClient title-screen softlock that could appear after leaving a singleplayer world or multiplayer server.
- MazClient no longer replaces Minecraft's vanilla TitleScreen or PauseScreen from inside Fabric's `AFTER_INIT` callback.
- Screen replacement is now queued and performed on the next client tick, after Minecraft finishes initializing the vanilla screen.
- Updated the queued replacement logic for Minecraft 26.2's `client.gui.screen()` screen accessor.

## 1.5.1

### Performance
- Rebuilt **FPS Booster** from a placeholder toggle into a real reversible performance profile.
- FPS Booster now caps render distance at 6 chunks when needed, simulation distance at 5 chunks, and entity render distance scaling at 75%.
- FPS Booster disables entity shadows while active to reduce per-frame rendering work.
- Keeps MazClient's existing particle-reduction mixin, which drops roughly two-thirds of particles while FPS Booster is enabled.
- All changed player settings are restored to their exact previous values when FPS Booster is disabled.

### Modules
- Removed **AutoWalk**, **AutoJump**, **AutoMine**, and **AutoRespawn** from MazClient completely.
- Deleted the removed module implementations so they are no longer registered, shown, or available through module hotkeys.

### Interface
- Added a description to every current MazClient module.
- Module descriptions now appear directly under module names in the Right Shift menu.
- Global module search now matches both module names and module descriptions.
- Expanded module rows and scrolling to keep descriptions readable without breaking the category layout.

### Included
- Keeps the custom MazClient home screen, pause menu, About screen, HUD editor, launcher cloud updates, caching, and automatic Sodium/Lithium/Fabric API setup from 1.5.0.

## 1.5.0

### Added
- Added a fully custom **MazClient home screen** replacing the vanilla title menu, with branded Singleplayer, Multiplayer, Client Settings, HUD Editor, About, and Quit controls.
- Added a custom **MazClient pause menu** with Resume, Client Settings, HUD Editor, module access, player/session details, and live world/server status.
- Added a dedicated **About / Client Info** screen showing the MazClient version, active/installed module count, controls, and client build identity.
- Added live module status and a visible **Right Shift** shortcut hint to the home screen.
- Added a safe **Return to MazClient Home** flow from the pause menu for both singleplayer and multiplayer.

### Changed
- The Minecraft window title now displays **MazClient <version>** using the current Fabric mod metadata instead of a hardcoded version.
- Expanded MazClient branding across the main menu and in-game pause experience.
- Navigation between MazClient settings, HUD editor, home, world selection, and multiplayer screens is now integrated directly into the custom UI.

### Included
- Keeps the full MazClient HUD/module system, global module search, Target Health, Item Counter, Armor Durability, Compass, Saturation, and existing performance/client modules.
- Keeps MazLauncher's cloud update, caching, automatic Fabric API/Sodium/Lithium installation, app icon, and user-data-preserving installer behavior.

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
- Preserves `%APPDATA%\\MazLauncher` user data during launcher replacement.

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
- New installer builds automatically detect and remove the previous MazLauncher installation before installing the new version while preserving `%APPDATA%\\MazLauncher` user data.

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
