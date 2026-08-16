package net.revilodev.runic.item;

import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.stat.RuneStatType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuneModelMappings {
    private static final Map<String, Float> STAT_PREDICATES = new LinkedHashMap<>();
    private static final Map<String, Float> ENCHANT_PREDICATES = new LinkedHashMap<>();
    private static final Map<String, Float> MYTHIC_PREDICATES = new LinkedHashMap<>();
    private static final Map<String, Float> SYNERGY_PREDICATES = new LinkedHashMap<>();
    private static final List<ModelDef> MODEL_DEFS;

    static {
        addStat("attack_damage", 1f, "stat/attack_damage");
        addStat("attack_range", 2f, "stat/attack_range");
        addStat("attack_speed", 3f, "stat/attack_speed");
        addStat("blast_resistance", 4f, "stat/blast_resistance");
        addStat("bleeding_chance", 5f, "stat/bleeding_chance");
        addStat("draw_speed", 6f, "stat/draw_speed");
        addStat("durability", 7f, "stat/durability");
        addLegacyModel(8f, "stat/fall_reduction");
        addStat("fire_resistance", 9f, "stat/fire_resistance");
        addStat("flame_chance", 10f, "stat/flame_chance");
        addStat("freezing_chance", 11f, "stat/freezing_chance");
        addStat("health", 12f, "stat/health");
        addStat("aegis", 13f, "stat/aegis");
        addStat("jump_height", 14f, "stat/jump_height");
        addStat("knockback_resistance", 15f, "stat/knockback_resistance");
        addStat("leeching_chance", 16f, "stat/leeching_chance");
        addStat("mining_speed", 17f, "stat/mining_speed");
        addStat("movement_speed", 18f, "stat/movement_speed");
        addStat("nether_damage", 19f, "stat/nether_damage");
        addStat("poison_chance", 20f, "stat/poison_chance");
        addStat("power", 21f, "stat/power");
        addStat("projectile_resistance", 22f, "stat/projectile_resistance");
        addStat("resistance", 23f, "stat/resistance");
        addStat("shocking_chance", 24f, "stat/shocking_chance");
        addStat("stun_chance", 25f, "stat/stun_chance");
        addStat("sweeping_range", 26f, "stat/sweeping_range");
        addStat("fangs", 27f, "stat/fangs");
        addStat("toughness", 28f, "stat/toughness");
        addStat("undead_damage", 29f, "stat/undead_damage");
        addStat("stone", 30f, "stat/stone_skin");
        addStat("weakening_chance", 31f, "stat/weakening_chance");
        addStat("withering_chance", 32f, "stat/withering_chance");
        addEnchant("combat_roll:acrobat", 33f, "effect/acrobat");
        addEnchant("farmersdelight:backstabbing", 34f, "effect/backstabbing");
        addEnchant("minecraft:binding_curse", 35f, "effect/binding_curse");
        addEnchant("expanded_combat:blocking", 36f, "effect/blocking");
        addEnchant("minecraft:breach", 37f, "effect/breach");
        addEnchant("create:capacity", 38f, "effect/capacity");
        addEnchant("deeperdarker:catalysis", 39f, "effect/catalysis");
        addEnchant("minecraft:channeling", 40f, "effect/channeling");
        addEnchant("twilightforest:chill_aura", 41f, "effect/chill_aura");
        addEnchant("minecraft:density", 42f, "effect/density");
        addEnchant("twilightforest:destruction", 43f, "effect/destruction");
        addEnchant("deeperdarker:discharge", 44f, "effect/discharge");
        addEnchant("dungeons_arise:ensnaring", 45f, "effect/ensnaring");
        addEnchant("simplyswords:fire_react", 46f, "effect/fire_react");
        addEnchant("minecraft:flame", 47f, "effect/flame");
        addEnchant("minecraft:fortune", 48f, "effect/fortune");
        addEnchant("minecraft:frost_walker", 49f, "effect/frost_walker");
        addEnchant("expanded_combat:ground_slam", 50f, "effect/ground_slam");
        addEnchant("minecraft:impaling", 51f, "effect/impaling");
        addEnchant("minecraft:infinity", 52f, "effect/infinity");
        addEnchant("dungeons_arise:lolths_curse", 53f, "effect/lolths_curse");
        addEnchant("combat_roll:longfooted", 54f, "effect/longfooted");
        addEnchant("minecraft:looting", 55f, "effect/looting");
        addEnchant("minecraft:loyalty", 56f, "effect/loyalty");
        addEnchant("minecraft:luck_of_the_sea", 57f, "effect/luck_of_the_sea");
        addEnchant("minecraft:lure", 58f, "effect/lure");
        addEnchant("minecraft:mending", 59f, "effect/mending");
        addEnchant("combat_roll:multi_roll", 60f, "effect/multi_roll");
        addEnchant("mysticalagriculture:mystical_enlightenment", 61f, "effect/mystical_enlightenment");
        addEnchant("minecraft:piercing", 62f, "effect/piercing");
        addEnchant("create:potato_recovery", 63f, "effect/potato_recovery");
        addEnchant("dungeons_arise:purification", 64f, "effect/purification");
        addEnchant("aether:renewal", 65f, "effect/renewal");
        addEnchant("minecraft:respiration", 66f, "effect/respiration");
        addEnchant("minecraft:riptide", 67f, "effect/riptide");
        addEnchant("deeperdarker:sculk_smite", 68f, "effect/sculk_smite");
        addEnchant("minecraft:silk_touch", 69f, "effect/silk_touch");
        addEnchant("mysticalagriculture:soul_siphoner", 70f, "effect/soul_siphoner");
        addEnchant("minecraft:soul_speed", 71f, "effect/soul_speed");
        addEnchant("supplementaries:stasis", 72f, "effect/stasis");
        addEnchant("minecraft:swift_sneak", 73f, "effect/swift_sneak");
        addEnchant("minecraft:thorns", 74f, "effect/thorns");
        addEnchant("minecraft:vanishing_curse", 75f, "effect/vanishing_curse");
        addEnchant("dungeons_arise:voltaic_shot", 76f, "effect/voltaic_shot");
        addEnchant("minecraft:wind_burst", 77f, "effect/wind_burst");
        addEnchant("minecraft:multishot", 78f, "effect/multishot");
        addEnchant("minecraft:punch", 79f, "effect/punch");
        addEnchant("minecraft:aqua_affinity", 80f, "effect/aqua_affinity");
        addEnchant("minecraft:depth_strider", 81f, "effect/depth_strider");
        addEnchant("minecraft:feather_falling", 82f, "effect/feather_falling");
        addStat("ability_power", 83f, "stat/ability_power");
        addMythic("ruin", 84f, "mythic");
        addMythic("dominion", 85f, "mythic");
        addMythic("hunger", 86f, "mythic");
        addMythic("void", 87f, "mythic");
        addMythic("ascendance", 88f, "mythic");
        addSynergy("shatter", 89f, "synergy");
        addSynergy("bloodfire", 90f, "synergy");
        addSynergy("corrosion", 91f, "synergy");
        addSynergy("fury", 92f, "synergy");
        addSynergy("juggernaut", 93f, "synergy");
        addSynergy("tempest", 94f, "synergy");
        addSynergy("reaper", 95f, "synergy");
        addSynergy("soulburn", 96f, "synergy");
        addSynergy("frostbite", 97f, "synergy");
        addSynergy("venom_burst", 98f, "synergy");
        addSynergy("berserk", 99f, "synergy");
        addSynergy("ice_burst", 100f, "synergy");

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

    public static float predicateForMythic(ResourceLocation mythicId) {
        if (mythicId == null || !mythicId.getPath().startsWith("mythic/")) return 0.0F;
        return MYTHIC_PREDICATES.getOrDefault(mythicId.getPath().substring("mythic/".length()), 0.0F);
    }

    public static float predicateForSynergy(ResourceLocation synergyId) {
        if (synergyId == null || !synergyId.getPath().startsWith("synergy/")) return 0.0F;
        return SYNERGY_PREDICATES.getOrDefault(synergyId.getPath().substring("synergy/".length()), 0.0F);
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

    private static void addMythic(String mythicId, float predicate, String modelPath) {
        MYTHIC_PREDICATES.put(mythicId, predicate);
        buildModelDefs().add(new ModelDef(predicate, modelPath));
    }

    private static void addSynergy(String synergyId, float predicate, String modelPath) {
        SYNERGY_PREDICATES.put(synergyId, predicate);
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
