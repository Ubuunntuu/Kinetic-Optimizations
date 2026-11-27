# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.6.0-alpha] - Phase 6: CPU and Memory Engine
### Added
- Implemented `FastMathEngine` and `MathHelperFastTrigMixin` providing a 16 KB L1-cache-resident trigonometric lookup table (`sin`/`cos`), branchless `fastFloor`, and `fastInvSqrt`.
- Implemented `AllocationReducer` with pre-computed 6-direction normal offset tables and a 32-slot thread-local `MutablePosCursor` ring to eliminate temporary `BlockPos` and `Direction.values()` array allocations.
- Implemented `MemoryPoolManager` to automatically trim thread-local pools under high heap occupancy (>85%) without calling `System.gc()`.
- Implemented `PacketBurstOptimizer` and `KineticCpuMemoryEngine`.
- Upgraded `EntityRenderDispatcherMixin` to hook `setCameraPos` and `shouldRender`.

## [0.5.0-alpha] - Phase 5: Entity and Particle Engine
### Added
- Implemented `EntityVisibilityOptimizer`, `BlockEntityCullingOptimizer`, `ParticleEngineOptimizer`, `KineticEntityParticleEngine`, `EntityRenderDispatcherMixin`, and `ParticleManagerMixin`.

## [0.4.0-alpha] - Phase 4: Chunk Engine
### Added
- Implemented `ChunkRebuildDeduplicator`, `ChunkUploadBudgeter`, `ChunkVisibilityOptimizer`, `KineticChunkEngine`, and `ChunkRebuildMixin`.

## [0.3.0-alpha] - Phase 3: High-Impact Rendering Optimizations
### Added
- Implemented `GlStateCache`, `GlStateDeduplicationMixin`, `FastFrustumCuller`, `RenderScratchPool`, and `KineticRenderOptimizer`.

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
### Added
- Implemented `FrameTimeTracker`, `MemoryAndGcTracker`, `WorldRenderStats`, `BottleneckAnalyzer`, `DiagnosticReportGenerator`, and `PerformanceOverlayRenderer`.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
