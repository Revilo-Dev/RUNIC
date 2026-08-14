package net.revilodev.runic.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
// responds to item blacklist events
public final class ItemBlacklistEvents {
    private ItemBlacklistEvents() {}

    @SubscribeEvent
    // responds to build creative tabs
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (Minecraft.getInstance().level == null) return;

        var enchReg = Minecraft.getInstance()
                .level
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        enchReg.holders().forEach(holder -> {
            Enchantment ench = holder.value();
            for (int lvl = ench.getMinLevel(); lvl <= ench.getMaxLevel(); lvl++) {
                ItemStack book =
                        EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(holder, lvl)
                        );
                event.remove(book, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        });
    }
}
