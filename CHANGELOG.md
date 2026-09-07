# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.6.15

### FPS Booster
- Added player-facing **Balanced**, **Aggressive**, and **Extreme** preset controls directly to the FPS Booster screen.
- The active preset is highlighted, while any manual Entity Distance, Particle Density, or Entity Shadows change continues to switch the booster into **Custom** mode.
- Preset changes are saved through the existing MazClient config path and apply immediately when FPS Booster is enabled.
- Render Distance and Simulation Distance remain read-only in MazClient and are never modified by FPS Booster.

## 1.6.14

### Stability
- Fixed the panorama-only softlock that could happen after leaving singleplayer or multiplayer, where Minecraft showed the title panorama but no usable buttons or MazClient home UI ever appeared.
- Removed MazClient's dependency on the one-time vanilla `TitleScreen` initialization event for home-screen recovery after disconnect.
- MazClient now watches the live client state every tick. Once player, world, connection, and integrated-server state are fully gone and the vanilla title screen remains stable, it forces a fresh `MazHomeScreen` even if the original title-screen init event was missed or reset during teardown.
- Transitional, null, or non-title screens reset the stability counter so MazClient still avoids replacing screens while Minecraft is actively disconnecting.
- Pause-screen replacement remains event-driven because it only runs while a world is active and is not part of the disconnect/title race.

## 1.6.13

### Stability
- Reworked the custom title-screen replacement to avoid racing Minecraft's disconnect teardown after leaving singleplayer or multiplayer.
- MazClient now remembers whether a world/server was active and requires the vanilla title screen to remain stable for 40 consecutive client ticks after player, world, connection, and integrated-server state are all gone before replacing it with the MazClient home screen.
- If Minecraft leaves the title screen or teardown becomes active again during that wait, the stability timer resets instead of forcing a MazClient screen swap mid-transition.
- Initial startup still replaces the vanilla title screen quickly with a shorter stability window, so the extra delay only applies after leaving a game.

## 1.6.12

### Ping HUD
- Reworked the Ping HUD so it no longer dumps Minecraft's raw `PlayerInfo` latency value directly onto the screen every frame.
- Ping samples are now rate-limited and tracked in a rolling seven-sample window, with the displayed value based on the median so one bad/stale sample does not make the HUD jump wildly.
- Invalid negative or absurd latency values are ignored instead of being rendered.
- Large real spikes are still surfaced as `Ping: <stable> ms (spike <raw>)` so the HUD stays readable without hiding actual network problems.
- This only improves MazClient's latency reporting; it does not claim to reduce real server/network ping.

## 1.6.11

### FPS Booster
- Restored the larger classic FPS Booster menu layout instead of the compact 1.6.8-style panel.
- Render Distance and Simulation Distance are shown again for visibility, but remain Minecraft-controlled and are never changed by FPS Booster.
- Entity Distance, Particle Density, and Entity Shadows remain the actual FPS Booster controls.
- Clarified particle-density behavior so the visible `-` button always means fewer particles and `+` always means more particles, even though the internal keep-every value works in the opposite direction.
- Kept the 1.6.9 rule that FPS Booster never modifies render distance or simulation distance.

## 1.6.10

### Config Profiles
- Added **Quick Config Profiles** to the Modules screen with three built-in profile slots.
- Each profile saves the same complete setup as a `.mazconfig`: Minecraft `options.txt`, MazClient module state/settings, and HUD positions/opacity.
- Each slot has **Save**, **Load**, and **Delete** controls and clearly shows whether it is saved or empty.
- Loading a profile backs up the current Minecraft options first; Minecraft-wide settings apply cleanly after restart.
- Existing **Export** and **Import** controls remain available for portable backups and sharing between installs.

### Release Reliability
- The MazClient release workflow now watches `launcher/**` and `LAUNCHER_CHANGELOG.md` because every public MazClient release packages the Windows launcher installer.
- Launcher fixes can now automatically repair/rebuild the current client installer instead of leaving a client tag missing after an earlier combined-installer failure.

