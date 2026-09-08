# MazClient performance investigations

This file tracks performance work that should be benchmarked before being promoted into the managed MazClient runtime.

## Current internal hot-path work

- O(1) module lookup instead of per-frame list scans.
- Cached particle-hook module references.
- Allocation-free default HUD position lookup.
- Disabled Frame Stats no longer samples in the background.
- Frame Stats reuses its sort buffer and caches formatted display text.
- Ping HUD formats its seven-sample statistics only on its existing 250 ms sample cadence.
- PotCounter scans inventory at most once per client tick instead of once per rendered frame.

## External candidates for Minecraft 26.2

Benchmark each candidate against the existing Sodium + Lithium + ImmediatelyFast + EntityCulling stack before making it launcher-managed.

- BadOptimizations: client hot-path optimizations that target work outside Sodium's renderer.
- FerriteCore: memory-use reduction; evaluate primarily for allocation pressure, GC pauses, and 1% lows.
- MoreCulling: additional model/block-face culling; test carefully alongside EntityCulling and Sodium for visual compatibility.
- Nvidium: optional NVIDIA-specific renderer for supported GPUs only; never make it a universal dependency.
- C2ME: evaluate separately for chunk-generation/loading workloads; do not assume it improves PvP standing FPS.

## Benchmark rules

- Measure average FPS and 1% lows, not average FPS alone.
- Use the same world/server, render distance, simulation distance, resolution, graphics API, and camera route.
- Warm the JVM/world before recording samples.
- Reject an optimization if it raises average FPS but materially worsens frame-time consistency or causes visual/gameplay regressions.
- Never make FPS Booster alter render distance or simulation distance automatically.
