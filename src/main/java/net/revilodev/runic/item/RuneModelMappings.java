package net.revilodev.runic.item;

import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.stat.RuneStatType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuneModelMappings {
    private static final Map<String, Float> STAT_PREDICATES = new LinkedHashMap<>();
    private static final Map<String, Float> ENCHANT_PREDICATES = new LinkedHashMap<>();
    private static final List<ModelDef> MODEL_DEFS;

    static {
        addStat("attack_damage", 1f, "stat/attack_damage");
        addStat("attack_range", 2f, "stat/attack_range");
        addStat("attack_speed", 3f, "stat/attack_speed");
        addStat("blast_resistance", 4f, "stat/blast_resistance");
        addStat("bleeding_chance", 5f, "stat/bleeding_chance");
        addStat("bonus_chance", 6f, "stat/bonus_chance");
        addStat("draw_speed", 7f, "stat/draw_speed");
        addStat("durability", 8f, "stat/durability");
        addLegacyModel(9f, "stat/fall_reduction");
        addStat("fire_resistance", 10f, "stat/fire_resistance");
        addStat("flame_chance", 11f, "stat/flame_chance");
        addStat("freezing_chance", 12f, "stat/freezing_chance");
        addStat("health", 13f, "stat/health");
        addStat("aegis", 14f, "stat/aegis");
        addStat("jump_height", 15f, "stat/jump_height");
        addStat("knockback_resistance", 16f, "stat/knockback_resistance");
        addStat("leeching_chance", 17f, "stat/leeching_chance");
        addStat("mining_speed", 18f, "stat/mining_speed");
        addStat("movement_speed", 19f, "stat/movement_speed");
        addStat("nether_damage", 20f, "stat/nether_damage");
        addStat("poison_chance", 21f, "stat/poison_chance");
        addStat("power", 22f, "stat/power");
        addStat("projectile_resistance", 23f, "stat/projectile_resistance");
        addStat("resistance", 24f, "stat/resistance");
        addStat("shocking_chance", 25f, "stat/shocking_chance");
        addStat("stun_chance", 26f, "stat/stun_chance");
        addStat("sweeping_range", 27f, "stat/sweeping_range");
        addStat("fangs", 28f, "stat/fangs");
        addStat("toughness", 29f, "stat/toughness");
        addStat("undead_damage", 30f, "stat/undead_damage");
        addStat("stone", 31f, "stat/stone_skin");
        addStat("weakening_chance", 32f, "stat/weakening_chance");
        addStat("withering_chance", 33f, "stat/withering_chance");
        addEnchant("combat_roll:acrobat", 34f, "effect/acrobat");
        addEnchant("farmersdelight:backstabbing", 35f, "effect/backstabbing");
        addEnchant("minecraft:binding_curse", 36f, "effect/binding_curse");
        addEnchant("expanded_combat:blocking", 37f, "effect/blocking");
        addEnchant("minecraft:breach", 38f, "effect/breach");
        addEnchant("create:capacity", 39f, "effect/capacity");
        addEnchant("deeperdarker:catalysis", 40f, "effect/catalysis");
        addEnchant("minecraft:channeling", 41f, "effect/channeling");
        addEnchant("twilightforest:chill_aura", 42f, "effect/chill_aura");
        addEnchant("minecraft:density", 43f, "effect/density");
        addEnchant("twilightforest:destruction", 44f, "effect/destruction");
        addEnchant("deeperdarker:discharge", 45f, "effect/discharge");
        addEnchant("dungeons_arise:ensnaring", 46f, "effect/ensnaring");
        addEnchant("simplyswords:fire_react", 47f, "effect/fire_react");
        addEnchant("minecraft:flame", 48f, "effect/flame");
        addEnchant("minecraft:fortune", 49f, "effect/fortune");
        addEnchant("minecraft:frost_walker", 50f, "effect/frost_walker");
        addEnchant("expanded_combat:ground_slam", 51f, "effect/ground_slam");
        addEnchant("minecraft:impaling", 52f, "effect/impaling");
        addEnchant("minecraft:infinity", 53f, "effect/infinity");
        addEnchant("dungeons_arise:lolths_curse", 54f, "effect/lolths_curse");
        addEnchant("combat_roll:longfooted", 55f, "effect/longfooted");
        addEnchant("minecraft:looting", 56f, "effect/looting");
        addEnchant("minecraft:loyalty", 57f, "effect/loyalty");
        addEnchant("minecraft:luck_of_the_sea", 58f, "effect/luck_of_the_sea");
        addEnchant("minecraft:lure", 59f, "effect/lure");
        addEnchant("minecraft:mending", 60f, "effect/mending");
        addEnchant("combat_roll:multi_roll", 61f, "effect/multi_roll");
        addEnchant("mysticalagriculture:mystical_enlightenment", 62f, "effect/mystical_enlightenment");
        addEnchant("minecraft:piercing", 63f, "effect/piercing");
        addEnchant("create:potato_recovery", 64f, "effect/potato_recovery");
        addEnchant("dungeons_arise:purification", 65f, "effect/purification");
        addEnchant("aether:renewal", 66f, "effect/renewal");
        addEnchant("minecraft:respiration", 67f, "effect/respiration");
        addEnchant("minecraft:riptide", 68f, "effect/riptide");
        addEnchant("deeperdarker:sculk_smite", 69f, "effect/sculk_smite");
        addEnchant("minecraft:silk_touch", 70f, "effect/silk_touch");
        addEnchant("mysticalagriculture:soul_siphoner", 71f, "effect/soul_siphoner");
        addEnchant("minecraft:soul_speed", 72f, "effect/soul_speed");
        addEnchant("supplementaries:stasis", 73f, "effect/stasis");
        addEnchant("minecraft:swift_sneak", 74f, "effect/swift_sneak");
        addEnchant("minecraft:thorns", 75f, "effect/thorns");
        addEnchant("minecraft:vanishing_curse", 76f, "effect/vanishing_curse");
        addEnchant("dungeons_arise:voltaic_shot", 77f, "effect/voltaic_shot");
        addEnchant("minecraft:wind_burst", 78f, "effect/wind_burst");
        addEnchant("minecraft:multishot", 79f, "effect/multishot");
        addEnchant("minecraft:punch", 80f, "effect/punch");
        addEnchant("minecraft:aqua_affinity", 81f, "effect/aqua_affinity");
        addEnchant("minecraft:depth_strider", 82f, "effect/depth_strider");
        addEnchant("minecraft:feather_falling", 83f, "effect/feather_falling");
        addStat("ability_power", 84f, "stat/ability_power");

        MODEL_DEFS = List.copyOf(buildModelDefs());
    }

    private RuneModelMappings() {}

    public static float predicateForStat(RuneStatType type) {
        return type == null ? 0.0F : predicateForStatId(type.id());
    }

    public static float predicateForStatId(String statId) {
        return statId == null ? 0.0F : STAT_PREDICATES.getOrDefault(statId, 0.0F);
    }

    public static float predicateForEnchant(ResourceLocation enchantId) {
        return enchantId == null ? 0.0F : ENCHANT_PREDICATES.getOrDefault(enchantId.toString(), 0.0F);
    }

    public static List<ModelDef> modelDefs() {
        return MODEL_DEFS;
    }

    private static void addStat(String statId, float predicate, String modelPath) {
        STAT_PREDICATES.put(statId, predicate);
        buildModelDefs().add(new ModelDef(predicate, modelPath));
    }

    private static void addEnchant(String enchantId, float predicate, String modelPath) {
        ENCHANT_PREDICATES.put(enchantId, predicate);
        buildModelDefs().add(new ModelDef(predicate, modelPath));
    }

    private static void addLegacyModel(float predicate, String modelPath) {
        buildModelDefs().add(new ModelDef(predicate, modelPath));
    }

    private static List<ModelDef> buildModelDefs() {
        return ModelDefsHolder.MODEL_DEFS;
    }

    private static final class ModelDefsHolder {
        private static final List<ModelDef> MODEL_DEFS = new java.util.ArrayList<>();
    }

    public record ModelDef(float predicateValue, String subPath) {}
}
