# Kinetic Optimizations

An extreme performance engineering, rendering, memory, and frame-pacing optimization mod for Minecraft 1.8.9 running on OneClient (Ornithe Loader + Java 25).

## Architecture Highlights
- Target Loader: OneClient Default 1.8.9 Loader (OrnitheMC Gen-2 / Fabric Loader + OSL)
- Target JVM: Java 25 (--release 25, Generational ZGC / modern JIT vectorization)
- Bytecode Engine: SpongePowered Mixin + IMixinConfigPlugin with automatic conflict detection and Safe Mode circuit breaker

## Building on Nobara Linux
Run `./gradlew build` inside `/home/admin/projects/minecraft/mods/kinetic optimization/`.
Compiled JAR artifacts are output to `build/libs/`.
