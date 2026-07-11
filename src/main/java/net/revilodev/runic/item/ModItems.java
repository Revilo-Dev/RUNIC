package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RelicItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.relic.RelicRegistry;

import java.util.List;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RunicMod.MOD_ID);

    public static final DeferredHolder<Item, Item> BLANK_INSCRIPTION =
            ITEMS.register("blank_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.runic.use_etching_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, EtchingItem> BLANK_ETCHING =
            ITEMS.register("blank_etching", () -> new EtchingItem(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.runic.use_etching_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, RuneItem> ENHANCED_RUNE =
            ITEMS.register("enhanced_rune", () -> new RuneItem(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, EtchingItem> ETCHING =
            ITEMS.register("etching", () -> new EtchingItem(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> REPAIR_INSCRIPTION =
            ITEMS.register("repair_rune", () -> inscription("tooltip.runic.repair_rune", EnhancementRarity.COMMON, true, "brittle"));

    public static final DeferredHolder<Item, Item> EXPANSION_INSCRIPTION =
            ITEMS.register("expansion_rune", () -> inscription("tooltip.runic.expansion_rune", EnhancementRarity.UNCOMMON, true));

    public static final DeferredHolder<Item, Item> NULLIFICATION_INSCRIPTION =
            ITEMS.register("nullification_rune", () -> inscription("tooltip.runic.nullification_rune", EnhancementRarity.RARE, true, "negative"));

    public static final DeferredHolder<Item, Item> UPGRADE_INSCRIPTION =
            ITEMS.register("upgrade_rune", () -> inscription("tooltip.runic.upgrade_rune", EnhancementRarity.RARE, true, "overforged"));

    public static final DeferredHolder<Item, Item> REROLL_INSCRIPTION =
            ITEMS.register("reroll_inscription", () -> inscription("tooltip.runic.reroll_inscription", EnhancementRarity.RARE, true, "instable"));

    public static final DeferredHolder<Item, Item> CURSED_INSCRIPTION =
            ITEMS.register("cursed_inscription", () -> inscription("tooltip.runic.cursed_inscription", EnhancementRarity.CURSED, false, "cursed", "brittle", "overforged"));

    public static final DeferredHolder<Item, Item> WILD_INSCRIPTION =
            ITEMS.register("wild_inscription", () -> inscription("tooltip.runic.wild_inscription", EnhancementRarity.LEGENDARY, false, "chaotic", "cursed"));

    public static final DeferredHolder<Item, Item> EXTRACTION_INSCRIPTION =
            ITEMS.register("extraction_inscription", () -> inscription("tooltip.runic.extraction_inscription", EnhancementRarity.EPIC, true, "sealed"));

    public static final DeferredHolder<Item, Item> RESONANCE_INSCRIPTION =
            ITEMS.register("resonance_inscription", () -> inscription("tooltip.runic.resonance_inscription", EnhancementRarity.LEGENDARY, false, "fractured"));

    public static final DeferredHolder<Item, Item> PURIFICATION_INSCRIPTION =
            ITEMS.register("purification_inscription", () -> inscription("tooltip.runic.purification_inscription", EnhancementRarity.EPIC, false));

    public static final DeferredHolder<Item, Item> STABILIZATION_INSCRIPTION =
            ITEMS.register("stabilization_inscription", () -> inscription("tooltip.runic.stabilization_inscription", EnhancementRarity.UNCOMMON, false, "instable", "brittle"));

    public static final DeferredHolder<Item, Item> TEMPERING_INSCRIPTION =
            ITEMS.register("tempering_inscription", () -> inscription("tooltip.runic.tempering_inscription", EnhancementRarity.RARE, false, "reinforced", "brittle"));

    public static final DeferredHolder<Item, Item> RELIC_SOCKET_INSCRIPTION =
            ITEMS.register("relic_socket_inscription", () -> inscription("tooltip.runic.relic_socket_inscription", EnhancementRarity.EPIC, true));

    public static final DeferredHolder<Item, RelicItem> DRAGON_HEART =
            ITEMS.register("dragon_heart", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.DRAGON_HEART));

    public static final DeferredHolder<Item, RelicItem> ELDER_GUARDIANS_EYE =
            ITEMS.register("elder_guardians_eye", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.ELDER_GUARDIANS_EYE));

    public static final DeferredHolder<Item, RelicItem> WITHER_CHARGE =
            ITEMS.register("wither_charge", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.WITHER_CHARGE));

    public static final DeferredHolder<Item, RelicItem> WARDENS_SOUL =
            ITEMS.register("wardens_soul", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.WARDENS_SOUL));

    private static Item inscription(String tooltipKey, EnhancementRarity rarity, boolean craftable, String... attributeIds) {
        return new Item(new Item.Properties().stacksTo(64)) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.runic.use_artisans_workbench").withStyle(ChatFormatting.DARK_GRAY));
                Component desc = Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY);
                if (attributeIds.length > 0 && !Screen.hasAltDown()) {
                    desc = desc.copy().append(Component.literal(" [Alt]").withStyle(ChatFormatting.DARK_GRAY));
                }
                tooltip.add(desc);
                tooltip.add(Component.translatable("tooltip.runic.inscription_rarity",
                        Component.translatable("tooltip.runic.rarity." + rarity.key()).withStyle(rarity.color())).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(craftable
                        ? "tooltip.runic.inscription_source.craftable"
                        : "tooltip.runic.inscription_source.loot_only").withStyle(craftable ? ChatFormatting.GREEN : ChatFormatting.GOLD));
                if (attributeIds.length > 0 && Screen.hasAltDown()) {
                    tooltip.add(Component.translatable("tooltip.runic.inscription_attributes").withStyle(ChatFormatting.GRAY));
                    for (String attr : attributeIds) {
                        tooltip.add(Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.attribute." + attr).withStyle(ChatFormatting.WHITE)));
                        tooltip.add(Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.attribute_desc." + attr).withStyle(ChatFormatting.DARK_GRAY)));
                    }
                }
            }
        };
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
