package com.kinetic.optimizations.memory;

import net.minecraft.util.math.Direction;

/**
 * Caches Direction.values() once in static memory so hot block-meshing and lighting loops
 * never allocate 24-byte Direction[6] array clones on the heap.
 */
public final class DirectionCache {

    public static final Direction[] ALL = Direction.values();

    private DirectionCache() {
    }

    public static Direction[] values() {
        return ALL;
    }
}
