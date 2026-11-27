# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.5.0-alpha] - Phase 5: Entity and Particle Engine
### Added
- Implemented `EntityVisibilityOptimizer` for zero-sqrt squared-distance entity culling, 6-plane AABB frustum culling, and invisible marker armor stand filtering while preserving gameplay-critical entities.
- Implemented `BlockEntityCullingOptimizer` to skip off-screen and out-of-range TileEntity special renderers (chests, signs, skulls, banners) while preserving global beacons.
- Implemented `ParticleEngineOptimizer` enforcing configurable active particle caps (`maxActiveParticles`), squared render distance thresholds (`particleRenderDistance`), and frustum culling.
- Added `KineticEntityParticleEngine`, `EntityRenderDispatcherMixin`, and `ParticleManagerMixin`.
- Upgraded `ChunkRebuildMixin` to hook `net.minecraft.client.render.world.RenderChunk` directly.

## [0.4.0-alpha] - Phase 4: Chunk Engine
### Added
- Implemented `ChunkRebuildDeduplicator` using a zero-allocation 64-bit packed section key (`long[2048]` open-addressed hash table).
- Implemented `ChunkUploadBudgeter` with adaptive frame-time awareness.
- Implemented `ChunkVisibilityOptimizer`, `KineticChunkEngine`, and `ChunkRebuildMixin`.

## [0.3.0-alpha] - Phase 3: High-Impact Rendering Optimizations
### Added
- Implemented `GlStateCache`, `GlStateDeduplicationMixin`, `FastFrustumCuller`, `RenderScratchPool`, and `KineticRenderOptimizer`.

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
### Added
- Implemented `FrameTimeTracker`, `MemoryAndGcTracker`, `WorldRenderStats`, `BottleneckAnalyzer`, `DiagnosticReportGenerator`, and `PerformanceOverlayRenderer`.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
