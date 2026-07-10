// src/main/java/net/revilodev/runic/screen/custom/ArtisansWorkbenchScreen.java
package net.revilodev.runic.screen.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.client.ArtisansPreviewRenderer;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.relic.RelicRegistry;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;
import net.revilodev.runic.synergy.SynergyRegistry;

import java.util.*;

public final class ArtisansWorkbenchScreen extends AbstractContainerScreen<ArtisansWorkbenchMenu> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/container/artisans_workbench.png");

    private static final int PREVIEW_X = 116;
    private static final int PREVIEW_Y = 16;
    private static final int PREVIEW_W = 52;
    private static final int PREVIEW_H = 52;

    private static final int FORGE_X = 52;
    private static final int FORGE_Y = 51;
    private static final String PREVIEW_DELTA = "preview_delta";
    private static final String PREVIEW_INVALID = "preview_invalid";

    private ForgeButton forgeButton;

    public ArtisansWorkbenchScreen(ArtisansWorkbenchMenu menu, Inventory inv, Component ignoredTitle) {
        super(menu, inv, Component.literal("Apply Upgrades"));
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 40;
        this.titleLabelY = 17;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        this.forgeButton = this.addRenderableWidget(new ForgeButton(this.leftPos + FORGE_X, this.topPos + FORGE_Y, this::pressForge));
        syncButtonState();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncButtonState();
    }

    private void syncButtonState() {
        if (this.forgeButton == null) return;
        this.forgeButton.setPosition(this.leftPos + FORGE_X, this.topPos + FORGE_Y);
        ItemStack preview = this.menu.getPreviewStack();
        this.forgeButton.active = !preview.isEmpty() && !isPreviewInvalid(preview);
        this.forgeButton.visible = true;
    }

    private void pressForge() {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.gameMode == null) return;
        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, ArtisansWorkbenchMenu.BUTTON_FORGE);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        gg.blit(TEX, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        ItemStack preview = this.menu.getPreviewStack();
        ItemStack base = this.menu.getGearStack();
        ItemStack toPreview = !preview.isEmpty() ? preview : base;

        if (!toPreview.isEmpty()) {
            ArtisansPreviewRenderer.render(
                    gg,
                    toPreview,
                    this.leftPos + PREVIEW_X,
                    this.topPos + PREVIEW_Y,
                    PREVIEW_W,
                    PREVIEW_H,
                    partialTick
            );
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        renderForgePreviewTooltip(gg);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    /**
     * Renders a stable tooltip panel to the right of the GUI (or left if no space),
     * with the full item tooltip and a "Changes" section appended at the bottom.
     */
    private void renderForgePreviewTooltip(GuiGraphics gg) {
        ItemStack base = this.menu.getGearStack();
        ItemStack preview = this.menu.getPreviewStack();
        if (base.isEmpty() || preview.isEmpty() || this.minecraft == null) return;

        ItemStack enhancement = this.menu.getEnhancementStack();
        boolean hideOutcome = hidesOutcomePreview(enhancement);
        boolean hideRolledStatValues = !isInscription(enhancement) && !RuneStats.get(enhancement).isEmpty();

        List<Component> lines = new ArrayList<>(this.getTooltipFromContainerItem((hideOutcome || hideRolledStatValues) ? base : preview));
        moveVanillaStatsToTop(lines);
        if (lines.isEmpty()) return;

        List<Component> delta = buildDeltaLines(base, preview, enhancement);
        if (!delta.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.runic.preview_changes_header").withStyle(ChatFormatting.GRAY));
            lines.addAll(delta);
        }

        List<Component> statRoll = buildStatRollLines(base, enhancement);
        if (!statRoll.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("tooltip.runic.preview_stat_roll_header").withStyle(ChatFormatting.GRAY));
            lines.addAll(statRoll);
        }

        // Place panel to the right of the GUI
        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos + 6;

        int tw = 0;
        for (Component c : lines) tw = Math.max(tw, this.font.width(c));
        int th = lines.size() * this.font.lineHeight;

        // If it doesn't fit on the right, move to the left of the GUI
        if (x + tw + 12 > this.width) {
            x = this.leftPos - tw - 20;
        }

        x -= 13;
        y += 8;

        // Clamp on screen (never "return" and disappear)
        x = Math.max(6, Math.min(x, this.width - tw - 12));
        y = Math.max(6, Math.min(y, this.height - th - 12));

        gg.renderTooltip(this.font, lines, Optional.empty(), x, y);
    }

    private List<Component> buildDeltaLines(ItemStack base, ItemStack preview, ItemStack enhancement) {
        if (hidesOutcomePreview(enhancement)) return List.of();
        List<Component> out = new ArrayList<>();
        boolean hideStatRoll = !isInscription(enhancement) && !RuneStats.get(enhancement).isEmpty();
        CompoundTag delta = getPreviewDelta(preview);

        if (delta != null && delta.contains("failure_key", Tag.TAG_STRING)) {
            out.add(Component.translatable(delta.getString("failure_key")).withStyle(ChatFormatting.RED));
            return out;
        }
        if (delta != null && delta.contains("warning_key", Tag.TAG_STRING)) {
            out.add(Component.translatable(delta.getString("warning_key")).withStyle(ChatFormatting.GOLD));
        }

        int baseCap = RuneSlots.capacity(base);
        int prevCap = RuneSlots.capacity(preview);
        int baseUsed = RuneSlots.used(base);
        int prevUsed = RuneSlots.used(preview);

        if (baseCap != prevCap || baseUsed != prevUsed) {
            out.add(Component.translatable("tooltip.runic.preview_slots_result",
                    baseUsed + "/" + baseCap,
                    prevUsed + "/" + prevCap).withStyle(ChatFormatting.AQUA));
        }

        int baseCorruption = RunicItemData.getCorruption(base);
        int prevCorruption = RunicItemData.getCorruption(preview);
        if (baseCorruption != prevCorruption) {
            int deltaCorruption = prevCorruption - baseCorruption;
            out.add(Component.translatable("tooltip.runic.preview_corruption_delta", signedPercent(deltaCorruption))
                    .withStyle(deltaCorruption > 0 ? ChatFormatting.DARK_PURPLE : ChatFormatting.GREEN));
            out.add(Component.translatable("tooltip.runic.preview_result_band",
                    prevCorruption,
                    RunicItemData.getCorruptionBand(preview).displayName()).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (delta != null && delta.contains("relic_id", Tag.TAG_STRING)) {
            ResourceLocation relicId = ResourceLocation.tryParse(delta.getString("relic_id"));
            out.add(Component.translatable("tooltip.runic.relic", RelicRegistry.displayName(relicId)).withStyle(ChatFormatting.GOLD));
            if (delta.contains("relic_corruption", Tag.TAG_INT)) {
                out.add(Component.translatable("tooltip.runic.relic_corruption", delta.getInt("relic_corruption")).withStyle(ChatFormatting.DARK_PURPLE));
            }
            if (delta.contains("relic_durability_use", Tag.TAG_DOUBLE)) {
                out.add(Component.translatable("tooltip.runic.relic_durability_use", formatNumber(delta.getDouble("relic_durability_use"))).withStyle(ChatFormatting.GRAY));
            }
        }

        int basePotential = RunicItemData.getSynergyPotential(base);
        int prevPotential = RunicItemData.getSynergyPotential(preview);
        if (basePotential != prevPotential) {
            out.add(Component.translatable("tooltip.runic.preview_synergy_potential_result",
                    toRoman(basePotential),
                    toRoman(prevPotential)).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        ResourceLocation newEnhancement = enhancementRef(enhancement);
        if (newEnhancement != null && !SynergyRegistry.possibleSynergies(newEnhancement, base).isEmpty()) {
            int chance = (int) Math.round(RunicItemData.getSynergyChance(base) * 100.0D);
            out.add(Component.translatable("tooltip.runic.possible_synergy_detected").withStyle(ChatFormatting.LIGHT_PURPLE));
            out.add(Component.translatable("tooltip.runic.synergy_chance", chance).withStyle(ChatFormatting.GRAY));
            out.add(Component.translatable("tooltip.runic.preview_synergy_failure", RunicConfig.failedSynergyCorruption()).withStyle(ChatFormatting.RED));
            if (GearAttributes.has(base, GearAttribute.FRACTURED)) {
                out.add(Component.translatable("tooltip.runic.preview_synergy_failure_fractured", RunicConfig.fracturedExtraFailureCorruption()).withStyle(ChatFormatting.RED));
            }
        }

        if (base.isDamageableItem() && preview.isDamageableItem()) {
            int baseMax = base.getMaxDamage();
            int prevMax = preview.getMaxDamage();
            int dMax = prevMax - baseMax;
            if (dMax != 0) out.add(coloredDelta("Max Durability", dMax));

            int baseRem = baseMax - base.getDamageValue();
            int prevRem = prevMax - preview.getDamageValue();
            int dRem = prevRem - baseRem;
            if (dRem != 0) out.add(coloredDelta("Durability (remaining)", dRem));
        }

        RuneStats bStats = RuneStats.get(base);
        RuneStats pStats = RuneStats.get(preview);

        Map<RuneStatType, Float> bm = bStats == null ? Map.of() : bStats.view();
        Map<RuneStatType, Float> pm = pStats == null ? Map.of() : pStats.view();

        if (!bm.isEmpty() || !pm.isEmpty()) {
            List<RuneStatType> keys = new ArrayList<>();
            keys.addAll(bm.keySet());
            for (RuneStatType t : pm.keySet()) if (!keys.contains(t)) keys.add(t);
            keys.sort(Comparator.comparing(RuneStatType::id));

            for (RuneStatType t : keys) {
                float bv = bm.getOrDefault(t, 0.0F);
                float pv = pm.getOrDefault(t, 0.0F);
                float dv = pv - bv;
                if (Math.abs(dv) <= 0.0001F) continue;
                out.add(hideStatRoll
                        ? Component.literal(pretty(t.id()) + ": Added").withStyle(ChatFormatting.GREEN)
                        : coloredValue(pretty(t.id()), t, dv));
            }
        }

        return out;
    }

    private static CompoundTag getPreviewDelta(ItemStack stack) {
        CustomDataAccessor accessor = new CustomDataAccessor(stack);
        return accessor.getPreviewDelta();
    }

    private static boolean isPreviewInvalid(ItemStack stack) {
        return new CustomDataAccessor(stack).isPreviewInvalid();
    }

    private static String formatNumber(double value) {
        long rounded = Math.round(value);
        return Math.abs(value - rounded) < 0.001D ? Long.toString(rounded) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static String signedPercent(int value) {
        return (value > 0 ? "+" : "") + value + "%";
    }

    private record CustomDataAccessor(ItemStack stack) {
        CompoundTag getPreviewDelta() {
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            return data.contains(PREVIEW_DELTA, Tag.TAG_COMPOUND) ? data.getCompound(PREVIEW_DELTA) : null;
        }

        boolean isPreviewInvalid() {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean(PREVIEW_INVALID);
        }
    }

    private List<Component> buildStatRollLines(ItemStack gear, ItemStack enhancement) {
        if (!Screen.hasControlDown()) return List.of();
        if (gear.isEmpty()) return List.of();

        RuneStats enhancementStats = RuneStats.get(enhancement);
        if (enhancementStats == null || enhancementStats.isEmpty()) return List.of();

        RuneStatType type = enhancementStats.view().keySet().stream().findFirst().orElse(null);
        if (type == null) return List.of();

        boolean etching = enhancement.getItem() instanceof net.revilodev.runic.item.custom.EtchingItem;
        int shift = this.menu.instableShiftForPreview(gear);

        float min = etching ? type.etchingMinPercent() : type.minPercent();
        float max = etching ? type.etchingMaxPercent() : type.maxPercent();
        min = Math.max(0.0F, min - shift);
        max = Math.max(min, max - shift);

        float multiplier = this.menu.powerMultiplierForPreview(gear);
        float minAdjusted = min * multiplier;
        float maxAdjusted = max * multiplier;

        return List.of(
                Component.translatable("tooltip.runic.preview_stat_roll_range",
                        pretty(type.id()),
                        formatSignedValue(type, minAdjusted),
                        formatSignedValue(type, maxAdjusted)).withStyle(ChatFormatting.AQUA)
        );
    }

    private static boolean isInscription(ItemStack stack) {
        return stack.is(ModItems.REPAIR_INSCRIPTION.get())
                || stack.is(ModItems.EXPANSION_INSCRIPTION.get())
                || stack.is(ModItems.NULLIFICATION_INSCRIPTION.get())
                || stack.is(ModItems.UPGRADE_INSCRIPTION.get())
                || stack.is(ModItems.REROLL_INSCRIPTION.get())
                || stack.is(ModItems.CURSED_INSCRIPTION.get())
                || stack.is(ModItems.WILD_INSCRIPTION.get())
                || stack.is(ModItems.EXTRACTION_INSCRIPTION.get())
                || stack.is(ModItems.RESONANCE_INSCRIPTION.get())
                || stack.is(ModItems.PURIFICATION_INSCRIPTION.get())
                || stack.is(ModItems.STABILIZATION_INSCRIPTION.get())
                || stack.is(ModItems.TEMPERING_INSCRIPTION.get())
                || stack.is(ModItems.RELIC_SOCKET_INSCRIPTION.get());
    }

    private static boolean hidesOutcomePreview(ItemStack stack) {
        return stack.is(ModItems.WILD_INSCRIPTION.get());
    }

    private static Component coloredDelta(String label, int delta) {
        ChatFormatting fmt = delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = (delta > 0 ? "+" : "") + delta;
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static Component coloredValue(String label, RuneStatType type, float delta) {
        ChatFormatting fmt = delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = formatSignedValue(type, delta);
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static String trimDouble(double v) {
        double av = Math.abs(v);
        if (av >= 1000.0) return String.format(Locale.ROOT, "%.0f", v);
        if (av >= 100.0) return String.format(Locale.ROOT, "%.1f", v);
        if (av >= 10.0) return String.format(Locale.ROOT, "%.2f", v);
        if (av >= 1.0) return String.format(Locale.ROOT, "%.2f", v);
        if (av >= 0.1) return String.format(Locale.ROOT, "%.2f", v);
        return String.format(Locale.ROOT, "%.3f", v);
    }

    private static String formatSignedValue(RuneStatType type, float v) {
        float av = Math.abs(v);
        String num = Math.abs(av - Math.round(av)) < 0.001f
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.1f", av);
        return (v >= 0 ? "+" : "-") + num + (type.isPercentBased() ? "%" : "");
    }

    private static String pretty(String id) {
        if (id == null || id.isBlank()) return "";
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static ResourceLocation enhancementRef(ItemStack enhancement) {
        RuneStats stats = RuneStats.get(enhancement);
        if (stats != null && !stats.isEmpty()) {
            RuneStatType type = stats.view().keySet().stream().findFirst().orElse(null);
            return type == null ? null : SynergyRegistry.statId(type);
        }

        ItemEnchantments enchants = enhancement.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) {
            enchants = enhancement.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        for (var entry : enchants.entrySet()) {
            if (entry.getIntValue() <= 0) continue;
            Optional<ResourceLocation> id = entry.getKey().unwrapKey().map(k -> k.location());
            if (id.isPresent()) return id.get();
        }
        return null;
    }

    private static String toRoman(int v) {
        if (v <= 0) return "0";
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

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.pose().pushPose();
        float s = 0.85f;
        gg.pose().scale(s, s, 1f);
        int tx = (int) (this.titleLabelX / s);
        int ty = (int) (this.titleLabelY / s);
        gg.drawString(this.font, this.title, tx, ty, 0x404040, false);
        gg.pose().popPose();

        gg.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
