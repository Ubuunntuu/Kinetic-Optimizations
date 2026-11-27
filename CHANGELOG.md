# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.8.0-beta] - Phase 8: Compatibility Engine, Safe Mode & Live In-Game HUD
### Added
- Implemented `ModCompatibilityManager` and `CompatibilityRule` to scan loaded mods and early classloader agents (`OptiFine`, `Sodium`, `Celeritas`, `VeloxCaelo`, `Iris`, `BetterFPS`, `Patcher`, `FoamFix`, `VanillaFix`, `OneConfig`) and surgically bypass only conflicting mixins.
- Upgraded `KineticMixinPlugin` (`IMixinConfigPlugin`) with per-mixin compatibility gating and applied/skipped mixin telemetry.
- Added `enableDepthTest`/`disableDepthTest` and `enableAlphaTest`/`disableAlphaTest` tri-state deduplication hooks in `GlStateDeduplicationMixin`.
- Implemented `InGameHudMixin` to render the real-time 5-line Kinetic telemetry HUD directly onto the in-game screen when `showPerformanceOverlay` is enabled.
- Upgraded `DiagnosticReportGenerator` to include compatibility advisories and mixin audit counts.

## [0.7.0-alpha] - Phase 7: Advanced Rendering & Resource Engine
### Added
- Expanded `GlStateCache`, `BufferUploadOptimizer`, `BufferUploaderMixin`, and `ResourceCacheOptimizer`.

## [0.6.0-alpha] - Phase 6: CPU and Memory Engine
### Added
- Implemented `FastMathEngine`, `MathHelperFastTrigMixin`, `AllocationReducer`, `MemoryPoolManager`, and `PacketBurstOptimizer`.

## [0.5.0-alpha] - Phase 5: Entity and Particle Engine
### Added
- Implemented `EntityVisibilityOptimizer`, `BlockEntityCullingOptimizer`, `ParticleEngineOptimizer`, `EntityRenderDispatcherMixin`, and `ParticleManagerMixin`.

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
