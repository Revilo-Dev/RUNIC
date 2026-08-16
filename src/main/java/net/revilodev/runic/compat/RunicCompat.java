package net.revilodev.runic.compat;

import net.neoforged.fml.ModList;
import net.revilodev.runic.stat.RuneStatType;

public final class RunicCompat {
    private static final String AURA = "aura";
    private static final String CODEX = "codex";

    private RunicCompat() {}

    public static boolean isAuraLoaded() {
        return ModList.get().isLoaded(AURA) || ModList.get().isLoaded(CODEX);
    }

    public static boolean isStatAvailable(RuneStatType stat) {
        return stat != RuneStatType.ABILITY_POWER || isAuraLoaded();
    }
}
