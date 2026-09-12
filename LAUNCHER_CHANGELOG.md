# MazLauncher Changelog

Historical notes through **0.6.40** are preserved verbatim in `LAUNCHER_CHANGELOG_ARCHIVE_0.6.40_AND_EARLIER.md`.

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
