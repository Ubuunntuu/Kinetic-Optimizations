# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.7.0-alpha] - Phase 7: Advanced Rendering & Resource Engine
### Added
- Expanded `GlStateCache` and `GlStateDeduplicationMixin` with tri-state OpenGL capability deduplication (`enableBlend`/`disableBlend`, `enableCull`/`disableCull`) with per-frame safety resynchronization.
- Implemented `BufferUploadOptimizer` and `BufferUploaderMixin` to track vertex buffer uploads and short-circuit empty (0-vertex) buffer draw calls.
- Implemented `ResourceCacheOptimizer` hooked into `Minecraft::reloadResources` to flush OpenGL shadow state and thread-local buffer pools on resource-pack reloads.

## [0.6.0-alpha] - Phase 6: CPU and Memory Engine
### Added
- Implemented `FastMathEngine` and `MathHelperFastTrigMixin` providing a 16 KB L1-cache-resident trigonometric lookup table (`sin`/`cos`), branchless `fastFloor`, and `fastInvSqrt`.
- Implemented `AllocationReducer`, `MemoryPoolManager`, `PacketBurstOptimizer`, and `KineticCpuMemoryEngine`.

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
