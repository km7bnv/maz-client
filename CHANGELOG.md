# MazClient Changelog

Every public MazClient release must have notes here before the GitHub Release is published.

## 1.6.44

### Last Death HUD
- Added an opt-in **Last Death** HUD that records the local player's most recent death coordinates and dimension and keeps them visible after respawn for easier item recovery.
- Death capture runs entirely from the already-loaded client player/world state on the death-state transition; it adds no packets, server requests, telemetry, combat automation, or multiplayer timing behavior.
- The HUD uses MazClient's existing module-state plus HUD position/opacity persistence paths and is hidden until a death has actually been observed in the current game session.
- This client-side QoL feature does not alter FPS Booster, render distance, simulation distance, networking, smart caching, Simple Voice Chat management, launcher state, or disconnect handling.

## 1.6.43

### Exit Game Stability
- Fixed the MazClient home-screen **Quit** button softlock that could leave the game window stuck instead of closing.
- Replaced the direct blocking `Minecraft.stop()` call from the screen click handler with GLFW's normal window-close request so Minecraft's main loop owns shutdown and teardown.
- The fix is isolated to the title-screen Quit action and does not change disconnect-to-title behavior, world saving, FPS Booster, render distance, simulation distance, networking, configs, caching, or launcher state.

## 1.6.42

### XP Progress HUD
- Added an opt-in **XP Progress** HUD that shows the player's current experience level plus progress toward the next level as both a percentage and current/required XP points.
- XP data is read only from the local player state already synchronized by Minecraft and refreshes at 4 Hz, so the overlay adds no extra packets, polling, telemetry, or gameplay automation.
- The module uses MazClient's existing module-state and HUD position/opacity persistence paths, so it stays portable with existing configs and works offline in singleplayer.
- This is an informational QoL feature only and does not alter FPS Booster, render distance, simulation distance, networking behavior, smart caching, Simple Voice Chat management, or disconnect handling.

## 1.6.41

### Durability Status HUD
- Added an opt-in **Durability Status** HUD that surfaces the weakest damageable item among the player's held item and equipped armor without opening inventory or debug screens.
- The HUD shows remaining durability as `<remaining>/<maximum>` plus a percentage and uses green, amber, and red accents above 30%, from 16-30%, and at 15% or below so nearly-broken gear is easier to notice.
- Durability sampling is fully client-local and refreshes at 4 Hz instead of every rendered frame, using MazClient's existing HUD position/opacity persistence path for lightweight operation.
- This is an informational QoL feature only and does not alter gameplay, networking, FPS Booster, render distance, simulation distance, config persistence, offline operation, smart caching, Simple Voice Chat management, or disconnect handling.

## 1.6.40

### World Time HUD
- Added an opt-in **World Time** HUD that shows the synchronized Overworld day count and in-game 24-hour time without opening the debug screen.
- Updated the implementation for Minecraft 26.2's world-clock API by reading `Level#getOverworldClockTime()` instead of the removed legacy `getDayTime()` API.
- The display refreshes at 2 Hz and uses MazClient's existing HUD position/opacity persistence path, keeping render-thread work lightweight and preserving offline/config behavior.
- This is a client-side informational feature only and does not alter networking, gameplay automation, FPS Booster, render distance, simulation distance, smart caching, Simple Voice Chat management, or disconnect handling.

## 1.6.39

### Light Level HUD
- Added an opt-in **Light Level** HUD that shows the local block-light and sky-light values at the player's current block without opening the debug screen.
- Light values are read only from the already-loaded client world and refresh at 4 Hz, avoiding per-frame lighting queries while keeping the display responsive during movement.
- The HUD uses the existing MazClient HUD position/opacity persistence path and requires no server support, packet polling, telemetry, or gameplay automation.
- This is a client-side informational feature only and does not alter config persistence, offline operation, smart caching, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.38

