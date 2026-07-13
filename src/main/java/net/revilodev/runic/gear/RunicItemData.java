package net.revilodev.runic.gear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.mythic.MythicRuneRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RunicItemData {
    private static final String ROOT = "runic";
    private static final String CORRUPTION = "corruption";
    private static final String SYNERGY_POTENTIAL = "synergy_potential";
    private static final String SYNERGIES = "synergies";
    private static final String RELIC_SOCKET = "relic_socket";
    private static final String RELIC = "relic";
    private static final String MYTHIC_RUNES = "mythic_runes";

    private RunicItemData() {}

    public static int getCorruption(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        if (runic == null) return 0;
        return clampCorruption(runic.getInt(CORRUPTION));
    }

    public static void setCorruption(ItemStack stack, int value) {
        int next = clampCorruption(value);
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        if (next <= 0) runic.remove(CORRUPTION);
        else runic.putInt(CORRUPTION, next);
        writeRunic(stack, root, runic);
        if (next >= RunicConfig.exhaustedCorruptionThreshold()) {
            GearAttributes.addLevel(stack, GearAttribute.EXHAUSTED, 1);
        }
    }

    public static void addCorruption(ItemStack stack, int amount) {
        if (amount == 0) return;
        int previous = getCorruption(stack);
        setCorruption(stack, previous + amount);
        int current = getCorruption(stack);
        if (amount > 0 && current > previous) {
            rollCorruptionConsequences(stack, previous, current);
        }
    }

    public static boolean isFullyCorrupted(ItemStack stack) {
        return getCorruption(stack) >= RunicConfig.exhaustedCorruptionThreshold();
    }

    public static boolean isExhausted(ItemStack stack) {
        return GearAttributes.has(stack, GearAttribute.EXHAUSTED) || isFullyCorrupted(stack);
    }

    public static boolean hasFractured(ItemStack stack) {
        return GearAttributes.has(stack, GearAttribute.FRACTURED);
    }

    public static int getSynergyPotential(ItemStack stack) {
        if (GearAttributes.has(stack, GearAttribute.DISSONANT)) return 0;
        CompoundTag runic = getRunic(stack);
        if (runic == null) return 0;
        return clampSynergyPotential(runic.getInt(SYNERGY_POTENTIAL));
    }

    public static void setSynergyPotential(ItemStack stack, int value) {
        if (value > 0 && GearAttributes.has(stack, GearAttribute.DISSONANT)) {
            value = 0;
        }
        int next = clampSynergyPotential(value);
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        if (next <= 0) runic.remove(SYNERGY_POTENTIAL);
        else runic.putInt(SYNERGY_POTENTIAL, next);
        writeRunic(stack, root, runic);
    }

    public static void addSynergyPotential(ItemStack stack, int amount) {
        if (amount == 0) return;
        setSynergyPotential(stack, getSynergyPotential(stack) + amount);
    }

    public static double getSynergyChance(ItemStack stack) {
        double chance = RunicConfig.baseSynergyChance()
                + (getSynergyPotential(stack) * RunicConfig.synergyPotentialBonus());
        return Math.min(chance, RunicConfig.maxSynergyChance());
    }

    public static List<ResourceLocation> getSynergies(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        if (runic == null || !runic.contains(SYNERGIES, Tag.TAG_LIST)) return List.of();
        ListTag list = runic.getList(SYNERGIES, Tag.TAG_STRING);
        List<ResourceLocation> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) out.add(id);
        }
        return List.copyOf(out);
    }

    public static boolean hasSynergy(ItemStack stack, ResourceLocation id) {
        if (id == null) return false;
        return getSynergies(stack).contains(id);
    }

    public static void addSynergy(ItemStack stack, ResourceLocation id) {
        if (id == null || hasSynergy(stack, id)) return;
        List<ResourceLocation> current = new ArrayList<>(getSynergies(stack));
        current.add(id);
        setSynergies(stack, current);
    }

    public static void removeSynergy(ItemStack stack, ResourceLocation id) {
        if (id == null) return;
        List<ResourceLocation> current = new ArrayList<>(getSynergies(stack));
        if (current.remove(id)) setSynergies(stack, current);
    }

    public static void clearMythicRunes(ItemStack stack) {
        setMythicRunes(stack, List.of());
    }

    public static void clearSynergies(ItemStack stack) {
        setSynergies(stack, List.of());
    }

    public static boolean hasRelicSocket(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        return runic != null && runic.getBoolean(RELIC_SOCKET);
    }

    public static boolean hasEmptyRelicSocket(ItemStack stack) {
        return hasRelicSocket(stack) && !hasRelic(stack);
    }

    public static void setRelicSocket(ItemStack stack, boolean value) {
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        if (value) {
            runic.putBoolean(RELIC_SOCKET, true);
        } else {
            runic.remove(RELIC_SOCKET);
            runic.remove(RELIC);
        }
        writeRunic(stack, root, runic);
    }

    public static boolean hasRelic(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        return runic != null && runic.contains(RELIC, Tag.TAG_STRING) && !runic.getString(RELIC).isBlank();
    }

    public static ResourceLocation getRelicId(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        if (runic == null || !runic.contains(RELIC, Tag.TAG_STRING)) {
            return null;
        }
        return ResourceLocation.tryParse(runic.getString(RELIC));
    }

    public static void setRelic(ItemStack stack, ResourceLocation relicId) {
        if (relicId == null) {
            clearRelic(stack);
            return;
        }

        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        runic.putBoolean(RELIC_SOCKET, true);
        runic.putString(RELIC, relicId.toString());
        writeRunic(stack, root, runic);
    }

    public static void clearRelic(ItemStack stack) {
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        runic.remove(RELIC);
        writeRunic(stack, root, runic);
    }

    public static List<ResourceLocation> getMythicRunes(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        if (runic == null || !runic.contains(MYTHIC_RUNES, Tag.TAG_LIST)) {
            return List.of();
        }
        List<ResourceLocation> out = new ArrayList<>();
        ListTag list = runic.getList(MYTHIC_RUNES, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                out.add(id);
            }
        }
        return List.copyOf(out);
    }

    public static boolean hasMythicRune(ItemStack stack, ResourceLocation id) {
        return id != null && getMythicRunes(stack).contains(id);
    }

    public static void addMythicRune(ItemStack stack, ResourceLocation id) {
        if (id == null || hasMythicRune(stack, id)) {
            return;
        }
        List<ResourceLocation> current = new ArrayList<>(getMythicRunes(stack));
        current.add(id);
        setMythicRunes(stack, current);
    }

    public static CorruptionBand getCorruptionBand(ItemStack stack) {
        return isExhausted(stack)
                ? CorruptionBand.EXHAUSTED
                : CorruptionBand.fromPercent(getCorruptionPercent(stack));
    }

    public static boolean isStable(ItemStack stack) {
        return getCorruptionBand(stack) == CorruptionBand.STABLE;
    }

    public static boolean isTainted(ItemStack stack) {
        return getCorruptionBand(stack) == CorruptionBand.TAINTED;
    }

    public static boolean isCorrupted(ItemStack stack) {
        return getCorruptionBand(stack) == CorruptionBand.CORRUPTED;
    }

    public static boolean isCritical(ItemStack stack) {
        return getCorruptionBand(stack) == CorruptionBand.CRITICAL;
    }

    public static int getCorruptionPercent(ItemStack stack) {
        int threshold = Math.max(1, RunicConfig.exhaustedCorruptionThreshold());
        return (int) Math.round((getCorruption(stack) * 100.0D) / threshold);
    }

    public static CompoundTag getMutableRunicTagCopy(ItemStack stack) {
        CompoundTag root = getRootCopy(stack);
        return root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
    }

    public static void writeMutableRunicTag(ItemStack stack, CompoundTag runic) {
        CompoundTag root = getRootCopy(stack);
        writeRunic(stack, root, runic == null ? new CompoundTag() : runic);
    }

    private static void setMythicRunes(ItemStack stack, List<ResourceLocation> ids) {
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        MythicRuneRegistry.writeList(runic, MYTHIC_RUNES, ids);
        writeRunic(stack, root, runic);
    }

    private static void setSynergies(ItemStack stack, List<ResourceLocation> ids) {
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        if (ids == null || ids.isEmpty()) {
            runic.remove(SYNERGIES);
        } else {
            ListTag list = new ListTag();
            for (ResourceLocation id : ids) {
                if (id != null) list.add(StringTag.valueOf(id.toString()));
            }
            runic.put(SYNERGIES, list);
        }
        writeRunic(stack, root, runic);
    }

    private static int clampCorruption(int value) {
        return Math.max(0, Math.min(RunicConfig.exhaustedCorruptionThreshold(), value));
    }

    private static int clampSynergyPotential(int value) {
        return Math.max(0, Math.min(RunicConfig.maxSynergyPotential(), value));
    }

    private static CompoundTag getRootCopy(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag();
    }

    private static CompoundTag getRunic(ItemStack stack) {
        CompoundTag root = getRootCopy(stack);
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) return null;
        return root.getCompound(ROOT);
    }

    private static void writeRunic(ItemStack stack, CompoundTag root, CompoundTag runic) {
        if (runic.isEmpty()) root.remove(ROOT);
        else root.put(ROOT, runic);

        if (root.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void rollCorruptionConsequences(ItemStack stack, int previousCorruption, int currentCorruption) {
        if (stack.isEmpty() || currentCorruption <= previousCorruption || isExhausted(stack)) {
            return;
        }

        CorruptionBand band = getCorruptionBand(stack);
        if (band == CorruptionBand.STABLE || band == CorruptionBand.EXHAUSTED) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (RunicConfig.corruptionEnableNegativeAttributes()
                && random.nextDouble() < negativeRollChance(band)) {
            rollAttribute(stack, negativePool(band), true, random);
        }
        if (RunicConfig.corruptionEnablePositiveAttributes()
                && random.nextDouble() < positiveRollChance(band)) {
            rollAttribute(stack, positivePool(band), false, random);
        }
    }

    private static void rollAttribute(ItemStack stack, List<GearAttribute> pool, boolean negative, ThreadLocalRandom random) {
        if (pool.isEmpty()) {
            return;
        }
        List<GearAttribute> available = new ArrayList<>(pool);
        Collections.shuffle(available, random);
        for (GearAttribute attr : available) {
            if (GearAttributes.getLevel(stack, attr) <= 0) {
                GearAttributes.addLevel(stack, attr, 1);
                return;
            }
        }
        if (!negative && !available.isEmpty()) {
            GearAttributes.addLevel(stack, available.get(0), 1);
        }
    }

    private static double negativeRollChance(CorruptionBand band) {
        return switch (band) {
            case TAINTED -> RunicConfig.taintedNegativeAttributeRollChance();
            case CORRUPTED -> RunicConfig.corruptedNegativeAttributeRollChance();
            case CRITICAL -> RunicConfig.criticalNegativeAttributeRollChance();
            default -> RunicConfig.stableCorruptionAttributeRollChance();
        };
    }

    private static double positiveRollChance(CorruptionBand band) {
        return switch (band) {
            case TAINTED -> RunicConfig.taintedPositiveAttributeRollChance();
            case CORRUPTED -> RunicConfig.corruptedPositiveAttributeRollChance();
            case CRITICAL -> RunicConfig.criticalPositiveAttributeRollChance();
            default -> 0.0D;
        };
    }

    private static List<GearAttribute> negativePool(CorruptionBand band) {
        return switch (band) {
            case TAINTED -> List.of(GearAttribute.BRITTLE, GearAttribute.FRACTURED, GearAttribute.INSTABLE);
            case CORRUPTED -> List.of(GearAttribute.BRITTLE, GearAttribute.FRACTURED, GearAttribute.INSTABLE, GearAttribute.CHAOTIC);
            case CRITICAL -> List.of(GearAttribute.BRITTLE, GearAttribute.FRACTURED, GearAttribute.INSTABLE, GearAttribute.CHAOTIC, GearAttribute.CURSED);
            default -> List.of();
        };
    }

    private static List<GearAttribute> positivePool(CorruptionBand band) {
        return switch (band) {
            case CORRUPTED -> List.of(GearAttribute.REINFORCED, GearAttribute.TEMPERED);
            case CRITICAL -> List.of(GearAttribute.REINFORCED, GearAttribute.TEMPERED, GearAttribute.ANCIENT, GearAttribute.HARMONIZED);
            default -> List.of();
        };
    }
}
