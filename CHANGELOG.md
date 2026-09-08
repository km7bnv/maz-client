# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.6.17

### Stability
- Removed MazClient's automatic title-screen replacement after leaving a singleplayer world or multiplayer server.
- Minecraft now owns the entire disconnect-to-title transition, which eliminates the panorama-only softlock path instead of trying to time a custom screen swap during teardown.
- MazClient's custom home screen is now installed only during clean startup, never after a world/server session has begun.
- The custom pause menu's disconnect button is now labeled **Disconnect to Title Screen** to match its actual behavior.
- Pause-screen replacement remains active while in-game and is isolated from the title/disconnect transition.

## 1.6.16

### Config Portability & Reliability
- Hardened `.mazconfig` imports so malformed or oversized bundles fail before MazClient module or HUD state is applied.
- Added limits for total bundle size and known entry size, and reject duplicate MazClient bundle entries instead of silently accepting ambiguous data.
- Imported Minecraft `options.txt` is now staged to a temporary file and replaced atomically when the filesystem supports it, while preserving the existing `options.txt.mazclient-backup` safety copy.
- Unknown bundle entries remain ignored for forward compatibility, while all required MazClient metadata/module/HUD entries are still validated before import.

### Release Reliability
- The MazClient public release workflow now removes stale release assets before uploading the current Windows installer, so each new public client tag exposes only the matching EXE installer.
- Raw MazClient JARs remain internal to the package/cloud path and are not published as GitHub Release assets.

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
