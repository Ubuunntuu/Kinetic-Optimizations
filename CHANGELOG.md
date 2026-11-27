# Changelog

All notable changes to Kinetic Optimizations are documented in this file.

## [0.1.0-alpha] - Phase 1: Foundation (OneClient 1.8.9 / Ornithe / Java 25)
### Added
- Configured Fabric Loom + Ornithe Ploceus (Gen-2 Feather mappings) build pipeline for Minecraft 1.8.9 targeting Java 25.
- Established modular package hierarchy under com.kinetic.optimizations.
- Implemented KineticPreLaunch and KineticMixinPlugin (IMixinConfigPlugin) with Safe Mode and compatibility gating.
- Implemented JSON-persisted configuration engine (KineticConfig) supporting 11 categories and 5 presets (BALANCED, PERFORMANCE, EXTREME, ULTRA, CUSTOM).
- Added structured diagnostic logger (KineticLogger) with Java 25 runtime telemetry.
