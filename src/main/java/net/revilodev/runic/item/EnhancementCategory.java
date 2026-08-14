package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;


public enum EnhancementCategory {
    OFFENSIVE("offensive", ChatFormatting.RED),
    DEFENSIVE("defensive", ChatFormatting.BLUE),
    ELEMENTAL("elemental", ChatFormatting.AQUA),
    UTILITY("utility", ChatFormatting.GREEN),
    FORBIDDEN("forbidden", ChatFormatting.DARK_PURPLE),
    SYNERGY("synergy", ChatFormatting.LIGHT_PURPLE);

    private final String key;
    private final ChatFormatting color;

    EnhancementCategory(String key, ChatFormatting color) {
        this.key = key;
        this.color = color;
    }

    public MutableComponent line() {
        return Component.translatable("tooltip.runic.category." + this.key).withStyle(this.color);
    }

    public static EnhancementCategory forStat(RuneStatType type) {
        return switch (type) {
            case ATTACK_DAMAGE, ATTACK_SPEED, ATTACK_RANGE, SWEEPING_RANGE, DRAW_SPEED, LEECHING_CHANCE,
                 POWER, FANGS -> OFFENSIVE;
            case RESISTANCE, FIRE_RESISTANCE, BLAST_RESISTANCE, PROJECTILE_RESISTANCE, KNOCKBACK_RESISTANCE,
                 HEALTH, TOUGHNESS, STONE, AEGIS, DURABILITY -> DEFENSIVE;
            case STUN_CHANCE, FLAME_CHANCE, FREEZING_CHANCE, BLEEDING_CHANCE, SHOCKING_CHANCE, POISON_CHANCE,
                 WITHERING_CHANCE, WEAKENING_CHANCE, NETHER_DAMAGE, UNDEAD_DAMAGE -> ELEMENTAL;
            case MOVEMENT_SPEED, MINING_SPEED, JUMP_HEIGHT, ABILITY_POWER -> UTILITY;
        };
    }


    public static EnhancementCategory forEnchantment(ResourceLocation id) {
        if (id == null) {
            return UTILITY;
        }

        if (id.getNamespace().equals(RunicMod.MOD_ID) && id.getPath().startsWith("synergy/")) {
            return SYNERGY;
        }

        String namespace = id.getNamespace();
        String path = id.getPath();

        if (path.contains("curse") || path.contains("corrupt") || path.contains("void")) {
            return FORBIDDEN;
        }

        if (namespace.equals("minecraft")) {
            return switch (path) {
                case "flame", "channeling" -> ELEMENTAL;
                case "feather_falling", "frost_walker", "mending", "respiration", "thorns" -> DEFENSIVE;
                case "breach", "density", "impaling", "looting", "multishot", "piercing", "punch", "wind_burst" -> OFFENSIVE;
                case "aqua_affinity", "depth_strider", "fortune", "infinity", "loyalty",
                     "luck_of_the_sea", "lure", "riptide", "silk_touch", "soul_speed",
                     "swift_sneak" -> UTILITY;
                default -> UTILITY;
            };
        }

        return switch (namespace + ":" + path) {
            case "aether:renewal" -> DEFENSIVE;

            case "combat_roll:acrobat", "combat_roll:longfooted", "combat_roll:multi_roll",
                 "create:potato_recovery",
                 "supplementaries:stasis" -> UTILITY;

            case "deeperdarker:discharge", "dungeons_arise:discharge", "dungeons_arise:voltaic_shot",
                 "deeperdarker:catalysis", "deeperdarker:sculk_smite", "dungeons_arise:ensnaring",
                 "dungeons_arise:purification", "twilightforest:destruction",
                 "twilightforest:fire_react" -> ELEMENTAL;

            case "expanded_combat:blocking", "twilightforest:chill_aura" -> DEFENSIVE;

            case "expanded_combat:ground_slam", "farmersdelight:backstabbing",
                 "mysticalagriculture:soul_siphoner", "simplyswords:catalysis",
                 "simplyswords:fire_react", "simplyswords:soul_siphoner" -> OFFENSIVE;

            case "create:capacity" -> UTILITY;
            case "dungeons_arise:lolths_curse" -> FORBIDDEN;

            default -> UTILITY;
        };
    }

    public static EnhancementCategory getCategory(ResourceLocation id) {
        if (id == null) return UTILITY;
        if (id.getNamespace().equals(RunicMod.MOD_ID) && id.getPath().startsWith("stat/")) {
            RuneStatType type = RuneStatType.byId(id.getPath().substring("stat/".length()));
            return type == null ? UTILITY : forStat(type);
        }
        return forEnchantment(id);
    }
}
