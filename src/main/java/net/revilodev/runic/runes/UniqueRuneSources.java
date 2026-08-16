package net.revilodev.runic.runes;

import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.stat.RuneStatType;

import java.util.Set;

public final class UniqueRuneSources {
    private UniqueRuneSources() {}

    public static final Set<RuneStatType> UNIQUE_ETCHING_STATS = Set.of(
            RuneStatType.AEGIS,
            RuneStatType.FANGS,
            RuneStatType.HEALTH,
            RuneStatType.LEECHING_CHANCE,
            RuneStatType.STUN_CHANCE,
            RuneStatType.WITHERING_CHANCE
    );

    public static final Set<ResourceLocation> UNIQUE_ETCHING_EFFECTS = Set.of(
            minecraft("binding_curse"),
            minecraft("breach"),
            minecraft("density"),
            minecraft("multishot"),
            minecraft("soul_speed"),
            minecraft("swift_sneak"),
            minecraft("vanishing_curse"),
            minecraft("wind_burst")
    );

    public static final Set<RuneStatType> SOURCE_LOCKED_RUNE_STATS = Set.of(
            RuneStatType.BLAST_RESISTANCE,
            RuneStatType.FANGS,
            RuneStatType.FIRE_RESISTANCE,
            RuneStatType.HEALTH,
            RuneStatType.LEECHING_CHANCE,
            RuneStatType.NETHER_DAMAGE,
            RuneStatType.STUN_CHANCE,
            RuneStatType.WITHERING_CHANCE
    );

    public static final Set<ResourceLocation> SOURCE_LOCKED_RUNE_EFFECTS = Set.of(
            minecraft("binding_curse"),
            minecraft("breach"),
            minecraft("density"),
            minecraft("multishot"),
            minecraft("soul_speed"),
            minecraft("swift_sneak"),
            minecraft("vanishing_curse"),
            minecraft("wind_burst")
    );

    public static boolean isUniqueEtchingStat(RuneStatType type) {
        return UNIQUE_ETCHING_STATS.contains(type);
    }

    public static boolean isUniqueEtchingEffect(ResourceLocation id) {
        return UNIQUE_ETCHING_EFFECTS.contains(id);
    }

    public static boolean isSourceLockedRuneStat(RuneStatType type) {
        return SOURCE_LOCKED_RUNE_STATS.contains(type);
    }

    public static boolean isSourceLockedRuneEffect(ResourceLocation id) {
        return SOURCE_LOCKED_RUNE_EFFECTS.contains(id);
    }

    public static ResourceLocation minecraft(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
