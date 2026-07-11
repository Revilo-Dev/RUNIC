package net.revilodev.runic.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.runes.RunicItemTargets;
import net.revilodev.runic.runes.UniqueRuneSources;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunicStructureLootInjector extends LootModifier {
    public static final MapCodec<RunicStructureLootInjector> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.FLOAT.fieldOf("rune_chance").orElse(0.35f).forGetter(m -> m.runeChance),
                    Codec.FLOAT.fieldOf("armor_chance").orElse(0.0f).forGetter(m -> m.armorChance),
                    Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel),
                    Codec.INT.fieldOf("max_level").orElse(1).forGetter(m -> m.maxLevel)
            )).apply(inst, RunicStructureLootInjector::new));

    private final float runeChance;
    private final float armorChance;
    private final int minLevel;
    private final int maxLevel;

    public RunicStructureLootInjector(LootItemCondition[] conditions, float runeChance, float armorChance, int minLevel, int maxLevel) {
        super(conditions);
        this.runeChance = runeChance;
        this.armorChance = armorChance;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext ctx) {
        if (RunicConfig.disableRunicLoot()) {
            return generated;
        }

        if (ctx.getParamOrNull(LootContextParams.BLOCK_STATE) != null) {
            return generated;
        }

        ResourceLocation tableId = ctx.getQueriedLootTableId();
        if (tableId == null) {
            return generated;
        }

        String id = tableId.toString().toLowerCase(Locale.ROOT);
        if (!isRunicLootTable(id)) {
            return generated;
        }

        Level level = ctx.getLevel();
        RandomSource rand = ctx.getRandom();

        maybeAddRunes(generated, rand, level, id);
        maybeAddRelics(generated, rand, id);
        maybeAddLootAttribute(generated, rand);

        return generated;
    }

    private static boolean isRunicLootTable(String id) {
        if (isUniqueRuneSource(id)) {
            return true;
        }
        if (id.contains("villager") || id.contains("fishing") || id.contains("entity/") ||
                id.contains("block/") || id.contains("blocks/") || id.contains("gameplay/") || id.contains("trades/")) {
            return false;
        }
        return id.contains("chest") || id.contains("chests/") || id.contains("structures/") ||
                id.contains("dungeon") || id.contains("temple") || id.contains("ruin") ||
                id.contains("bastion") || id.contains("ancient_city") || id.contains("shipwreck") ||
                id.contains("fortress") || id.contains("stronghold") || id.contains("mineshaft");
    }

    private void maybeAddRelics(ObjectArrayList<ItemStack> generated, RandomSource rand, String tableId) {
        if (!RunicConfig.relicLootInjectionEnabled()) {
            return;
        }
        float chance = relicChanceFor(tableId);
        if (chance <= 0.0F || rand.nextFloat() > chance) {
            return;
        }

        ItemStack relic = randomRelic(rand, tableId);
        if (!relic.isEmpty()) {
            generated.add(relic);
        }
    }

    private void maybeAddRunes(ObjectArrayList<ItemStack> generated, RandomSource rand, Level level, String tableId) {
        if (rand.nextFloat() >= this.runeChance) {
            return;
        }

        int rolls = tableId.contains("bastion") || tableId.contains("ancient_city") ? 2 : 1;

        for (int i = 0; i < rolls; i++) {
            if (shouldRollMythic(tableId, rand)) {
                ItemStack mythic = randomMythicRune(rand);
                if (!mythic.isEmpty()) {
                    generated.add(mythic);
                    continue;
                }
            }

            if (isUniqueRuneSource(tableId)) {
                ItemStack unique = randomUniqueRune(level, rand, tableId);
                if (!unique.isEmpty()) {
                    generated.add(unique);
                }
                continue;
            }

            if (rand.nextFloat() < 0.25f) {
                ItemStack util = randomUtilityRune(rand);
                if (!util.isEmpty()) {
                    generated.add(util);
                }
            } else {
                if (rand.nextFloat() < 0.8f) {
                    ItemStack statRune = randomGenericStatRune(rand);
                    if (!statRune.isEmpty()) {
                        generated.add(statRune);
                    }
                } else {
                    ItemStack effectRune = randomEffectRune(level, rand);
                    if (!effectRune.isEmpty()) {
                        generated.add(effectRune);
                    }
                }
            }
        }
    }

    private static boolean isUniqueRuneSource(String tableId) {
        return isMansionOutpostOrRaider(tableId)
                || isEvoker(tableId)
                || isTrialChamber(tableId)
                || isAncientCity(tableId)
                || isNetherSource(tableId);
    }

    private static boolean isMansionOutpostOrRaider(String tableId) {
        return tableId.contains("woodland_mansion")
                || tableId.contains("pillager_outpost")
                || tableId.contains("entities/pillager")
                || tableId.contains("entities/vindicator")
                || tableId.contains("entities/ravager")
                || tableId.contains("entities/illusioner");
    }

    private static boolean isEvoker(String tableId) {
        return tableId.contains("entities/evoker");
    }

    private static boolean isTrialChamber(String tableId) {
        return tableId.contains("trial_chamber")
                || tableId.contains("trial_chambers")
                || tableId.contains("trial_spawner")
                || tableId.contains("vault");
    }

    private static boolean isAncientCity(String tableId) {
        return tableId.contains("ancient_city");
    }

    private static boolean isNetherSource(String tableId) {
        return tableId.contains("nether_bridge")
                || tableId.contains("fortress")
                || tableId.contains("bastion")
                || tableId.contains("piglin_bartering");
    }

    private static boolean isMythicSource(String tableId) {
        return tableId.contains("ancient_city")
                || tableId.contains("end_city")
                || tableId.contains("bastion")
                || tableId.contains("fortress")
                || tableId.contains("trial_chamber")
                || tableId.contains("trial_chambers")
                || tableId.contains("vault")
                || tableId.contains("deep_dark");
    }

    private static ItemStack randomUniqueRune(Level level, RandomSource rand, String tableId) {
        List<RuneChoice> pool = uniqueRunePool(tableId);
        if (pool.isEmpty()) return ItemStack.EMPTY;
        int start = rand.nextInt(pool.size());
        for (int i = 0; i < pool.size(); i++) {
            ItemStack stack = pool.get((start + i) % pool.size()).create(level, rand);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static List<RuneChoice> uniqueRunePool(String tableId) {
        List<RuneChoice> pool = new ArrayList<>();

        if (isMansionOutpostOrRaider(tableId)) {
            addRemovedEtchingChoice(pool, RuneChoice.stat(RuneStatType.LEECHING_CHANCE));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("multishot")));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("binding_curse")));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("vanishing_curse")));
            addRemovedEtchingChoice(pool, RuneChoice.stat(RuneStatType.STUN_CHANCE));
        }

        if (isEvoker(tableId)) {
            pool.add(RuneChoice.stat(RuneStatType.FANGS));
        }

        if (isTrialChamber(tableId)) {
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("wind_burst")));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("density")));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("breach")));
        }

        if (isAncientCity(tableId)) {
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("swift_sneak")));
            addRemovedEtchingChoice(pool, RuneChoice.stat(RuneStatType.LEECHING_CHANCE));
            addRemovedEtchingChoice(pool, RuneChoice.stat(RuneStatType.STUN_CHANCE));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("binding_curse")));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("vanishing_curse")));
        }

        if (isNetherSource(tableId)) {
            pool.add(RuneChoice.stat(RuneStatType.NETHER_DAMAGE));
            addRemovedEtchingChoice(pool, RuneChoice.effect(UniqueRuneSources.minecraft("soul_speed")));
            addRemovedEtchingChoice(pool, RuneChoice.stat(RuneStatType.HEALTH));
            pool.add(RuneChoice.stat(RuneStatType.FIRE_RESISTANCE));
            pool.add(RuneChoice.stat(RuneStatType.BLAST_RESISTANCE));
            pool.add(RuneChoice.stat(RuneStatType.WITHERING_CHANCE));
        }

        return pool;
    }

    private static ItemStack randomUtilityRune(RandomSource rand) {
        int roll = rand.nextInt(12);
        if (roll < 6) {
            return new ItemStack(ModItems.REPAIR_INSCRIPTION.get());
        } else if (roll < 9) {
            return new ItemStack(ModItems.EXPANSION_INSCRIPTION.get());
        } else if (roll < 11) {
            return new ItemStack(ModItems.NULLIFICATION_INSCRIPTION.get());
        } else {
            return new ItemStack(ModItems.UPGRADE_INSCRIPTION.get());
        }
    }

    private static ItemStack randomEffectRune(Level level, RandomSource rand) {
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> pool = new ArrayList<>();
        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            if (UniqueRuneSources.isSourceLockedRuneEffect(id)) {
                continue;
            }
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            reg.getHolder(key)
                    .filter(holder -> !EnchantBlacklist.isBlacklisted(holder))
                    .ifPresent(pool::add);
        }
        if (pool.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Holder<Enchantment> weighted = pickWeightedEffect(pool, rand);
        return RuneItem.createEffectRune(weighted);

    }

    private static float relicChanceFor(String tableId) {
        float weightScale = Math.max(0, RunicConfig.relicLootWeight()) / 2.0F;
        if (weightScale <= 0.0F) return 0.0F;
        if (tableId.contains("ancient_city")) return 0.12F * weightScale;
        if (tableId.contains("bastion") || tableId.contains("fortress") || tableId.contains("nether_bridge")) return 0.10F * weightScale;
        if (tableId.contains("ocean_monument") || tableId.contains("buried_treasure") || tableId.contains("underwater_ruin")) return 0.10F * weightScale;
        if (tableId.contains("end_city") || tableId.contains("stronghold") || tableId.contains("dungeon")) return 0.08F * weightScale;
        return 0.0F;
    }

    private static ItemStack randomRelic(RandomSource rand, String tableId) {
        List<ItemStack> pool = new ArrayList<>();

        if (tableId.contains("bastion") || tableId.contains("fortress") || tableId.contains("nether_bridge") || tableId.contains("end_city") || tableId.contains("dungeon")) {
            pool.add(new ItemStack(ModItems.DRAGON_HEART.get()));
            pool.add(new ItemStack(ModItems.WITHER_CHARGE.get()));
        }
        if (tableId.contains("ocean_monument") || tableId.contains("buried_treasure") || tableId.contains("underwater_ruin") || tableId.contains("shipwreck")) {
            pool.add(new ItemStack(ModItems.ELDER_GUARDIANS_EYE.get()));
        }
        if (tableId.contains("ancient_city") || tableId.contains("deep_dark") || tableId.contains("dungeon")) {
            pool.add(new ItemStack(ModItems.WARDENS_SOUL.get()));
            pool.add(new ItemStack(ModItems.WITHER_CHARGE.get()));
        }

        if (pool.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return pool.get(rand.nextInt(pool.size()));
    }

    private static ItemStack randomGenericStatRune(RandomSource rand) {
        List<WeightedStatChoice> pool = new ArrayList<>();
        for (RuneStatType type : RuneStatType.values()) {
            if (UniqueRuneSources.isSourceLockedRuneStat(type)) continue;
            if (EnchantBlacklist.isStatBlacklisted(type)) continue;
            int weight = weightForRarity(EnhancementRarities.getStat(type.id()));
            if (weight > 0) {
                pool.add(new WeightedStatChoice(type, weight));
            }
        }
        if (pool.isEmpty()) return ItemStack.EMPTY;
        return RuneItem.createStatRune(rand, pickWeightedStat(pool, rand));
    }

    private static boolean shouldRollMythic(String tableId, RandomSource rand) {
        if (!RunicConfig.mythicRunesEnabled() || !RunicConfig.mythicRuneLootEnabled()) {
            return false;
        }
        if (!isMythicSource(tableId) || lootDifficulty(tableId) < RunicConfig.mythicRuneMinLootDifficulty()) {
            return false;
        }
        int weight = Math.max(0, RunicConfig.mythicRuneLootWeight());
        return weight > 0 && rand.nextInt(24) < weight;
    }

    private static ItemStack randomMythicRune(RandomSource rand) {
        List<ResourceLocation> ids = MythicRuneRegistry.ids();
        if (ids.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return RuneItem.createMythicRune(ids.get(rand.nextInt(ids.size())));
    }

    private static void addRemovedEtchingChoice(List<RuneChoice> pool, RuneChoice choice) {
        if (RunicConfig.removedEtchingsLootEnabled()) {
            int weight = Math.max(0, RunicConfig.lootOnlyEtchingWeight());
            for (int i = 0; i < Math.max(1, weight); i++) {
                pool.add(choice);
            }
        }
    }

    private static RuneStatType pickWeightedStat(List<WeightedStatChoice> pool, RandomSource rand) {
        int total = 0;
        for (WeightedStatChoice choice : pool) {
            total += choice.weight;
        }
        if (total <= 0) {
            return pool.get(rand.nextInt(pool.size())).type;
        }
        int roll = rand.nextInt(total);
        for (WeightedStatChoice choice : pool) {
            roll -= choice.weight;
            if (roll < 0) {
                return choice.type;
            }
        }
        return pool.get(pool.size() - 1).type;
    }

    private static int lootDifficulty(String tableId) {
        if (tableId.contains("ancient_city") || tableId.contains("end_city")) return 5;
        if (tableId.contains("bastion") || tableId.contains("fortress") || tableId.contains("deep_dark")) return 4;
        if (tableId.contains("trial_chamber") || tableId.contains("trial_chambers") || tableId.contains("vault")) return 4;
        if (tableId.contains("stronghold") || tableId.contains("ocean_monument")) return 3;
        if (tableId.contains("shipwreck") || tableId.contains("dungeon")) return 2;
        return 1;
    }

    private static ItemStack effectRune(Level level, ResourceLocation id) {
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
        Holder<Enchantment> holder = reg.getHolder(key).orElse(null);
        if (holder == null || EnchantBlacklist.isBlacklisted(holder)) {
            return ItemStack.EMPTY;
        }
        return RuneItem.createEffectRune(holder);
    }

    private record RuneChoice(RuneStatType stat, ResourceLocation effect) {
        static RuneChoice stat(RuneStatType stat) {
            return new RuneChoice(stat, null);
        }

        static RuneChoice effect(ResourceLocation effect) {
            return new RuneChoice(null, effect);
        }

        ItemStack create(Level level, RandomSource rand) {
            if (stat != null) {
                if (EnchantBlacklist.isStatBlacklisted(stat)) return ItemStack.EMPTY;
                return RuneItem.createStatRune(rand, stat);
            }
            return effectRune(level, effect);
        }
    }

    private static Holder<Enchantment> pickWeightedEffect(List<Holder<Enchantment>> pool, RandomSource rand) {
        int total = 0;
        int[] weights = new int[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            int w = weightForRarity(EnhancementRarities.get(pool.get(i)));
            weights[i] = Math.max(0, w);
            total += weights[i];
        }
        if (total <= 0) {
            return pool.get(rand.nextInt(pool.size()));
        }
        int roll = rand.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return pool.get(i);
            }
        }
        return pool.get(pool.size() - 1);
    }

    private static int weightForRarity(net.revilodev.runic.loot.rarity.EnhancementRarity rarity) {
        return switch (rarity) {
            case COMMON -> Math.max(0, RunicConfig.commonRuneLootWeight());
            case UNCOMMON -> Math.max(0, RunicConfig.uncommonRuneLootWeight());
            case RARE -> Math.max(0, RunicConfig.rareRuneLootWeight());
            case EPIC -> Math.max(0, RunicConfig.epicRuneLootWeight());
            case LEGENDARY -> Math.max(0, RunicConfig.legendaryRuneLootWeight());
            case MYTHIC -> Math.max(0, RunicConfig.mythicRuneLootWeight());
            case CURSED -> Math.max(0, rarity.weight());
        };
    }

    private record WeightedStatChoice(RuneStatType type, int weight) {}

    private void maybeAddLootAttribute(ObjectArrayList<ItemStack> generated, RandomSource rand) {
        if (generated.isEmpty() || rand.nextFloat() >= this.armorChance) {
            return;
        }

        List<ItemStack> candidates = new ArrayList<>();
        for (ItemStack stack : generated) {
            if (isAttributeEligible(stack)) {
                RuneSlots.syncUsedToContents(stack);
                candidates.add(stack);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        ItemStack chosen = candidates.get(rand.nextInt(candidates.size()));
        GearAttribute attribute = switch (rand.nextInt(4)) {
            case 0 -> GearAttribute.SEALED;
            case 1 -> GearAttribute.ANCIENT;
            case 2 -> GearAttribute.BRITTLE;
            default -> GearAttribute.INSTABLE;
        };

        int min = Math.max(1, this.minLevel);
        int max = Math.max(min, this.maxLevel);
        int level = min + rand.nextInt(max - min + 1);
        GearAttributes.addLevel(chosen, attribute, level);
    }

    private static boolean isAttributeEligible(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return false;
        }
        return RunicItemTargets.isRunicGear(stack);
    }
}
