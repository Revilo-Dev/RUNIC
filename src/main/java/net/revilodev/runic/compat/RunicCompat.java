package net.revilodev.runic.compat;

import net.neoforged.fml.ModList;
import net.revilodev.runic.stat.RuneStatType;

// supports runic compat

// supports runic compat
public final class RunicCompat {
    private RunicCompat() {}

    public static boolean isAuraLoaded() {
        return ModList.get().isLoaded("aura") || ModList.get().isLoaded("codex");
    }

    public static boolean isStatAvailable(RuneStatType type) {
        return type != RuneStatType.ABILITY_POWER || isAuraLoaded();
    }
}
