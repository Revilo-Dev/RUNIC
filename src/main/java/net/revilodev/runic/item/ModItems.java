package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicConfig;
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
                    tooltip.add(Component.translatable("tooltip.runic.use_inscription_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, EtchingItem> BLANK_ETCHING =
            ITEMS.register("blank_etching", () -> new EtchingItem(etchingProperties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.runic.use_enchanting_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, RuneItem> ENHANCED_RUNE =
            ITEMS.register("enhanced_rune", () -> new RuneItem(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, EtchingItem> ETCHING =
            ITEMS.register("etching", () -> new EtchingItem(etchingProperties()));

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
            ITEMS.register("purification_inscription", () -> inscription("tooltip.runic.purification_inscription", EnhancementRarity.EPIC, false, "brittle"));

    public static final DeferredHolder<Item, Item> STABILIZATION_INSCRIPTION =
            ITEMS.register("stabilization_inscription", () -> inscription("tooltip.runic.stabilization_inscription", EnhancementRarity.UNCOMMON, false, "instable", "brittle"));

    public static final DeferredHolder<Item, Item> TEMPERING_INSCRIPTION =
            ITEMS.register("tempering_inscription", () -> inscription("tooltip.runic.tempering_inscription", EnhancementRarity.RARE, false, "reinforced"));

    public static final DeferredHolder<Item, Item> RELIC_SOCKET_INSCRIPTION =
            ITEMS.register("relic_socket_inscription", () -> inscription("tooltip.runic.relic_socket_inscription", EnhancementRarity.EPIC, true));

    public static final DeferredHolder<Item, Item> DISSONANT_INSCRIPTION =
            ITEMS.register("dissonant_inscription", () -> inscription("tooltip.runic.dissonant_inscription", EnhancementRarity.LEGENDARY, false, true, "dissonant"));

    public static final DeferredHolder<Item, RelicItem> DRAGON_HEART =
            ITEMS.register("dragon_heart", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.DRAGON_HEART));

    public static final DeferredHolder<Item, RelicItem> ELDER_GUARDIANS_EYE =
            ITEMS.register("elder_guardians_eye", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.ELDER_GUARDIANS_EYE));

    public static final DeferredHolder<Item, RelicItem> WITHER_CHARGE =
            ITEMS.register("wither_charge", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.WITHER_CHARGE));

    public static final DeferredHolder<Item, RelicItem> WARDENS_SOUL =
            ITEMS.register("wardens_soul", () -> new RelicItem(new Item.Properties().stacksTo(16), RelicRegistry.WARDENS_SOUL));

    private static Item inscription(String tooltipKey, EnhancementRarity rarity, boolean craftable, String... attributeIds) {
        return inscription(tooltipKey, rarity, craftable, false, attributeIds);
    }

    private static Item inscription(String tooltipKey, EnhancementRarity rarity, boolean craftable, boolean creativeOnly, String... attributeIds) {
        return new Item(new Item.Properties().stacksTo(64)) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.runic.use_artisans_workbench").withStyle(ChatFormatting.DARK_GRAY));
                Component desc = Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY);
                if (hasDetailedInscriptionLines(tooltipKey, attributeIds) && !Screen.hasAltDown()) {
                    desc = desc.copy().append(Component.literal(" [Alt]").withStyle(ChatFormatting.DARK_GRAY));
                }
                tooltip.add(desc);
                tooltip.add(rarity.applyTo(Component.literal(titleize(rarity.key()) + starString(rarity.stars()))));
                if (hasDetailedInscriptionLines(tooltipKey, attributeIds) && Screen.hasAltDown()) {
                    tooltip.add(Component.translatable("tooltip.runic.inscription_attributes").withStyle(ChatFormatting.GRAY));
                    if (tooltipKey.equals("tooltip.runic.resonance_inscription")) {
                        tooltip.add(Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.synergy_potential", "+1").withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    tooltip.add(Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.inscription_corruption",
                                    formatSigned(corruptionForInscription(tooltipKey))).withStyle(ChatFormatting.DARK_PURPLE)));
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

    private static boolean hasDetailedInscriptionLines(String tooltipKey, String[] attributeIds) {
        return attributeIds.length > 0 || isKnownInscriptionTooltip(tooltipKey);
    }

    private static boolean isKnownInscriptionTooltip(String tooltipKey) {
        return switch (tooltipKey) {
            case "tooltip.runic.repair_rune",
                 "tooltip.runic.expansion_rune",
                 "tooltip.runic.nullification_rune",
                 "tooltip.runic.upgrade_rune",
                 "tooltip.runic.reroll_inscription",
                 "tooltip.runic.cursed_inscription",
                 "tooltip.runic.wild_inscription",
                 "tooltip.runic.extraction_inscription",
                 "tooltip.runic.resonance_inscription",
                 "tooltip.runic.purification_inscription",
                 "tooltip.runic.stabilization_inscription",
                 "tooltip.runic.tempering_inscription",
                 "tooltip.runic.relic_socket_inscription",
                 "tooltip.runic.dissonant_inscription" -> true;
            default -> false;
        };
    }

    private static int corruptionForInscription(String tooltipKey) {
        return switch (tooltipKey) {
            case "tooltip.runic.repair_rune" -> -RunicConfig.restorationInscriptionCorruptionReduction();
            case "tooltip.runic.expansion_rune" -> 20;
            case "tooltip.runic.nullification_rune" -> RunicConfig.nullificationInscriptionCorruption();
            case "tooltip.runic.upgrade_rune" -> RunicConfig.upgradeInscriptionCorruption();
            case "tooltip.runic.reroll_inscription" -> RunicConfig.rerollInscriptionCorruption();
            case "tooltip.runic.cursed_inscription" -> RunicConfig.cursedInscriptionCorruption();
            case "tooltip.runic.wild_inscription" -> RunicConfig.wildInscriptionCorruption();
            case "tooltip.runic.extraction_inscription" -> RunicConfig.extractionInscriptionCorruption();
            case "tooltip.runic.resonance_inscription" -> RunicConfig.resonanceInscriptionCorruption();
            case "tooltip.runic.purification_inscription" -> RunicConfig.purificationInscriptionCorruption();
            case "tooltip.runic.stabilization_inscription" -> RunicConfig.stabilizationInscriptionCorruption();
            case "tooltip.runic.tempering_inscription" -> RunicConfig.temperingInscriptionCorruption();
            case "tooltip.runic.relic_socket_inscription" -> RunicConfig.relicSocketInscriptionCorruption();
            default -> 0;
        };
    }

    private static String formatSigned(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    private static Item.Properties etchingProperties() {
        return new Item.Properties()
                .stacksTo(64)
                .component(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    private static String titleize(String key) {
        if (key == null || key.isBlank()) return "";
        return Character.toUpperCase(key.charAt(0)) + key.substring(1).toLowerCase();
    }

    private static String starString(int stars) {
        if (stars <= 0) return "";
        return " " + "\u2605".repeat(stars);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
