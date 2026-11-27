# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.9.0-rc1] - Phase 9: Benchmarking & Automated Verification Suite
### Added
- Implemented `KineticBenchmarkSession` for repeatable 60-second in-game preset benchmarks exporting CSV telemetry (`avg_fps`, `one_percent_low_fps`, `avg_frame_ms`, `stddev_jitter_ms`, `avg_alloc_mb_s`, `gc_count_delta`, `gc_time_delta_ms`) to `config/kinetic_ingame_benchmarks.csv`.
- Added JUnit 5 correctness and hardware microbenchmark suite (`KineticBenchmarkAndVerificationTest`) verifying `FastMathEngine` LUT precision, negative `fastFloor` boundaries, 64-bit chunk key packing, tri-state `GlStateCache`, frustum/armor-stand culling invariants, and preset safety gates while measuring real Java 25 throughput on local hardware.

## [0.8.0-beta] - Phase 8: Compatibility Engine, Safe Mode & Live In-Game HUD
### Added
- Implemented `ModCompatibilityManager`, `CompatibilityRule`, surgical `KineticMixinPlugin` gating, depth/alpha GL state hooks, and `InGameHudMixin`.

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
