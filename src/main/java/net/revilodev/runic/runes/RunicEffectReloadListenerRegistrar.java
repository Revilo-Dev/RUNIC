package net.revilodev.runic.runes;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RunicEffectReloadListenerRegistrar {
    private RunicEffectReloadListenerRegistrar() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new RunicEffectEnchantmentData());
    }
}