### Dimension HUD
- Added an opt-in **Dimension** HUD that shows the current world dimension without opening the debug screen.
- The HUD reads the already-loaded client world's dimension registry key locally, formats the registry path into readable title case, and refreshes at 2 Hz to keep the overlay lightweight.
- The new HUD uses the existing HUD position/opacity persistence path and requires no server support, packet polling, telemetry, or gameplay automation.
- This is a client-side informational feature only and does not alter config persistence, offline operation, smart caching, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.37

### Coordinates HUD
- Expanded the existing **Coordinates** HUD with automatic Overworld ↔ Nether X/Z conversion so portal coordinates are available without manual 8:1 math or opening the debug screen.
- In the Overworld, converted Nether coordinates use floor division by 8 so negative positions map consistently; in the Nether, corresponding Overworld coordinates are multiplied by 8 using `long` arithmetic to avoid accidental integer overflow.
- The End keeps the normal XYZ, chunk, and chunk-local readout without showing an unrelated conversion target.
- Updated the HUD Editor preview to match the expanded live Coordinates layout.
- This is a local display-only QoL change and does not alter networking, gameplay behavior, config persistence, offline operation, smart caching, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.36

### FPS Booster Performance
- Optimized the particle-filter hot path used by **FPS Booster** and **NoParticles** so module references are resolved once instead of repeating null-resolution work for every particle insertion.
- Replaced per-particle modulo arithmetic with a lightweight countdown while preserving the configured `keep every N` particle ratio.
- The countdown resets when FPS Booster is disabled or the configured particle ratio changes, so runtime tuning remains predictable without stale filter state.
- This optimization does not modify render distance, simulation distance, networking, gameplay automation, config persistence, offline behavior, smart caching, Simple Voice Chat management, or disconnect handling.

## 1.6.35

### Biome HUD
- Added an opt-in **Biome** HUD that shows the player's current biome without opening the debug screen.
- Biome names come from the local world's existing biome registry entry and are formatted into readable title case; no server requests, packet polling, or gameplay automation are added.
- The HUD refreshes at 4 Hz instead of every rendered frame and uses the existing HUD position/opacity persistence path for lightweight operation and layout consistency.
- This is a client-side informational feature only and does not alter config persistence, offline operation, smart caching, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.34

### Coordinates HUD
- Expanded the existing **Coordinates** HUD with chunk-local X/Z offsets (`0-15`) so players can see their exact position inside the current chunk without opening the debug screen.
- Chunk-local offsets use floor-modulo math, so negative world coordinates remain correct and consistent with the existing floor-divided chunk coordinates.
- Updated the HUD Editor preview to match the expanded live Coordinates layout.
- This is a local display-only QoL change and does not alter networking, gameplay behavior, config persistence, offline operation, caching, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.33

### HUD Performance
- Reduced avoidable MazHud render-thread work by caching module references instead of resolving the same HUD modules every rendered frame.
- Inventory-, armor-, potion-, coordinates-, speed-, direction-, compass-, and related text are now refreshed on a 20 Hz cache instead of being rebuilt at uncapped render rates.
- Memory, clock, and session text use a slower 500 ms refresh where sub-frame updates provide no player-visible benefit.
- Reused a static armor-slot array instead of recreating it during HUD work, while combat-sensitive overlays such as CPS, combo, reach, target health, keystrokes, FPS, and ping remain render-responsive.
- This optimization does not alter networking, gameplay, config persistence, offline behavior, Simple Voice Chat management, disconnect handling, render distance, simulation distance, or FPS Booster behavior.

## 1.6.32

### Direction HUD
- Expanded the existing **Direction** HUD to show precise yaw and pitch angles alongside the cardinal facing label.
- Yaw and pitch are read directly from the local player orientation and formatted to one decimal place for useful building, navigation, and alignment feedback without opening the debug screen.
- Updated the HUD Editor preview to match the expanded live Direction layout so positioning remains predictable.
- This is a local display-only change and does not alter packets, multiplayer behavior, render distance, simulation distance, or FPS Booster behavior.

## 1.6.31

