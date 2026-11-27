package com.kinetic.optimizations.memory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Eliminates high-frequency temporary heap allocations in block-iteration,
 * neighbor lookup, and lighting hot paths using thread-local primitive coordinate cursors.
 */
public final class AllocationReducer {

    // Pre-computed 6-direction normal offsets: DOWN, UP, NORTH, SOUTH, WEST, EAST
    public static final int[] DIR_OFFSET_X = {0, 0, 0, 0, -1, 1};
    public static final int[] DIR_OFFSET_Y = {-1, 1, 0, 0, 0, 0};
    public static final int[] DIR_OFFSET_Z = {0, 0, -1, 1, 0, 0};
    public static final int DIRECTION_COUNT = 6;

    private static final ThreadLocal<CursorRing> CURSOR_RING =
        ThreadLocal.withInitial(CursorRing::new);

    private static final AtomicLong AVOIDED_ALLOCATIONS = new AtomicLong(0L);

    private AllocationReducer() {
    }

    public static MutablePosCursor acquireCursor(int x, int y, int z) {
        AVOIDED_ALLOCATIONS.incrementAndGet();
        return CURSOR_RING.get().next().set(x, y, z);
    }

    public static long getAvoidedAllocationsCount() {
        return AVOIDED_ALLOCATIONS.get();
    }

    public static void resetThreadLocalRing() {
        CURSOR_RING.remove();
    }

    public static final class MutablePosCursor {
        public int x;
        public int y;
        public int z;

        public MutablePosCursor set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public MutablePosCursor move(int dirIndex) {
            this.x += DIR_OFFSET_X[dirIndex];
            this.y += DIR_OFFSET_Y[dirIndex];
            this.z += DIR_OFFSET_Z[dirIndex];
            return this;
        }

        public long toPackedLong() {
            return ((long) (x & 0x3FFFFFF) << 38)
                 | ((long) (y & 0xFFF) << 26)
                 | ((long) (z & 0x3FFFFFF));
        }
    }

    private static final class CursorRing {
        private static final int RING_SIZE = 32;
        private static final int RING_MASK = RING_SIZE - 1;
        private final MutablePosCursor[] cursors = new MutablePosCursor[RING_SIZE];
        private int index = 0;

        CursorRing() {
            for (int i = 0; i < RING_SIZE; i++) {
                cursors[i] = new MutablePosCursor();
            }
        }

        MutablePosCursor next() {
            MutablePosCursor cursor = cursors[index];
            index = (index + 1) & RING_MASK;
            return cursor;
        }
    }
}
