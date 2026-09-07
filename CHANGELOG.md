# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.6.3

### Performance
- Reworked FPS Booster so the presets make a much larger performance tradeoff instead of only nudging vanilla settings.
- **Balanced** now caps render distance at 8 chunks, simulation distance at 5, entity distance at 80%, keeps roughly one-third of particles, and disables entity shadows.
- **Aggressive** now caps render distance at 4 chunks, simulation distance at 5, entity distance at 50%, keeps roughly one-eighth of particles, and disables entity shadows.
- **Extreme** now caps render distance at 2 chunks, simulation distance at 5, entity distance at 50%, keeps roughly one-sixteenth of particles, and disables entity shadows.
- Custom particle reduction now supports values up to 16x filtering instead of stopping at 8x.
- The default FPS Booster profile is now the new aggressive profile.

### Included
- Keeps the global module search, module detail screens, explicit toggle-only interaction, launcher shortcut icon fix, and installer-based launcher update handoff from the previous release line.

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
