# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.2.0-alpha] - Phase 2: Performance Diagnostics & Bottleneck Analyzer
### Added
- Implemented zero-allocation frame-time ring buffer (`FrameTimeTracker`) tracking current FPS, rolling average FPS, 1% low FPS, frame duration (ms), and frame-time standard deviation (jitter).
- Implemented JVM memory, real-time allocation rate (MB/s), GC pause telemetry, and process CPU load tracker (`MemoryAndGcTracker`).
- Added thread-safe world rendering counters (`WorldRenderStats`) for entities, visible chunks, chunk rebuilds/sec, and particles.
- Added automatic bottleneck detection engine (`BottleneckAnalyzer` & `BottleneckType`) identifying memory pressure, GC churn, chunk rebuild spikes, entity/particle overload, CPU main-thread variance, and GPU/driver limits.
- Added diagnostic snapshot exporter (`DiagnosticReportGenerator`) and 4 Hz low-overhead HUD overlay state builder (`PerformanceOverlayRenderer`).
- Wired `MinecraftDiagnosticsMixin` into the client loop.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom 1.18.4 + Ornithe Ploceus 1.18.1 (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
- Established modular package hierarchy under com.kinetic.optimizations.
- Implemented KineticPreLaunch and KineticMixinPlugin (IMixinConfigPlugin) with Safe Mode and compatibility gating.
- Implemented JSON-persisted configuration engine (KineticConfig) supporting 11 categories and 5 presets (BALANCED, PERFORMANCE, EXTREME, ULTRA, CUSTOM).
- Added structured diagnostic logger (KineticLogger) with Java 25 runtime telemetry.
