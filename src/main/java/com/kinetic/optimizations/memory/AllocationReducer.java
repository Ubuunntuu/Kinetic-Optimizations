package com.kinetic.optimizations.memory;

public final class AllocationReducer {

    public static final int[] DIR_OFFSET_X = {0, 0, 0, 0, -1, 1};
    public static final int[] DIR_OFFSET_Y = {-1, 1, 0, 0, 0, 0};
    public static final int[] DIR_OFFSET_Z = {0, 0, -1, 1, 0, 0};
    public static final int DIRECTION_COUNT = 6;

    private static final int X_BITS = 26;
    private static final int Z_BITS = 26;
    private static final int Y_BITS = 12;
    private static final int Y_SHIFT = Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + Y_BITS;
    private static final long X_MASK = (1L << X_BITS) - 1L;
    private static final long Y_MASK = (1L << Y_BITS) - 1L;
    private static final long Z_MASK = (1L << Z_BITS) - 1L;

    private static final ThreadLocal<CursorRing> CURSOR_RING =
        ThreadLocal.withInitial(CursorRing::new);

    private AllocationReducer() {
    }

    public static long packBlockPos(int x, int y, int z) {
        return (((long) x & X_MASK) << X_SHIFT)
             | (((long) y & Y_MASK) << Y_SHIFT)
             | ((long) z & Z_MASK);
    }

    public static int unpackX(long packed) {
        return (int) (packed << (64 - X_SHIFT - X_BITS) >> (64 - X_BITS));
    }

    public static int unpackY(long packed) {
        return (int) (packed << (64 - Y_SHIFT - Y_BITS) >> (64 - Y_BITS));
    }

    public static int unpackZ(long packed) {
        return (int) (packed << (64 - Z_BITS) >> (64 - Z_BITS));
    }

    public static long offsetPacked(long packed, int dirIndex) {
        int nx = unpackX(packed) + DIR_OFFSET_X[dirIndex];
        int ny = unpackY(packed) + DIR_OFFSET_Y[dirIndex];
        int nz = unpackZ(packed) + DIR_OFFSET_Z[dirIndex];
        return packBlockPos(nx, ny, nz);
    }

    public static CursorRing acquireRing() {
        return CURSOR_RING.get();
    }

    public static MutablePosCursor acquireCursor(int x, int y, int z) {
        return CURSOR_RING.get().next(x, y, z);
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
            return packBlockPos(x, y, z);
        }
    }

    public static final class CursorRing {
        private static final int RING_SIZE = 32;
        private static final int RING_MASK = RING_SIZE - 1;
        private final MutablePosCursor[] cursors = new MutablePosCursor[RING_SIZE];
        private int index = 0;

        CursorRing() {
            for (int i = 0; i < RING_SIZE; i++) {
                cursors[i] = new MutablePosCursor();
            }
        }

        public MutablePosCursor next(int x, int y, int z) {
            MutablePosCursor cursor = cursors[index];
            index = (index + 1) & RING_MASK;
            cursor.x = x;
            cursor.y = y;
            cursor.z = z;
            return cursor;
        }
    }
}
