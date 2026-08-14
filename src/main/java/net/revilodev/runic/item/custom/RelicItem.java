package net.revilodev.runic.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.revilodev.runic.relic.RelicRegistry;

import java.util.List;


public final class RelicItem extends Item {
    private final ResourceLocation relicId;

    public RelicItem(Properties properties, ResourceLocation relicId) {
        super(properties);
        this.relicId = relicId;
    }

    public ResourceLocation relicId() {
        return relicId;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.runic.use_artisans_workbench").withStyle(ChatFormatting.DARK_GRAY));
        RelicRegistry.appendRelicItemTooltip(relicId, tooltip, Screen.hasAltDown());
    }
}
