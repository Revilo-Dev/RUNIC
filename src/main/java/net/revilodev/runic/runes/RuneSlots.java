package net.revilodev.runic.runes;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStats;
import net.revilodev.runic.registry.ModDataComponents;

import static net.revilodev.runic.registry.ModDataComponents.DATA_COMPONENT_TYPES;

public final class RuneSlots {
    public static boolean enabled() {
        return !RunicConfig.disableRuneSlots();
    }

    public static int capacity(ItemStack stack) {
        if (!enabled()) return 0;
        Integer stored = stack.get(ModDataComponents.RUNE_SLOTS_CAPACITY.get());
        if (stored != null) return Math.max(0, stored);
        return RuneSlotCapacityData.capacity(stack);
    }

    public static int used(ItemStack stack) {
        if (!enabled()) return 0;
        Integer v = stack.get(ModDataComponents.RUNE_SLOTS_USED.get());
        return v == null ? 0 : Math.max(0, v);
    }

    public static int countAppliedEnhancements(ItemStack stack) {
        int total = 0;

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            total += stats.view().size();
        }

        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!enchants.isEmpty()) {
            for (var entry : enchants.entrySet()) {
                if (RuneItem.isEffectEnchantment(entry.getKey()) && entry.getIntValue() > 0) {
                    total++;
                }
            }
        }

        total += RunicItemData.getMythicRunes(stack).size();

        return total;
    }

    public static void syncUsedToContents(ItemStack stack) {
        if (!enabled()) {
            if (stack.has(ModDataComponents.RUNE_SLOTS_USED.get())) {
                stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), 0);
            }
            return;
        }
        int derived = Math.min(capacity(stack), countAppliedEnhancements(stack));
        if (used(stack) != derived) {
            stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), derived);
        }
    }

    public static int remaining(ItemStack stack) {
        if (!enabled()) return Integer.MAX_VALUE;
        int cap = capacity(stack);
        return Math.max(0, cap - used(stack));
    }


    public static boolean tryConsumeSlot(ItemStack stack) {
        if (!enabled()) return true;
        int cap = capacity(stack);
        if (cap <= 0) return false;
        int u = used(stack);
        if (u >= cap) return false;
        stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), u + 1);
        return true;
    }

    public static void refundOne(ItemStack stack) {
        if (!enabled()) return;
        int u = used(stack);
        if (u > 0) stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), u - 1);
    }

    public static int expansionsUsed(ItemStack stack) {
        Integer v = stack.get(ModDataComponents.RUNE_EXPANSIONS_USED.get());
        return v == null ? 0 : v;
    }

    public static void incrementExpansion(ItemStack stack) {
        int used = expansionsUsed(stack);
        stack.set(ModDataComponents.RUNE_EXPANSIONS_USED.get(), used + 1);
    }

    public static void addOneSlot(ItemStack stack) {
        if (!enabled()) return;
        int cap = capacity(stack);
        stack.set(ModDataComponents.RUNE_SLOTS_CAPACITY.get(), cap + 1);
    }


    public static void removeOneSlot(ItemStack stack) {
        if (!enabled()) return;
        int cap = capacity(stack);
        if (cap <= 0) return;
        int newCap = Math.max(0, cap - 1);
        stack.set(ModDataComponents.RUNE_SLOTS_CAPACITY.get(), newCap);
        int used = used(stack);
        if (used > newCap) {
            stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), newCap);
        }
    }

    public static Component bar(ItemStack stack) {
        if (!enabled()) return Component.literal("Rune slots disabled").withStyle(ChatFormatting.DARK_GRAY);
        int cap = capacity(stack);
        int u = used(stack);
        if (cap <= 0) return Component.literal("No rune slots").withStyle(ChatFormatting.DARK_GRAY);

        int rem = Math.max(0, cap - u);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < u; i++) sb.append('⬤');
        for (int i = 0; i < rem; i++) sb.append('◯');
        return Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA);
    }

    private RuneSlots() {}
}
