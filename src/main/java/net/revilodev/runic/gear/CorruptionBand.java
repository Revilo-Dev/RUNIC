package net.revilodev.runic.gear;

import net.minecraft.network.chat.Component;

public enum CorruptionBand {
    STABLE("stable", 0, 24),
    TAINTED("tainted", 25, 49),
    CORRUPTED("corrupted", 50, 74),
    CRITICAL("critical", 75, 99),
    EXHAUSTED("exhausted", 100, Integer.MAX_VALUE);

    private final String id;
    private final int min;
    private final int max;

    CorruptionBand(String id, int min, int max) {
        this.id = id;
        this.min = min;
        this.max = max;
    }

    public String id() {
        return id;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public Component displayName() {
        return Component.translatable("tooltip.runic.corruption_band." + id);
    }

    public static CorruptionBand fromPercent(int percent) {
        for (CorruptionBand band : values()) {
            if (percent >= band.min && percent <= band.max) {
                return band;
            }
        }
        return EXHAUSTED;
    }
}
