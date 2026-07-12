package net.revilodev.runic.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.revilodev.runic.RunicMod;

import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class VillagerTradeBlacklistEvents {
    private VillagerTradeBlacklistEvents() {
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) {
            return;
        }
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        trades.int2ObjectEntrySet().forEach(entry ->
                entry.getValue().removeIf(listing -> listing instanceof VillagerTrades.EnchantBookForEmeralds)
        );
    }
}
