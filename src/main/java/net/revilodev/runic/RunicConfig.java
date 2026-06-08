package net.revilodev.runic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.revilodev.runic.event.EnchantBlacklist;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class RunicConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue DISABLE_ALL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_RAW;
    private static final ModConfigSpec.BooleanValue DISABLE_RUNE_SLOTS;
    private static final ModConfigSpec.BooleanValue DISABLE_RUNIC_LOOT;
    private static final ModConfigSpec.BooleanValue DISABLE_ETCHING_CRAFTING;
    private static final ModConfigSpec.BooleanValue DISABLE_STAT_CAPS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_STATS_RAW;

    private static final AtomicBoolean DISABLE_ALL_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_RUNE_SLOTS_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_RUNIC_LOOT_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_ETCHING_CRAFTING_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_STAT_CAPS_CACHE = new AtomicBoolean(false);
    private static final AtomicReference<Set<ResourceLocation>> BLACKLIST_CACHE =
            new AtomicReference<>(Set.of());
    private static final AtomicReference<Set<String>> DISABLED_STATS_CACHE =
            new AtomicReference<>(Set.of());

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DISABLE_ALL = builder
                .comment("When true, all enchantments are treated as blacklisted")
                .define("enchant_blacklist.disable_all", false);

        BLACKLIST_RAW = builder
                .comment("Enchantments disabled entirely")
                .defineList(
                        "enchant_blacklist.blacklisted",
                        List.of(),
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                );

        DISABLE_RUNE_SLOTS = builder
                .comment("When true, rune slots are ignored and no longer limit applying runes or etchings")
                .define("mechanics.disable_rune_slots", false);

        DISABLE_RUNIC_LOOT = builder
                .comment("When true, RUNIC loot injection and enchanted-book stripping are disabled")
                .define("loot.disable_runic_loot", false);

        DISABLE_ETCHING_CRAFTING = builder
                .comment("When true, the etching table cannot craft etchings or inscriptions")
                .define("crafting.disable_etching_crafting", false);

        DISABLE_STAT_CAPS = builder
                .comment("When true, stat rune application is no longer clamped by stat caps")
                .define("mechanics.disable_stat_caps", false);

        DISABLED_STATS_RAW = builder
                .comment("Disabled runic stat ids")
                .defineList(
                        "enhancement_blacklist.stats",
                        List.of(),
                        o -> o instanceof String s && !s.isBlank()
                );

        SPEC = builder.build();
    }

    private RunicConfig() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RunicConfig::onConfigLoading);
        modBus.addListener(RunicConfig::onConfigReloading);
    }

    public static Set<ResourceLocation> blacklistedEnchantments() {
        return BLACKLIST_CACHE.get();
    }

    public static boolean disableAllEnchantments() {
        return DISABLE_ALL_CACHE.get();
    }

    public static boolean disableRuneSlots() {
        return DISABLE_RUNE_SLOTS_CACHE.get();
    }

    public static boolean disableRunicLoot() {
        return DISABLE_RUNIC_LOOT_CACHE.get();
    }

    public static boolean disableEtchingCrafting() {
        return DISABLE_ETCHING_CRAFTING_CACHE.get();
    }

    public static boolean disableStatCaps() {
        return DISABLE_STAT_CAPS_CACHE.get();
    }

    public static Set<String> disabledStats() {
        return DISABLED_STATS_CACHE.get();
    }

    public static boolean isStatDisabled(String statId) {
        return statId != null && DISABLED_STATS_CACHE.get().contains(statId);
    }

    public static boolean disableConfigByName(String name) {
        if (name == null || name.isBlank()) return false;
        String key = name.trim().toLowerCase();
        switch (key) {
            case "rune_slots" -> DISABLE_RUNE_SLOTS.set(true);
            case "runic_loot" -> DISABLE_RUNIC_LOOT.set(true);
            case "etching_crafting" -> DISABLE_ETCHING_CRAFTING.set(true);
            case "stat_caps" -> DISABLE_STAT_CAPS.set(true);
            default -> {
                if (net.revilodev.runic.stat.RuneStatType.byId(key) != null) {
                    java.util.List<String> next = new java.util.ArrayList<>(DISABLED_STATS_RAW.get());
                    if (!next.contains(key)) {
                        next.add(key);
                        DISABLED_STATS_RAW.set(next);
                    }
                } else {
                    ResourceLocation rl = key.contains(":")
                            ? ResourceLocation.tryParse(key)
                            : ResourceLocation.withDefaultNamespace(key);
                    if (rl == null) return false;
                    java.util.List<String> next = new java.util.ArrayList<>(BLACKLIST_RAW.get());
                    String id = rl.toString();
                    if (!next.contains(id)) {
                        next.add(id);
                        BLACKLIST_RAW.set(next);
                    }
                }
            }
        }
        rebuildCache();
        return true;
    }

    private static void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void rebuildCache() {
        Set<ResourceLocation> parsed = BLACKLIST_RAW.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> disabledStats = DISABLED_STATS_RAW.get().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        boolean disableAll = DISABLE_ALL.get();
        boolean disableRuneSlots = DISABLE_RUNE_SLOTS.get();
        boolean disableRunicLoot = DISABLE_RUNIC_LOOT.get();
        boolean disableEtchingCrafting = DISABLE_ETCHING_CRAFTING.get();
        boolean disableStatCaps = DISABLE_STAT_CAPS.get();
        BLACKLIST_CACHE.set(parsed);
        DISABLED_STATS_CACHE.set(disabledStats);
        DISABLE_ALL_CACHE.set(disableAll);
        DISABLE_RUNE_SLOTS_CACHE.set(disableRuneSlots);
        DISABLE_RUNIC_LOOT_CACHE.set(disableRunicLoot);
        DISABLE_ETCHING_CRAFTING_CACHE.set(disableEtchingCrafting);
        DISABLE_STAT_CAPS_CACHE.set(disableStatCaps);

        EnchantBlacklist.setConfigDisabled(parsed);
        EnchantBlacklist.setConfigDisabledStats(disabledStats);
        EnchantBlacklist.setDisableAll(disableAll);
    }
}
