package net.revilodev.runic.mythic;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.RarityTintedItemName;
import net.revilodev.runic.runes.RunicItemTargets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MythicRuneRegistry {
    public static final ResourceLocation RUIN = id("ruin");
    public static final ResourceLocation DOMINION = id("dominion");
    public static final ResourceLocation HUNGER = id("hunger");
    public static final ResourceLocation VOID = id("void");
    public static final ResourceLocation ASCENDANCE = id("ascendance");

    private static final String ROOT = "runic";
    private static final String MYTHIC_RUNE_ITEM = "mythic_rune_item";
    private static final Map<ResourceLocation, MythicRuneDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        register(new MythicRuneDefinition(
                RUIN, "tooltip.runic.mythic.ruin",
                RunicConfig::mythicRuneWeight,
                RunicConfig::ruinDamageBonusPercent,
                RunicConfig::ruinExtraCorruptionChance,
                RunicConfig::ruinExtraCorruptionAmount,
                RunicConfig::ruinDurabilityUseIncreasePercent,
                () -> 0.0D, () -> 0.0D,
                () -> 0, () -> 0.0D, () -> 0,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0,
                () -> 0.0D, () -> 0, () -> 0.0D, () -> 0.0D
        ));
        register(new MythicRuneDefinition(
                DOMINION, "tooltip.runic.mythic.dominion",
                RunicConfig::mythicRuneWeight,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0.0D,
                RunicConfig::dominionEnhancementPowerBonusPercent,
                RunicConfig::dominionSynergyPowerBonusPercent,
                () -> 0, () -> 0.0D, () -> 0,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0,
                () -> 0.0D, () -> 0, () -> 0.0D, () -> 0.0D
        ));
        register(new MythicRuneDefinition(
                HUNGER, "tooltip.runic.mythic.hunger",
                RunicConfig::mythicRuneWeight,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0.0D,
                () -> 0.0D, () -> 0.0D,
                RunicConfig::hungerDurabilityRestoreOnKill,
                RunicConfig::hungerExtraCorruptionOnHitChance,
                RunicConfig::hungerExtraCorruptionAmount,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0,
                () -> 0.0D, () -> 0, () -> 0.0D, () -> 0.0D
        ));
        register(new MythicRuneDefinition(
                VOID, "tooltip.runic.mythic.void",
                RunicConfig::mythicRuneWeight,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0.0D,
                () -> 0.0D, () -> 0.0D,
                () -> 0, () -> 0.0D, () -> 0,
                RunicConfig::voidLowHealthThreshold,
                RunicConfig::voidDamageBonusPercent,
                RunicConfig::voidCombatCorruptionIntervalTicks,
                RunicConfig::voidCombatCorruptionAmount,
                () -> 0.0D, () -> 0, () -> 0.0D, () -> 0.0D
        ));
        register(new MythicRuneDefinition(
                ASCENDANCE, "tooltip.runic.mythic.ascendance",
                RunicConfig::mythicRuneWeight,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0.0D,
                () -> 0.0D, () -> 0.0D,
                () -> 0, () -> 0.0D, () -> 0,
                () -> 0.0D, () -> 0.0D, () -> 0, () -> 0,
                RunicConfig::ascendanceTargetMaxHealthThreshold,
                RunicConfig::ascendanceDurationTicks,
                RunicConfig::ascendanceDamageBonusPercent,
                RunicConfig::ascendanceSpeedBonusPercent
        ));
    }

    private MythicRuneRegistry() {}

    private static void register(MythicRuneDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "mythic/" + path);
    }

    public static List<ResourceLocation> ids() {
        return List.copyOf(DEFINITIONS.keySet());
    }

    public static MythicRuneDefinition get(ResourceLocation id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static boolean isKnown(ResourceLocation id) {
        return get(id) != null;
    }

    public static boolean canApplyTo(ItemStack stack, ResourceLocation id) {
        if (stack == null || stack.isEmpty() || id == null) return false;
        if (GearAttributes.has(stack, GearAttribute.DISSONANT)) return false;
        if (DOMINION.equals(id)) {
            return RunicItemTargets.isWeapon(stack) || RunicItemTargets.isArmor(stack);
        }
        return RunicItemTargets.isWeapon(stack) || RunicItemTargets.isRangedWeapon(stack);
    }

    public static boolean isMythicRune(ItemStack stack) {
        return getItemRuneId(stack) != null;
    }

    public static ResourceLocation getItemRuneId(ItemStack stack) {
        CompoundTag runic = RunicItemData.getMutableRunicTagCopy(stack);
        if (!runic.contains(MYTHIC_RUNE_ITEM, Tag.TAG_STRING)) {
            return null;
        }
        return ResourceLocation.tryParse(runic.getString(MYTHIC_RUNE_ITEM));
    }

    public static void setItemRuneId(ItemStack stack, ResourceLocation runeId) {
        CompoundTag runic = RunicItemData.getMutableRunicTagCopy(stack);
        if (runeId == null) {
            runic.remove(MYTHIC_RUNE_ITEM);
        } else {
            runic.putString(MYTHIC_RUNE_ITEM, runeId.toString());
        }
        RunicItemData.writeMutableRunicTag(stack, runic);
    }

    public static List<ResourceLocation> getApplied(ItemStack stack) {
        return RunicItemData.getMythicRunes(stack);
    }

    public static boolean has(ItemStack stack, ResourceLocation id) {
        return RunicItemData.hasMythicRune(stack, id);
    }

    public static void add(ItemStack stack, ResourceLocation id) {
        RunicItemData.addMythicRune(stack, id);
    }

    public static int count(ItemStack stack) {
        return getApplied(stack).size();
    }

    public static double dominionMultiplier(ItemStack stack) {
        int count = 0;
        for (ResourceLocation id : getApplied(stack)) {
            if (DOMINION.equals(id)) {
                count++;
            }
        }
        if (count <= 0) {
            return 1.0D;
        }
        return 1.0D + ((RunicConfig.dominionEnhancementPowerBonusPercent() / 100.0D) * count);
    }

    public static double dominionSynergyMultiplier(ItemStack stack) {
        int count = 0;
        for (ResourceLocation id : getApplied(stack)) {
            if (DOMINION.equals(id)) {
                count++;
            }
        }
        if (count <= 0) {
            return 1.0D;
        }
        return 1.0D + ((RunicConfig.dominionSynergyPowerBonusPercent() / 100.0D) * count);
    }

    public static double ruinDurabilityMultiplier(ItemStack stack) {
        int count = 0;
        for (ResourceLocation id : getApplied(stack)) {
            if (RUIN.equals(id)) {
                count++;
            }
        }
        if (count <= 0) {
            return 1.0D;
        }
        return 1.0D + ((RunicConfig.ruinDurabilityUseIncreasePercent() / 100.0D) * count);
    }

    public static List<net.minecraft.network.chat.Component> buildTooltip(ItemStack stack, boolean detailed) {
        List<ResourceLocation> ids = getApplied(stack);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<net.minecraft.network.chat.Component> out = new ArrayList<>();
        out.add(net.minecraft.network.chat.Component.translatable("tooltip.runic.mythic_runes").withStyle(ChatFormatting.DARK_PURPLE));
        for (ResourceLocation id : ids) {
            MythicRuneDefinition definition = get(id);
            net.minecraft.network.chat.Component name = definition == null
                    ? net.minecraft.network.chat.Component.translatable("tooltip.runic.mythic_unknown")
                    : net.minecraft.network.chat.Component.translatable(definition.translationKey());
            out.add(net.minecraft.network.chat.Component.literal("  ")
                    .append(RarityTintedItemName.tintedName(ChatFormatting.DARK_PURPLE, stack, name)));
            out.add(net.minecraft.network.chat.Component.literal("  ").append(net.minecraft.network.chat.Component.translatable("tooltip.runic.mythic_rune").withStyle(ChatFormatting.DARK_RED)));
            if (detailed && definition != null) {
                out.add(net.minecraft.network.chat.Component.literal("  ")
                        .append(net.minecraft.network.chat.Component.translatable("tooltip.runic.mythic_desc." + id.getPath().substring("mythic/".length())).withStyle(ChatFormatting.DARK_GRAY)));
            } else if (definition != null) {
                out.add(net.minecraft.network.chat.Component.literal("  [Alt]").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return out;
    }

    // saves list
    public static void writeList(CompoundTag runic, String key, List<ResourceLocation> ids) {
        if (ids == null || ids.isEmpty()) {
            runic.remove(key);
            return;
        }
        ListTag list = new ListTag();
        for (ResourceLocation id : ids) {
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }
        if (list.isEmpty()) {
            runic.remove(key);
        } else {
            runic.put(key, list);
        }
    }
}
