package net.revilodev.runic.relic;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RelicBossDrops {
    private RelicBossDrops() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;

        Item relic = relicFor(event.getEntity().getType());
        if (relic == null) return;

        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(relic)
        ));
    }

    private static Item relicFor(EntityType<?> type) {
        if (type == EntityType.ENDER_DRAGON) return ModItems.DRAGON_HEART.get();
        if (type == EntityType.WITHER) return ModItems.WITHER_CHARGE.get();
        if (type == EntityType.ELDER_GUARDIAN) return ModItems.ELDER_GUARDIANS_EYE.get();
        if (type == EntityType.WARDEN) return ModItems.WARDENS_SOUL.get();
        return null;
    }
}
