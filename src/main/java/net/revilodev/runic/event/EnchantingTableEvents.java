package net.revilodev.runic.event;

import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

/** Restricts enchanting-table offers to RUNIC etchings and explicitly configured books. */
@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class EnchantingTableEvents {
    private static final int MAX_ETCHING_COST = 25;
    private static final int MIN_SECOND_TIER_COST = 15;

    private EnchantingTableEvents() {}

    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (event.getItem().is(ModItems.BLANK_ETCHING.get())) {
            int enchantmentCost = Math.min(event.getEnchantLevel(), MAX_ETCHING_COST);
            if (event.getEnchantRow() == 1) {
                enchantmentCost = Math.max(enchantmentCost, MIN_SECOND_TIER_COST);
            } else if (event.getEnchantRow() == 2) {
                enchantmentCost = MAX_ETCHING_COST;
            }
            event.setEnchantLevel(enchantmentCost);
            return;
        }

        if (event.getItem().is(Items.BOOK) && RunicConfig.hasEnchantableBookEnchantments()) {
            return;
        }

        event.setEnchantLevel(0);
    }
}