### Ping HUD
- Added a live connection-consistency label to the existing **Ping HUD** so players can distinguish a stable connection from one with meaningful latency variation at a glance.
- The label reports **Stable**, **Variable**, or **Unstable** from the existing rolling median and jitter values after at least three samples are available.
- Stability uses the same seven-sample, 250 ms rate-limited window already maintained by MazClient; it adds no extra network requests, packet polling, or background probes.
- Existing median ping, quality, jitter, range, and spike reporting remain intact.
- This is diagnostic only and does not alter packets, multiplayer behavior, render distance, simulation distance, or FPS Booster behavior.

## 1.6.30

### Coordinates HUD
- Added current chunk X/Z coordinates to the existing **Coordinates** HUD alongside world X/Y/Z so players can identify chunk boundaries without opening the debug screen.
- Chunk coordinates use floor division, so positions with negative X or Z values map to the correct Minecraft chunk instead of truncating toward zero.
- Updated the HUD Editor preview to match the expanded live Coordinates layout.
- The feature derives chunk position only from the local player's existing coordinates and does not add networking, gameplay automation, render-distance changes, simulation-distance changes, or FPS Booster behavior.

## 1.6.29

### HUD Performance
- Reduced avoidable work in several enabled HUD hot paths without changing their player-facing behavior.
- Ping HUD now formats and sorts its seven-sample statistics only when a new 250 ms sample is accepted instead of rebuilding the same text every rendered frame.
- Potion counting now scans inventory at most once per client tick and reuses the cached result across higher render rates.
- Frame Stats now caches its formatted frametime/1% low text and health accent on the existing 15-sample recalculation cadence instead of recreating them every frame.
- Added benchmark guidance for future 26.2 optimization candidates so external mods are measured against MazClient's existing Sodium, Lithium, ImmediatelyFast, and EntityCulling stack before becoming managed dependencies.
- These optimizations do not alter networking, gameplay, render distance, simulation distance, or FPS Booster behavior.

## 1.6.28

### Potion HUD
- Upgraded the existing **Potion HUD** from a simple active-effect count to useful live effect details.
- The HUD now shows up to three active effects with translated effect names, amplifier levels, and remaining durations, plus a compact `+N more` indicator when additional effects are active.
- Updated the HUD Editor preview to match the expanded live layout so sizing and placement stay predictable.
- Effect data is read only from the local player's existing active-effect collection; this adds no packet polling and does not alter gameplay, render distance, simulation distance, or FPS Booster behavior.

## 1.6.27

### Item Counter HUD
- Added live durability details for the currently held damageable item directly to the existing **Item Counter** HUD.
- Damageable tools, weapons, and other held items now show remaining durability as `<remaining>/<maximum>` plus a percentage, while normal stackable items keep the compact inventory-count-only display.
- Updated the HUD Editor preview to match the expanded damageable-item layout so sizing and placement are accurate before joining a world.
- Durability is read entirely from the local held `ItemStack`; this feature adds no packet polling and does not alter gameplay, render distance, simulation distance, or FPS Booster behavior.

## 1.6.26

### Ping HUD
- Added a rolling **Range** readout to the existing Ping HUD so players can see the lowest and highest sampled latency alongside stabilized median ping and jitter.
- Range uses the same seven-sample, 250 ms rate-limited window already maintained by MazClient; it does not perform extra network requests or packet polling.
- One-off spike reporting remains intact, and the rolling range stays visible during spikes so players can distinguish a single outlier from sustained instability.
- This is diagnostic only: it does not alter packets, multiplayer behavior, render distance, simulation distance, or FPS Booster settings.

## 1.6.25

### Memory HUD
- Added live memory-pressure status to the existing **Memory** HUD so players can distinguish normal heap use from conditions that may contribute to garbage-collection stutter.
- The HUD now labels usage as **OK** below 75%, **WARN** from 75–89%, and **HIGH** at 90% or above, with matching green, amber, and red accent colors.
- The HUD Editor preview now matches the expanded memory readout and healthy-state accent.
- Memory pressure is calculated locally from the JVM heap only; this change does not alter Minecraft graphics settings, render distance, simulation distance, networking, or gameplay.

