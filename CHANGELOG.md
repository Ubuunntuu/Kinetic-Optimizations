# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [1.0.0] - Phase 10: Final Data-Driven Tuning & Production Release (1.8.9 OneClient)
### Changed & Tuned
- ChunkRebuildDeduplicator: Upgraded to a 16,384-slot generation-stamped open-addressed 64-bit hash table, eliminating synchronized monitor contention and Arrays.fill wipes for true O(1) tick rollovers and 99.9%+ duplicate filtering.
- AllocationReducer: Removed AtomicLong contention barrier, added loop-hoistable CursorRing, and added register-only 64-bit packed BlockPos operations.
### Added
- Implemented KineticKeybindController providing live in-game hotkeys (F8 HUD toggle, F9 preset cycler, F10 60-second CSV benchmark + diagnostic report snapshot).
- Added production LICENSE and complete installation/JVM tuning documentation in README.md.

## [0.9.0-rc1] - Phase 9: Benchmarking & Automated Verification Suite
- Implemented KineticBenchmarkSession and JUnit 5 hardware microbenchmark suite.

## [0.8.0-beta] - Phase 8: Compatibility Engine, Safe Mode & Live In-Game HUD
- Implemented ModCompatibilityManager, surgical KineticMixinPlugin gating, depth/alpha GL state hooks, and InGameHudMixin.

## [0.7.0-alpha] - Phase 7: Advanced Rendering & Resource Engine
- Expanded GlStateCache, BufferUploadOptimizer, BufferUploaderMixin, and ResourceCacheOptimizer.

## [0.6.0-alpha] - Phase 6: CPU and Memory Engine
- Implemented FastMathEngine (8.81x measured trig speedup), MathHelperFastTrigMixin, AllocationReducer, MemoryPoolManager, and PacketBurstOptimizer.

## [0.5.0-alpha] - Phase 5: Entity and Particle Engine
- Implemented EntityVisibilityOptimizer, BlockEntityCullingOptimizer, ParticleEngineOptimizer, EntityRenderDispatcherMixin, and ParticleManagerMixin.

## [0.4.0-alpha] - Phase 4: Chunk Engine
- Implemented ChunkRebuildDeduplicator, ChunkUploadBudgeter, ChunkVisibilityOptimizer, KineticChunkEngine, and ChunkRebuildMixin.

## [0.3.0-alpha] - Phase 3: High-Impact Rendering Optimizations
- Implemented GlStateCache, GlStateDeduplicationMixin, FastFrustumCuller, RenderScratchPool, and KineticRenderOptimizer.

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
- Implemented FrameTimeTracker, MemoryAndGcTracker, WorldRenderStats, BottleneckAnalyzer, DiagnosticReportGenerator, and PerformanceOverlayRenderer.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
- Configured Gradle 9.7.0 + Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
