# Managed resource-pack launch ordering

Low Fire, Low Shield PvP, and Smaller Totem are managed visual packs for MazClient.

The existing launcher warmup is asynchronous. Minecraft may be launched before that warmup finishes, which means the game can scan the installation's `resourcepacks` directory before the managed ZIPs exist. The launch path must await `ManagedResourcePackService.EnsureForMazClientAsync(...)` before creating the Minecraft process. This note is temporary implementation tracking and should be removed when the launch-path wiring lands.
