package net.revilodev.runic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.revilodev.runic.event.EnchantBlacklist;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class RunicConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue DISABLE_ALL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_RAW;
    private static final ModConfigSpec.BooleanValue DISABLE_RUNE_SLOTS;
    private static final ModConfigSpec.BooleanValue DISABLE_RUNIC_LOOT;
    private static final ModConfigSpec.BooleanValue DISABLE_ETCHING_CRAFTING;
    private static final ModConfigSpec.BooleanValue DISABLE_STAT_CAPS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_STATS_RAW;
    private static final ModConfigSpec.DoubleValue BASE_SYNERGY_CHANCE;
    private static final ModConfigSpec.DoubleValue SYNERGY_POTENTIAL_BONUS;
    private static final ModConfigSpec.IntValue MAX_SYNERGY_POTENTIAL;
    private static final ModConfigSpec.DoubleValue MAX_SYNERGY_CHANCE;
    private static final ModConfigSpec.IntValue COMMON_CORRUPTION;
    private static final ModConfigSpec.IntValue UNCOMMON_CORRUPTION;
    private static final ModConfigSpec.IntValue RARE_CORRUPTION;
    private static final ModConfigSpec.IntValue EPIC_CORRUPTION;
    private static final ModConfigSpec.IntValue LEGENDARY_CORRUPTION;
    private static final ModConfigSpec.IntValue MYTHIC_CORRUPTION;
    private static final ModConfigSpec.IntValue ETCHING_CORRUPTION;
    private static final ModConfigSpec.IntValue SUCCESSFUL_SYNERGY_CORRUPTION;
    private static final ModConfigSpec.IntValue FAILED_SYNERGY_CORRUPTION;
    private static final ModConfigSpec.IntValue FRACTURED_EXTRA_FAILURE_CORRUPTION;
    private static final ModConfigSpec.IntValue RESONANCE_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.IntValue EXHAUSTED_CORRUPTION_THRESHOLD;
    private static final ModConfigSpec.IntValue EXPANSION_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.DoubleValue EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT;
    private static final ModConfigSpec.IntValue RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION;
    private static final ModConfigSpec.DoubleValue RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT;
    private static final ModConfigSpec.BooleanValue RESTORATION_INSCRIPTION_ADDS_BRITTLE;
    private static final ModConfigSpec.IntValue NULLIFICATION_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue NULLIFICATION_INSCRIPTION_REMOVES_SLOT;
    private static final ModConfigSpec.BooleanValue NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES;
    private static final ModConfigSpec.IntValue UPGRADE_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.IntValue UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED;
    private static final ModConfigSpec.DoubleValue UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT;
    private static final ModConfigSpec.IntValue REROLL_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL;
    private static final ModConfigSpec.IntValue WILD_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES;
    private static final ModConfigSpec.IntValue CURSED_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.DoubleValue CURSED_INSCRIPTION_SUCCESS_CHANCE;
    private static final ModConfigSpec.DoubleValue CURSED_INSCRIPTION_OVERUPGRADE_PERCENT;
    private static final ModConfigSpec.BooleanValue CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE;
    private static final ModConfigSpec.IntValue EXTRACTION_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES;
    private static final ModConfigSpec.BooleanValue EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC;
    private static final ModConfigSpec.IntValue PURIFICATION_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.DoubleValue PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE;
    private static final ModConfigSpec.DoubleValue PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT;
    private static final ModConfigSpec.IntValue STABILIZATION_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue STABILIZATION_INSCRIPTION_ADDS_BRITTLE;
    private static final ModConfigSpec.IntValue TEMPERING_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.DoubleValue TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT;
    private static final ModConfigSpec.IntValue RELIC_SOCKET_INSCRIPTION_CORRUPTION;
    private static final ModConfigSpec.BooleanValue RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE;
    private static final ModConfigSpec.BooleanValue RELIC_LOOT_INJECTION_ENABLED;
    private static final ModConfigSpec.IntValue RELIC_DEFAULT_CORRUPTION;
    private static final ModConfigSpec.DoubleValue RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.IntValue RELIC_FULL_SET_REQUIRED_COUNT;
    private static final ModConfigSpec.BooleanValue RELIC_ENABLE_SET_BONUSES;
    private static final ModConfigSpec.BooleanValue RELIC_EFFECTS_REQUIRE_EQUIPPED;
    private static final ModConfigSpec.IntValue DRAGON_HEART_CORRUPTION;
    private static final ModConfigSpec.DoubleValue DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.DoubleValue DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.IntValue DRAGON_HEART_BURN_DURATION_BONUS_SECONDS;
    private static final ModConfigSpec.DoubleValue DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE;
    private static final ModConfigSpec.DoubleValue DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS;
    private static final ModConfigSpec.IntValue ELDER_GUARDIANS_EYE_CORRUPTION;
    private static final ModConfigSpec.DoubleValue ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.DoubleValue ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE;
    private static final ModConfigSpec.IntValue ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS;
    private static final ModConfigSpec.IntValue WITHER_CHARGE_CORRUPTION;
    private static final ModConfigSpec.DoubleValue WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.DoubleValue WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE;
    private static final ModConfigSpec.DoubleValue WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS;
    private static final ModConfigSpec.IntValue WARDENS_SOUL_CORRUPTION;
    private static final ModConfigSpec.DoubleValue WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.DoubleValue WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE;
    private static final ModConfigSpec.DoubleValue WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE;
    private static final ModConfigSpec.IntValue WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue WARDENS_SOUL_HIGH_HEALTH_THRESHOLD;
    private static final ModConfigSpec.BooleanValue MYTHIC_RUNES_ENABLED;
    private static final ModConfigSpec.BooleanValue MYTHIC_RUNE_LOOT_ENABLED;
    private static final ModConfigSpec.DoubleValue MYTHIC_RUNE_EXTRA_CURSE_CHANCE;
    private static final ModConfigSpec.BooleanValue MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS;
    private static final ModConfigSpec.BooleanValue MYTHIC_RUNE_CAN_BE_EXTRACTED;
    private static final ModConfigSpec.BooleanValue MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD;
    private static final ModConfigSpec.IntValue MYTHIC_RUNE_MIN_LOOT_DIFFICULTY;
    private static final ModConfigSpec.IntValue MYTHIC_RUNE_WEIGHT;
    private static final ModConfigSpec.DoubleValue STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.DoubleValue CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE;
    private static final ModConfigSpec.BooleanValue CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES;
    private static final ModConfigSpec.BooleanValue CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES;
    private static final ModConfigSpec.DoubleValue ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue HARMONIZED_SYNERGY_POWER_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT;
    private static final ModConfigSpec.DoubleValue REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT;
    private static final ModConfigSpec.BooleanValue REMOVED_ETCHINGS_LOOT_ENABLED;
    private static final ModConfigSpec.IntValue COMMON_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue UNCOMMON_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue RARE_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue EPIC_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue LEGENDARY_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue MYTHIC_RUNE_LOOT_WEIGHT;
    private static final ModConfigSpec.IntValue LOOT_ONLY_ETCHING_WEIGHT;
    private static final ModConfigSpec.IntValue RELIC_LOOT_WEIGHT;
    private static final ModConfigSpec.DoubleValue RUIN_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue RUIN_EXTRA_CORRUPTION_CHANCE;
    private static final ModConfigSpec.IntValue RUIN_EXTRA_CORRUPTION_AMOUNT;
    private static final ModConfigSpec.DoubleValue RUIN_DURABILITY_USE_INCREASE_PERCENT;
    private static final ModConfigSpec.DoubleValue DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue DOMINION_SYNERGY_POWER_BONUS_PERCENT;
    private static final ModConfigSpec.IntValue HUNGER_DURABILITY_RESTORE_ON_KILL;
    private static final ModConfigSpec.DoubleValue HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE;
    private static final ModConfigSpec.IntValue HUNGER_EXTRA_CORRUPTION_AMOUNT;
    private static final ModConfigSpec.DoubleValue VOID_LOW_HEALTH_THRESHOLD;
    private static final ModConfigSpec.DoubleValue VOID_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.IntValue VOID_COMBAT_CORRUPTION_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue VOID_COMBAT_CORRUPTION_AMOUNT;
    private static final ModConfigSpec.DoubleValue ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD;
    private static final ModConfigSpec.IntValue ASCENDANCE_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue ASCENDANCE_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue ASCENDANCE_SPEED_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue SHATTER_RADIUS;
    private static final ModConfigSpec.DoubleValue SHATTER_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.IntValue SHATTER_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue BLOODFIRE_FIRE_SECONDS;
    private static final ModConfigSpec.DoubleValue BLOODFIRE_BLEED_CHANCE;
    private static final ModConfigSpec.IntValue BLOODFIRE_BLEED_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue CORROSION_ARMOR_IGNORE_PERCENT;
    private static final ModConfigSpec.DoubleValue CORROSION_BONUS_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.IntValue EXECUTIONERS_FURY_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD;
    private static final ModConfigSpec.DoubleValue JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT;
    private static final ModConfigSpec.IntValue JUGGERNAUT_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue JUGGERNAUT_ARMOR_BONUS;
    private static final ModConfigSpec.DoubleValue JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS;
    private static final ModConfigSpec.IntValue JUGGERNAUT_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue TEMPEST_HITS_REQUIRED;
    private static final ModConfigSpec.IntValue TEMPEST_CHAIN_TARGETS;
    private static final ModConfigSpec.DoubleValue TEMPEST_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue TEMPEST_RADIUS;
    private static final ModConfigSpec.DoubleValue REAPER_HEAL_AMOUNT;
    private static final ModConfigSpec.DoubleValue REAPER_ATTACK_SPEED_BONUS_PERCENT;
    private static final ModConfigSpec.IntValue REAPER_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue REAPER_EXECUTION_HEALTH_THRESHOLD;
    private static final ModConfigSpec.DoubleValue SOULBURN_RADIUS;
    private static final ModConfigSpec.IntValue SOULBURN_WITHER_DURATION_TICKS;
    private static final ModConfigSpec.IntValue SOULBURN_WITHER_AMPLIFIER;
    private static final ModConfigSpec.IntValue SOULBURN_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue FROSTBITE_FREEZE_BONUS_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue FROSTBITE_CHILLED_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue VENOM_BURST_CHANCE;
    private static final ModConfigSpec.DoubleValue VENOM_BURST_RADIUS;
    private static final ModConfigSpec.DoubleValue VENOM_BURST_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.IntValue VENOM_BURST_POISON_DURATION_TICKS;
    private static final ModConfigSpec.IntValue BERSERK_HITS_REQUIRED;
    private static final ModConfigSpec.IntValue BERSERK_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue BERSERK_ATTACK_SPEED_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue BERSERK_MOVEMENT_SPEED_BONUS_PERCENT;
    private static final ModConfigSpec.DoubleValue ICE_PRISON_RADIUS;
    private static final ModConfigSpec.IntValue ICE_PRISON_DURATION_TICKS;
    private static final ModConfigSpec.DoubleValue ICE_PRISON_BOSS_DURATION_MULTIPLIER;
    private static final ModConfigSpec.IntValue ICE_PRISON_COOLDOWN_TICKS;

    private static final AtomicBoolean DISABLE_ALL_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_RUNE_SLOTS_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_RUNIC_LOOT_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_ETCHING_CRAFTING_CACHE = new AtomicBoolean(false);
    private static final AtomicBoolean DISABLE_STAT_CAPS_CACHE = new AtomicBoolean(false);
    private static volatile double BASE_SYNERGY_CHANCE_CACHE = 0.20D;
    private static volatile double SYNERGY_POTENTIAL_BONUS_CACHE = 0.20D;
    private static volatile int MAX_SYNERGY_POTENTIAL_CACHE = 3;
    private static volatile double MAX_SYNERGY_CHANCE_CACHE = 0.80D;
    private static volatile int COMMON_CORRUPTION_CACHE = 1;
    private static volatile int UNCOMMON_CORRUPTION_CACHE = 2;
    private static volatile int RARE_CORRUPTION_CACHE = 3;
    private static volatile int EPIC_CORRUPTION_CACHE = 4;
    private static volatile int LEGENDARY_CORRUPTION_CACHE = 5;
    private static volatile int MYTHIC_CORRUPTION_CACHE = 20;
    private static volatile int ETCHING_CORRUPTION_CACHE = 1;
    private static volatile int SUCCESSFUL_SYNERGY_CORRUPTION_CACHE = 5;
    private static volatile int FAILED_SYNERGY_CORRUPTION_CACHE = 2;
    private static volatile int FRACTURED_EXTRA_FAILURE_CORRUPTION_CACHE = 5;
    private static volatile int RESONANCE_INSCRIPTION_CORRUPTION_CACHE = 6;
    private static volatile int EXHAUSTED_CORRUPTION_THRESHOLD_CACHE = 100;
    private static volatile int EXPANSION_INSCRIPTION_CORRUPTION_CACHE = 8;
    private static volatile double EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = 10.0D;
    private static volatile int RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION_CACHE = 10;
    private static volatile double RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = 15.0D;
    private static volatile boolean RESTORATION_INSCRIPTION_ADDS_BRITTLE_CACHE = true;
    private static volatile int NULLIFICATION_INSCRIPTION_CORRUPTION_CACHE = 10;
    private static volatile boolean NULLIFICATION_INSCRIPTION_REMOVES_SLOT_CACHE = true;
    private static volatile boolean NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES_CACHE = true;
    private static volatile int UPGRADE_INSCRIPTION_CORRUPTION_CACHE = 5;
    private static volatile int UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED_CACHE = 5;
    private static volatile double UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT_CACHE = 10.0D;
    private static volatile int REROLL_INSCRIPTION_CORRUPTION_CACHE = 3;
    private static volatile boolean REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL_CACHE = true;
    private static volatile int WILD_INSCRIPTION_CORRUPTION_CACHE = 12;
    private static volatile boolean WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES_CACHE = false;
    private static volatile int CURSED_INSCRIPTION_CORRUPTION_CACHE = 10;
    private static volatile double CURSED_INSCRIPTION_SUCCESS_CHANCE_CACHE = 0.50D;
    private static volatile double CURSED_INSCRIPTION_OVERUPGRADE_PERCENT_CACHE = 25.0D;
    private static volatile boolean CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE_CACHE = true;
    private static volatile int EXTRACTION_INSCRIPTION_CORRUPTION_CACHE = 8;
    private static volatile boolean EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES_CACHE = false;
    private static volatile boolean EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC_CACHE = false;
    private static volatile int PURIFICATION_INSCRIPTION_CORRUPTION_CACHE = 10;
    private static volatile double PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE_CACHE = 0.50D;
    private static volatile double PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = 10.0D;
    private static volatile int STABILIZATION_INSCRIPTION_CORRUPTION_CACHE = 5;
    private static volatile boolean STABILIZATION_INSCRIPTION_ADDS_BRITTLE_CACHE = true;
    private static volatile int TEMPERING_INSCRIPTION_CORRUPTION_CACHE = 5;
    private static volatile double TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE = 10.0D;
    private static volatile int RELIC_SOCKET_INSCRIPTION_CORRUPTION_CACHE = 10;
    private static volatile boolean RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE_CACHE = true;
    private static volatile boolean RELIC_LOOT_INJECTION_ENABLED_CACHE = true;
    private static volatile int RELIC_DEFAULT_CORRUPTION_CACHE = 10;
    private static volatile double RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT_CACHE = 20.0D;
    private static volatile int RELIC_FULL_SET_REQUIRED_COUNT_CACHE = 4;
    private static volatile boolean RELIC_ENABLE_SET_BONUSES_CACHE = true;
    private static volatile boolean RELIC_EFFECTS_REQUIRE_EQUIPPED_CACHE = true;
    private static volatile int DRAGON_HEART_CORRUPTION_CACHE = 10;
    private static volatile double DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT_CACHE = 20.0D;
    private static volatile double DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile int DRAGON_HEART_BURN_DURATION_BONUS_SECONDS_CACHE = 2;
    private static volatile double DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT_CACHE = 25.0D;
    private static volatile double DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE_CACHE = 0.15D;
    private static volatile double DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS_CACHE = 4.0D;
    private static volatile int ELDER_GUARDIANS_EYE_CORRUPTION_CACHE = 10;
    private static volatile double ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT_CACHE = 20.0D;
    private static volatile double ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT_CACHE = 15.0D;
    private static volatile double ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE_CACHE = 0.20D;
    private static volatile int ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS_CACHE = 60;
    private static volatile int WITHER_CHARGE_CORRUPTION_CACHE = 12;
    private static volatile double WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT_CACHE = 25.0D;
    private static volatile double WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT_CACHE = 20.0D;
    private static volatile double WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE_CACHE = 0.15D;
    private static volatile double WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS_CACHE = 4.0D;
    private static volatile int WARDENS_SOUL_CORRUPTION_CACHE = 15;
    private static volatile double WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT_CACHE = 35.0D;
    private static volatile double WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE_CACHE = 0.20D;
    private static volatile double WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE_CACHE = 6.0D;
    private static volatile int WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS_CACHE = 300;
    private static volatile double WARDENS_SOUL_HIGH_HEALTH_THRESHOLD_CACHE = 100.0D;
    private static volatile boolean MYTHIC_RUNES_ENABLED_CACHE = true;
    private static volatile boolean MYTHIC_RUNE_LOOT_ENABLED_CACHE = true;
    private static volatile double MYTHIC_RUNE_EXTRA_CURSE_CHANCE_CACHE = 0.25D;
    private static volatile boolean MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS_CACHE = true;
    private static volatile boolean MYTHIC_RUNE_CAN_BE_EXTRACTED_CACHE = false;
    private static volatile boolean MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD_CACHE = false;
    private static volatile int MYTHIC_RUNE_MIN_LOOT_DIFFICULTY_CACHE = 4;
    private static volatile int MYTHIC_RUNE_WEIGHT_CACHE = 1;
    private static volatile double STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.0D;
    private static volatile double TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.05D;
    private static volatile double TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.0D;
    private static volatile double CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.10D;
    private static volatile double CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.03D;
    private static volatile double CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.20D;
    private static volatile double CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = 0.05D;
    private static volatile boolean CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES_CACHE = true;
    private static volatile boolean CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES_CACHE = true;
    private static volatile double ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE = 5.0D;
    private static volatile double HARMONIZED_SYNERGY_POWER_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT_CACHE = 10.0D;
    private static volatile double REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE = 10.0D;
    private static volatile boolean REMOVED_ETCHINGS_LOOT_ENABLED_CACHE = true;
    private static volatile int COMMON_RUNE_LOOT_WEIGHT_CACHE = 60;
    private static volatile int UNCOMMON_RUNE_LOOT_WEIGHT_CACHE = 35;
    private static volatile int RARE_RUNE_LOOT_WEIGHT_CACHE = 18;
    private static volatile int EPIC_RUNE_LOOT_WEIGHT_CACHE = 8;
    private static volatile int LEGENDARY_RUNE_LOOT_WEIGHT_CACHE = 3;
    private static volatile int MYTHIC_RUNE_LOOT_WEIGHT_CACHE = 1;
    private static volatile int LOOT_ONLY_ETCHING_WEIGHT_CACHE = 4;
    private static volatile int RELIC_LOOT_WEIGHT_CACHE = 2;
    private static volatile double RUIN_DAMAGE_BONUS_PERCENT_CACHE = 20.0D;
    private static volatile double RUIN_EXTRA_CORRUPTION_CHANCE_CACHE = 0.05D;
    private static volatile int RUIN_EXTRA_CORRUPTION_AMOUNT_CACHE = 1;
    private static volatile double RUIN_DURABILITY_USE_INCREASE_PERCENT_CACHE = 20.0D;
    private static volatile double DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double DOMINION_SYNERGY_POWER_BONUS_PERCENT_CACHE = 5.0D;
    private static volatile int HUNGER_DURABILITY_RESTORE_ON_KILL_CACHE = 2;
    private static volatile double HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE_CACHE = 0.03D;
    private static volatile int HUNGER_EXTRA_CORRUPTION_AMOUNT_CACHE = 1;
    private static volatile double VOID_LOW_HEALTH_THRESHOLD_CACHE = 0.35D;
    private static volatile double VOID_DAMAGE_BONUS_PERCENT_CACHE = 25.0D;
    private static volatile int VOID_COMBAT_CORRUPTION_INTERVAL_TICKS_CACHE = 200;
    private static volatile int VOID_COMBAT_CORRUPTION_AMOUNT_CACHE = 1;
    private static volatile double ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD_CACHE = 50.0D;
    private static volatile int ASCENDANCE_DURATION_TICKS_CACHE = 200;
    private static volatile double ASCENDANCE_DAMAGE_BONUS_PERCENT_CACHE = 15.0D;
    private static volatile double ASCENDANCE_SPEED_BONUS_PERCENT_CACHE = 10.0D;
    private static volatile double SHATTER_RADIUS_CACHE = 3.0D;
    private static volatile double SHATTER_DAMAGE_MULTIPLIER_CACHE = 0.35D;
    private static volatile int SHATTER_COOLDOWN_TICKS_CACHE = 40;
    private static volatile int BLOODFIRE_FIRE_SECONDS_CACHE = 4;
    private static volatile double BLOODFIRE_BLEED_CHANCE_CACHE = 0.35D;
    private static volatile int BLOODFIRE_BLEED_DURATION_TICKS_CACHE = 80;
    private static volatile double CORROSION_ARMOR_IGNORE_PERCENT_CACHE = 0.25D;
    private static volatile double CORROSION_BONUS_DAMAGE_MULTIPLIER_CACHE = 0.20D;
    private static volatile int EXECUTIONERS_FURY_DURATION_TICKS_CACHE = 100;
    private static volatile double EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT_CACHE = 0.15D;
    private static volatile double EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD_CACHE = 0.30D;
    private static volatile double JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT_CACHE = 0.20D;
    private static volatile int JUGGERNAUT_DURATION_TICKS_CACHE = 100;
    private static volatile double JUGGERNAUT_ARMOR_BONUS_CACHE = 4.0D;
    private static volatile double JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS_CACHE = 0.5D;
    private static volatile int JUGGERNAUT_COOLDOWN_TICKS_CACHE = 300;
    private static volatile int TEMPEST_HITS_REQUIRED_CACHE = 5;
    private static volatile int TEMPEST_CHAIN_TARGETS_CACHE = 3;
    private static volatile double TEMPEST_DAMAGE_MULTIPLIER_CACHE = 0.25D;
    private static volatile double TEMPEST_RADIUS_CACHE = 5.0D;
    private static volatile double REAPER_HEAL_AMOUNT_CACHE = 3.0D;
    private static volatile double REAPER_ATTACK_SPEED_BONUS_PERCENT_CACHE = 0.15D;
    private static volatile int REAPER_DURATION_TICKS_CACHE = 80;
    private static volatile double REAPER_EXECUTION_HEALTH_THRESHOLD_CACHE = 0.30D;
    private static volatile double SOULBURN_RADIUS_CACHE = 4.0D;
    private static volatile int SOULBURN_WITHER_DURATION_TICKS_CACHE = 100;
    private static volatile int SOULBURN_WITHER_AMPLIFIER_CACHE = 0;
    private static volatile int SOULBURN_COOLDOWN_TICKS_CACHE = 40;
    private static volatile double FROSTBITE_FREEZE_BONUS_MULTIPLIER_CACHE = 1.5D;
    private static volatile double FROSTBITE_CHILLED_DAMAGE_MULTIPLIER_CACHE = 0.15D;
    private static volatile double VENOM_BURST_CHANCE_CACHE = 0.25D;
    private static volatile double VENOM_BURST_RADIUS_CACHE = 3.5D;
    private static volatile double VENOM_BURST_DAMAGE_MULTIPLIER_CACHE = 0.20D;
    private static volatile int VENOM_BURST_POISON_DURATION_TICKS_CACHE = 80;
    private static volatile int BERSERK_HITS_REQUIRED_CACHE = 5;
    private static volatile int BERSERK_DURATION_TICKS_CACHE = 100;
    private static volatile double BERSERK_ATTACK_SPEED_BONUS_PERCENT_CACHE = 0.20D;
    private static volatile double BERSERK_MOVEMENT_SPEED_BONUS_PERCENT_CACHE = 0.10D;
    private static volatile double ICE_PRISON_RADIUS_CACHE = 3.0D;
    private static volatile int ICE_PRISON_DURATION_TICKS_CACHE = 40;
    private static volatile double ICE_PRISON_BOSS_DURATION_MULTIPLIER_CACHE = 0.25D;
    private static volatile int ICE_PRISON_COOLDOWN_TICKS_CACHE = 100;
    private static final AtomicReference<Set<ResourceLocation>> BLACKLIST_CACHE =
            new AtomicReference<>(Set.of());
    private static final AtomicReference<Set<String>> DISABLED_STATS_CACHE =
            new AtomicReference<>(Set.of());

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DISABLE_ALL = builder
                .comment("When true, all enchantments are treated as blacklisted")
                .define("enchant_blacklist.disable_all", false);

        BLACKLIST_RAW = builder
                .comment("Enchantments disabled entirely")
                .defineList(
                        "enchant_blacklist.blacklisted",
                        List.of(),
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                );

        DISABLE_RUNE_SLOTS = builder
                .comment("When true, rune slots are ignored and no longer limit applying runes or etchings")
                .define("mechanics.disable_rune_slots", false);

        DISABLE_RUNIC_LOOT = builder
                .comment("When true, RUNIC loot injection and enchanted-book stripping are disabled")
                .define("loot.disable_runic_loot", false);

        DISABLE_ETCHING_CRAFTING = builder
                .comment("When true, the etching table cannot craft etchings or inscriptions")
                .define("crafting.disable_etching_crafting", false);

        DISABLE_STAT_CAPS = builder
                .comment("When true, stat rune application is no longer clamped by stat caps")
                .define("mechanics.disable_stat_caps", false);

        DISABLED_STATS_RAW = builder
                .comment("Disabled runic stat ids")
                .defineList(
                        "enhancement_blacklist.stats",
                        List.of(),
                        o -> o instanceof String s && !s.isBlank()
                );

        BASE_SYNERGY_CHANCE = builder
                .comment("Base chance for compatible enhancements to combine into a synergy")
                .defineInRange("update_5.base_synergy_chance", 0.20D, 0.0D, 1.0D);
        SYNERGY_POTENTIAL_BONUS = builder
                .comment("Additional synergy chance per Synergy Potential level")
                .defineInRange("update_5.synergy_potential_bonus", 0.20D, 0.0D, 1.0D);
        MAX_SYNERGY_POTENTIAL = builder
                .comment("Maximum Synergy Potential level an item can store")
                .defineInRange("update_5.max_synergy_potential", 3, 0, Integer.MAX_VALUE);
        MAX_SYNERGY_CHANCE = builder
                .comment("Maximum final synergy chance")
                .defineInRange("update_5.max_synergy_chance", 0.80D, 0.0D, 1.0D);

        COMMON_CORRUPTION = builder.defineInRange("update_5.common_corruption", 1, 0, Integer.MAX_VALUE);
        UNCOMMON_CORRUPTION = builder.defineInRange("update_5.uncommon_corruption", 2, 0, Integer.MAX_VALUE);
        RARE_CORRUPTION = builder.defineInRange("update_5.rare_corruption", 3, 0, Integer.MAX_VALUE);
        EPIC_CORRUPTION = builder.defineInRange("update_5.epic_corruption", 4, 0, Integer.MAX_VALUE);
        LEGENDARY_CORRUPTION = builder.defineInRange("update_5.legendary_corruption", 5, 0, Integer.MAX_VALUE);
        MYTHIC_CORRUPTION = builder.defineInRange("update_5.mythic_corruption", 20, 0, Integer.MAX_VALUE);
        ETCHING_CORRUPTION = builder.defineInRange("update_5.etching_corruption", 1, 0, Integer.MAX_VALUE);
        SUCCESSFUL_SYNERGY_CORRUPTION = builder.defineInRange("update_5.successful_synergy_corruption", 5, 0, Integer.MAX_VALUE);
        FAILED_SYNERGY_CORRUPTION = builder.defineInRange("update_5.failed_synergy_corruption", 2, 0, Integer.MAX_VALUE);
        FRACTURED_EXTRA_FAILURE_CORRUPTION = builder.defineInRange("update_5.fractured_extra_failure_corruption", 5, 0, Integer.MAX_VALUE);
        RESONANCE_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.resonance_inscription_corruption", 6, 0, Integer.MAX_VALUE);
        EXHAUSTED_CORRUPTION_THRESHOLD = builder
                .comment("Corruption value at which an item becomes Exhausted")
                .defineInRange("update_5.exhausted_corruption_threshold", 100, 1, Integer.MAX_VALUE);
        EXPANSION_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.expansion_inscription_corruption", 8, 0, Integer.MAX_VALUE);
        EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT = builder.defineInRange("update_5.expansion_inscription_max_durability_loss_percent", 10.0D, 0.0D, 100.0D);
        RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION = builder.defineInRange("update_5.restoration_inscription_corruption_reduction", 10, 0, Integer.MAX_VALUE);
        RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT = builder.defineInRange("update_5.restoration_inscription_max_durability_loss_percent", 15.0D, 0.0D, 100.0D);
        RESTORATION_INSCRIPTION_ADDS_BRITTLE = builder.define("update_5.restoration_inscription_adds_brittle", true);
        NULLIFICATION_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.nullification_inscription_corruption", 10, 0, Integer.MAX_VALUE);
        NULLIFICATION_INSCRIPTION_REMOVES_SLOT = builder.define("update_5.nullification_inscription_removes_slot", true);
        NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES = builder.define("update_5.nullification_inscription_can_remove_synergies", true);
        UPGRADE_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.upgrade_inscription_corruption", 5, 0, Integer.MAX_VALUE);
        UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED = builder.defineInRange("update_5.upgrade_inscription_extra_corruption_if_overforged", 5, 0, Integer.MAX_VALUE);
        UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT = builder.defineInRange("update_5.upgrade_inscription_stat_increase_percent", 10.0D, 0.0D, 1000.0D);
        REROLL_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.reroll_inscription_corruption", 3, 0, Integer.MAX_VALUE);
        REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL = builder.define("update_5.reroll_inscription_add_unstable_on_higher_roll", true);
        WILD_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.wild_inscription_corruption", 12, 0, Integer.MAX_VALUE);
        WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES = builder.define("update_5.wild_inscription_can_mutate_synergies", false);
        CURSED_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.cursed_inscription_corruption", 10, 0, Integer.MAX_VALUE);
        CURSED_INSCRIPTION_SUCCESS_CHANCE = builder.defineInRange("update_5.cursed_inscription_success_chance", 0.50D, 0.0D, 1.0D);
        CURSED_INSCRIPTION_OVERUPGRADE_PERCENT = builder.defineInRange("update_5.cursed_inscription_overupgrade_percent", 25.0D, 0.0D, 1000.0D);
        CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE = builder.define("update_5.cursed_inscription_failure_adds_brittle", true);
        EXTRACTION_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.extraction_inscription_corruption", 8, 0, Integer.MAX_VALUE);
        EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES = builder.define("update_5.extraction_inscription_can_extract_synergies", false);
        EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC = builder.define("update_5.extraction_inscription_can_extract_mythic", false);
        PURIFICATION_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.purification_inscription_corruption", 10, 0, Integer.MAX_VALUE);
        PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE = builder.defineInRange("update_5.purification_inscription_durability_loss_chance", 0.50D, 0.0D, 1.0D);
        PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT = builder.defineInRange("update_5.purification_inscription_max_durability_loss_percent", 10.0D, 0.0D, 100.0D);
        STABILIZATION_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.stabilization_inscription_corruption", 5, 0, Integer.MAX_VALUE);
        STABILIZATION_INSCRIPTION_ADDS_BRITTLE = builder.define("update_5.stabilization_inscription_adds_brittle", true);
        TEMPERING_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.tempering_inscription_corruption", 5, 0, Integer.MAX_VALUE);
        TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT = builder.defineInRange("update_5.tempering_inscription_durability_loss_reduction_percent", 10.0D, 0.0D, 100.0D);
        RELIC_SOCKET_INSCRIPTION_CORRUPTION = builder.defineInRange("update_5.relic_socket_inscription_corruption", 10, 0, Integer.MAX_VALUE);
        RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE = builder.define("update_5.relic_socket_inscription_adds_brittle", true);
        RELIC_LOOT_INJECTION_ENABLED = builder.define("update_5.relic_loot_injection_enabled", true);
        RELIC_DEFAULT_CORRUPTION = builder.defineInRange("update_5.relic.default_corruption", 10, 0, Integer.MAX_VALUE);
        RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.relic.default_durability_use_increase_percent", 20.0D, 0.0D, 1000.0D);
        RELIC_FULL_SET_REQUIRED_COUNT = builder.defineInRange("update_5.relic.full_set_required_count", 4, 0, Integer.MAX_VALUE);
        RELIC_ENABLE_SET_BONUSES = builder.define("update_5.relic.enable_set_bonuses", true);
        RELIC_EFFECTS_REQUIRE_EQUIPPED = builder.define("update_5.relic.effects_require_equipped", true);
        DRAGON_HEART_CORRUPTION = builder.defineInRange("update_5.relic.dragon_heart.corruption", 10, 0, Integer.MAX_VALUE);
        DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.relic.dragon_heart.durability_use_increase_percent", 20.0D, 0.0D, 1000.0D);
        DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.relic.dragon_heart.fire_damage_bonus_percent", 10.0D, 0.0D, 1000.0D);
        DRAGON_HEART_BURN_DURATION_BONUS_SECONDS = builder.defineInRange("update_5.relic.dragon_heart.burn_duration_bonus_seconds", 2, 0, Integer.MAX_VALUE);
        DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.relic.dragon_heart.full_set_fire_damage_bonus_percent", 25.0D, 0.0D, 1000.0D);
        DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE = builder.defineInRange("update_5.relic.dragon_heart.full_set_ignite_aura_chance", 0.15D, 0.0D, 1.0D);
        DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS = builder.defineInRange("update_5.relic.dragon_heart.full_set_ignite_aura_radius", 4.0D, 0.0D, 128.0D);
        ELDER_GUARDIANS_EYE_CORRUPTION = builder.defineInRange("update_5.relic.elder_guardians_eye.corruption", 10, 0, Integer.MAX_VALUE);
        ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.relic.elder_guardians_eye.durability_use_increase_percent", 20.0D, 0.0D, 1000.0D);
        ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.relic.elder_guardians_eye.underwater_damage_bonus_percent", 15.0D, 0.0D, 1000.0D);
        ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT = builder.defineInRange("update_5.relic.elder_guardians_eye.mining_speed_bonus_percent", 10.0D, 0.0D, 1000.0D);
        ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE = builder.defineInRange("update_5.relic.elder_guardians_eye.full_set_slow_chance", 0.20D, 0.0D, 1.0D);
        ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS = builder.defineInRange("update_5.relic.elder_guardians_eye.full_set_slow_duration_ticks", 60, 0, Integer.MAX_VALUE);
        WITHER_CHARGE_CORRUPTION = builder.defineInRange("update_5.relic.wither_charge.corruption", 12, 0, Integer.MAX_VALUE);
        WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.relic.wither_charge.durability_use_increase_percent", 25.0D, 0.0D, 1000.0D);
        WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT = builder.defineInRange("update_5.relic.wither_charge.wither_duration_bonus_percent", 20.0D, 0.0D, 1000.0D);
        WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT = builder.defineInRange("update_5.relic.wither_charge.damage_to_withered_bonus_percent", 10.0D, 0.0D, 1000.0D);
        WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE = builder.defineInRange("update_5.relic.wither_charge.full_set_wither_pulse_chance", 0.15D, 0.0D, 1.0D);
        WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS = builder.defineInRange("update_5.relic.wither_charge.full_set_wither_pulse_radius", 4.0D, 0.0D, 128.0D);
        WARDENS_SOUL_CORRUPTION = builder.defineInRange("update_5.relic.wardens_soul.corruption", 15, 0, Integer.MAX_VALUE);
        WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.relic.wardens_soul.durability_use_increase_percent", 35.0D, 0.0D, 1000.0D);
        WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.relic.wardens_soul.boss_damage_bonus_percent", 10.0D, 0.0D, 1000.0D);
        WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE = builder.defineInRange("update_5.relic.wardens_soul.heavy_damage_pulse_chance", 0.20D, 0.0D, 1.0D);
        WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE = builder.defineInRange("update_5.relic.wardens_soul.full_set_sonic_pulse_damage", 6.0D, 0.0D, 1000.0D);
        WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS = builder.defineInRange("update_5.relic.wardens_soul.full_set_sonic_pulse_cooldown_ticks", 300, 0, Integer.MAX_VALUE);
        WARDENS_SOUL_HIGH_HEALTH_THRESHOLD = builder.defineInRange("update_5.relic.wardens_soul.high_health_threshold", 100.0D, 0.0D, 1000000.0D);
        MYTHIC_RUNES_ENABLED = builder.define("update_5.mythic.enabled", true);
        MYTHIC_RUNE_LOOT_ENABLED = builder.define("update_5.mythic.loot_enabled", true);
        MYTHIC_RUNE_EXTRA_CURSE_CHANCE = builder.defineInRange("update_5.mythic.extra_curse_chance", 0.25D, 0.0D, 1.0D);
        MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS = builder.define("update_5.mythic.apply_curse_on_success", true);
        MYTHIC_RUNE_CAN_BE_EXTRACTED = builder.define("update_5.mythic.can_be_extracted", false);
        MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD = builder.define("update_5.mythic.can_be_mutated_by_wild", false);
        MYTHIC_RUNE_MIN_LOOT_DIFFICULTY = builder.defineInRange("update_5.mythic.min_loot_difficulty", 4, 0, Integer.MAX_VALUE);
        MYTHIC_RUNE_WEIGHT = builder.defineInRange("update_5.mythic.weight", 1, 0, Integer.MAX_VALUE);
        STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.stable_attribute_roll_chance", 0.0D, 0.0D, 1.0D);
        TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.tainted_negative_attribute_roll_chance", 0.05D, 0.0D, 1.0D);
        TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.tainted_positive_attribute_roll_chance", 0.0D, 0.0D, 1.0D);
        CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.corrupted_negative_attribute_roll_chance", 0.10D, 0.0D, 1.0D);
        CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.corrupted_positive_attribute_roll_chance", 0.03D, 0.0D, 1.0D);
        CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.critical_negative_attribute_roll_chance", 0.20D, 0.0D, 1.0D);
        CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE = builder.defineInRange("update_5.corruption.critical_positive_attribute_roll_chance", 0.05D, 0.0D, 1.0D);
        CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES = builder.define("update_5.corruption.enable_negative_attributes", true);
        CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES = builder.define("update_5.corruption.enable_positive_attributes", true);
        ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT = builder.defineInRange("update_5.attributes.ancient_enhancement_power_bonus_percent", 5.0D, 0.0D, 1000.0D);
        HARMONIZED_SYNERGY_POWER_BONUS_PERCENT = builder.defineInRange("update_5.attributes.harmonized_synergy_power_bonus_percent", 10.0D, 0.0D, 1000.0D);
        TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT = builder.defineInRange("update_5.attributes.tempered_inscription_corruption_reduction_percent", 10.0D, 0.0D, 1000.0D);
        REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT = builder.defineInRange("update_5.attributes.reinforced_durability_loss_reduction_percent", 10.0D, 0.0D, 1000.0D);
        REMOVED_ETCHINGS_LOOT_ENABLED = builder.define("loot.removed_etchings_enabled", true);
        COMMON_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.common_rune_weight", 60, 0, Integer.MAX_VALUE);
        UNCOMMON_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.uncommon_rune_weight", 35, 0, Integer.MAX_VALUE);
        RARE_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.rare_rune_weight", 18, 0, Integer.MAX_VALUE);
        EPIC_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.epic_rune_weight", 8, 0, Integer.MAX_VALUE);
        LEGENDARY_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.legendary_rune_weight", 3, 0, Integer.MAX_VALUE);
        MYTHIC_RUNE_LOOT_WEIGHT = builder.defineInRange("loot.mythic_rune_weight", 1, 0, Integer.MAX_VALUE);
        LOOT_ONLY_ETCHING_WEIGHT = builder.defineInRange("loot.loot_only_etching_weight", 4, 0, Integer.MAX_VALUE);
        RELIC_LOOT_WEIGHT = builder.defineInRange("loot.relic_weight", 2, 0, Integer.MAX_VALUE);
        RUIN_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.mythic.ruin.damage_bonus_percent", 20.0D, 0.0D, 1000.0D);
        RUIN_EXTRA_CORRUPTION_CHANCE = builder.defineInRange("update_5.mythic.ruin.extra_corruption_chance", 0.05D, 0.0D, 1.0D);
        RUIN_EXTRA_CORRUPTION_AMOUNT = builder.defineInRange("update_5.mythic.ruin.extra_corruption_amount", 1, 0, Integer.MAX_VALUE);
        RUIN_DURABILITY_USE_INCREASE_PERCENT = builder.defineInRange("update_5.mythic.ruin.durability_use_increase_percent", 20.0D, 0.0D, 1000.0D);
        DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT = builder.defineInRange("update_5.mythic.dominion.enhancement_power_bonus_percent", 10.0D, 0.0D, 1000.0D);
        DOMINION_SYNERGY_POWER_BONUS_PERCENT = builder.defineInRange("update_5.mythic.dominion.synergy_power_bonus_percent", 5.0D, 0.0D, 1000.0D);
        HUNGER_DURABILITY_RESTORE_ON_KILL = builder.defineInRange("update_5.mythic.hunger.durability_restore_on_kill", 2, 0, Integer.MAX_VALUE);
        HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE = builder.defineInRange("update_5.mythic.hunger.extra_corruption_on_hit_chance", 0.03D, 0.0D, 1.0D);
        HUNGER_EXTRA_CORRUPTION_AMOUNT = builder.defineInRange("update_5.mythic.hunger.extra_corruption_amount", 1, 0, Integer.MAX_VALUE);
        VOID_LOW_HEALTH_THRESHOLD = builder.defineInRange("update_5.mythic.void.low_health_threshold", 0.35D, 0.0D, 1.0D);
        VOID_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.mythic.void.damage_bonus_percent", 25.0D, 0.0D, 1000.0D);
        VOID_COMBAT_CORRUPTION_INTERVAL_TICKS = builder.defineInRange("update_5.mythic.void.combat_corruption_interval_ticks", 200, 0, Integer.MAX_VALUE);
        VOID_COMBAT_CORRUPTION_AMOUNT = builder.defineInRange("update_5.mythic.void.combat_corruption_amount", 1, 0, Integer.MAX_VALUE);
        ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD = builder.defineInRange("update_5.mythic.ascendance.target_max_health_threshold", 50.0D, 0.0D, 1000000.0D);
        ASCENDANCE_DURATION_TICKS = builder.defineInRange("update_5.mythic.ascendance.duration_ticks", 200, 0, Integer.MAX_VALUE);
        ASCENDANCE_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.mythic.ascendance.damage_bonus_percent", 15.0D, 0.0D, 1000.0D);
        ASCENDANCE_SPEED_BONUS_PERCENT = builder.defineInRange("update_5.mythic.ascendance.speed_bonus_percent", 10.0D, 0.0D, 1000.0D);

        SHATTER_RADIUS = builder.defineInRange("update_5.synergy.shatter_radius", 3.0D, 0.0D, 128.0D);
        SHATTER_DAMAGE_MULTIPLIER = builder.defineInRange("update_5.synergy.shatter_damage_multiplier", 0.35D, 0.0D, 1000.0D);
        SHATTER_COOLDOWN_TICKS = builder.defineInRange("update_5.synergy.shatter_cooldown_ticks", 40, 0, Integer.MAX_VALUE);
        BLOODFIRE_FIRE_SECONDS = builder.defineInRange("update_5.synergy.bloodfire_fire_seconds", 4, 0, Integer.MAX_VALUE);
        BLOODFIRE_BLEED_CHANCE = builder.defineInRange("update_5.synergy.bloodfire_bleed_chance", 0.35D, 0.0D, 1.0D);
        BLOODFIRE_BLEED_DURATION_TICKS = builder.defineInRange("update_5.synergy.bloodfire_bleed_duration_ticks", 80, 0, Integer.MAX_VALUE);
        CORROSION_ARMOR_IGNORE_PERCENT = builder.defineInRange("update_5.synergy.corrosion_armor_ignore_percent", 0.25D, 0.0D, 1.0D);
        CORROSION_BONUS_DAMAGE_MULTIPLIER = builder.defineInRange("update_5.synergy.corrosion_bonus_damage_multiplier", 0.20D, 0.0D, 1000.0D);
        EXECUTIONERS_FURY_DURATION_TICKS = builder.defineInRange("update_5.synergy.executioners_fury_duration_ticks", 100, 0, Integer.MAX_VALUE);
        EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT = builder.defineInRange("update_5.synergy.executioners_fury_damage_bonus_percent", 0.15D, 0.0D, 1000.0D);
        EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD = builder.defineInRange("update_5.synergy.executioners_fury_execution_health_threshold", 0.30D, 0.0D, 1.0D);
        JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT = builder.defineInRange("update_5.synergy.juggernaut_damage_threshold_percent", 0.20D, 0.0D, 1.0D);
        JUGGERNAUT_DURATION_TICKS = builder.defineInRange("update_5.synergy.juggernaut_duration_ticks", 100, 0, Integer.MAX_VALUE);
        JUGGERNAUT_ARMOR_BONUS = builder.defineInRange("update_5.synergy.juggernaut_armor_bonus", 4.0D, 0.0D, 1000.0D);
        JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS = builder.defineInRange("update_5.synergy.juggernaut_knockback_resistance_bonus", 0.5D, 0.0D, 1.0D);
        JUGGERNAUT_COOLDOWN_TICKS = builder.defineInRange("update_5.synergy.juggernaut_cooldown_ticks", 300, 0, Integer.MAX_VALUE);
        TEMPEST_HITS_REQUIRED = builder.defineInRange("update_5.synergy.tempest_hits_required", 5, 0, Integer.MAX_VALUE);
        TEMPEST_CHAIN_TARGETS = builder.defineInRange("update_5.synergy.tempest_chain_targets", 3, 0, Integer.MAX_VALUE);
        TEMPEST_DAMAGE_MULTIPLIER = builder.defineInRange("update_5.synergy.tempest_damage_multiplier", 0.25D, 0.0D, 1000.0D);
        TEMPEST_RADIUS = builder.defineInRange("update_5.synergy.tempest_radius", 5.0D, 0.0D, 128.0D);
        REAPER_HEAL_AMOUNT = builder.defineInRange("update_5.synergy.reaper_heal_amount", 3.0D, 0.0D, 1000.0D);
        REAPER_ATTACK_SPEED_BONUS_PERCENT = builder.defineInRange("update_5.synergy.reaper_attack_speed_bonus_percent", 0.15D, 0.0D, 1000.0D);
        REAPER_DURATION_TICKS = builder.defineInRange("update_5.synergy.reaper_duration_ticks", 80, 0, Integer.MAX_VALUE);
        REAPER_EXECUTION_HEALTH_THRESHOLD = builder.defineInRange("update_5.synergy.reaper_execution_health_threshold", 0.30D, 0.0D, 1.0D);
        SOULBURN_RADIUS = builder.defineInRange("update_5.synergy.soulburn_radius", 4.0D, 0.0D, 128.0D);
        SOULBURN_WITHER_DURATION_TICKS = builder.defineInRange("update_5.synergy.soulburn_wither_duration_ticks", 100, 0, Integer.MAX_VALUE);
        SOULBURN_WITHER_AMPLIFIER = builder.defineInRange("update_5.synergy.soulburn_wither_amplifier", 0, 0, Integer.MAX_VALUE);
        SOULBURN_COOLDOWN_TICKS = builder.defineInRange("update_5.synergy.soulburn_cooldown_ticks", 40, 0, Integer.MAX_VALUE);
        FROSTBITE_FREEZE_BONUS_MULTIPLIER = builder.defineInRange("update_5.synergy.frostbite_freeze_bonus_multiplier", 1.5D, 0.0D, 1000.0D);
        FROSTBITE_CHILLED_DAMAGE_MULTIPLIER = builder.defineInRange("update_5.synergy.frostbite_chilled_damage_multiplier", 0.15D, 0.0D, 1000.0D);
        VENOM_BURST_CHANCE = builder.defineInRange("update_5.synergy.venom_burst_chance", 0.25D, 0.0D, 1.0D);
        VENOM_BURST_RADIUS = builder.defineInRange("update_5.synergy.venom_burst_radius", 3.5D, 0.0D, 128.0D);
        VENOM_BURST_DAMAGE_MULTIPLIER = builder.defineInRange("update_5.synergy.venom_burst_damage_multiplier", 0.20D, 0.0D, 1000.0D);
        VENOM_BURST_POISON_DURATION_TICKS = builder.defineInRange("update_5.synergy.venom_burst_poison_duration_ticks", 80, 0, Integer.MAX_VALUE);
        BERSERK_HITS_REQUIRED = builder.defineInRange("update_5.synergy.berserk_hits_required", 5, 0, Integer.MAX_VALUE);
        BERSERK_DURATION_TICKS = builder.defineInRange("update_5.synergy.berserk_duration_ticks", 100, 0, Integer.MAX_VALUE);
        BERSERK_ATTACK_SPEED_BONUS_PERCENT = builder.defineInRange("update_5.synergy.berserk_attack_speed_bonus_percent", 0.20D, 0.0D, 1000.0D);
        BERSERK_MOVEMENT_SPEED_BONUS_PERCENT = builder.defineInRange("update_5.synergy.berserk_movement_speed_bonus_percent", 0.10D, 0.0D, 1000.0D);
        ICE_PRISON_RADIUS = builder.defineInRange("update_5.synergy.ice_prison_radius", 3.0D, 0.0D, 128.0D);
        ICE_PRISON_DURATION_TICKS = builder.defineInRange("update_5.synergy.ice_prison_duration_ticks", 40, 0, Integer.MAX_VALUE);
        ICE_PRISON_BOSS_DURATION_MULTIPLIER = builder.defineInRange("update_5.synergy.ice_prison_boss_duration_multiplier", 0.25D, 0.0D, 1.0D);
        ICE_PRISON_COOLDOWN_TICKS = builder.defineInRange("update_5.synergy.ice_prison_cooldown_ticks", 100, 0, Integer.MAX_VALUE);

        SPEC = builder.build();
    }

    private RunicConfig() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RunicConfig::onConfigLoading);
        modBus.addListener(RunicConfig::onConfigReloading);
    }

    public static Set<ResourceLocation> blacklistedEnchantments() {
        return BLACKLIST_CACHE.get();
    }

    public static boolean disableAllEnchantments() {
        return DISABLE_ALL_CACHE.get();
    }

    public static boolean disableRuneSlots() {
        return DISABLE_RUNE_SLOTS_CACHE.get();
    }

    public static boolean disableRunicLoot() {
        return DISABLE_RUNIC_LOOT_CACHE.get();
    }

    public static boolean disableEtchingCrafting() {
        return DISABLE_ETCHING_CRAFTING_CACHE.get();
    }

    public static boolean disableStatCaps() {
        return DISABLE_STAT_CAPS_CACHE.get();
    }

    public static Set<String> disabledStats() {
        return DISABLED_STATS_CACHE.get();
    }

    public static boolean isStatDisabled(String statId) {
        return statId != null && DISABLED_STATS_CACHE.get().contains(statId);
    }

    public static double baseSynergyChance() { return BASE_SYNERGY_CHANCE_CACHE; }
    public static double synergyPotentialBonus() { return SYNERGY_POTENTIAL_BONUS_CACHE; }
    public static int maxSynergyPotential() { return MAX_SYNERGY_POTENTIAL_CACHE; }
    public static double maxSynergyChance() { return MAX_SYNERGY_CHANCE_CACHE; }
    public static int commonCorruption() { return COMMON_CORRUPTION_CACHE; }
    public static int uncommonCorruption() { return UNCOMMON_CORRUPTION_CACHE; }
    public static int rareCorruption() { return RARE_CORRUPTION_CACHE; }
    public static int epicCorruption() { return EPIC_CORRUPTION_CACHE; }
    public static int legendaryCorruption() { return LEGENDARY_CORRUPTION_CACHE; }
    public static int mythicCorruption() { return MYTHIC_CORRUPTION_CACHE; }
    public static int etchingCorruption() { return ETCHING_CORRUPTION_CACHE; }
    public static int successfulSynergyCorruption() { return SUCCESSFUL_SYNERGY_CORRUPTION_CACHE; }
    public static int failedSynergyCorruption() { return FAILED_SYNERGY_CORRUPTION_CACHE; }
    public static int fracturedExtraFailureCorruption() { return FRACTURED_EXTRA_FAILURE_CORRUPTION_CACHE; }
    public static int resonanceInscriptionCorruption() { return RESONANCE_INSCRIPTION_CORRUPTION_CACHE; }
    public static int exhaustedCorruptionThreshold() { return EXHAUSTED_CORRUPTION_THRESHOLD_CACHE; }
    public static int expansionInscriptionCorruption() { return EXPANSION_INSCRIPTION_CORRUPTION_CACHE; }
    public static double expansionInscriptionMaxDurabilityLossPercent() { return EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE; }
    public static int restorationInscriptionCorruptionReduction() { return RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION_CACHE; }
    public static double restorationInscriptionMaxDurabilityLossPercent() { return RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE; }
    public static boolean restorationInscriptionAddsBrittle() { return RESTORATION_INSCRIPTION_ADDS_BRITTLE_CACHE; }
    public static int nullificationInscriptionCorruption() { return NULLIFICATION_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean nullificationInscriptionRemovesSlot() { return NULLIFICATION_INSCRIPTION_REMOVES_SLOT_CACHE; }
    public static boolean nullificationInscriptionCanRemoveSynergies() { return NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES_CACHE; }
    public static int upgradeInscriptionCorruption() { return UPGRADE_INSCRIPTION_CORRUPTION_CACHE; }
    public static int upgradeInscriptionExtraCorruptionIfOverforged() { return UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED_CACHE; }
    public static double upgradeInscriptionStatIncreasePercent() { return UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT_CACHE; }
    public static int rerollInscriptionCorruption() { return REROLL_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean rerollInscriptionAddUnstableOnHigherRoll() { return REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL_CACHE; }
    public static int wildInscriptionCorruption() { return WILD_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean wildInscriptionCanMutateSynergies() { return WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES_CACHE; }
    public static int cursedInscriptionCorruption() { return CURSED_INSCRIPTION_CORRUPTION_CACHE; }
    public static double cursedInscriptionSuccessChance() { return CURSED_INSCRIPTION_SUCCESS_CHANCE_CACHE; }
    public static double cursedInscriptionOverupgradePercent() { return CURSED_INSCRIPTION_OVERUPGRADE_PERCENT_CACHE; }
    public static boolean cursedInscriptionFailureAddsBrittle() { return CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE_CACHE; }
    public static int extractionInscriptionCorruption() { return EXTRACTION_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean extractionInscriptionCanExtractSynergies() { return EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES_CACHE; }
    public static boolean extractionInscriptionCanExtractMythic() { return EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC_CACHE; }
    public static int purificationInscriptionCorruption() { return PURIFICATION_INSCRIPTION_CORRUPTION_CACHE; }
    public static double purificationInscriptionDurabilityLossChance() { return PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE_CACHE; }
    public static double purificationInscriptionMaxDurabilityLossPercent() { return PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE; }
    public static int stabilizationInscriptionCorruption() { return STABILIZATION_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean stabilizationInscriptionAddsBrittle() { return STABILIZATION_INSCRIPTION_ADDS_BRITTLE_CACHE; }
    public static int temperingInscriptionCorruption() { return TEMPERING_INSCRIPTION_CORRUPTION_CACHE; }
    public static double temperingInscriptionDurabilityLossReductionPercent() { return TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE; }
    public static int relicSocketInscriptionCorruption() { return RELIC_SOCKET_INSCRIPTION_CORRUPTION_CACHE; }
    public static boolean relicSocketInscriptionAddsBrittle() { return RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE_CACHE; }
    public static boolean relicLootInjectionEnabled() { return RELIC_LOOT_INJECTION_ENABLED_CACHE; }
    public static int relicDefaultCorruption() { return RELIC_DEFAULT_CORRUPTION_CACHE; }
    public static double relicDefaultDurabilityUseIncreasePercent() { return RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static int relicFullSetRequiredCount() { return RELIC_FULL_SET_REQUIRED_COUNT_CACHE; }
    public static boolean relicEnableSetBonuses() { return RELIC_ENABLE_SET_BONUSES_CACHE; }
    public static boolean relicEffectsRequireEquipped() { return RELIC_EFFECTS_REQUIRE_EQUIPPED_CACHE; }
    public static int dragonHeartCorruption() { return DRAGON_HEART_CORRUPTION_CACHE; }
    public static double dragonHeartDurabilityUseIncreasePercent() { return DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static double dragonHeartFireDamageBonusPercent() { return DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT_CACHE; }
    public static int dragonHeartBurnDurationBonusSeconds() { return DRAGON_HEART_BURN_DURATION_BONUS_SECONDS_CACHE; }
    public static double dragonHeartFullSetFireDamageBonusPercent() { return DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double dragonHeartFullSetIgniteAuraChance() { return DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE_CACHE; }
    public static double dragonHeartFullSetIgniteAuraRadius() { return DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS_CACHE; }
    public static int elderGuardiansEyeCorruption() { return ELDER_GUARDIANS_EYE_CORRUPTION_CACHE; }
    public static double elderGuardiansEyeDurabilityUseIncreasePercent() { return ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static double elderGuardiansEyeUnderwaterDamageBonusPercent() { return ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double elderGuardiansEyeMiningSpeedBonusPercent() { return ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT_CACHE; }
    public static double elderGuardiansEyeFullSetSlowChance() { return ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE_CACHE; }
    public static int elderGuardiansEyeFullSetSlowDurationTicks() { return ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS_CACHE; }
    public static int witherChargeCorruption() { return WITHER_CHARGE_CORRUPTION_CACHE; }
    public static double witherChargeDurabilityUseIncreasePercent() { return WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static double witherChargeWitherDurationBonusPercent() { return WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT_CACHE; }
    public static double witherChargeDamageToWitheredBonusPercent() { return WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT_CACHE; }
    public static double witherChargeFullSetWitherPulseChance() { return WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE_CACHE; }
    public static double witherChargeFullSetWitherPulseRadius() { return WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS_CACHE; }
    public static int wardensSoulCorruption() { return WARDENS_SOUL_CORRUPTION_CACHE; }
    public static double wardensSoulDurabilityUseIncreasePercent() { return WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static double wardensSoulBossDamageBonusPercent() { return WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double wardensSoulHeavyDamagePulseChance() { return WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE_CACHE; }
    public static double wardensSoulFullSetSonicPulseDamage() { return WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE_CACHE; }
    public static int wardensSoulFullSetSonicPulseCooldownTicks() { return WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS_CACHE; }
    public static double wardensSoulHighHealthThreshold() { return WARDENS_SOUL_HIGH_HEALTH_THRESHOLD_CACHE; }
    public static boolean mythicRunesEnabled() { return MYTHIC_RUNES_ENABLED_CACHE; }
    public static boolean mythicRuneLootEnabled() { return MYTHIC_RUNE_LOOT_ENABLED_CACHE; }
    public static double mythicRuneExtraCurseChance() { return MYTHIC_RUNE_EXTRA_CURSE_CHANCE_CACHE; }
    public static boolean mythicRuneApplyCurseOnSuccess() { return MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS_CACHE; }
    public static boolean mythicRuneCanBeExtracted() { return MYTHIC_RUNE_CAN_BE_EXTRACTED_CACHE; }
    public static boolean mythicRuneCanBeMutatedByWild() { return MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD_CACHE; }
    public static int mythicRuneMinLootDifficulty() { return MYTHIC_RUNE_MIN_LOOT_DIFFICULTY_CACHE; }
    public static int mythicRuneWeight() { return MYTHIC_RUNE_WEIGHT_CACHE; }
    public static double stableCorruptionAttributeRollChance() { return STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double taintedNegativeAttributeRollChance() { return TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double taintedPositiveAttributeRollChance() { return TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double corruptedNegativeAttributeRollChance() { return CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double corruptedPositiveAttributeRollChance() { return CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double criticalNegativeAttributeRollChance() { return CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static double criticalPositiveAttributeRollChance() { return CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE; }
    public static boolean corruptionEnableNegativeAttributes() { return CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES_CACHE; }
    public static boolean corruptionEnablePositiveAttributes() { return CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES_CACHE; }
    public static double ancientEnhancementPowerBonusPercent() { return ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE; }
    public static double harmonizedSynergyPowerBonusPercent() { return HARMONIZED_SYNERGY_POWER_BONUS_PERCENT_CACHE; }
    public static double temperedInscriptionCorruptionReductionPercent() { return TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT_CACHE; }
    public static double reinforcedDurabilityLossReductionPercent() { return REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE; }
    public static boolean removedEtchingsLootEnabled() { return REMOVED_ETCHINGS_LOOT_ENABLED_CACHE; }
    public static int commonRuneLootWeight() { return COMMON_RUNE_LOOT_WEIGHT_CACHE; }
    public static int uncommonRuneLootWeight() { return UNCOMMON_RUNE_LOOT_WEIGHT_CACHE; }
    public static int rareRuneLootWeight() { return RARE_RUNE_LOOT_WEIGHT_CACHE; }
    public static int epicRuneLootWeight() { return EPIC_RUNE_LOOT_WEIGHT_CACHE; }
    public static int legendaryRuneLootWeight() { return LEGENDARY_RUNE_LOOT_WEIGHT_CACHE; }
    public static int mythicRuneLootWeight() { return MYTHIC_RUNE_LOOT_WEIGHT_CACHE; }
    public static int lootOnlyEtchingWeight() { return LOOT_ONLY_ETCHING_WEIGHT_CACHE; }
    public static int relicLootWeight() { return RELIC_LOOT_WEIGHT_CACHE; }
    public static double ruinDamageBonusPercent() { return RUIN_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double ruinExtraCorruptionChance() { return RUIN_EXTRA_CORRUPTION_CHANCE_CACHE; }
    public static int ruinExtraCorruptionAmount() { return RUIN_EXTRA_CORRUPTION_AMOUNT_CACHE; }
    public static double ruinDurabilityUseIncreasePercent() { return RUIN_DURABILITY_USE_INCREASE_PERCENT_CACHE; }
    public static double dominionEnhancementPowerBonusPercent() { return DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE; }
    public static double dominionSynergyPowerBonusPercent() { return DOMINION_SYNERGY_POWER_BONUS_PERCENT_CACHE; }
    public static int hungerDurabilityRestoreOnKill() { return HUNGER_DURABILITY_RESTORE_ON_KILL_CACHE; }
    public static double hungerExtraCorruptionOnHitChance() { return HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE_CACHE; }
    public static int hungerExtraCorruptionAmount() { return HUNGER_EXTRA_CORRUPTION_AMOUNT_CACHE; }
    public static double voidLowHealthThreshold() { return VOID_LOW_HEALTH_THRESHOLD_CACHE; }
    public static double voidDamageBonusPercent() { return VOID_DAMAGE_BONUS_PERCENT_CACHE; }
    public static int voidCombatCorruptionIntervalTicks() { return VOID_COMBAT_CORRUPTION_INTERVAL_TICKS_CACHE; }
    public static int voidCombatCorruptionAmount() { return VOID_COMBAT_CORRUPTION_AMOUNT_CACHE; }
    public static double ascendanceTargetMaxHealthThreshold() { return ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD_CACHE; }
    public static int ascendanceDurationTicks() { return ASCENDANCE_DURATION_TICKS_CACHE; }
    public static double ascendanceDamageBonusPercent() { return ASCENDANCE_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double ascendanceSpeedBonusPercent() { return ASCENDANCE_SPEED_BONUS_PERCENT_CACHE; }
    public static double shatterRadius() { return SHATTER_RADIUS_CACHE; }
    public static double shatterDamageMultiplier() { return SHATTER_DAMAGE_MULTIPLIER_CACHE; }
    public static int shatterCooldownTicks() { return SHATTER_COOLDOWN_TICKS_CACHE; }
    public static int bloodfireFireSeconds() { return BLOODFIRE_FIRE_SECONDS_CACHE; }
    public static double bloodfireBleedChance() { return BLOODFIRE_BLEED_CHANCE_CACHE; }
    public static int bloodfireBleedDurationTicks() { return BLOODFIRE_BLEED_DURATION_TICKS_CACHE; }
    public static double corrosionArmorIgnorePercent() { return CORROSION_ARMOR_IGNORE_PERCENT_CACHE; }
    public static double corrosionBonusDamageMultiplier() { return CORROSION_BONUS_DAMAGE_MULTIPLIER_CACHE; }
    public static int executionersFuryDurationTicks() { return EXECUTIONERS_FURY_DURATION_TICKS_CACHE; }
    public static double executionersFuryDamageBonusPercent() { return EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT_CACHE; }
    public static double executionersFuryExecutionHealthThreshold() { return EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD_CACHE; }
    public static double juggernautDamageThresholdPercent() { return JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT_CACHE; }
    public static int juggernautDurationTicks() { return JUGGERNAUT_DURATION_TICKS_CACHE; }
    public static double juggernautArmorBonus() { return JUGGERNAUT_ARMOR_BONUS_CACHE; }
    public static double juggernautKnockbackResistanceBonus() { return JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS_CACHE; }
    public static int juggernautCooldownTicks() { return JUGGERNAUT_COOLDOWN_TICKS_CACHE; }
    public static int tempestHitsRequired() { return TEMPEST_HITS_REQUIRED_CACHE; }
    public static int tempestChainTargets() { return TEMPEST_CHAIN_TARGETS_CACHE; }
    public static double tempestDamageMultiplier() { return TEMPEST_DAMAGE_MULTIPLIER_CACHE; }
    public static double tempestRadius() { return TEMPEST_RADIUS_CACHE; }
    public static double reaperHealAmount() { return REAPER_HEAL_AMOUNT_CACHE; }
    public static double reaperAttackSpeedBonusPercent() { return REAPER_ATTACK_SPEED_BONUS_PERCENT_CACHE; }
    public static int reaperDurationTicks() { return REAPER_DURATION_TICKS_CACHE; }
    public static double reaperExecutionHealthThreshold() { return REAPER_EXECUTION_HEALTH_THRESHOLD_CACHE; }
    public static double soulburnRadius() { return SOULBURN_RADIUS_CACHE; }
    public static int soulburnWitherDurationTicks() { return SOULBURN_WITHER_DURATION_TICKS_CACHE; }
    public static int soulburnWitherAmplifier() { return SOULBURN_WITHER_AMPLIFIER_CACHE; }
    public static int soulburnCooldownTicks() { return SOULBURN_COOLDOWN_TICKS_CACHE; }
    public static double frostbiteFreezeBonusMultiplier() { return FROSTBITE_FREEZE_BONUS_MULTIPLIER_CACHE; }
    public static double frostbiteChilledDamageMultiplier() { return FROSTBITE_CHILLED_DAMAGE_MULTIPLIER_CACHE; }
    public static double venomBurstChance() { return VENOM_BURST_CHANCE_CACHE; }
    public static double venomBurstRadius() { return VENOM_BURST_RADIUS_CACHE; }
    public static double venomBurstDamageMultiplier() { return VENOM_BURST_DAMAGE_MULTIPLIER_CACHE; }
    public static int venomBurstPoisonDurationTicks() { return VENOM_BURST_POISON_DURATION_TICKS_CACHE; }
    public static int berserkHitsRequired() { return BERSERK_HITS_REQUIRED_CACHE; }
    public static int berserkDurationTicks() { return BERSERK_DURATION_TICKS_CACHE; }
    public static double berserkAttackSpeedBonusPercent() { return BERSERK_ATTACK_SPEED_BONUS_PERCENT_CACHE; }
    public static double berserkMovementSpeedBonusPercent() { return BERSERK_MOVEMENT_SPEED_BONUS_PERCENT_CACHE; }
    public static double icePrisonRadius() { return ICE_PRISON_RADIUS_CACHE; }
    public static int icePrisonDurationTicks() { return ICE_PRISON_DURATION_TICKS_CACHE; }
    public static double icePrisonBossDurationMultiplier() { return ICE_PRISON_BOSS_DURATION_MULTIPLIER_CACHE; }
    public static int icePrisonCooldownTicks() { return ICE_PRISON_COOLDOWN_TICKS_CACHE; }

    public static boolean disableConfigByName(String name) {
        if (name == null || name.isBlank()) return false;
        String key = name.trim().toLowerCase();
        switch (key) {
            case "rune_slots" -> DISABLE_RUNE_SLOTS.set(true);
            case "runic_loot" -> DISABLE_RUNIC_LOOT.set(true);
            case "etching_crafting" -> DISABLE_ETCHING_CRAFTING.set(true);
            case "stat_caps" -> DISABLE_STAT_CAPS.set(true);
            default -> {
                if (net.revilodev.runic.stat.RuneStatType.byId(key) != null) {
                    java.util.List<String> next = new java.util.ArrayList<>(DISABLED_STATS_RAW.get());
                    if (!next.contains(key)) {
                        next.add(key);
                        DISABLED_STATS_RAW.set(next);
                    }
                } else {
                    ResourceLocation rl = key.contains(":")
                            ? ResourceLocation.tryParse(key)
                            : ResourceLocation.withDefaultNamespace(key);
                    if (rl == null) return false;
                    java.util.List<String> next = new java.util.ArrayList<>(BLACKLIST_RAW.get());
                    String id = rl.toString();
                    if (!next.contains(id)) {
                        next.add(id);
                        BLACKLIST_RAW.set(next);
                    }
                }
            }
        }
        rebuildCache();
        return true;
    }

    private static void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void rebuildCache() {
        Set<ResourceLocation> parsed = BLACKLIST_RAW.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> disabledStats = DISABLED_STATS_RAW.get().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        boolean disableAll = DISABLE_ALL.get();
        boolean disableRuneSlots = DISABLE_RUNE_SLOTS.get();
        boolean disableRunicLoot = DISABLE_RUNIC_LOOT.get();
        boolean disableEtchingCrafting = DISABLE_ETCHING_CRAFTING.get();
        boolean disableStatCaps = DISABLE_STAT_CAPS.get();
        BASE_SYNERGY_CHANCE_CACHE = BASE_SYNERGY_CHANCE.get();
        SYNERGY_POTENTIAL_BONUS_CACHE = SYNERGY_POTENTIAL_BONUS.get();
        MAX_SYNERGY_POTENTIAL_CACHE = MAX_SYNERGY_POTENTIAL.get();
        MAX_SYNERGY_CHANCE_CACHE = MAX_SYNERGY_CHANCE.get();
        COMMON_CORRUPTION_CACHE = COMMON_CORRUPTION.get();
        UNCOMMON_CORRUPTION_CACHE = UNCOMMON_CORRUPTION.get();
        RARE_CORRUPTION_CACHE = RARE_CORRUPTION.get();
        EPIC_CORRUPTION_CACHE = EPIC_CORRUPTION.get();
        LEGENDARY_CORRUPTION_CACHE = LEGENDARY_CORRUPTION.get();
        MYTHIC_CORRUPTION_CACHE = MYTHIC_CORRUPTION.get();
        ETCHING_CORRUPTION_CACHE = ETCHING_CORRUPTION.get();
        SUCCESSFUL_SYNERGY_CORRUPTION_CACHE = SUCCESSFUL_SYNERGY_CORRUPTION.get();
        FAILED_SYNERGY_CORRUPTION_CACHE = FAILED_SYNERGY_CORRUPTION.get();
        FRACTURED_EXTRA_FAILURE_CORRUPTION_CACHE = FRACTURED_EXTRA_FAILURE_CORRUPTION.get();
        RESONANCE_INSCRIPTION_CORRUPTION_CACHE = RESONANCE_INSCRIPTION_CORRUPTION.get();
        EXHAUSTED_CORRUPTION_THRESHOLD_CACHE = EXHAUSTED_CORRUPTION_THRESHOLD.get();
        EXPANSION_INSCRIPTION_CORRUPTION_CACHE = EXPANSION_INSCRIPTION_CORRUPTION.get();
        EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = EXPANSION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT.get();
        RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION_CACHE = RESTORATION_INSCRIPTION_CORRUPTION_REDUCTION.get();
        RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = RESTORATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT.get();
        RESTORATION_INSCRIPTION_ADDS_BRITTLE_CACHE = RESTORATION_INSCRIPTION_ADDS_BRITTLE.get();
        NULLIFICATION_INSCRIPTION_CORRUPTION_CACHE = NULLIFICATION_INSCRIPTION_CORRUPTION.get();
        NULLIFICATION_INSCRIPTION_REMOVES_SLOT_CACHE = NULLIFICATION_INSCRIPTION_REMOVES_SLOT.get();
        NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES_CACHE = NULLIFICATION_INSCRIPTION_CAN_REMOVE_SYNERGIES.get();
        UPGRADE_INSCRIPTION_CORRUPTION_CACHE = UPGRADE_INSCRIPTION_CORRUPTION.get();
        UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED_CACHE = UPGRADE_INSCRIPTION_EXTRA_CORRUPTION_IF_OVERFORGED.get();
        UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT_CACHE = UPGRADE_INSCRIPTION_STAT_INCREASE_PERCENT.get();
        REROLL_INSCRIPTION_CORRUPTION_CACHE = REROLL_INSCRIPTION_CORRUPTION.get();
        REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL_CACHE = REROLL_INSCRIPTION_ADD_UNSTABLE_ON_HIGHER_ROLL.get();
        WILD_INSCRIPTION_CORRUPTION_CACHE = WILD_INSCRIPTION_CORRUPTION.get();
        WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES_CACHE = WILD_INSCRIPTION_CAN_MUTATE_SYNERGIES.get();
        CURSED_INSCRIPTION_CORRUPTION_CACHE = CURSED_INSCRIPTION_CORRUPTION.get();
        CURSED_INSCRIPTION_SUCCESS_CHANCE_CACHE = CURSED_INSCRIPTION_SUCCESS_CHANCE.get();
        CURSED_INSCRIPTION_OVERUPGRADE_PERCENT_CACHE = CURSED_INSCRIPTION_OVERUPGRADE_PERCENT.get();
        CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE_CACHE = CURSED_INSCRIPTION_FAILURE_ADDS_BRITTLE.get();
        EXTRACTION_INSCRIPTION_CORRUPTION_CACHE = EXTRACTION_INSCRIPTION_CORRUPTION.get();
        EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES_CACHE = EXTRACTION_INSCRIPTION_CAN_EXTRACT_SYNERGIES.get();
        EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC_CACHE = EXTRACTION_INSCRIPTION_CAN_EXTRACT_MYTHIC.get();
        PURIFICATION_INSCRIPTION_CORRUPTION_CACHE = PURIFICATION_INSCRIPTION_CORRUPTION.get();
        PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE_CACHE = PURIFICATION_INSCRIPTION_DURABILITY_LOSS_CHANCE.get();
        PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT_CACHE = PURIFICATION_INSCRIPTION_MAX_DURABILITY_LOSS_PERCENT.get();
        STABILIZATION_INSCRIPTION_CORRUPTION_CACHE = STABILIZATION_INSCRIPTION_CORRUPTION.get();
        STABILIZATION_INSCRIPTION_ADDS_BRITTLE_CACHE = STABILIZATION_INSCRIPTION_ADDS_BRITTLE.get();
        TEMPERING_INSCRIPTION_CORRUPTION_CACHE = TEMPERING_INSCRIPTION_CORRUPTION.get();
        TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE = TEMPERING_INSCRIPTION_DURABILITY_LOSS_REDUCTION_PERCENT.get();
        RELIC_SOCKET_INSCRIPTION_CORRUPTION_CACHE = RELIC_SOCKET_INSCRIPTION_CORRUPTION.get();
        RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE_CACHE = RELIC_SOCKET_INSCRIPTION_ADDS_BRITTLE.get();
        RELIC_LOOT_INJECTION_ENABLED_CACHE = RELIC_LOOT_INJECTION_ENABLED.get();
        RELIC_DEFAULT_CORRUPTION_CACHE = RELIC_DEFAULT_CORRUPTION.get();
        RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT_CACHE = RELIC_DEFAULT_DURABILITY_USE_INCREASE_PERCENT.get();
        RELIC_FULL_SET_REQUIRED_COUNT_CACHE = RELIC_FULL_SET_REQUIRED_COUNT.get();
        RELIC_ENABLE_SET_BONUSES_CACHE = RELIC_ENABLE_SET_BONUSES.get();
        RELIC_EFFECTS_REQUIRE_EQUIPPED_CACHE = RELIC_EFFECTS_REQUIRE_EQUIPPED.get();
        DRAGON_HEART_CORRUPTION_CACHE = DRAGON_HEART_CORRUPTION.get();
        DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT_CACHE = DRAGON_HEART_DURABILITY_USE_INCREASE_PERCENT.get();
        DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT_CACHE = DRAGON_HEART_FIRE_DAMAGE_BONUS_PERCENT.get();
        DRAGON_HEART_BURN_DURATION_BONUS_SECONDS_CACHE = DRAGON_HEART_BURN_DURATION_BONUS_SECONDS.get();
        DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT_CACHE = DRAGON_HEART_FULL_SET_FIRE_DAMAGE_BONUS_PERCENT.get();
        DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE_CACHE = DRAGON_HEART_FULL_SET_IGNITE_AURA_CHANCE.get();
        DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS_CACHE = DRAGON_HEART_FULL_SET_IGNITE_AURA_RADIUS.get();
        ELDER_GUARDIANS_EYE_CORRUPTION_CACHE = ELDER_GUARDIANS_EYE_CORRUPTION.get();
        ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT_CACHE = ELDER_GUARDIANS_EYE_DURABILITY_USE_INCREASE_PERCENT.get();
        ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT_CACHE = ELDER_GUARDIANS_EYE_UNDERWATER_DAMAGE_BONUS_PERCENT.get();
        ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT_CACHE = ELDER_GUARDIANS_EYE_MINING_SPEED_BONUS_PERCENT.get();
        ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE_CACHE = ELDER_GUARDIANS_EYE_FULL_SET_SLOW_CHANCE.get();
        ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS_CACHE = ELDER_GUARDIANS_EYE_FULL_SET_SLOW_DURATION_TICKS.get();
        WITHER_CHARGE_CORRUPTION_CACHE = WITHER_CHARGE_CORRUPTION.get();
        WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT_CACHE = WITHER_CHARGE_DURABILITY_USE_INCREASE_PERCENT.get();
        WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT_CACHE = WITHER_CHARGE_WITHER_DURATION_BONUS_PERCENT.get();
        WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT_CACHE = WITHER_CHARGE_DAMAGE_TO_WITHERED_BONUS_PERCENT.get();
        WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE_CACHE = WITHER_CHARGE_FULL_SET_WITHER_PULSE_CHANCE.get();
        WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS_CACHE = WITHER_CHARGE_FULL_SET_WITHER_PULSE_RADIUS.get();
        WARDENS_SOUL_CORRUPTION_CACHE = WARDENS_SOUL_CORRUPTION.get();
        WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT_CACHE = WARDENS_SOUL_DURABILITY_USE_INCREASE_PERCENT.get();
        WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT_CACHE = WARDENS_SOUL_BOSS_DAMAGE_BONUS_PERCENT.get();
        WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE_CACHE = WARDENS_SOUL_HEAVY_DAMAGE_PULSE_CHANCE.get();
        WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE_CACHE = WARDENS_SOUL_FULL_SET_SONIC_PULSE_DAMAGE.get();
        WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS_CACHE = WARDENS_SOUL_FULL_SET_SONIC_PULSE_COOLDOWN_TICKS.get();
        WARDENS_SOUL_HIGH_HEALTH_THRESHOLD_CACHE = WARDENS_SOUL_HIGH_HEALTH_THRESHOLD.get();
        MYTHIC_RUNES_ENABLED_CACHE = MYTHIC_RUNES_ENABLED.get();
        MYTHIC_RUNE_LOOT_ENABLED_CACHE = MYTHIC_RUNE_LOOT_ENABLED.get();
        MYTHIC_RUNE_EXTRA_CURSE_CHANCE_CACHE = MYTHIC_RUNE_EXTRA_CURSE_CHANCE.get();
        MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS_CACHE = MYTHIC_RUNE_APPLY_CURSE_ON_SUCCESS.get();
        MYTHIC_RUNE_CAN_BE_EXTRACTED_CACHE = MYTHIC_RUNE_CAN_BE_EXTRACTED.get();
        MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD_CACHE = MYTHIC_RUNE_CAN_BE_MUTATED_BY_WILD.get();
        MYTHIC_RUNE_MIN_LOOT_DIFFICULTY_CACHE = MYTHIC_RUNE_MIN_LOOT_DIFFICULTY.get();
        MYTHIC_RUNE_WEIGHT_CACHE = MYTHIC_RUNE_WEIGHT.get();
        STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE_CACHE = STABLE_CORRUPTION_ATTRIBUTE_ROLL_CHANCE.get();
        TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = TAINTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE.get();
        TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = TAINTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE.get();
        CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = CORRUPTED_NEGATIVE_ATTRIBUTE_ROLL_CHANCE.get();
        CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = CORRUPTED_POSITIVE_ATTRIBUTE_ROLL_CHANCE.get();
        CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = CRITICAL_NEGATIVE_ATTRIBUTE_ROLL_CHANCE.get();
        CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE_CACHE = CRITICAL_POSITIVE_ATTRIBUTE_ROLL_CHANCE.get();
        CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES_CACHE = CORRUPTION_ENABLE_NEGATIVE_ATTRIBUTES.get();
        CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES_CACHE = CORRUPTION_ENABLE_POSITIVE_ATTRIBUTES.get();
        ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE = ANCIENT_ENHANCEMENT_POWER_BONUS_PERCENT.get();
        HARMONIZED_SYNERGY_POWER_BONUS_PERCENT_CACHE = HARMONIZED_SYNERGY_POWER_BONUS_PERCENT.get();
        TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT_CACHE = TEMPERED_INSCRIPTION_CORRUPTION_REDUCTION_PERCENT.get();
        REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT_CACHE = REINFORCED_DURABILITY_LOSS_REDUCTION_PERCENT.get();
        REMOVED_ETCHINGS_LOOT_ENABLED_CACHE = REMOVED_ETCHINGS_LOOT_ENABLED.get();
        COMMON_RUNE_LOOT_WEIGHT_CACHE = COMMON_RUNE_LOOT_WEIGHT.get();
        UNCOMMON_RUNE_LOOT_WEIGHT_CACHE = UNCOMMON_RUNE_LOOT_WEIGHT.get();
        RARE_RUNE_LOOT_WEIGHT_CACHE = RARE_RUNE_LOOT_WEIGHT.get();
        EPIC_RUNE_LOOT_WEIGHT_CACHE = EPIC_RUNE_LOOT_WEIGHT.get();
        LEGENDARY_RUNE_LOOT_WEIGHT_CACHE = LEGENDARY_RUNE_LOOT_WEIGHT.get();
        MYTHIC_RUNE_LOOT_WEIGHT_CACHE = MYTHIC_RUNE_LOOT_WEIGHT.get();
        LOOT_ONLY_ETCHING_WEIGHT_CACHE = LOOT_ONLY_ETCHING_WEIGHT.get();
        RELIC_LOOT_WEIGHT_CACHE = RELIC_LOOT_WEIGHT.get();
        RUIN_DAMAGE_BONUS_PERCENT_CACHE = RUIN_DAMAGE_BONUS_PERCENT.get();
        RUIN_EXTRA_CORRUPTION_CHANCE_CACHE = RUIN_EXTRA_CORRUPTION_CHANCE.get();
        RUIN_EXTRA_CORRUPTION_AMOUNT_CACHE = RUIN_EXTRA_CORRUPTION_AMOUNT.get();
        RUIN_DURABILITY_USE_INCREASE_PERCENT_CACHE = RUIN_DURABILITY_USE_INCREASE_PERCENT.get();
        DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT_CACHE = DOMINION_ENHANCEMENT_POWER_BONUS_PERCENT.get();
        DOMINION_SYNERGY_POWER_BONUS_PERCENT_CACHE = DOMINION_SYNERGY_POWER_BONUS_PERCENT.get();
        HUNGER_DURABILITY_RESTORE_ON_KILL_CACHE = HUNGER_DURABILITY_RESTORE_ON_KILL.get();
        HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE_CACHE = HUNGER_EXTRA_CORRUPTION_ON_HIT_CHANCE.get();
        HUNGER_EXTRA_CORRUPTION_AMOUNT_CACHE = HUNGER_EXTRA_CORRUPTION_AMOUNT.get();
        VOID_LOW_HEALTH_THRESHOLD_CACHE = VOID_LOW_HEALTH_THRESHOLD.get();
        VOID_DAMAGE_BONUS_PERCENT_CACHE = VOID_DAMAGE_BONUS_PERCENT.get();
        VOID_COMBAT_CORRUPTION_INTERVAL_TICKS_CACHE = VOID_COMBAT_CORRUPTION_INTERVAL_TICKS.get();
        VOID_COMBAT_CORRUPTION_AMOUNT_CACHE = VOID_COMBAT_CORRUPTION_AMOUNT.get();
        ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD_CACHE = ASCENDANCE_TARGET_MAX_HEALTH_THRESHOLD.get();
        ASCENDANCE_DURATION_TICKS_CACHE = ASCENDANCE_DURATION_TICKS.get();
        ASCENDANCE_DAMAGE_BONUS_PERCENT_CACHE = ASCENDANCE_DAMAGE_BONUS_PERCENT.get();
        ASCENDANCE_SPEED_BONUS_PERCENT_CACHE = ASCENDANCE_SPEED_BONUS_PERCENT.get();
        SHATTER_RADIUS_CACHE = SHATTER_RADIUS.get();
        SHATTER_DAMAGE_MULTIPLIER_CACHE = SHATTER_DAMAGE_MULTIPLIER.get();
        SHATTER_COOLDOWN_TICKS_CACHE = SHATTER_COOLDOWN_TICKS.get();
        BLOODFIRE_FIRE_SECONDS_CACHE = BLOODFIRE_FIRE_SECONDS.get();
        BLOODFIRE_BLEED_CHANCE_CACHE = BLOODFIRE_BLEED_CHANCE.get();
        BLOODFIRE_BLEED_DURATION_TICKS_CACHE = BLOODFIRE_BLEED_DURATION_TICKS.get();
        CORROSION_ARMOR_IGNORE_PERCENT_CACHE = CORROSION_ARMOR_IGNORE_PERCENT.get();
        CORROSION_BONUS_DAMAGE_MULTIPLIER_CACHE = CORROSION_BONUS_DAMAGE_MULTIPLIER.get();
        EXECUTIONERS_FURY_DURATION_TICKS_CACHE = EXECUTIONERS_FURY_DURATION_TICKS.get();
        EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT_CACHE = EXECUTIONERS_FURY_DAMAGE_BONUS_PERCENT.get();
        EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD_CACHE = EXECUTIONERS_FURY_EXECUTION_HEALTH_THRESHOLD.get();
        JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT_CACHE = JUGGERNAUT_DAMAGE_THRESHOLD_PERCENT.get();
        JUGGERNAUT_DURATION_TICKS_CACHE = JUGGERNAUT_DURATION_TICKS.get();
        JUGGERNAUT_ARMOR_BONUS_CACHE = JUGGERNAUT_ARMOR_BONUS.get();
        JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS_CACHE = JUGGERNAUT_KNOCKBACK_RESISTANCE_BONUS.get();
        JUGGERNAUT_COOLDOWN_TICKS_CACHE = JUGGERNAUT_COOLDOWN_TICKS.get();
        TEMPEST_HITS_REQUIRED_CACHE = TEMPEST_HITS_REQUIRED.get();
        TEMPEST_CHAIN_TARGETS_CACHE = TEMPEST_CHAIN_TARGETS.get();
        TEMPEST_DAMAGE_MULTIPLIER_CACHE = TEMPEST_DAMAGE_MULTIPLIER.get();
        TEMPEST_RADIUS_CACHE = TEMPEST_RADIUS.get();
        REAPER_HEAL_AMOUNT_CACHE = REAPER_HEAL_AMOUNT.get();
        REAPER_ATTACK_SPEED_BONUS_PERCENT_CACHE = REAPER_ATTACK_SPEED_BONUS_PERCENT.get();
        REAPER_DURATION_TICKS_CACHE = REAPER_DURATION_TICKS.get();
        REAPER_EXECUTION_HEALTH_THRESHOLD_CACHE = REAPER_EXECUTION_HEALTH_THRESHOLD.get();
        SOULBURN_RADIUS_CACHE = SOULBURN_RADIUS.get();
        SOULBURN_WITHER_DURATION_TICKS_CACHE = SOULBURN_WITHER_DURATION_TICKS.get();
        SOULBURN_WITHER_AMPLIFIER_CACHE = SOULBURN_WITHER_AMPLIFIER.get();
        SOULBURN_COOLDOWN_TICKS_CACHE = SOULBURN_COOLDOWN_TICKS.get();
        FROSTBITE_FREEZE_BONUS_MULTIPLIER_CACHE = FROSTBITE_FREEZE_BONUS_MULTIPLIER.get();
        FROSTBITE_CHILLED_DAMAGE_MULTIPLIER_CACHE = FROSTBITE_CHILLED_DAMAGE_MULTIPLIER.get();
        VENOM_BURST_CHANCE_CACHE = VENOM_BURST_CHANCE.get();
        VENOM_BURST_RADIUS_CACHE = VENOM_BURST_RADIUS.get();
        VENOM_BURST_DAMAGE_MULTIPLIER_CACHE = VENOM_BURST_DAMAGE_MULTIPLIER.get();
        VENOM_BURST_POISON_DURATION_TICKS_CACHE = VENOM_BURST_POISON_DURATION_TICKS.get();
        BERSERK_HITS_REQUIRED_CACHE = BERSERK_HITS_REQUIRED.get();
        BERSERK_DURATION_TICKS_CACHE = BERSERK_DURATION_TICKS.get();
        BERSERK_ATTACK_SPEED_BONUS_PERCENT_CACHE = BERSERK_ATTACK_SPEED_BONUS_PERCENT.get();
        BERSERK_MOVEMENT_SPEED_BONUS_PERCENT_CACHE = BERSERK_MOVEMENT_SPEED_BONUS_PERCENT.get();
        ICE_PRISON_RADIUS_CACHE = ICE_PRISON_RADIUS.get();
        ICE_PRISON_DURATION_TICKS_CACHE = ICE_PRISON_DURATION_TICKS.get();
        ICE_PRISON_BOSS_DURATION_MULTIPLIER_CACHE = ICE_PRISON_BOSS_DURATION_MULTIPLIER.get();
        ICE_PRISON_COOLDOWN_TICKS_CACHE = ICE_PRISON_COOLDOWN_TICKS.get();
        BLACKLIST_CACHE.set(parsed);
        DISABLED_STATS_CACHE.set(disabledStats);
        DISABLE_ALL_CACHE.set(disableAll);
        DISABLE_RUNE_SLOTS_CACHE.set(disableRuneSlots);
        DISABLE_RUNIC_LOOT_CACHE.set(disableRunicLoot);
        DISABLE_ETCHING_CRAFTING_CACHE.set(disableEtchingCrafting);
        DISABLE_STAT_CAPS_CACHE.set(disableStatCaps);

        EnchantBlacklist.setConfigDisabled(parsed);
        EnchantBlacklist.setConfigDisabledStats(disabledStats);
        EnchantBlacklist.setDisableAll(disableAll);
    }
}
