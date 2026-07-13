package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.relic.RelicRegistry;
import net.revilodev.runic.runes.RunicItemTargets;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;
import net.revilodev.runic.synergy.SynergyRegistry;

import java.util.*;

public final class GearTooltips {

    private GearTooltips() {}

    private static final int ENCHANT_TOOLTIP_PREVIEW_LIMIT = 4;
    private static final String SYNERGY_ICON = "\u2605";

    private static final char SLOT_FILLED = '⬤';
    private static final char SLOT_EMPTY = '◯';

    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        if (!isGear(stack)) return false;
        if (stack.getItem() instanceof RuneItem || stack.getItem() instanceof EtchingItem) return false;
        if (!shouldOverride(stack)) return false;

        RuneSlots.syncUsedToContents(stack);
        moveVanillaStatsToTop(tooltip);
        stripAllEnchantmentLines(stack, tooltip);

        boolean showDetails = Screen.hasControlDown();

        RuneStats stats = RuneStats.get(stack);
        boolean hasRunicStats = stats != null && !stats.isEmpty();
        List<Component> runicStats = hasRunicStats ? buildStatLines(stats, showDetails) : List.of();

        List<Component> enchLines = buildEnchantmentLines(stack, showDetails);
        List<Component> synergyLines = buildSynergyLines(stack, showDetails);
        List<Component> relicLines = buildRelicLines(stack, showDetails);
        List<Component> mythicLines = MythicRuneRegistry.buildTooltip(stack, showDetails || Screen.hasShiftDown());
        List<Component> slots = buildRuneSlots(stack);
        List<Component> updateFive = buildUpdateFiveLines(stack);
        boolean hasAttributes = !GearAttributes.getAll(stack).isEmpty() || RunicItemData.getSynergyPotential(stack) > 0;
        List<Component> attrs = buildAttributeIndicators(stack, showDetails);

        int insertAt = afterVanillaStatLines(tooltip);

        if (!updateFive.isEmpty()) {
            tooltip.addAll(insertAt, updateFive);
            insertAt += updateFive.size();
        }

        if (stack.getMaxDamage() > 0) {
            tooltip.add(insertAt, buildDurabilityLine(stack));
            insertAt++;
        }

        if (!slots.isEmpty()) {
            tooltip.addAll(insertAt, slots);
            insertAt += slots.size();
        }

        if (hasRunicStats || !enchLines.isEmpty() || !synergyLines.isEmpty() || !mythicLines.isEmpty()) {
            tooltip.add(insertAt, Component.translatable("tooltip.runic.enhancements_header").withStyle(ChatFormatting.GRAY));
            insertAt++;

            if (!synergyLines.isEmpty()) {
                tooltip.addAll(insertAt, synergyLines);
                insertAt += synergyLines.size();
            }
            if (hasRunicStats) {
                tooltip.addAll(insertAt, runicStats);
                insertAt += runicStats.size();
            }
            if (!enchLines.isEmpty()) {
                tooltip.addAll(insertAt, enchLines);
                insertAt += enchLines.size();
            }
            if (!mythicLines.isEmpty()) {
                tooltip.addAll(insertAt, mythicLines);
                insertAt += mythicLines.size();
            }
        }

        if (!relicLines.isEmpty()) {
            tooltip.addAll(insertAt, relicLines);
            insertAt += relicLines.size();
        }

        if (!attrs.isEmpty()) {
            tooltip.addAll(insertAt, attrs);
            insertAt += attrs.size();
        }

        if (!showDetails && (hasRunicStats || !enchLines.isEmpty() || !synergyLines.isEmpty() || !mythicLines.isEmpty() || hasAttributes || !relicLines.isEmpty())) {
            tooltip.add(insertAt, Component.translatable("tooltip.runic.details_hint").withStyle(ChatFormatting.DARK_GRAY));
        }