## 1.6.24

### Ping HUD
- Added live **jitter** reporting to the existing Ping HUD so players can distinguish a consistently high connection from a connection that rapidly fluctuates.
- Jitter is calculated locally from the average absolute change between consecutive values in the existing seven-sample, rate-limited ping window; MazClient does not add extra network polling.
- The existing rolling-median ping and one-off spike reporting remain intact, with jitter shown alongside the stabilized connection-quality label once at least two samples exist.
- This is diagnostic only: it does not alter packets, networking behavior, multiplayer gameplay, render distance, simulation distance, or FPS Booster settings.

## 1.6.23

### Frame Stats HUD
- Added a live frame-consistency health indicator to the existing **Frame Stats** HUD without changing its compact text layout.
- The HUD accent is green when rolling 1% lows stay within 80% of rolling average FPS, amber while warming up or when consistency falls into the middle range, and red when 1% lows fall below 60% of average FPS.
- Health coloring uses only the existing local frametime sample window and does not change graphics settings, render distance, simulation distance, networking, or gameplay.
- The first 30 samples use the amber warm-up state so the HUD does not present a false stable/stutter judgment before enough frame history exists.

## 1.6.22

### Frame Stats HUD
- Added an opt-in **Frame Stats** HUD that shows rolling average frametime in milliseconds alongside a rolling **1% low FPS** estimate, making stutter visible even when the normal FPS counter still looks healthy.
- Frame pacing is sampled locally from HUD render intervals using a bounded 180-frame window, with percentile calculations refreshed every 15 samples to keep the overlay lightweight.
- The HUD uses the existing MazClient HUD position and opacity persistence path, so it can be moved and styled through the HUD Editor like other informational overlays.
- Frame Stats is diagnostic only: it does not change graphics settings, networking, gameplay, render distance, or simulation distance.

## 1.6.21

### Armor HUD
- Reworked **Armor HUD** to show the actual equipped helmet, chestplate, leggings, and boots instead of only Minecraft's total armor-point value.
- Each equipped damageable armor piece now shows its raw remaining durability as `<remaining>/<maximum>` points, with no percentage conversion.
- Empty armor slots are shown as `Empty`, and non-damageable equipment reports `n/a` instead of a fake durability value.
- Updated the HUD Editor preview so the element's real width/layout matches the new per-piece durability readout.

## 1.6.20

### Ping HUD
- Added live connection-quality labels to the Ping HUD: **Good**, **Fair**, **HIGH**, **SEVERE**, and **CRITICAL**.
- The stable rolling-median ping now always includes its current quality label so bad latency is obvious before combat starts feeling delayed.
- Large one-off spikes still preserve the stabilized ping readout, but now also show the spike latency and its severity label instead of hiding how bad the spike was.
- This remains a display/diagnostic feature only; MazClient does not claim to reduce real network latency.

## 1.6.19

### Armor Durability HUD
- Improved the existing **Armor Durability** HUD so it shows both the combined durability percentage and the weakest equipped armor piece.
- The weakest-piece readout includes the slot name and its remaining durability percentage, making a nearly-broken helmet, chestplate, leggings, or boots visible even when the overall armor average is still healthy.
- The HUD reports `Armor: none` when no damageable armor is equipped instead of showing a misleading zero-percent value.
- Updated the HUD Editor preview text to match the new live readout.

## 1.6.18

### HUD Editor
- Added a **Center** action for the currently selected HUD element so one overlay can be repositioned cleanly without resetting the rest of the layout.
- Added a **100%** action for the selected HUD element so its opacity can be restored independently of every other overlay.
- Selected-element actions save immediately through the existing HUD layout persistence path.
- Tightened the opacity slider width so the new actions remain separate from slider interaction while preserving drag-to-adjust opacity.

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