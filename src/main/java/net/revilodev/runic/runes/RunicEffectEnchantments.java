package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashSet;
import java.util.Set;

// supports runic effect enchantments

// supports runic effect enchantments
public final class RunicEffectEnchantments {
    // built in effect whitelist
    private static final Set<ResourceLocation> BUILTIN_EFFECTS = Set.of(
            ResourceLocation.fromNamespaceAndPath("aether", "renewal"),

            ResourceLocation.fromNamespaceAndPath("combat_roll", "acrobat"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "longfooted"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "multi_roll"),

            ResourceLocation.fromNamespaceAndPath("create", "capacity"),
            ResourceLocation.fromNamespaceAndPath("create", "potato_recovery"),

            ResourceLocation.fromNamespaceAndPath("deeperdarker", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "discharge"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "sculk_smite"),

            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "discharge"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "ensnaring"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "lolths_curse"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "purification"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "voltaic_shot"),

            ResourceLocation.fromNamespaceAndPath("expanded_combat", "blocking"),
            ResourceLocation.fromNamespaceAndPath("expanded_combat", "ground_slam"),

            ResourceLocation.fromNamespaceAndPath("farmersdelight", "backstabbing"),

            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "mystical_enlightenment"),
            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("simplyswords", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "fire_react"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("supplementaries", "stasis"),

            ResourceLocation.fromNamespaceAndPath("twilightforest", "chill_aura"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "destruction"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "fire_react"),

            ResourceLocation.withDefaultNamespace("aqua_affinity"),
            ResourceLocation.withDefaultNamespace("depth_strider"),
            ResourceLocation.withDefaultNamespace("feather_falling"),

            ResourceLocation.withDefaultNamespace("binding_curse"),
            ResourceLocation.withDefaultNamespace("breach"),
            ResourceLocation.withDefaultNamespace("channeling"),
            ResourceLocation.withDefaultNamespace("density"),
            ResourceLocation.withDefaultNamespace("flame"),
            ResourceLocation.withDefaultNamespace("impaling"),
            ResourceLocation.withDefaultNamespace("infinity"),
            ResourceLocation.withDefaultNamespace("looting"),
            ResourceLocation.withDefaultNamespace("luck_of_the_sea"),
            ResourceLocation.withDefaultNamespace("multishot"),
            ResourceLocation.withDefaultNamespace("respiration"),
            ResourceLocation.withDefaultNamespace("riptide"),
            ResourceLocation.withDefaultNamespace("fortune"),
            ResourceLocation.withDefaultNamespace("frost_walker"),
            ResourceLocation.withDefaultNamespace("loyalty"),
            ResourceLocation.withDefaultNamespace("lure"),
            ResourceLocation.withDefaultNamespace("mending"),
            ResourceLocation.withDefaultNamespace("piercing"),
            ResourceLocation.withDefaultNamespace("punch"),
            ResourceLocation.withDefaultNamespace("silk_touch"),
            ResourceLocation.withDefaultNamespace("soul_speed"),
            ResourceLocation.withDefaultNamespace("swift_sneak"),
            ResourceLocation.withDefaultNamespace("thorns"),
            ResourceLocation.withDefaultNamespace("vanishing_curse"),
            ResourceLocation.withDefaultNamespace("wind_burst")
    );

    private static Set<ResourceLocation> loadedEffects = new HashSet<>(BUILTIN_EFFECTS);

    private RunicEffectEnchantments() {
    }

    public static Set<ResourceLocation> allowedEffectIds() {
        return Set.copyOf(loadedEffects);
    }

    public static boolean isEffectEnchantment(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .map(loadedEffects::contains)
                .orElse(false);
    }

    public static void replaceDatapackEffects(Set<ResourceLocation> additions, Set<ResourceLocation> removals) {
        // datapacks extend then prune the builtin list
        Set<ResourceLocation> next = new HashSet<>(BUILTIN_EFFECTS);
        next.addAll(additions);
        next.removeAll(removals);
        loadedEffects = next;
    }

    public static void importFromNetwork(Set<ResourceLocation> ids) {
        // client copy from server sync
        loadedEffects = new HashSet<>(ids);
    }
}
