# Kinetic Optimizations (v1.0.0 — Minecraft 1.8.9 OneClient / Ornithe Edition)

Kinetic Optimizations is a low-level Java 25 performance engineering, OpenGL state deduplication, chunk scheduling, entity/particle culling, and real-time frame-pacing telemetry mod for Minecraft Java Edition 1.8.9 running on OneClient (Ornithe Gen-2 / Fabric Loader + OSL).

## Core Subsystems
- Rendering & GPU Engine: Tri-state OpenGL capability deduplication (Blend, DepthTest, AlphaTest, CullFace), multi-unit glBindTexture shadow caching, 0-vertex BufferUploader short-circuiting, and 6-plane AABB frustum culling.
- Chunk Engine: 16,384-slot generation-stamped 64-bit primitive hash table (O(1) tick rollover, zero heap allocations) eliminating duplicate sub-chunk rebuilds within the same tick, plus frame-time-aware GPU upload budgeting.
- Entity, BlockEntity & Particle Engine: Squared-distance entity/tile-entity culling, invisible marker armor stand filtering, and particle count/distance caps.
- CPU & Memory Engine: 16 KB L1D-cache-resident trigonometric lookup table (8.8x+ faster than Math.sin/cos on Intel Core i3-10100 + Java 25 JVMCI) and 64-bit register-packed BlockPos math.
- Diagnostics & Repeatable CSV Benchmarks: Real-time FPS, 1% Low FPS, Frame Time (ms), StdDev Jitter, Allocation Rate (MB/s), GC telemetry, and automatic bottleneck classification.
- Mod Compatibility & Safe Mode: Detects OptiFine, Sodium, Celeritas, VeloxCaelo, Iris, BetterFPS, Patcher, FoamFix, VanillaFix, and OneConfig, surgically yielding only overlapping mixins. Emergency JVM Safe Mode flag: -Dkinetic.safemode=true

## In-Game Hotkeys
- F8: Toggle real-time 5-line Kinetic Performance Telemetry Overlay.
- F9: Cycle Performance Presets (Balanced -> Performance -> Extreme -> Ultra).
- F10: Start a 60-second repeatable benchmark (exports to config/kinetic_ingame_benchmarks.csv and config/kinetic_diagnostics_report.txt).

## Installation & Building (Nobara / Fedora Linux)
    ./gradlew clean build

Compiled JAR output:
    build/libs/KineticOptimizations-1.8.9-OneClient-1.0.0.jar

Recommended OneClient Java 25 JVM Flags:
    -Xmx4G -Xms4G -XX:+UseZGC -XX:+AlwaysPreTouch
