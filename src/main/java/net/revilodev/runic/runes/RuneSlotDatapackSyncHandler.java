package net.revilodev.runic.runes;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.revilodev.runic.network.RunicEffectDataSync;
import net.revilodev.runic.network.RuneSlotDataSync;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public class RuneSlotDatapackSyncHandler {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent e) {
        RuneSlotDataSync payload = new RuneSlotDataSync(
                RuneSlotCapacityData.exportItemIdMap(),
                RuneSlotCapacityData.exportDefaults(),
                RuneSlotCapacityData.exportTagCapacityMap(),
                RuneSlotCapacityData.exportItemTypeMap(),
                RuneSlotCapacityData.exportTagTypeMap()
        );

        ServerPlayer player = e.getPlayer();
        RunicEffectDataSync effects = new RunicEffectDataSync(RunicEffectEnchantments.allowedEffectIds());

        if (player != null) {
            PacketDistributor.sendToPlayer(player, payload);
            PacketDistributor.sendToPlayer(player, effects);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
            PacketDistributor.sendToAllPlayers(effects);
        }
    }
}
