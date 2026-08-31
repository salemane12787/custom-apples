package com.customapples.progression;

public enum ProgressionTier {
    APPLE(0, "Apple"),
    APPLE_AXE(1, "Apple Axe"),
    APPLE_BOOTS(2, "Apple Boots"),
    GOLDEN_APPLE(3, "Golden Apple Tier"),
    LAPIS_APPLE(4, "Lapis Apple Tier"),
    END_TIER(5, "End Tier"),
    DRAGON_APPLE(6, "Dragon Apple");

    private final int index;
    private final String displayName;

    ProgressionTier(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProgressionTier fromIndex(int index) {
        for (ProgressionTier tier : values()) {
            if (tier.index == index) {
                return tier;
            }
        }
        return APPLE;
    }
}
