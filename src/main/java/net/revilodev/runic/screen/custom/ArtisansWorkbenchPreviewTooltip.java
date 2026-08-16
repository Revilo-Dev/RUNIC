
package net.revilodev.runic.screen.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.gear.GearAttribute;

import java.util.*;


public final class ArtisansWorkbenchPreviewTooltip {

    private static final String ROOT = "runic";
    private static final String PREVIEW_DELTA = "preview_delta";

    private ArtisansWorkbenchPreviewTooltip() {}


    public static List<Component> build(ItemStack base, ItemStack out) {
        CompoundTag delta = getPreviewDelta(out);
        if (delta == null || delta.isEmpty()) return List.of();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Changes:").withStyle(ChatFormatting.GRAY));

        addCorruptionChanges(lines, delta);
        addItemStats(lines, delta);
        addRuneStats(lines, delta);
        addEnchantChanges(lines, base, out);
        addRuneSlotChanges(lines, delta);
        addUpdateFiveChanges(lines, delta);
        addAttributeChanges(lines, delta);

        if (lines.size() == 1) {
            return List.of(
                    Component.literal("Changes:").withStyle(ChatFormatting.GRAY),
                    Component.literal("None").withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        return lines;
    }


    private static void addItemStats(List<Component> out, CompoundTag delta) {
        CompoundTag item = delta.contains("item_stats", Tag.TAG_COMPOUND) ? delta.getCompound("item_stats") : null;

        if (item != null) {
            addDouble(out, item, "attack_damage", "Attack Damage");
            addDouble(out, item, "attack_speed", "Attack Speed");
            addDouble(out, item, "range", "Range");
            addDouble(out, item, "block_break_speed", "Mining Speed");

            addDouble(out, item, "armor", "Armor");
            addDouble(out, item, "toughness", "Toughness");
            addDouble(out, item, "knockback_resistance", "Knockback Resistance");
            addDouble(out, item, "max_health", "Max Health");
            addDouble(out, item, "movement_speed", "Movement Speed");
            addDouble(out, item, "movement_efficiency", "Soul Speed");

            addDouble(out, item, "draw_speed", "Draw Speed");
            addDouble(out, item, "power", "Power");
        }

        if (delta.contains("dur_max", Tag.TAG_INT)) {
            int v = delta.getInt("dur_max");
            if (v != 0) {
                out.add(
                        Component.literal("  Max Durability ")
                                .append(Component.literal(formatSignedInt(v)).withStyle(colorForSign(v)))
                                .withStyle(ChatFormatting.WHITE)
                );
            }
        }

        if (delta.contains("dur_rem", Tag.TAG_INT)) {
            int v = delta.getInt("dur_rem");
            if (v != 0) {
                out.add(
                        Component.literal("  Durability ")
                                .append(Component.literal(formatSignedInt(v)).withStyle(colorForSign(v)))
                                .withStyle(ChatFormatting.WHITE)
                );
            }
        }
    }

    // adds rune stats
    private static void addRuneStats(List<Component> out, CompoundTag delta) {
        if (!delta.contains("rune_stats", Tag.TAG_COMPOUND)) return;
        CompoundTag rs = delta.getCompound("rune_stats");
        if (rs.isEmpty()) return;

        for (String key : rs.getAllKeys()) {
            if (!rs.contains(key, Tag.TAG_FLOAT)) continue;
            float v = rs.getFloat(key);
            if (Math.abs(v) < 1.0e-6f) continue;

            out.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.stat." + key))
                            .append(Component.literal(" "))
                            .append(Component.literal(formatSignedPercent(v)).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.WHITE)
            );
        }
    }


    private static void addEnchantChanges(List<Component> out, ItemStack base, ItemStack result) {
        ItemEnchantments b = base.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments r = result.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (b.isEmpty() && r.isEmpty()) return;

        Set<Holder<Enchantment>> union = new LinkedHashSet<>();
        union.addAll(b.keySet());
        union.addAll(r.keySet());

        for (Holder<Enchantment> h : union) {
            int bl = b.getLevel(h);
            int rl = r.getLevel(h);
            if (bl == rl) continue;

            if (rl > 0 && bl == 0) {
                out.add(enchantChangeLine("+", h, rl));
            } else if (rl == 0 && bl > 0) {
                out.add(enchantChangeLine("-", h, bl));
            } else if (rl > bl) {
                out.add(enchantChangeLine("+", h, rl));
            } else {
                out.add(enchantChangeLine("-", h, bl));
            }
        }
    }

    private static Component enchantChangeLine(String sign, Holder<Enchantment> h, int level) {
        Component name = h.value().description().copy().withStyle(ChatFormatting.GRAY);

        return Component.literal("  " + sign)
                .append(name)
                .withStyle(ChatFormatting.WHITE);
    }

    private static void addRuneSlotChanges(List<Component> out, CompoundTag delta) {
        int cap = delta.getInt("slot_cap_result");
        int used = delta.getInt("slot_used_result");
        if (delta.contains("slot_cap_result", Tag.TAG_INT)) {
            out.add(Component.literal("  " + slotIcons(used, cap)).withStyle(ChatFormatting.AQUA));
        }
    }

    private static void addCorruptionChanges(List<Component> out, CompoundTag delta) {
        if (delta.contains("corruption", Tag.TAG_INT)) {
            int v = delta.getInt("corruption");
            if (v != 0) {
                int result = delta.getInt("corruption_result");
                out.add(Component.literal("  Corruption " + result + "%: ")
                        .append(Component.literal(formatSignedInt(v) + "%").withStyle(colorForSign(v)))
                        .withStyle(ChatFormatting.WHITE));
            }
        }
    }


    private static void addUpdateFiveChanges(List<Component> out, CompoundTag delta) {
        if (delta.getBoolean("corruption_risk_negative")) {
            out.add(Component.translatable("tooltip.runic.preview_risk_negative").withStyle(ChatFormatting.RED));
        }
        if (delta.getBoolean("corruption_risk_positive")) {
            out.add(Component.translatable("tooltip.runic.preview_risk_positive").withStyle(ChatFormatting.AQUA));
        }

        if (delta.contains("synergies", Tag.TAG_INT)) {
            int v = delta.getInt("synergies");
            if (v != 0) {
                out.add(Component.literal("  Synergy ")
                        .append(Component.literal(formatSignedInt(v)).withStyle(colorForSign(v)))
                        .withStyle(ChatFormatting.WHITE));
            }
        }

        if (delta.contains("relic_socket", Tag.TAG_INT)) {
            int v = delta.getInt("relic_socket");
            if (v != 0) {
                out.add(Component.literal("  Relic Socket ")
                        .append(Component.literal(v > 0 ? "Added" : "Removed").withStyle(colorForSign(v)))
                        .withStyle(ChatFormatting.WHITE));
            }
        }
        if (delta.contains("mythic_rune_id", Tag.TAG_STRING)) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(delta.getString("mythic_rune_id"));
            if (id != null) {
                out.add(Component.literal("  Mythic Rune ")
                        .append(Component.translatable("tooltip.runic.mythic_name." + id.getPath().substring("mythic/".length())).withStyle(ChatFormatting.RED))
                        .withStyle(ChatFormatting.WHITE));
            }
        }
    }


