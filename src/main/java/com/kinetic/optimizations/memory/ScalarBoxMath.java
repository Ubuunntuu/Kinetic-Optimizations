package com.kinetic.optimizations.memory;

/**
 * Pure primitive scalar AABB (AxisAlignedBB / Box) operations that eliminate 48-byte heap Box
 * allocations during offset intersection, expansion checks, and ray-slab queries.
 */
public final class ScalarBoxMath {

    private ScalarBoxMath() {
    }

    public static boolean intersects(
        double aMinX, double aMinY, double aMinZ, double aMaxX, double aMaxY, double aMaxZ,
        double bMinX, double bMinY, double bMinZ, double bMaxX, double bMaxY, double bMaxZ
    ) {
        return aMaxX > bMinX && aMinX < bMaxX
            && aMaxY > bMinY && aMinY < bMaxY
            && aMaxZ > bMinZ && aMinZ < bMaxZ;
    }

    public static boolean intersectsOffset(
        double aMinX, double aMinY, double aMinZ, double aMaxX, double aMaxY, double aMaxZ,
        double offX, double offY, double offZ,
        double bMinX, double bMinY, double bMinZ, double bMaxX, double bMaxY, double bMaxZ
    ) {
        return (aMaxX + offX) > bMinX && (aMinX + offX) < bMaxX
            && (aMaxY + offY) > bMinY && (aMinY + offY) < bMaxY
            && (aMaxZ + offZ) > bMinZ && (aMinZ + offZ) < bMaxZ;
    }

    public static boolean containsPointExpanded(
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ,
        double expand,
        double px, double py, double pz
    ) {
        return px >= (minX - expand) && px <= (maxX + expand)
            && py >= (minY - expand) && py <= (maxY + expand)
            && pz >= (minZ - expand) && pz <= (maxZ + expand);
    }
}