## 1.6.9

### Configs
- Added **EXPORT** and **IMPORT** controls directly to the Modules screen.
- Export creates one portable `.mazconfig` bundle containing the complete Minecraft `options.txt`, MazClient module state/settings, and HUD positions/opacity.
- The bundle therefore carries GUI scale, all sound-volume sliders, render distance, simulation distance, graphics/video options, FOV, sensitivity, keybinds, accessibility/chat settings, resource-pack selections, and the other settings Minecraft stores in `options.txt`.
- Import restores MazClient module/HUD state and writes the bundled Minecraft options, while first backing up the current `options.txt` as `options.txt.mazclient-backup`.
- Minecraft-wide imported options are applied after restarting Minecraft so live option state is not corrupted mid-session.

### Modules
- Added a new **Combat** category.
- Moved Target Health, Combo Counter, Reach Display, Potion HUD, CPS, and PotCounter into Combat while keeping their HUD-style rendering and editor support.
- FPS is now draggable in the HUD Editor even though the FPS module remains in the Performance category.
- Combat HUD elements remain draggable and retain HUD opacity/position persistence.

### FPS Booster
- FPS Booster no longer modifies **render distance or simulation distance at all**.
- Removed Render Distance and Simulation Distance controls from the FPS Booster screen.
- FPS Booster now limits itself to entity-distance scaling, particle reduction, entity shadows, and future internal performance optimizations.

### Stability
- Hardened the custom home-screen replacement after leaving singleplayer or multiplayer.
- MazClient now waits for the vanilla title screen to remain stable and for world/player/connection/server teardown to complete before replacing it, reducing the exit-world softlock race.

## 1.6.8

### We are incredibly sorry for the mishaps in version 1.6.5-7. This was caused by human error, and we apologize for the inconvenience.

### FPS Booster
- Removed the Balanced, Aggressive, and Extreme preset buttons from the FPS Booster menu.
- FPS Booster now presents a compact manual tuning panel for render distance, simulation distance, entity distance, particle density, and entity shadows.
- Enabling FPS Booster still applies its automatic performance baseline for simulation distance, entity distance, particle reduction, and entity shadows.
- Disabling FPS Booster restores the player settings that were active before the booster was enabled.
- Render distance remains fully manual and is never changed by enabling or disabling FPS Booster.
- Fixed the FPS Booster `+` / `-` and entity-shadow click hitboxes so they now line up with the controls that are actually drawn on screen.

### Included
- Keeps the clickable home-screen Modules shortcut and HUD opacity/text contrast fixes from 1.6.7.

## 1.6.7

### Interface
- Made the **Modules** status area on the MazClient home screen clickable so it opens the Modules menu directly instead of only showing the Right Shift hint.
- Added hover feedback to the clickable Modules area.

### HUD Editor
- Fixed HUD opacity so lowering background opacity no longer fades the text away with it.
- HUD text now stays fully opaque and automatically changes contrast with the background opacity: transparent backgrounds use white text, fully opaque white backgrounds use black text, with grayscale interpolation between them.
- Keystrokes labels use the same readable adaptive text behavior while pressed keys keep high-contrast white text.

### Included
- Keeps the Render Saver removal and module-description QA fixes from 1.6.6.

## 1.6.6

### Modules
- Removed **Render Saver** completely so render distance is no longer duplicated by a separate module that can cap it to 8 chunks.
- Deleted the Render Saver implementation and removed it from module registration and descriptions.
- Fixed exact-name description mismatches for **Advanced Item Tooltips**, **NoDynamicFOV**, **NoHurtCam**, and **NoRain**, so module details and global search no longer fall back to the generic "MazClient module." text.
- Clarified the **FPS Booster** description to explicitly state that simulation/entity/particle tuning is automatic while render distance remains manual.

### QA
- Kept ToggleSprint and ToggleSneak behavior unchanged after review.