        return true;
    }

    private static boolean isGear(ItemStack stack) {
        return RunicItemTargets.isRunicGear(stack);
    }

    private static boolean shouldOverride(ItemStack stack) {
        if (stack.isEnchanted()) return true;
        if (RuneSlots.capacity(stack) > 0) return true;

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) return true;

        return !GearAttributes.getAll(stack).isEmpty()
                || RunicItemData.getCorruption(stack) > 0
                || RunicItemData.getSynergyPotential(stack) > 0
                || !RunicItemData.getSynergies(stack).isEmpty()
                || !RunicItemData.getMythicRunes(stack).isEmpty()
                || RunicItemData.hasRelicSocket(stack)
                || RunicItemData.hasRelic(stack);
    }

    private static int findFirstVanillaStatLine(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (isLikelyVanillaStatLine(tooltip.get(i))) return i;
        }
        return -1;
    }

    private static boolean isLikelyVanillaStatLine(Component c) {
        String key = keyOf(c);
        if (key != null) {
            if (key.startsWith("attribute.modifier.") || key.startsWith("attribute.name.")) return true;
        }

        String s = c.getString();
        if (s == null || s.isEmpty()) return false;

        String trimmed = s.trim();
        if (trimmed.isEmpty()) return false;

        char ch = trimmed.charAt(0);
        boolean startsNumeric = (ch == '+' || ch == '-' || (ch >= '0' && ch <= '9'));
        if (!startsNumeric) return false;

        if (trimmed.contains("%")) return false;
        if (trimmed.endsWith(":")) return false;
        return true;
    }

    private static void moveVanillaStatsToTop(List<Component> tooltip) {
        if (tooltip.size() <= 1) return;

        List<Component> statLines = new ArrayList<>();
        List<Integer> removeIdx = new ArrayList<>();

        for (int i = 0; i < tooltip.size(); i++) {
            Component c = tooltip.get(i);
            if (isAttributeHeader(c)) {
                removeIdx.add(i);
                continue;
            }
            if (isLikelyVanillaStatLine(c)) {
                statLines.add(c);
                removeIdx.add(i);
            }
        }

        if (statLines.isEmpty()) {
            for (int i = removeIdx.size() - 1; i >= 0; i--) {
                tooltip.remove((int) removeIdx.get(i));
            }
            return;
        }

        for (int i = removeIdx.size() - 1; i >= 0; i--) {
            tooltip.remove((int) removeIdx.get(i));
        }

        int insertAt = Math.min(1, tooltip.size());
        tooltip.addAll(insertAt, statLines);
    }

    private static int afterVanillaStatLines(List<Component> tooltip) {
        int statsStart = findFirstVanillaStatLine(tooltip);
        if (statsStart == -1) return Math.min(1, tooltip.size());

        int end = statsStart;
        while (end < tooltip.size() && isLikelyVanillaStatLine(tooltip.get(end))) {
            end++;
        }
        return end;
    }

    private static List<Component> buildStatLines(RuneStats stats, boolean showDetails) {
        List<Component> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            float v = stats.get(type);
            if (Math.abs(v) < 1.0e-6f) continue;

            out.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.stat." + type.id()))
                            .append(Component.literal(" "))
                            .append(Component.literal(formatSignedValue(type, v)).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.WHITE)
            );

            if (showDetails) {
                String descKey = "tooltip.runic.stat_desc." + type.id();
                if (I18n.exists(descKey)) {
                    out.add(Component.literal("  ")
                            .append(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    String fallback = statDescription(type);
                    if (fallback != null && !fallback.isBlank()) {
                        out.add(Component.literal("  ")
                                .append(Component.literal(fallback).withStyle(ChatFormatting.DARK_GRAY)));
                    }
                }
            }
        }

        return out;
    }

    private static Component buildDurabilityLine(ItemStack stack) {
        int max = stack.getMaxDamage();
        int curr = max - stack.getDamageValue();
        float pct = max <= 0 ? 1.0f : (float) curr / (float) max;

        ChatFormatting color =
                pct > 0.50f ? ChatFormatting.GREEN :
                        pct > 0.25f ? ChatFormatting.YELLOW :
                                pct > 0.10f ? ChatFormatting.GOLD :
                                        ChatFormatting.RED;

        return Component.literal("Durability: " + curr + "/" + max).withStyle(color);
    }

    private static List<Component> buildEnchantmentLines(ItemStack stack, boolean showDetails) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (live.isEmpty() && stored.isEmpty()) return List.of();

        LinkedHashMap<String, EnchLine> ordered = new LinkedHashMap<>();
        addEnchantments(ordered, live);
        addEnchantments(ordered, stored);

        List<Component> out = new ArrayList<>();
        boolean showAll = Screen.hasAltDown();
        int shown = 0;
        for (EnchLine e : ordered.values()) {
            if (!showAll && shown >= ENCHANT_TOOLTIP_PREVIEW_LIMIT) break;

            Component name = e.rarity.applyTo(e.name.copy());
            String roman = toRoman(e.level);
            Component lvl = roman.isEmpty()
                    ? Component.empty()
                    : Component.literal(" " + roman).withStyle(e.rarity.style());
            out.add(Component.literal("  ").append(name).append(lvl));
            shown++;

            if (showDetails) {
                String descKey = descriptionKey(e.id);
                if (descKey != null && I18n.exists(descKey)) {
                    out.add(Component.literal("  ")
                            .append(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }
        if (!showAll && ordered.size() > ENCHANT_TOOLTIP_PREVIEW_LIMIT) {
            int hidden = ordered.size() - ENCHANT_TOOLTIP_PREVIEW_LIMIT;
            out.add(Component.literal("  " + hidden + " more - press Alt to view more").withStyle(ChatFormatting.DARK_GRAY));
        }
        return out;
    }

    private static void addEnchantments(Map<String, EnchLine> out, ItemEnchantments ench) {
        ench.entrySet().forEach(e -> {
            var holder = e.getKey();
            int lvl = e.getIntValue();
            String key = holder.unwrapKey().map(k -> k.location().toString()).orElse(holder.toString());
            EnhancementRarity rarity = EnhancementRarities.get(holder);
            ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
            out.putIfAbsent(key, new EnchLine(holder.value().description().copy(), lvl, rarity, id));
        });
    }

    private record EnchLine(Component name, int level, EnhancementRarity rarity, ResourceLocation id) {}

    private static List<Component> buildRuneSlots(ItemStack stack) {
        int baseCap = RuneSlots.capacity(stack);
        int neg = GearAttributes.getLevel(stack, GearAttribute.NEGATIVE);
        int cap = Math.max(0, baseCap - neg);
        if (cap <= 0) return List.of();

        int used = RuneSlots.used(stack);
        int u = Math.min(used, cap);
        int rem = Math.max(0, cap - used);

        boolean hasMythic = !RunicItemData.getMythicRunes(stack).isEmpty();
        MutableComponent line = Component.empty();
        for (int i = 0; i < cap; i++) {
            char icon = i < u ? SLOT_FILLED : SLOT_EMPTY;
            ChatFormatting color = hasMythic && i == 0
                    ? ChatFormatting.DARK_PURPLE
                    : ChatFormatting.WHITE;
            line.append(Component.literal(String.valueOf(icon)).withStyle(color));
        }

        return List.of(line);
    }

    private static List<Component> buildSynergyLines(ItemStack stack, boolean showDetails) {
        List<ResourceLocation> synergies = RunicItemData.getSynergies(stack);
        if (synergies.isEmpty()) return List.of();
        List<Component> out = new ArrayList<>();
        for (ResourceLocation id : synergies) {
            if (!id.getPath().startsWith("synergy/")) continue;
            String path = id.getPath().substring("synergy/".length());
            out.add(Component.literal("  ")
                    .append(Component.literal(synergyIcon(id) + " ").withStyle(ChatFormatting.GOLD))
                    .append(RarityTintedItemName.tintedName(ChatFormatting.GOLD, stack, Component.translatable("tooltip.runic.synergy." + path))));
            if (showDetails) {
                out.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.runic.category.synergy").withStyle(ChatFormatting.LIGHT_PURPLE)));
                out.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.runic.synergy_desc." + path).withStyle(ChatFormatting.DARK_GRAY)));
                SynergyRegistry.definitionForResult(id).ifPresent(def -> {
                    out.add(Component.literal("    ")
                            .append(Component.translatable("tooltip.runic.synergy_influenced_by").withStyle(ChatFormatting.YELLOW)));
                    out.add(Component.literal("      ")
                            .append(Component.translatable(displayKey(def.inputA())).withStyle(ChatFormatting.YELLOW)));
                    out.add(Component.literal("      ")
                            .append(Component.translatable(displayKey(def.inputB())).withStyle(ChatFormatting.YELLOW)));
                });
            }
        }
        return out;
    }

    private static String synergyIcon(ResourceLocation id) {
        return SynergyRegistry.JUGGERNAUT.equals(id) ? "\u26E8" : SYNERGY_ICON;
    }

    private static List<Component> buildUpdateFiveLines(ItemStack stack) {
        List<Component> out = new ArrayList<>();
        int corruption = RunicItemData.getCorruption(stack);
        if (corruption > 0 || RunicItemData.isExhausted(stack)) {
            out.add(Component.translatable("tooltip.runic.corruption", corruption).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (RunicItemData.isExhausted(stack)) {
            out.add(Component.translatable("tooltip.runic.attribute.exhausted").withStyle(ChatFormatting.DARK_RED));
            out.add(Component.translatable("tooltip.runic.attribute_desc.exhausted").withStyle(ChatFormatting.GRAY));
        }
        return out;
    }

    private static List<Component> buildRelicLines(ItemStack stack, boolean showDetails) {
        return RelicRegistry.buildGearTooltipLines(stack, showDetails);
    }

    private static String displayKey(ResourceLocation id) {
        if (id != null && id.getNamespace().equals("runic") && id.getPath().startsWith("stat/")) {
            return "tooltip.runic.stat." + id.getPath().substring("stat/".length());
        }
        return descriptionKey(id);
    }

    private static List<Component> buildAttributeIndicators(ItemStack stack, boolean showDetails) {
        Map<GearAttribute, Integer> attrs = GearAttributes.getAll(stack);
        int potential = RunicItemData.getSynergyPotential(stack);
        if (attrs.isEmpty() && potential <= 0) return List.of();

        List<Component> out = new ArrayList<>();
        out.add(Component.translatable("tooltip.runic.attributes_header").withStyle(ChatFormatting.GRAY));

        if (potential > 0) {
            out.add(Component.literal("  ")
                    .append(Component.translatable("tooltip.runic.synergy_potential", toRoman(potential)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        }

        for (GearAttribute attr : GearAttribute.values()) {
            int lvl = attrs.getOrDefault(attr, 0);
            if (lvl <= 0) continue;

            String roman = toRoman(Math.min(lvl, 10));
            out.add(
                    Component.literal("  ")
                            .append(attr.displayName().copy().withStyle(attr.color()))
                            .append(roman.isEmpty()
                                    ? Component.empty()
                                    : Component.literal(" " + roman).withStyle(attr.color()))
            );

            if (showDetails) {
                out.add(Component.literal("  ")
                        .append(attributeDescription(attr).withStyle(ChatFormatting.DARK_GRAY)));
            }
        }

        return out;
    }

    private static MutableComponent attributeDescription(GearAttribute attr) {
        return switch (attr) {
            case CURSED -> Component.literal("Reduces all runic stat values by 5% per stack.");
            case INSTABLE -> Component.literal("Raises forging risk and weakens future rerolls.");
            case NEGATIVE -> Component.literal("Reduces effective rune slot capacity by 1 per stack.");
            case SEALED -> Component.literal("Prevents further modifications at the Artisan's Workbench.");
            case ANCIENT -> Component.literal("Boosts all rune power on the item by 5% per stack.");
            case BRITTLE -> Component.literal("This item loses durability 10% faster.");
            case FRACTURED -> Component.translatable("tooltip.runic.attribute_desc.fractured");
            case EXHAUSTED -> Component.translatable("tooltip.runic.attribute_desc.exhausted");
            case OVERFORGED -> Component.translatable("tooltip.runic.attribute_desc.overforged");
            case CHAOTIC -> Component.translatable("tooltip.runic.attribute_desc.chaotic");
            case REINFORCED -> Component.translatable("tooltip.runic.attribute_desc.reinforced");
            case TEMPERED -> Component.translatable("tooltip.runic.attribute_desc.tempered");
            case HARMONIZED -> Component.translatable("tooltip.runic.attribute_desc.harmonized");
            case DISSONANT -> Component.translatable("tooltip.runic.attribute_desc.dissonant");
        };
    }

    private static String formatSignedValue(RuneStatType type, float v) {
        float av = Math.abs(v);
        String num = Math.abs(av - Math.round(av)) < 0.001f
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.1f", av);

        return (v >= 0 ? "+" : "-") + num + (type.isPercentBased() ? "%" : "");
    }

    private static String toRoman(int v) {
        if (v <= 0) return "";
        if (v >= 10) return "X";
        return switch (v) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> "";
        };
    }

    private static String descriptionKey(ResourceLocation id) {
        if (id == null) return null;
        return "tooltip.runic." + id.getPath();
    }

    private static String statDescription(RuneStatType type) {
        return switch (type) {
            case ATTACK_SPEED -> "Increases attack speed.";
            case ATTACK_DAMAGE -> "Increases attack damage.";
            case ATTACK_RANGE -> "Increases melee reach.";
            case MOVEMENT_SPEED -> "Increases movement speed.";
            case SWEEPING_RANGE -> "Increases sweeping attack range.";
            case DURABILITY -> "Increases maximum durability.";
            case RESISTANCE -> "Reduces incoming damage.";
            case FIRE_RESISTANCE -> "Reduces fire damage.";
            case BLAST_RESISTANCE -> "Reduces explosion damage.";
            case PROJECTILE_RESISTANCE -> "Reduces projectile damage.";
            case KNOCKBACK_RESISTANCE -> "Reduces knockback taken.";
            case MINING_SPEED -> "Increases mining speed.";
            case UNDEAD_DAMAGE -> "Increases damage to undead.";
            case NETHER_DAMAGE -> "Increases damage to nether mobs.";
            case HEALTH -> "Increases maximum health.";
            case STUN_CHANCE -> "Chance to stun for 3s.";
            case FLAME_CHANCE -> "Chance to ignite for 4s.";
            case BLEEDING_CHANCE -> "Chance to apply bleeding for 5s.";
            case SHOCKING_CHANCE -> "Chance to call visual lightning and slow for 4s.";
            case POISON_CHANCE -> "Chance to apply poison for 2s.";
            case WITHERING_CHANCE -> "Chance to apply wither for 5s.";
            case WEAKENING_CHANCE -> "Chance to apply weakness for 6s.";
            case DRAW_SPEED -> "Increases bow draw speed.";
            case TOUGHNESS -> "Increases toughness.";
            case FREEZING_CHANCE -> "Chance to freeze mobs for 3s; etchings freeze for 2s.";
            case LEECHING_CHANCE -> "Chance to leach 10% max health on critical hit.";
            case FANGS -> "Chance to summon evoker fangs on hit.";
            case STONE -> "Gain temporary resistance after a heavy hit.";
            case AEGIS -> "Chance to negate an incoming hit.";
            case JUMP_HEIGHT -> "Increases leaping height.";
            case POWER -> "Increases ranged damage.";
            case ABILITY_POWER -> "Increases ability damage and scaling.";
        };
    }

    private static void stripAllEnchantmentLines(ItemStack stack, List<Component> tooltip) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (live.isEmpty() && stored.isEmpty()) return;

        Set<String> vanillaLines = new HashSet<>();
        live.entrySet().forEach(e -> vanillaLines.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).getString()));
        stored.entrySet().forEach(e -> vanillaLines.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).getString()));

        tooltip.removeIf(line -> vanillaLines.contains(line.getString()));
    }

    private static String keyOf(Component c) {
        if (c == null) return null;
        if (c.getContents() instanceof TranslatableContents tc) {
            return tc.getKey();
        }
        return null;
    }

    private static boolean isAttributeHeader(Component c) {
        String key = keyOf(c);
        if (key != null && key.startsWith("item.modifiers.")) return true;
        String s = c.getString();
        return s != null && s.startsWith("When ") && s.endsWith(":");
    }
}

