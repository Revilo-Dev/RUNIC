package net.revilodev.runic.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.custom.RelicItem;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.runes.RunicItemTargets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RelicRegistry {
    public static final ResourceLocation DRAGON_HEART = relicId("dragon_heart");
    public static final ResourceLocation ELDER_GUARDIANS_EYE = relicId("elder_guardians_eye");
    public static final ResourceLocation WITHER_CHARGE = relicId("wither_charge");
    public static final ResourceLocation WARDENS_SOUL = relicId("wardens_soul");

    private static final Map<ResourceLocation, RelicDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        register(new RelicDefinition(
                DRAGON_HEART,
                "item.runic.dragon_heart",
                RunicConfig::dragonHeartCorruption,
                RunicConfig::dragonHeartDurabilityUseIncreasePercent,
                new SimpleEffect("dragon_heart"),
                new SimpleSetBonus("dragon_heart"),
                RelicRegistry::supportsRunicModification
        ));
        register(new RelicDefinition(
                ELDER_GUARDIANS_EYE,
                "item.runic.elder_guardians_eye",
                RunicConfig::elderGuardiansEyeCorruption,
                RunicConfig::elderGuardiansEyeDurabilityUseIncreasePercent,
                new SimpleEffect("elder_guardians_eye"),
                new SimpleSetBonus("elder_guardians_eye"),
                RelicRegistry::supportsRunicModification
        ));
        register(new RelicDefinition(
                WITHER_CHARGE,
                "item.runic.wither_charge",
                RunicConfig::witherChargeCorruption,
                RunicConfig::witherChargeDurabilityUseIncreasePercent,
                new SimpleEffect("wither_charge"),
                new SimpleSetBonus("wither_charge"),
                RelicRegistry::supportsRunicModification
        ));
        register(new RelicDefinition(
                WARDENS_SOUL,
                "item.runic.wardens_soul",
                RunicConfig::wardensSoulCorruption,
                RunicConfig::wardensSoulDurabilityUseIncreasePercent,
                new SimpleEffect("wardens_soul"),
                new SimpleSetBonus("wardens_soul"),
                RelicRegistry::supportsRunicModification
        ));
    }

    private RelicRegistry() {}

    public static ResourceLocation relicId(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, path);
    }

    public static RelicDefinition get(ResourceLocation relicId) {
        return relicId == null ? null : DEFINITIONS.get(relicId);
    }

    public static List<RelicDefinition> all() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static boolean isRelicItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RelicItem;
    }

    public static ResourceLocation relicItemId(ItemStack stack) {
        return stack.getItem() instanceof RelicItem relicItem ? relicItem.relicId() : null;
    }

    public static boolean supportsRunicModification(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
            return false;
        }

        return RunicItemTargets.isRunicGear(stack);
    }

    public static boolean canApplyTo(ItemStack target, ResourceLocation relicId) {
        RelicDefinition definition = get(relicId);
        return definition != null && definition.canApplyTo(target);
    }

    public static int countEquippedRelics(LivingEntity entity, ResourceLocation relicId) {
        if (entity == null || relicId == null) {
            return 0;
        }

        int count = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) {
                continue;
            }
            if (relicId.equals(RunicItemData.getRelicId(stack))) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullRelicSet(LivingEntity entity, ResourceLocation relicId) {
        return RunicConfig.relicEnableSetBonuses()
                && countEquippedRelics(entity, relicId) >= RunicConfig.relicFullSetRequiredCount();
    }

    public static double durabilityPenaltyPercent(ItemStack stack) {
        RelicDefinition definition = get(RunicItemData.getRelicId(stack));
        return definition == null ? 0.0D : definition.durabilityUseIncreasePercentValue() / 100.0D;
    }

    public static int relicCorruption(ItemStack stack) {
        RelicDefinition definition = get(RunicItemData.getRelicId(stack));
        return definition == null ? 0 : definition.corruptionValue();
    }

    public static Component displayName(ResourceLocation relicId) {
        RelicDefinition definition = get(relicId);
        return definition == null
                ? Component.translatable("tooltip.runic.relic_unknown")
                : Component.translatable(definition.translationKey());
    }

    public static void appendRelicItemTooltip(ResourceLocation relicId, List<Component> tooltip, boolean detailed) {
        RelicDefinition definition = get(relicId);
        if (definition == null) {
            tooltip.add(Component.translatable("tooltip.runic.relic_unknown").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.runic.requires_empty_relic_socket").withStyle(ChatFormatting.GRAY));
        tooltip.add(EnhancementRarity.LEGENDARY.applyTo(Component.translatable("tooltip.runic.rarity.legendary")));

        if (detailed) {
            tooltip.add(Component.translatable("tooltip.runic.relic_durability_use", formatPercent(definition.durabilityUseIncreasePercentValue())).withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("tooltip.runic.relic_corruption", definition.corruptionValue()).withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("tooltip.runic.relic_base." + definition.id().getPath()).withStyle(ChatFormatting.GRAY));
            tooltip.add(relicSetLine(definition));
        } else {
            tooltip.add(Component.translatable("tooltip.runic.hold_ctrl_more").withStyle(ChatFormatting.DARK_GRAY));
        }
    }


    public static List<Component> buildGearTooltipLines(ItemStack stack, boolean showDetails) {
        if (!RunicItemData.hasRelicSocket(stack)) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        if (RunicItemData.hasEmptyRelicSocket(stack)) {
            lines.add(Component.translatable("tooltip.runic.relic_socket.empty").withStyle(ChatFormatting.GRAY));
            return lines;
        }

        if (!RunicItemData.hasRelic(stack)) {
            return lines;
        }

        ResourceLocation relicId = RunicItemData.getRelicId(stack);
        RelicDefinition definition = get(relicId);
        lines.add(Component.translatable("tooltip.runic.relic", displayName(relicId)).withStyle(ChatFormatting.GOLD));
        lines.add(EnhancementRarity.LEGENDARY.applyTo(Component.translatable("tooltip.runic.rarity.legendary")));

        if (definition == null) {
            return lines;
        }

        if (showDetails) {
            lines.add(Component.translatable("tooltip.runic.relic_durability_use", formatPercent(definition.durabilityUseIncreasePercentValue())).withStyle(ChatFormatting.RED));
            lines.add(Component.translatable("tooltip.runic.relic_corruption", definition.corruptionValue()).withStyle(ChatFormatting.DARK_PURPLE));
            lines.add(Component.translatable("tooltip.runic.relic_base." + definition.id().getPath()).withStyle(ChatFormatting.GRAY));
            lines.add(relicSetLine(definition));
        } else {
            lines.add(Component.translatable("tooltip.runic.hold_ctrl_more").withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private static String formatPercent(double value) {
        long rounded = Math.round(value);
        return Math.abs(value - rounded) < 0.001D ? Long.toString(rounded) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static Component relicSetLine(RelicDefinition definition) {
        return Component.literal("Full Set: ").withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("tooltip.runic.relic_set." + definition.id().getPath()).withStyle(ChatFormatting.GRAY));
    }

    private static void register(RelicDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
    }

    private record SimpleEffect(String id) implements RelicDefinition.Effect {
    }

    private record SimpleSetBonus(String id) implements RelicDefinition.SetBonus {
    }
}
