package net.revilodev.runic.event;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public final class EnchantBlacklist {
    private EnchantBlacklist() {}

    /* ===============================
       HARD (PERMANENT) DISABLED
       =============================== */
    private static final Set<ResourceLocation> HARD_DISABLED = Set.of(
            ResourceLocation.parse("minecraft:bane_of_arthropods"),
            ResourceLocation.parse("minecraft:efficiency"),
            ResourceLocation.parse("minecraft:unbreaking"),
            ResourceLocation.parse("minecraft:sharpness"),
            ResourceLocation.parse("minecraft:smite"),
            ResourceLocation.parse("minecraft:protection"),
            ResourceLocation.parse("minecraft:fire_protection"),
            ResourceLocation.parse("minecraft:blast_protection"),
            ResourceLocation.parse("minecraft:projectile_protection"),
            ResourceLocation.parse("minecraft:sweeping_edge"),
            ResourceLocation.parse("minecraft:respiration"),
            ResourceLocation.parse("minecraft:power")
    );

    /* ===============================
       CONFIG DISABLED (RUNTIME)
       =============================== */
    private static final Set<ResourceLocation> CONFIG_DISABLED = new HashSet<>();
    private static final Set<String> CONFIG_DISABLED_STATS = new HashSet<>();
    private static volatile boolean DISABLE_ALL = false;

    public static void setConfigDisabled(Set<ResourceLocation> ids) {
        CONFIG_DISABLED.clear();
        CONFIG_DISABLED.addAll(ids);
    }

    public static void setDisableAll(boolean disableAll) {
        DISABLE_ALL = disableAll;
    }

    public static void setConfigDisabledStats(Set<String> ids) {
        CONFIG_DISABLED_STATS.clear();
        CONFIG_DISABLED_STATS.addAll(ids);
    }

    /* ===============================
       CORE CHECKS
       =============================== */
    public static boolean isBlacklisted(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .map(EnchantBlacklist::isBlacklisted)
                .orElse(false);
    }

    public static boolean isBlacklisted(ResourceLocation id) {
        return DISABLE_ALL || HARD_DISABLED.contains(id) || CONFIG_DISABLED.contains(id);
    }

    public static boolean isStatBlacklisted(RuneStatType type) {
        return type != null && isStatBlacklisted(type.id());
    }

    public static boolean isStatBlacklisted(String statId) {
        return statId != null && CONFIG_DISABLED_STATS.contains(statId);
    }

    /* ===============================
       ETCHING TABLE SUPPORT
       =============================== */
    public static boolean isRecipeBlacklisted(EtchingTableRecipe recipe) {
        if (recipe.stat().map(EnchantBlacklist::isStatBlacklisted).orElse(false)) {
            return true;
        }
        return recipe.effect()
                .map(EnchantBlacklist::isBlacklisted)
                .orElse(false);
    }

    /* ===============================
       STRIPPING
       =============================== */
    public static boolean strip(ItemStack stack) {
        boolean changed = false;

        ItemEnchantments cur = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!cur.isEmpty()) {
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(cur);
            cur.entrySet().forEach(e -> {
                if (isBlacklisted(e.getKey())) mut.set(e.getKey(), 0);
            });
            ItemEnchantments cleaned = mut.toImmutable();
            if (!cleaned.equals(cur)) {
                stack.set(DataComponents.ENCHANTMENTS, cleaned);
                changed = true;
            }
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(stored);
            stored.entrySet().forEach(e -> {
                if (isBlacklisted(e.getKey())) mut.set(e.getKey(), 0);
            });
            ItemEnchantments cleaned = mut.toImmutable();
            if (!cleaned.equals(stored)) {
                stack.set(DataComponents.STORED_ENCHANTMENTS, cleaned);
                changed = true;
            }
        }

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(stats.view());
            boolean removedAny = false;
            for (RuneStatType type : RuneStatType.values()) {
                if (isStatBlacklisted(type) && map.remove(type) != null) {
                    removedAny = true;
                }
            }
            if (removedAny) {
                RuneStats.set(stack, map.isEmpty() ? RuneStats.empty() : new RuneStats(map));
                changed = true;
            }
        }

        RuneStats remainingStats = RuneStats.get(stack);
        ItemEnchantments remainingEnchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if ((remainingStats == null || remainingStats.isEmpty()) && remainingEnchants.isEmpty()) {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        return changed;
    }
}
