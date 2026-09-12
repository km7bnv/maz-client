# MazLauncher Changelog

Historical notes through **0.6.40** are preserved verbatim in `LAUNCHER_CHANGELOG_ARCHIVE_0.6.40_AND_EARLIER.md`.

## 0.6.44

### Coalesced cloud-manifest reads
- Added a short-lived in-memory cache for the validated cloud manifest so launch/update paths that ask for the same manifest within 30 seconds reuse one result instead of repeating identical GitHub requests.
- Added a `SemaphoreSlim` gate around manifest refreshes so concurrent callers share one network/cache refresh instead of racing multiple downloads and cache writes.
- Online and offline manifest results both populate the same memory cache after successful parsing; malformed or unavailable manifests are never cached as valid state.
- Preserved the existing atomic on-disk manifest cache, 5-second network timeout, SHA256 verification, offline fallback, and launcher-update verification behavior.
- Existing MazClient config, HUD setup, managed performance mods, resource packs, account cache, offline operation, smart caching, Simple Voice Chat management, and disconnect stability remain unchanged.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Research notes
- Current Minecraft 26.2 client packs still commonly combine Sodium/Lithium with targeted optimizers such as ImmediatelyFast, Entity Culling, FerriteCore, ModernFix and related culling/loading tools. MazLauncher already covers the core managed stack, so this cycle avoids stacking another overlapping performance mod merely for feature count.
- Current 26.2 HUD/QoL clients commonly expose FPS, ping, coordinates and frame/memory diagnostics. MazClient already includes those capabilities, including detailed frame pacing and GC diagnostics, so this cycle focuses on launcher reliability rather than duplicating HUD modules.

### Versioning and distribution
- This is a **MazLauncher-only reliability release**. MazClient remains **1.9.16**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.44**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.43

### Atomic cloud-manifest cache
- Changed MazLauncher's cloud manifest cache to use a write-to-temp + atomic replace path instead of writing directly over the last known-good cache file.
- A launcher interruption, disk hiccup, or process termination during a manifest refresh can no longer leave a partially written `latest-cloud-manifest.json` behind and destroy offline fallback state.
- Online manifest parsing and offline cached-manifest parsing now share the same deserialization path so fallback behavior stays consistent.
- Temporary manifest cache files are cleaned up on both success and failure.
- Existing MazClient config, HUD setup, managed performance mods, resource packs, account cache, offline operation, smart caching, Simple Voice Chat management, and disconnect stability remain unchanged.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Research notes
- Current Minecraft 26.2 Fabric performance stacks continue to center on Sodium/Lithium plus targeted rendering and memory optimizations; MazLauncher already manages Sodium, Lithium, ImmediatelyFast, Entity Culling, and FerriteCore, so this cycle avoids adding another overlapping optimization mod.
- Simple Voice Chat 2.6.22 is the current stable Minecraft 26.2 Fabric release observed during this cycle and includes fixes for reconnect races, audio initialization failures, OpenAL resource leaks, and speaker breakage. MazLauncher's existing Modrinth resolver already selects the latest stable compatible release automatically, so no hardcoded voice-chat version change is needed.

### Versioning and distribution
- This is a **MazLauncher-only reliability release**. MazClient remains **1.9.3**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.43**.
- Public GitHub Release assets must remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.42

### 3D skin and cape appearance preview
- Replaced the flat skin-image preview path with an interactive **3D Minecraft player preview** built directly into MazLauncher. Imported skins are mapped onto the block model, Classic/Steve and Slim/Alex arm widths update live, and the model can be dragged to rotate or mouse-wheeled to zoom.
- Added cape support to the Skins tab. Signed-in users can load the capes their Minecraft account already owns, preview each cape on the same 3D player, activate an owned cape, or hide the currently active account cape.
- The launcher reads the signed-in profile from Minecraft Services and can also load the currently active account skin into the 3D preview when no local skin PNG has been selected.
- Added **custom cape PNG import for local 3D preview**. Standard 2:1 cape atlases such as 64x32, 128x64, and 256x128 are accepted and wrapped onto the 3D cape model.
- Custom cape PNGs are deliberately not presented as account uploads: Minecraft Services only allows activating a cape entitlement already owned by the signed-in account. The launcher explains this distinction instead of pretending an arbitrary cape can be uploaded to Mojang.
- Skin uploading continues to use the existing authenticated Minecraft Services skin endpoint; cape activation uses the account cape endpoint and never alters gameplay packets or server behavior.
- Existing MazClient config, HUD setup, managed performance mods, resource packs, account cache, offline behavior, smart caching, Simple Voice Chat management, and disconnect stability remain untouched.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only appearance/QoL release**. MazClient remains **1.9.3**.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.42**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.

## 0.6.41

### SmoothHud removal
- Removed **SmoothHud** from MazLauncher's managed Minecraft 26.2 Fabric stack. MazLauncher no longer resolves, downloads, validates, caches, or launches SmoothHud.
- Deleted the dedicated `SmoothHudService` integration and removed SmoothHud from managed-stack initialization.
- Expanded retired-mod cleanup so existing MazClient installations automatically delete legacy SmoothHud JARs, the `.maz-smoothhud` marker, and SmoothHud-named config files on launcher startup. This covers previously managed cached installs so upgrading users do not keep the mod or its leftover config after the integration is removed.
- BetterF3 retirement cleanup remains intact.
- Existing MazClient config, HUD setup, performance mods, resource packs, account cache, offline behavior, smart caching, Simple Voice Chat management, and disconnect stability remain untouched.
- FPS Booster is unchanged and still never modifies render distance or simulation distance.

### Versioning and distribution
- This is a **MazLauncher-only** removal/cleanup release. MazClient remains unchanged.
- MazLauncher assembly and Inno Setup metadata are synchronized at **0.6.41**.
- Public GitHub Release assets remain limited to exactly one Windows EXE installer; raw MazClient and third-party mod JARs remain internal to the installer/cache path.
