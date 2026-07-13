package net.revilodev.runic.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.item.RuneModelMappings;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.RunicStructureLootInjector;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.synergy.SynergyRegistry;
import net.revilodev.runic.stat.RuneStatType;

import java.util.EnumMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RunicCommands {
    private static final SimpleCommandExceptionType INVALID_TARGET =
            new SimpleCommandExceptionType(Component.literal("Target must be a living entity holding an item."));
    private static final SimpleCommandExceptionType UNKNOWN_STAT =
            new SimpleCommandExceptionType(Component.literal("Unknown runic stat."));
    private static final SimpleCommandExceptionType UNKNOWN_EFFECT =
            new SimpleCommandExceptionType(Component.literal("Unknown runic effect enchantment."));
    private static final SimpleCommandExceptionType UNKNOWN_INSCRIPTION =
            new SimpleCommandExceptionType(Component.literal("Unknown inscription."));
    private static final SimpleCommandExceptionType UNKNOWN_SYNERGY =
            new SimpleCommandExceptionType(Component.literal("Unknown synergy."));
    private static final SimpleCommandExceptionType UNKNOWN_CONFIG_TARGET =
            new SimpleCommandExceptionType(Component.literal("Unknown runic config target."));

    private RunicCommands() {}

    public static void register(RegisterCommandsEvent event) {
        var apply = Commands.literal("apply")
                .then(Commands.literal("stat")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (RuneStatType type : RuneStatType.values()) {
                                        builder.suggest(type.id());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> applyStat(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "name"),
                                                        FloatArgumentType.getFloat(context, "amount"),
                                                        EntityArgument.getEntities(context, "targets")
                                                ))))))
                .then(Commands.literal("effect")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (String effectId : RuneModelMappings.modelDefs().stream()
                                            .map(RuneModelMappings.ModelDef::subPath)
                                            .filter(path -> path.startsWith("effect/"))
                                            .map(path -> path.substring("effect/".length()))
                                            .toList()) {
                                        builder.suggest(effectId);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> applyEffect(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "name"),
                                                        IntegerArgumentType.getInteger(context, "level"),
                                                        EntityArgument.getEntities(context, "targets")
                                                ))))))
                .then(Commands.literal("synergy")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (ResourceLocation id : SynergyRegistry.ids()) {
                                        builder.suggest(id.getPath().substring("synergy/".length()));
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> applySynergy(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "name"),
                                                EntityArgument.getEntities(context, "targets")
                                        )))));

        event.getDispatcher().register(
                Commands.literal("runic")
                        .requires(source -> source.hasPermission(2))
                        .then(apply)
                        .then(Commands.literal("clear")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> clear(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets")
                                        ))))
                        .then(Commands.literal("inscribe")
                                .then(Commands.argument("inscription", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (String id : RunicCommandHelper.inscriptionIds()) {
                                                builder.suggest(id);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> inscribe(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "inscription"),
                                                        EntityArgument.getEntities(context, "targets")
                                                )))))
                        .then(Commands.literal("config")
                                .then(Commands.literal("disable")
                                        .then(Commands.argument("name", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    for (String id : configDisableSuggestions()) {
                                                        builder.suggest(id);
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> disableConfig(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "name")
                                                )))))
                        .then(Commands.literal("test")
                                .executes(context -> runicTestHelp(context.getSource()))
                                .then(Commands.literal("droprate")
                                        .executes(context -> dropRate(context.getSource(), null))
                                        .then(Commands.argument("structure", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    for (String id : structureSuggestions()) {
                                                        builder.suggest(id);
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> dropRate(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "structure")
                                                )))))
        );
    }

    private static int applyStat(CommandSourceStack source, String statName, float amount, Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        RuneStatType type = RuneStatType.byId(statName);
        if (type == null) {
            throw UNKNOWN_STAT.create();
        }

        int changed = 0;
        for (Entity entity : targets) {
            ItemStack stack = requireHeldItem(entity);
            if (RunicCommandHelper.applyStat(stack, type, amount)) {
                changed++;
            }
        }

        sendSummary(source, "Applied stat " + type.id() + " (" + amount + ") to", changed, targets.size());
        return changed;
    }

    private static int applyEffect(CommandSourceStack source, String effectName, int level, Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        Holder<Enchantment> enchantment = resolveEffect(source, effectName);

        int changed = 0;
        for (Entity entity : targets) {
            ItemStack stack = requireHeldItem(entity);
            if (RunicCommandHelper.applyEffect(stack, enchantment, level)) {
                changed++;
            }
        }

        sendSummary(source, "Applied effect " + effectName + " (" + level + ") to", changed, targets.size());
        return changed;
    }

    private static int applySynergy(CommandSourceStack source, String synergyName, Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        ResourceLocation synergyId = resolveSynergy(synergyName);

        int changed = 0;
        for (Entity entity : targets) {
            ItemStack stack = requireHeldItem(entity);
            if (RunicCommandHelper.applySynergy(stack, synergyId)) {
                changed++;
            }
        }

        sendSummary(source, "Applied synergy " + synergyName + " to", changed, targets.size());
        return changed;
    }

    private static int clear(CommandSourceStack source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        int changed = 0;
        for (Entity entity : targets) {
            ItemStack stack = requireHeldItem(entity);
            if (RunicCommandHelper.clear(stack)) {
                changed++;
            }
        }

        sendSummary(source, "Cleared runic data on", changed, targets.size());
        return changed;
    }

    private static int inscribe(CommandSourceStack source, String inscription, Collection<? extends Entity> targets)
            throws CommandSyntaxException {
        if (!RunicCommandHelper.inscriptionIds().contains(normalizeInscription(inscription))) {
            throw UNKNOWN_INSCRIPTION.create();
        }

        int changed = 0;
        for (Entity entity : targets) {
            ItemStack stack = requireHeldItem(entity);
            if (RunicCommandHelper.inscribe(stack, inscription)) {
                changed++;
            }
        }

        sendSummary(source, "Applied inscription " + inscription + " to", changed, targets.size());
        return changed;
    }

    private static int disableConfig(CommandSourceStack source, String name) throws CommandSyntaxException {
        if (!RunicConfig.disableConfigByName(name)) {
            throw UNKNOWN_CONFIG_TARGET.create();
        }
        source.sendSuccess(() -> Component.literal("Disabled runic config target: " + name.toLowerCase(Locale.ROOT)), true);
        return 1;
    }

    private static int runicTestHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Runic test commands:"), false);
        source.sendSuccess(() -> Component.literal("/runic test droprate"), false);
        source.sendSuccess(() -> Component.literal("/runic test droprate <structure>"), false);
        return 1;
    }

    private static int dropRate(CommandSourceStack source, String structure) {
        String table = structure == null || structure.isBlank()
                ? "base"
                : structure.trim().toLowerCase(Locale.ROOT);
        boolean specific = !"base".equals(table);
        int rolls = specific && (table.contains("bastion") || table.contains("ancient_city")) ? 2 : 1;
        double runeChance = RunicStructureLootInjector.DEFAULT_RUNE_CHANCE;
        double mythicPerRoll = mythicChancePerRoll(table);
        double genericFactor = runeChance * rolls * Math.max(0.0D, 1.0D - mythicPerRoll) * 0.75D;
        Map<EnhancementRarity, Double> distribution = combinedGenericDistribution(source);

        source.sendSuccess(() -> Component.literal("Runic droprate report: " + table), false);
        source.sendSuccess(() -> Component.literal("Rune roll chance: " + percent(runeChance) + " per chest; rolls: " + rolls), false);
        for (EnhancementRarity rarity : List.of(EnhancementRarity.COMMON, EnhancementRarity.UNCOMMON, EnhancementRarity.RARE,
                EnhancementRarity.EPIC, EnhancementRarity.LEGENDARY, EnhancementRarity.CURSED)) {
            double chance = genericFactor * distribution.getOrDefault(rarity, 0.0D);
            source.sendSuccess(() -> Component.literal(" - " + rarity.key() + ": " + percent(chance)), false);
        }
        if (mythicPerRoll > 0.0D) {
            double expected = runeChance * rolls * mythicPerRoll;
            source.sendSuccess(() -> Component.literal(" - mythic: " + percent(expected)), false);
        }
        if (specific) {
            List<String> unique = RunicStructureLootInjector.uniqueRuneNames(table);
            source.sendSuccess(() -> Component.literal("Unique pool: " + (unique.isEmpty() ? "none" : String.join(", ", unique))), false);
            source.sendSuccess(() -> Component.literal("Excluded from generic pool: " + String.join(", ", RunicStructureLootInjector.excludedRuneNames())), false);
        }
        source.sendSuccess(() -> Component.literal("Relics: 0% in all chests; boss drops only."), false);
        return 1;
    }

    private static Map<EnhancementRarity, Double> combinedGenericDistribution(CommandSourceStack source) {
        EnumMap<EnhancementRarity, Double> out = new EnumMap<>(EnhancementRarity.class);
        Map<EnhancementRarity, Double> stat = RunicStructureLootInjector.genericStatRarityDistribution();
        Map<EnhancementRarity, Double> effect = genericEffectRarityDistribution(source);
        for (EnhancementRarity rarity : EnhancementRarity.values()) {
            out.put(rarity, (stat.getOrDefault(rarity, 0.0D) * 0.8D) + (effect.getOrDefault(rarity, 0.0D) * 0.2D));
        }
        return out;
    }

    private static Map<EnhancementRarity, Double> genericEffectRarityDistribution(CommandSourceStack source) {
        EnumMap<EnhancementRarity, Double> out = new EnumMap<>(EnhancementRarity.class);
        EnumMap<EnhancementRarity, Integer> weights = new EnumMap<>(EnhancementRarity.class);
        int total = 0;
        var registry = source.getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            if (net.revilodev.runic.runes.UniqueRuneSources.isSourceLockedRuneEffect(id)) continue;
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            Holder<Enchantment> holder = registry.get(key).orElse(null);
            if (holder == null || EnchantBlacklist.isBlacklisted(holder)) continue;
            EnhancementRarity rarity = EnhancementRarities.get(holder);
            int weight = RunicStructureLootInjector.weightForRarity(rarity);
            if (weight <= 0) continue;
            weights.merge(rarity, weight, Integer::sum);
            total += weight;
        }
        for (EnhancementRarity rarity : EnhancementRarity.values()) {
            out.put(rarity, total <= 0 ? 0.0D : (double) weights.getOrDefault(rarity, 0) / (double) total);
        }
        return out;
    }

    private static double mythicChancePerRoll(String table) {
        if (table == null || table.equals("base")) return 0.0D;
        if (!RunicConfig.mythicRunesEnabled() || !RunicConfig.mythicRuneLootEnabled()) return 0.0D;
        if (!RunicStructureLootInjector.isMythicSource(table)
                || RunicStructureLootInjector.lootDifficulty(table) < RunicConfig.mythicRuneMinLootDifficulty()) {
            return 0.0D;
        }
        return Math.max(0, RunicConfig.mythicRuneLootWeight()) / 24.0D;
    }

    private static String percent(double chance) {
        return String.format(Locale.ROOT, "%.2f%%", Math.max(0.0D, chance) * 100.0D);
    }

    private static List<String> structureSuggestions() {
        return List.of("ancient_city", "bastion", "fortress", "ocean_monument", "end_city",
                "stronghold", "dungeon", "trial_chamber", "woodland_mansion", "pillager_outpost");
    }

    private static ItemStack requireHeldItem(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity living)) {
            throw INVALID_TARGET.create();
        }

        ItemStack stack = living.getMainHandItem();
        if (stack.isEmpty()) {
            throw INVALID_TARGET.create();
        }
        return stack;
    }

    private static Holder<Enchantment> resolveEffect(CommandSourceStack source, String name) throws CommandSyntaxException {
        ResourceLocation id = name.contains(":")
                ? ResourceLocation.parse(name)
                : ResourceLocation.withDefaultNamespace(name.toLowerCase(Locale.ROOT));

        return source.getServer()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(ResourceKey.create(Registries.ENCHANTMENT, id))
                .filter(RunicCommands::isRunicEffect)
                .orElseThrow(UNKNOWN_EFFECT::create);
    }

    private static ResourceLocation resolveSynergy(String name) throws CommandSyntaxException {
        String normalized = name.toLowerCase(Locale.ROOT);
        ResourceLocation id = normalized.contains(":")
                ? ResourceLocation.parse(normalized)
                : SynergyRegistry.synergyId(normalized);
        if (!SynergyRegistry.isRegisteredResult(id)) {
            throw UNKNOWN_SYNERGY.create();
        }
        return id;
    }

    private static boolean isRunicEffect(Holder<Enchantment> enchantment) {
        return net.revilodev.runic.item.custom.RuneItem.isEffectEnchantment(enchantment);
    }

    private static Set<String> configDisableSuggestions() {
        Set<String> suggestions = new LinkedHashSet<>();
        suggestions.add("rune_slots");
        suggestions.add("runic_loot");
        suggestions.add("etching_crafting");
        suggestions.add("stat_caps");

        for (RuneStatType type : RuneStatType.values()) {
            suggestions.add(type.id());
        }

        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            suggestions.add(id.toString());
            if ("minecraft".equals(id.getNamespace())) {
                suggestions.add(id.getPath());
            }
        }
        return suggestions;
    }

    private static String normalizeInscription(String inscription) {
        return switch (inscription.toLowerCase(Locale.ROOT)) {
            case "repair_rune", "repair_inscription" -> "repair";
            case "expansion_rune", "expansion_inscription" -> "expansion";
            case "nullification_rune", "nullification_inscription" -> "nullification";
            case "upgrade_rune", "upgrade_inscription" -> "upgrade";
            case "reroll_inscription" -> "reroll";
            case "cursed_inscription" -> "cursed";
            case "wild_inscription" -> "wild";
            case "extraction_inscription" -> "extraction";
            default -> inscription.toLowerCase(Locale.ROOT);
        };
    }

    private static void sendSummary(CommandSourceStack source, String action, int changed, int totalTargets) {
        source.sendSuccess(() -> Component.literal(action + " " + changed + "/" + totalTargets + " target(s)."), true);
    }
}
