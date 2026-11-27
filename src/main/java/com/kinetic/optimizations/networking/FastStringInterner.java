package com.kinetic.optimizations.networking;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-speed bounded 2,048-slot string canonicalizer for network packets, scoreboard lines,
 * team prefixes/suffixes, and tab-list strings.
 * Eliminates duplicate String heap allocations and turns String.equals() into a 1-cycle == hit!
 */
public final class FastStringInterner {

    private static final FastStringInterner INSTANCE = new FastStringInterner();
    private static final int TABLE_SIZE = 2048;
    private static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final int MAX_STRING_LEN = 64;

    private final String[] table = new String[TABLE_SIZE];
    private final AtomicInteger internHits = new AtomicInteger(0);

    private FastStringInterner() {
    }

    public static FastStringInterner getInstance() {
        return INSTANCE;
    }

    public String canonicalize(String input) {
        if (!KineticConfig.modEnabled || !KineticConfig.internScoreboardAndPacketStrings || input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return "";
        }
        if (len > MAX_STRING_LEN) {
            return input;
        }

        int h = input.hashCode();
        int slot = ((h ^ (h >>> 16)) * 0x85ebca6b) & TABLE_MASK;
        String cached = table[slot];
        if (cached != null && cached.equals(input)) {
            internHits.lazySet(internHits.get() + 1);
            return cached;
        }
        table[slot] = input;
        return input;
    }

    public void clear() {
        Arrays.fill(table, null);
    }

    public int getInternHits() {
        return internHits.get();
    }
}
