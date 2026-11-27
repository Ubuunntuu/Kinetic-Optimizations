# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.4.0-alpha] - Phase 4: Chunk Engine
### Added
- Implemented `ChunkRebuildDeduplicator` using a zero-allocation 64-bit packed section key (`long[2048]` open-addressed hash table) to eliminate duplicate sub-chunk rebuild requests within the same tick.
- Implemented `ChunkUploadBudgeter` with adaptive frame-time awareness to throttle non-urgent GPU chunk buffer uploads when frame duration exceeds 22 ms.
- Implemented `ChunkVisibilityOptimizer` for fast sub-chunk frustum/distance culling and near-camera urgent rebuild prioritization.
- Added `KineticChunkEngine` and `ChunkRebuildMixin` wired into `WorldRenderer`.
- Upgraded `MinecraftDiagnosticsMixin` to hook `updateDisplay()` for exact per-frame telemetry and `GlStateDeduplicationMixin` to hook `deleteTextures(int)`.

## [0.3.0-alpha] - Phase 3: High-Impact Rendering Optimizations
### Added
- Implemented `GlStateCache` and `GlStateDeduplicationMixin` to eliminate redundant OpenGL `glBindTexture` and state transitions across frames.
- Implemented `FastFrustumCuller` providing zero-allocation 6-plane AABB frustum testing, squared-distance culling, and invisible armor stand filtering.
- Implemented `RenderScratchPool` thread-local direct buffers and matrix arrays to eliminate per-frame rendering allocations.
- Added `KineticRenderOptimizer` coordinating frame-start state and telemetry counters.

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
### Added
- Implemented zero-allocation frame-time ring buffer (`FrameTimeTracker`) tracking current FPS, rolling average FPS, 1% low FPS, frame duration (ms), and frame-time standard deviation (jitter).
- Implemented JVM memory, real-time allocation rate (MB/s), GC pause telemetry, and process CPU load tracker (`MemoryAndGcTracker`).
- Added thread-safe world rendering counters (`WorldRenderStats`), `BottleneckAnalyzer`, `DiagnosticReportGenerator`, and `PerformanceOverlayRenderer`.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
