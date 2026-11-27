# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [1.1.0] - Phase 12: Interactive F7 Settings GUI, Animated Sprite Throttler & Deep Bytecode Release
### Added
- Implemented KineticConfigScreen (opened via F7): full 5-category clickable in-game GUI screen (General, Render/GPU, Chunks, Entities/Particles, CPU & HUD) with live persistence and Reset to Defaults.
- Implemented AnimatedSpriteOptimizer and TextureAtlasAnimatedMixin to pause animated texture PCIe uploads while unfocused and pace background sprite ticks during frame-time spikes.
- Active HEAD Mixin cancellation for EntityRenderDispatcher (distant entities & unnamed invisible ArmorStands), BlockEntityRenderDispatcher (distant/hidden chests, signs, skulls, banners), and duplicate RenderChunk.setDirty calls.
- Added UnfocusedFpsLimiter (throttles background FPS to 15 FPS when Alt+Tabbed).

## [1.0.0] - Phase 10: Initial Production Release (1.8.9 OneClient)
- Generation-stamped 16,384-slot ChunkRebuildDeduplicator, 16KB L1D FastMath LUT, GlStateCache, FastFrustumCuller, BottleneckAnalyzer, and F8/F9/F10 hotkeys.
