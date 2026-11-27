# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.3.0-alpha] - Phase 3: High-Impact Rendering Optimizations
### Added
- Implemented `GlStateCache` and `GlStateDeduplicationMixin` to eliminate redundant OpenGL `glBindTexture` and state transitions across frames.
- Implemented `FastFrustumCuller` providing zero-allocation 6-plane AABB frustum testing, squared-distance culling, and invisible armor stand filtering.
- Implemented `RenderScratchPool` thread-local direct buffers and matrix arrays to eliminate per-frame rendering allocations.
- Added `KineticRenderOptimizer` coordinating frame-start state and telemetry counters (`Saved GL Calls/s` and `Culled Objects/s`).

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
### Added
- Implemented zero-allocation frame-time ring buffer (`FrameTimeTracker`) tracking current FPS, rolling average FPS, 1% low FPS, frame duration (ms), and frame-time standard deviation (jitter).
- Implemented JVM memory, real-time allocation rate (MB/s), GC pause telemetry, and process CPU load tracker (`MemoryAndGcTracker`).
- Added thread-safe world rendering counters (`WorldRenderStats`) for entities, visible chunks, chunk rebuilds/sec, and particles.
- Added automatic bottleneck detection engine (`BottleneckAnalyzer` & `BottleneckType`).
- Added diagnostic snapshot exporter (`DiagnosticReportGenerator`) and 4 Hz low-overhead HUD overlay state builder (`PerformanceOverlayRenderer`).

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
- Established modular package hierarchy under com.kinetic.optimizations.
- Implemented KineticPreLaunch and KineticMixinPlugin (IMixinConfigPlugin) with Safe Mode and compatibility gating.
- Implemented JSON-persisted configuration engine (KineticConfig) supporting 11 categories and 5 presets.
