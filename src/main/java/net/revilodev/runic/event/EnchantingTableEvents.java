package net.revilodev.runic.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
// responds to enchanting table events
public final class EnchantingTableEvents {
    private static final int MAX_ETCHING_COST = 20;

    private EnchantingTableEvents() {}

    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (!event.getItem().is(ModItems.BLANK_ETCHING.get())) {
            // A zero offer is rejected by EnchantmentMenu before it can consume lapis or XP.
            event.setEnchantLevel(0);
            return;
        }

        event.setEnchantLevel(Math.min(event.getEnchantLevel(), MAX_ETCHING_COST));
    }
}
