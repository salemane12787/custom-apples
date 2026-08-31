package com.customapples.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientEntityScaleCache {
    private static final Map<Integer, Float> SCALES = new ConcurrentHashMap<>();

    private ClientEntityScaleCache() {}

    public static void set(int entityId, float scale) {
        if (scale >= 1.0f) {
            SCALES.remove(entityId);
        } else {
            SCALES.put(entityId, scale);
        }
    }

    public static float get(int entityId, float defaultScale) {
        return SCALES.getOrDefault(entityId, defaultScale);
    }

    public static boolean has(int entityId) {
        return SCALES.containsKey(entityId);
    }

    public static void remove(int entityId) {
        SCALES.remove(entityId);
    }
}
