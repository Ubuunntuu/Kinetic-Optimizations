# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [1.1.0-beta1] - Phase 11: Deep Bytecode Culling Pack & Unfocused GPU Power Saver
### Added
- Upgraded EntityRenderDispatcherMixin to actively cancel rendering at HEAD for distant entities beyond maxEntityRenderDistanceSq and unnamed invisible marker ArmorStands while preserving all Players.
- Added BlockEntityRenderDispatcherMixin to actively cull distant and off-frustum TileEntities (chests, signs, skulls, banners) while preserving global Beacons and active block-breaking animations.
- Upgraded ChunkRebuildMixin to actively short-circuit redundant non-urgent RenderChunk.setDirty calls within the same tick via our 6.18 ns generation-stamped deduplicator.
- Added UnfocusedFpsLimiter (limitUnfocusedFps = true, unfocusedFpsTarget = 15) to drop background GPU/CPU load to near-idle when Alt+Tabbed.

## [1.0.0] - Phase 10: Final Data-Driven Tuning & Production Release (1.8.9 OneClient)
- Generation-stamped 16,384-slot ChunkRebuildDeduplicator (25.8x faster), lock-free CursorRing, F8/F9/F10 in-game controls, and production documentation.
