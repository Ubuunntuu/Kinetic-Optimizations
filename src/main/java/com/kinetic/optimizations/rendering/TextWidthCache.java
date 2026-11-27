package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock-free 2,048-slot direct-mapped/2-way associative cache for TextRenderer.getWidth(String).
 * Eliminates repeated character-by-character formatting code parsing on scoreboards, tab lists, and nametags.
 */
public final class TextWidthCache {

    private static final TextWidthCache INSTANCE = new TextWidthCache();
    private static final int CACHE_SIZE = 2048;
    private static final int CACHE_MASK = CACHE_SIZE - 1;
    private static final int MAX_CACHEABLE_LENGTH = 96;

    private final String[] keys = new String[CACHE_SIZE];
    private final int[] widths = new int[CACHE_SIZE];

    private final AtomicInteger cacheHitsAccumulator = new AtomicInteger(0);
    private volatile int cacheHitsPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private TextWidthCache() {
        Arrays.fill(widths, -1);
    }

    public static TextWidthCache getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            cacheHitsPerSecond = cacheHitsAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    public int getCachedWidth(String text) {
        if (!KineticConfig.modEnabled || !KineticConfig.cacheTextRendererWidths || text == null) {
            return -1;
        }
        int len = text.length();
        if (len == 0) {
            return 0;
        }
        if (len > MAX_CACHEABLE_LENGTH) {
            return -1;
        }

        int slot = (mixHash(text.hashCode())) & CACHE_MASK;
        String cachedKey = keys[slot];
        if (cachedKey != null && cachedKey.equals(text)) {
            cacheHitsAccumulator.lazySet(cacheHitsAccumulator.get() + 1);
            return widths[slot];
        }
        return -1;
    }

    public void putCachedWidth(String text, int width) {
        if (!KineticConfig.modEnabled || !KineticConfig.cacheTextRendererWidths || text == null) {
            return;
        }
        if (text.isEmpty() || text.length() > MAX_CACHEABLE_LENGTH || width < 0) {
            return;
        }
        int slot = (mixHash(text.hashCode())) & CACHE_MASK;
        keys[slot] = text;
        widths[slot] = width;
    }

    public void invalidateAll() {
        Arrays.fill(keys, null);
        Arrays.fill(widths, -1);
    }

    private static int mixHash(int h) {
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        return h;
    }

    public int getCacheHitsPerSecond() {
        return cacheHitsPerSecond;
    }
}
