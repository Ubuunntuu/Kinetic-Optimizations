# Kinetic Optimizations (v1.2.0 — Minecraft 1.8.9 OneClient / Ornithe Edition)

Kinetic Optimizations is a low-level Java 25 performance engineering, active bytecode culling, OpenGL state deduplication, lighting/chunk deduplication, and real-time frame-pacing telemetry mod for Minecraft Java Edition 1.8.9 running on OneClient (Ornithe Gen-2 / Fabric Loader + OSL).

## Complete v1.2.0 Optimization Architecture (14 Active Mixins)
- Interactive 5-Tab Settings GUI (F7): Instant live toggles for every rendering, chunk, lighting, entity, particle, CPU, memory, and HUD subsystem.
- Java 25 FFM & FMA3 Engine: 64-byte CPU cache-line-aligned off-heap Arena MemorySegments (RenderScratchPool) and unrolled hardware FMA3 6-plane frustum culling (FastFrustumCuller).
- Lighting & Chunk Engine: 32,768-slot generation-stamped 64-bit sky/block light deduplicator (WorldLightingMixin), 16,384-slot chunk section rebuild deduplicator (ChunkRebuildMixin), Direction.values() static array caching (BlockModelDirectionMixin), and adaptive GPU upload budgeting.
- Entity, TileEntity & Particle Engine: Active HEAD cancellation for distant entities, unnamed invisible marker ArmorStands, distant/hidden chests/signs/skulls/banners (preserving Beacons), off-range/excess particle spawns (ParticleManagerMixin), and crammed entity client push scans (EntityPushThrottleMixin).
- GPU, Font & Power Engine: Tri-state OpenGL capability & multi-unit texture bind deduplication, 2,048-slot TextRenderer string width cache (TextRendererCacheMixin), 0-vertex BufferUploader skip, smart animated sprite throttling, and unfocused window FPS limiting (15 FPS when Alt+Tabbed).

## In-Game Hotkeys
- F7: Open Interactive 5-Tab Kinetic Settings GUI.
- F8: Toggle real-time 5-line Kinetic Performance Telemetry Overlay.
- F9: Cycle Performance Presets (Balanced -> Performance -> Extreme -> Ultra).
- F10: Start a 60-second repeatable benchmark (exports to config/kinetic_ingame_benchmarks.csv and config/kinetic_diagnostics_report.txt).

## Building & Recommended Java 25 Flags (Nobara / Fedora Linux)
    ./gradlew clean build

Compiled JAR output:
    build/libs/KineticOptimizations-1.8.9-OneClient-1.2.0.jar

Recommended OneClient Java 25 JVM Arguments (Generational ZGC + JEP 519 Compact Object Headers):
    -Xmx4G -Xms4G -XX:+UseZGC -XX:+UseCompactObjectHeaders -XX:+AlwaysPreTouch
