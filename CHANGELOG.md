# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [1.2.0] - Phase 13 & 14: Complete Bytecode Suite, Lighting/Font Caches & Native Java 25 FFM/FMA Engine
### Added
- Native Java 25 Foreign Function & Memory (Arena / MemorySegment) 64-byte cache-line-aligned off-heap buffers in RenderScratchPool.
- Unrolled hardware Math.fma (x86-64 FMA3) 6-plane AABB frustum testing in FastFrustumCuller.
- Active ParticleManager::addParticle admission gate enforcing maxActiveParticles and squared render distance before heap allocation.
- LightingDeduplicator + WorldLightingMixin (32,768-slot generation-stamped 64-bit packed sky/block light check filter).
- DirectionCache + BlockModelDirectionMixin eliminating Direction.values() array cloning during chunk compilation.
- TextWidthCache + TextRendererCacheMixin (2,048-slot pixel width cache for scoreboards, tab lists, and nametags).
- EntityCollisionThrottler + EntityPushThrottleMixin capping client-side crammed entity push scans at 16 entities per 2x2x2 cell.
- Exposed Light Dedup, Text Width Cache, and Crammed Entity Cap toggles inside the F7 Settings GUI.

## [1.1.0] - Phase 11 & 12: Interactive F7 Settings GUI, Animated Sprite Throttler & Deep Bytecode Culling
- Added KineticConfigScreen (F7), EntityRenderDispatcher/BlockEntityRenderDispatcher HEAD culling, TextureAtlasAnimatedMixin, and UnfocusedFpsLimiter.

## [1.0.0] - Phase 1-10: Initial Production Release (1.8.9 OneClient)
- Generation-stamped ChunkRebuildDeduplicator, 16KB L1D FastMath LUT, GlStateCache, BottleneckAnalyzer, and F8/F9/F10 hotkeys.
