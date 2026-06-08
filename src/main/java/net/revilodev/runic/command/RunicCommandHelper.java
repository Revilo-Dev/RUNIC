package net.revilodev.runic.command;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.registry.ModDataComponents;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RunicCommandHelper {
    private static final String ROOT = "runic";
    private static final String CURSED_APPLIED = "cursed_applied";
    private static final String GEAR_ATTRIBUTES = "gear_attributes";
    private static final String PREVIEW_DELTA = "preview_delta";
    private static final String LEECHING_FROM_ETCHING = "leeching_from_etching";
    private static final int MAX_ATTR_LEVEL = 10;
    private static final RandomSource RNG = RandomSource.create();

    private RunicCommandHelper() {}

    public static boolean applyStat(ItemStack stack, RuneStatType type, float amount) {
        if (stack.isEmpty() || type == null || amount == 0.0F || EnchantBlacklist.isStatBlacklisted(type) || !canApplyStatTo(stack, type)) {
            return false;
        }

        RuneStats current = RuneStats.get(stack);
        RuneStats updated = RuneStats.combine(current, RuneStats.single(type, amount), false);
        if (statsEqual(current, updated)) {
            return false;
        }

        RuneStats.set(stack, updated);
        if (type == RuneStatType.LEECHING_CHANCE) {
            setLeechingSource(stack, false);
        }
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        RuneSlots.syncUsedToContents(stack);
        return true;
    }

    public static boolean applyEffect(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        if (stack.isEmpty() || enchantment == null || !RuneItem.isEffectEnchantment(enchantment)
                || EnchantBlacklist.isBlacklisted(enchantment)) {
            return false;
        }

        int desired = RuneItem.clampEffectLevel(enchantment, level);
        if (desired <= 0) {
            return false;
        }

        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        int existing = mutable.getLevel(enchantment);
        if (existing == desired) {
            return false;
        }

        if (existing == 0) {
            List<Holder<Enchantment>> incompatibleCheck = new ArrayList<>(mutable.keySet());
            if (!EnchantmentHelper.isEnchantmentCompatible(incompatibleCheck, enchantment)) {
                return false;
            }
        }

        mutable.set(enchantment, desired);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        RuneSlots.syncUsedToContents(stack);
        return true;
    }

    public static boolean clear(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        boolean changed = false;

        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!enchants.isEmpty()) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
                if (!RuneItem.isEffectEnchantment(entry.getKey()) && entry.getIntValue() > 0) {
                    mutable.set(entry.getKey(), entry.getIntValue());
                } else if (entry.getIntValue() > 0) {
                    changed = true;
                }
            }

            ItemEnchantments filtered = mutable.toImmutable();
            if (filtered.isEmpty()) {
                stack.remove(DataComponents.ENCHANTMENTS);
            } else {
                stack.set(DataComponents.ENCHANTMENTS, filtered);
            }
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
            changed = true;
        }

        if (!RuneStats.get(stack).isEmpty()) {
            RuneStats.set(stack, RuneStats.empty());
            changed = true;
        }

        CompoundTag root = getRootCopy(stack);
        if (root.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag runic = root.getCompound(ROOT);
            runic.remove(LEECHING_FROM_ETCHING);
            runic.remove(CURSED_APPLIED);
            runic.remove(PREVIEW_DELTA);
            runic.remove(GEAR_ATTRIBUTES);
            if (runic.isEmpty()) {
                root.remove(ROOT);
            } else {
                root.put(ROOT, runic);
            }
            changed = true;
        }
        root.remove(PREVIEW_DELTA);
        setRoot(stack, root);

        if (stack.has(ModDataComponents.RUNE_SLOTS_USED.get())) {
            stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), 0);
            changed = true;
        }

        updateGlintAfter(stack);
        return changed;
    }

    public static boolean inscribe(ItemStack stack, String inscription) {
        if (stack.isEmpty() || inscription == null || inscription.isBlank()) {
            return false;
        }

        String key = normalizeInscription(inscription);
        return switch (key) {
            case "repair" -> applyRepair(stack);
            case "expansion" -> applyExpansionInscription(stack);
            case "nullification" -> applyNullificationInscription(stack);
            case "upgrade" -> applyUpgrade(stack);
            case "reroll" -> applyReroll(stack);
            case "cursed" -> applyCursedInscription(stack);
            case "wild" -> applyWild(stack);
            case "extraction" -> applyExtraction(stack);
            default -> false;
        };
    }

    public static List<String> inscriptionIds() {
        return List.of("repair", "expansion", "nullification", "upgrade", "reroll", "cursed", "wild", "extraction");
    }

    private static String normalizeInscription(String inscription) {
        String key = inscription.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (key) {
            case "repair_rune", "repair_inscription" -> "repair";
            case "expansion_rune", "expansion_inscription" -> "expansion";
            case "nullification_rune", "nullification_inscription" -> "nullification";
            case "upgrade_rune", "upgrade_inscription" -> "upgrade";
            case "reroll_inscription" -> "reroll";
            case "cursed_inscription" -> "cursed";
            case "wild_inscription" -> "wild";
            case "extraction_inscription" -> "extraction";
            default -> key;
        };
    }

    private static boolean canApplyStatTo(ItemStack target, RuneStatType stat) {
        Item item = target.getItem();
        ArmorItem armor = item instanceof ArmorItem armorItem ? armorItem : null;

        return switch (stat) {
            case ATTACK_DAMAGE, ATTACK_SPEED, ATTACK_RANGE, SWEEPING_RANGE,
                    UNDEAD_DAMAGE, NETHER_DAMAGE,
                    STUN_CHANCE, FLAME_CHANCE, BLEEDING_CHANCE, SHOCKING_CHANCE,
                    POISON_CHANCE, WITHERING_CHANCE, WEAKENING_CHANCE,
                    FREEZING_CHANCE, LEECHING_CHANCE, FANGS ->
                    item instanceof SwordItem
                            || item instanceof AxeItem
                            || item instanceof TridentItem
                            || item instanceof MaceItem;

            case POWER, DRAW_SPEED, ABILITY_POWER ->
                    item instanceof BowItem || item instanceof CrossbowItem;

            case MINING_SPEED -> item instanceof DiggerItem;
            case MOVEMENT_SPEED -> armor != null && armor.getEquipmentSlot() == EquipmentSlot.FEET;
            case JUMP_HEIGHT -> armor != null && armor.getEquipmentSlot() == EquipmentSlot.LEGS;
            case HEALTH, RESISTANCE, FIRE_RESISTANCE, BLAST_RESISTANCE,
                    PROJECTILE_RESISTANCE, KNOCKBACK_RESISTANCE,
                    TOUGHNESS, STONE, AEGIS -> armor != null;
            case DURABILITY -> target.isDamageableItem();
        };
    }

    private static boolean applyRepair(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 1 || stack.getDamageValue() <= 0) {
            return false;
        }
        if (!reduceMaxDurability(stack, 0.05D)) {
            return false;
        }
        stack.set(DataComponents.DAMAGE, 0);
        updateGlintAfter(stack);
        return true;
    }

    private static boolean applyExpansionInscription(ItemStack stack) {
        if (!RuneSlots.enabled()) {
            return false;
        }
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 1 || RuneSlots.expansionsUsed(stack) >= 3) {
            return false;
        }
        if (!reduceMaxDurability(stack, 0.20D)) {
            return false;
        }
        RuneSlots.addOneSlot(stack);
        RuneSlots.incrementExpansion(stack);
        updateGlintAfter(stack);
        return true;
    }

    private static boolean applyNullificationInscription(ItemStack stack) {
        if (GearAttributes.getLevel(stack, GearAttribute.NEGATIVE) >= MAX_ATTR_LEVEL || !canApplyNullification(stack)) {
            return false;
        }

        clear(stack);
        GearAttributes.addLevel(stack, GearAttribute.NEGATIVE, 1);
        updateGlintAfter(stack);
        return true;
    }

    private static boolean canApplyNullification(ItemStack stack) {
        if (!stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return true;
        }
        if (!stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return true;
        }
        if (!RuneStats.get(stack).isEmpty()) {
            return true;
        }
        return RuneSlots.used(stack) > 0 || stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static boolean applyUpgrade(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return false;
        }

        RuneStats stats = RuneStats.get(stack);
        if (stats.isEmpty()) {
            return false;
        }

        float curseMult = GearAttributes.cursedMultiplier(stack);
        int max = stack.getMaxDamage();
        int damage = stack.getDamageValue();
        int minRemaining = (int) Math.ceil(max * 0.25D);
        int maxCost = (max - minRemaining) - damage;
        if (maxCost < 1) {
            return false;
        }

        List<RuneStatType> candidates = new ArrayList<>();
        Map<RuneStatType, Integer> allowed = new EnumMap<>(RuneStatType.class);

        for (Map.Entry<RuneStatType, Float> entry : stats.view().entrySet()) {
            RuneStatType type = entry.getKey();
            float value = entry.getValue();
            float cap = type.cap();
            if (value <= 0.0F || cap <= 0.0F) {
                continue;
            }

            float effectiveCap = cap * curseMult;
            int byCap = (int) Math.floor(effectiveCap - value + 1e-6);
            int maxIncrease = Math.min(10, Math.min(byCap, maxCost));
            if (maxIncrease >= 1) {
                candidates.add(type);
                allowed.put(type, maxIncrease);
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        RuneStatType chosen = candidates.get(RNG.nextInt(candidates.size()));
        int increase = 1 + RNG.nextInt(allowed.get(chosen));

        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.putAll(stats.view());
        map.put(chosen, map.getOrDefault(chosen, 0.0F) + increase);

        RuneStats.set(stack, new RuneStats(map));
        stack.set(DataComponents.DAMAGE, damage + increase);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        updateGlintAfter(stack);
        return true;
    }

    private static boolean applyReroll(ItemStack stack) {
        if (GearAttributes.getLevel(stack, GearAttribute.INSTABLE) >= MAX_ATTR_LEVEL) {
            return false;
        }

        RuneStats stats = RuneStats.get(stack);
        if (stats.isEmpty()) {
            return false;
        }

        List<RuneStatType> options = new ArrayList<>();
        for (Map.Entry<RuneStatType, Float> entry : stats.view().entrySet()) {
            if (entry.getValue() != 0.0F) {
                options.add(entry.getKey());
            }
        }
        if (options.isEmpty()) {
            return false;
        }

        GearAttributes.addLevel(stack, GearAttribute.INSTABLE, 1);
        RuneStatType chosen = options.get(RNG.nextInt(options.size()));
        int shift = instableShift(stack);
        float rerolled = powerAdjusted(stack, rollBaseStat(chosen, false, shift));

        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.putAll(stats.view());
        map.put(chosen, rerolled);

        RuneStats.set(stack, new RuneStats(map));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        updateGlintAfter(stack);
        return true;
    }

    private static boolean applyCursedInscription(ItemStack stack) {
        if (GearAttributes.getLevel(stack, GearAttribute.CURSED) >= MAX_ATTR_LEVEL) {
            return false;
        }

        RuneStats stats = RuneStats.get(stack);
        boolean canOvercap = false;
        if (!stats.isEmpty()) {
            for (Map.Entry<RuneStatType, Float> entry : stats.view().entrySet()) {
                if (entry.getValue() != 0.0F && entry.getKey().cap() > 0.0F) {
                    canOvercap = true;
                    break;
                }
            }
        }

        float overChance = GearAttributes.nextCurseChance(stack, 0.50F);
        if (canOvercap && RNG.nextFloat() < overChance) {
            List<RuneStatType> options = new ArrayList<>();
            for (Map.Entry<RuneStatType, Float> entry : stats.view().entrySet()) {
                if (entry.getValue() != 0.0F && entry.getKey().cap() > 0.0F) {
                    options.add(entry.getKey());
                }
            }
            if (options.isEmpty()) {
                return false;
            }

            RuneStatType chosen = options.get(RNG.nextInt(options.size()));
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(stats.view());
            map.put(chosen, powerAdjusted(stack, chosen.cap() * 1.10F));
            RuneStats.set(stack, new RuneStats(map));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            updateGlintAfter(stack);
            return true;
        }

        int before = GearAttributes.getLevel(stack, GearAttribute.CURSED);
        if (before >= MAX_ATTR_LEVEL) {
            return false;
        }

        GearAttributes.addLevel(stack, GearAttribute.CURSED, 1);
        applyCursedDelta(stack, 1);
        GearAttributes.setCursedAppliedLevel(stack, Math.min(MAX_ATTR_LEVEL, before + 1));
        return true;
    }

    private static boolean applyWild(ItemStack stack) {
        if (GearAttributes.getLevel(stack, GearAttribute.CURSED) >= MAX_ATTR_LEVEL) {
            return false;
        }

        clear(stack);
        GearAttributes.addLevel(stack, GearAttribute.CURSED, 1);
        applyCursedDelta(stack, 1);

        int cap = effectiveCapacity(stack);
        if (cap <= 0) {
            updateGlintAfter(stack);
            return true;
        }

        int shift = instableShift(stack);
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        List<RuneStatType> pool = new ArrayList<>(Arrays.asList(RuneStatType.values()));
        Collections.shuffle(pool, new java.util.Random(RNG.nextLong()));

        int applied = 0;
        for (RuneStatType type : pool) {
            if (applied >= cap) {
                break;
            }
            if (!canApplyStatTo(stack, type)) {
                continue;
            }
            map.put(type, powerAdjusted(stack, rollBaseStat(type, false, shift)));
            RuneSlots.tryConsumeSlot(stack);
            applied++;
        }

        if (!map.isEmpty()) {
            RuneStats.set(stack, new RuneStats(map));
            if (map.containsKey(RuneStatType.LEECHING_CHANCE)) {
                setLeechingSource(stack, false);
            }
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        updateGlintAfter(stack);
        return true;
    }

    private static boolean applyExtraction(ItemStack stack) {
        if (GearAttributes.getLevel(stack, GearAttribute.SEALED) >= MAX_ATTR_LEVEL) {
            return false;
        }

        boolean hasEffect = !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
        boolean hasStats = !RuneStats.get(stack).isEmpty();
        if (!hasEffect && !hasStats) {
            return false;
        }

        if (hasEffect) {
            ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            boolean skippedFirst = false;
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : current.entrySet()) {
                if (!skippedFirst && entry.getIntValue() > 0) {
                    skippedFirst = true;
                    continue;
                }
                if (entry.getIntValue() > 0) {
                    mutable.set(entry.getKey(), entry.getIntValue());
                }
            }
            ItemEnchantments out = mutable.toImmutable();
            if (out.isEmpty()) {
                stack.remove(DataComponents.ENCHANTMENTS);
            } else {
                stack.set(DataComponents.ENCHANTMENTS, out);
            }
        } else {
            RuneStats stats = RuneStats.get(stack);
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(stats.view());
            for (RuneStatType type : RuneStatType.values()) {
                if (map.remove(type) != null) {
                    if (type == RuneStatType.LEECHING_CHANCE) {
                        clearLeechingSource(stack);
                    }
                    break;
                }
            }
            RuneStats.set(stack, map.isEmpty() ? RuneStats.empty() : new RuneStats(map));
        }

        GearAttributes.addLevel(stack, GearAttribute.SEALED, 1);
        updateGlintAfter(stack);
        RuneSlots.syncUsedToContents(stack);
        return true;
    }

    private static int effectiveCapacity(ItemStack stack) {
        if (!RuneSlots.enabled()) {
            return RuneStatType.values().length;
        }
        return Math.max(0, RuneSlots.capacity(stack) - GearAttributes.getLevel(stack, GearAttribute.NEGATIVE));
    }

    private static int instableShift(ItemStack stack) {
        return Math.max(0, GearAttributes.getLevel(stack, GearAttribute.INSTABLE)) * 2;
    }

    private static float rollBaseStat(RuneStatType type, boolean etching, int instableShift) {
        float min = etching ? type.etchingMinPercent() : type.minPercent();
        float max = etching ? type.etchingMaxPercent() : type.maxPercent();
        float minAdjusted = Math.max(0.0F, min - instableShift);
        float maxAdjusted = Math.max(minAdjusted, max - instableShift);
        if (Math.abs(minAdjusted - maxAdjusted) < 0.0001F) {
            return minAdjusted;
        }
        float step = Math.max(0.1F, type.rollStep());
        float span = maxAdjusted - minAdjusted;
        int steps = Math.max(1, (int) Math.floor(span / step + 0.0001F));
        return Math.min(maxAdjusted, minAdjusted + RNG.nextInt(steps + 1) * step);
    }

    private static float powerAdjusted(ItemStack stack, float value) {
        return value * GearAttributes.cursedMultiplier(stack) * GearAttributes.ancientMultiplier(stack);
    }

    private static void applyCursedDelta(ItemStack stack, int deltaLevels) {
        if (deltaLevels <= 0) {
            return;
        }

        RuneStats stats = RuneStats.get(stack);
        if (!stats.isEmpty()) {
            float mult = (float) Math.pow(0.95D, deltaLevels);
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(stats.view());
            for (Map.Entry<RuneStatType, Float> entry : map.entrySet()) {
                if (entry.getValue() != 0.0F) {
                    map.put(entry.getKey(), entry.getValue() * mult);
                }
            }
            RuneStats.set(stack, new RuneStats(map));
        }

        GearAttributes.setCursedAppliedLevel(stack, GearAttributes.getLevel(stack, GearAttribute.CURSED));
        updateGlintAfter(stack);
    }

    private static boolean reduceMaxDurability(ItemStack stack, double fraction) {
        if (!stack.isDamageableItem()) {
            return false;
        }

        int max = stack.getMaxDamage();
        if (max <= 1) {
            return false;
        }

        int newMax = (int) Math.floor(max * (1.0D - fraction));
        if (newMax >= max) {
            newMax = max - 1;
        }
        if (newMax < 1) {
            newMax = 1;
        }
        if (newMax == max) {
            return false;
        }

        stack.set(DataComponents.MAX_DAMAGE, newMax);
        if (stack.getDamageValue() >= newMax) {
            stack.set(DataComponents.DAMAGE, newMax - 1);
        }
        return true;
    }

    private static void updateGlintAfter(ItemStack stack) {
        RuneStats stats = RuneStats.get(stack);
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (stats.isEmpty() && enchants.isEmpty()) {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    private static boolean statsEqual(RuneStats left, RuneStats right) {
        return left.view().equals(right.view());
    }

    private static CompoundTag getRootCopy(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = customData.copyTag();
        return root == null ? new CompoundTag() : root;
    }

    private static void setRoot(ItemStack stack, CompoundTag root) {
        if (root == null || root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }

    private static void setLeechingSource(ItemStack stack, boolean fromEtching) {
        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        runic.putBoolean(LEECHING_FROM_ETCHING, fromEtching);
        root.put(ROOT, runic);
        setRoot(stack, root);
    }

    private static void clearLeechingSource(ItemStack stack) {
        CompoundTag root = getRootCopy(stack);
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag runic = root.getCompound(ROOT);
        runic.remove(LEECHING_FROM_ETCHING);
        if (runic.isEmpty()) {
            root.remove(ROOT);
        } else {
            root.put(ROOT, runic);
        }
        setRoot(stack, root);
    }
}
