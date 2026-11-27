# Kinetic Optimizations (v1.1.0 — Minecraft 1.8.9 OneClient / Ornithe Edition)

Kinetic Optimizations is a low-level Java 25 performance engineering, active bytecode culling, OpenGL state deduplication, chunk scheduling, and real-time frame-pacing telemetry mod for Minecraft Java Edition 1.8.9 running on OneClient (Ornithe Gen-2 / Fabric Loader + OSL).

## Core Subsystems
- Interactive In-Game Settings GUI (F7): Native 5-tab configuration menu (General, Render/GPU, Chunks, Entities/Particles, CPU & HUD) with instant live toggles, preset switching, diagnostic export, and one-click Reset to Defaults.
- Deep Bytecode Culling Pack: Active HEAD Mixin cancellation for distant entities, unnamed invisible marker ArmorStands, distant/off-frustum TileEntities (chests, signs, skulls, banners while preserving global Beacons), 0-vertex BufferUploader calls, and redundant RenderChunk.setDirty rebuilds.
- Rendering & GPU Engine: Tri-state OpenGL capability deduplication (Blend, DepthTest, AlphaTest, CullFace), multi-unit glBindTexture shadow caching, smart animated sprite PCIe upload throttling, and unfocused window FPS limiting (15 FPS when Alt+Tabbed).
- Chunk Engine: 16,384-slot generation-stamped 64-bit primitive hash table (6.18 ns/lookup, O(1) tick rollover, zero heap allocations) + frame-time-aware GPU upload budgeting.
- CPU & Memory Engine: 16 KB L1D-cache-resident trigonometric lookup table (10.5x-11.5x faster than Math.sin/cos on Intel Core i3-10100 + Java 25 JVMCI) and 64-bit register-packed BlockPos math.
- Diagnostics & Repeatable CSV Benchmarks: Real-time FPS, 1% Low FPS, Frame Time (ms), StdDev Jitter, Allocation Rate (MB/s), GC telemetry, and automatic bottleneck classification.

## In-Game Hotkeys
- F7: Open Interactive 5-Tab Kinetic Settings GUI.
- F8: Toggle real-time 5-line Kinetic Performance Telemetry Overlay.
- F9: Cycle Performance Presets (Balanced -> Performance -> Extreme -> Ultra).
- F10: Start a 60-second repeatable benchmark (exports to config/kinetic_ingame_benchmarks.csv and config/kinetic_diagnostics_report.txt).

## Installation & Building (Nobara / Fedora Linux)
    ./gradlew clean build

Compiled JAR output:
    build/libs/KineticOptimizations-1.8.9-OneClient-1.1.0.jar

Recommended OneClient Java 25 JVM Flags:
    -Xmx4G -Xms4G -XX:+UseZGC -XX:+AlwaysPreTouch