    private static void addAttributeChanges(List<Component> out, CompoundTag delta) {
        CompoundTag attrs = delta.contains("attrs", Tag.TAG_COMPOUND) ? delta.getCompound("attrs") : new CompoundTag();
        boolean hasPotential = delta.contains("synergy_potential", Tag.TAG_INT) && delta.getInt("synergy_potential") != 0;
        if (attrs.isEmpty() && !hasPotential) return;

        if (hasPotential) {
            int v = delta.getInt("synergy_potential");
            out.add(Component.literal("  Synergy Potential ")
                    .append(Component.literal(formatSignedInt(v)).withStyle(colorForSign(v)))
                    .withStyle(ChatFormatting.WHITE));
        }
        for (GearAttribute a : GearAttribute.values()) {
            String id = a.id();
            if (!attrs.contains(id, Tag.TAG_INT)) continue;
            int v = attrs.getInt(id);
            if (v == 0) continue;

            String prefix = v > 0 ? "+" : "-";
            String suffix = Math.abs(v) > 1 ? " " + toRoman(Math.abs(v)) : "";
            out.add(
                    Component.literal("  " + prefix)
                            .append(a.displayName().copy())
                            .append(Component.literal(suffix))
                            .withStyle(colorForSign(v))
            );
        }
    }

    private static void addDouble(List<Component> out, CompoundTag tag, String key, String name) {
        if (!tag.contains(key, Tag.TAG_DOUBLE)) return;
        double v = tag.getDouble(key);
        if (Math.abs(v) < 1.0e-9) return;

        out.add(
                Component.literal("  " + name + " ")
                        .append(Component.literal(formatSignedDouble(v)).withStyle(colorForSign(v)))
                        .withStyle(ChatFormatting.WHITE)
        );
    }

    private static ChatFormatting colorForSign(double v) {
        return v >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    private static String formatSignedInt(int v) {
        return (v >= 0 ? "+" : "-") + Math.abs(v);
    }

    private static String formatSignedDouble(double v) {
        double av = Math.abs(v);
        String num = Math.abs(av - Math.round(av)) < 0.001
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.2f", av);
        return (v >= 0 ? "+" : "-") + num;
    }

    private static String formatSignedPercent(float v) {
        float av = Math.abs(v);
        String num = Math.abs(av - Math.round(av)) < 0.001f
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.1f", av);
        return (v >= 0 ? "+" : "-") + num + "%";
    }

    private static String slotIcons(int used, int capacity) {
        int cap = Math.max(0, capacity);
        int filled = Math.max(0, Math.min(used, cap));
        StringBuilder sb = new StringBuilder(cap);
        for (int i = 0; i < filled; i++) sb.append('O');
        for (int i = filled; i < cap; i++) sb.append('o');
        return sb.toString();
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

    // gets preview delta
    private static CompoundTag getPreviewDelta(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = cd.copyTag();

        if (root.contains(PREVIEW_DELTA, Tag.TAG_COMPOUND)) {
            return root.getCompound(PREVIEW_DELTA);
        }

        if (root.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag runic = root.getCompound(ROOT);
            if (runic.contains(PREVIEW_DELTA, Tag.TAG_COMPOUND)) {
                return runic.getCompound(PREVIEW_DELTA);
            }
        }

        return null;
    }
}
