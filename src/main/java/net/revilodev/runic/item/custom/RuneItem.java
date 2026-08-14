package net.revilodev.runic.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.compat.RunicCompat;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.RarityTintedItemName;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.runes.RunicEffectEnchantments;
import net.revilodev.runic.synergy.SynergyRegistry;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class RuneItem extends Item implements RarityTintedItemName {

    // fixed levels for generated effects
    public static final int EFFECT_LEVEL_ETCHING = 1;
    public static final int EFFECT_LEVEL_RUNE = 2;

    public RuneItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        // synergy and mythic runes override the base name
        ResourceLocation synergyId = getItemSynergyId(stack);
        if (synergyId != null) {
            return RarityTintedItemName.super.tintedName(stack, Component.translatable("tooltip.runic.synergy_rune"));
        }

        ResourceLocation mythicId = MythicRuneRegistry.getItemRuneId(stack);
        if (mythicId != null) {
            return RarityTintedItemName.tintedName(ChatFormatting.DARK_PURPLE, stack, Component.translatable("tooltip.runic.mythic_rune"));
        }
        return super.getName(stack);
    }

    @Override
    public ChatFormatting nameColor() {
        return ChatFormatting.GOLD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ResourceLocation mythicId = MythicRuneRegistry.getItemRuneId(stack);
        if (mythicId != null && MythicRuneRegistry.get(mythicId) != null) {
            String path = mythicId.getPath().substring("mythic/".length());
            tooltip.add(Component.translatable("tooltip.runic.mythic_desc." + path).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static Set<ResourceLocation> allowedEffectIds() {
        return RunicEffectEnchantments.allowedEffectIds();
    }

    public static boolean isEffectEnchantment(Holder<Enchantment> holder) {
        return RunicEffectEnchantments.isEffectEnchantment(holder);
    }

    public static int clampEffectLevel(Holder<Enchantment> holder, int requested) {
        int desired = Math.max(1, Math.min(EFFECT_LEVEL_RUNE, requested));
        return Math.min(holder.value().getMaxLevel(), desired);
    }

    public static int forcedEffectLevel(Holder<Enchantment> holder) {
        return clampEffectLevel(holder, EFFECT_LEVEL_RUNE);
    }

    public static int forcedEtchingEffectLevel(Holder<Enchantment> holder) {
        return clampEffectLevel(holder, EFFECT_LEVEL_ETCHING);
    }

    public static ItemStack createEffectRune(Holder<Enchantment> enchantment) {
        if (!isEffectEnchantment(enchantment) || EnchantBlacklist.isBlacklisted(enchantment)) {
            return ItemStack.EMPTY;
        }

        // effect runes store the enchant directly on the item
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        stack.enchant(enchantment, forcedEffectLevel(enchantment)); // Level 2 (clamped)
        return stack;
    }

    public static ItemStack createStatRune(RandomSource random, RuneStatType type) {
        if (!RunicCompat.isStatAvailable(type)) {
            return ItemStack.EMPTY;
        }
        RuneStats stats = RuneStats.singleUnrolled(type);
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        RuneStats.set(stack, stats);
        return stack;
    }

    // creates random stat rune
    public static ItemStack createRandomStatRune(RandomSource random) {
        RuneStatType[] all = RuneStatType.values();
        List<RuneStatType> allowed = new ArrayList<>();

        // skip disabled and blacklisted stats
        for (RuneStatType type : all) {
            if (!EnchantBlacklist.isStatBlacklisted(type) && RunicCompat.isStatAvailable(type)) {
                allowed.add(type);
            }
        }
        if (allowed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RuneStatType chosen = pickWeightedStat(allowed.toArray(RuneStatType[]::new), random);
        return createStatRune(random, chosen);
    }

    // picks weighted stat
    private static RuneStatType pickWeightedStat(RuneStatType[] all, RandomSource random) {
        int total = 0;
        int[] weights = new int[all.length];

        // rarity weights drive the roll
        for (int i = 0; i < all.length; i++) {
            int w = EnhancementRarities.getStat(all[i].id()).weight();
            weights[i] = Math.max(0, w);
            total += weights[i];
        }
        if (total <= 0) {
            return all[random.nextInt(all.length)];
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return all[i];
            }
        }
        return all[all.length - 1];
    }

    public static RuneStats getRolledStatsForTooltip(ItemStack rune) {
        RuneStats template = RuneStats.get(rune);
        if (template == null || template.isEmpty()) {
            return RuneStats.empty();
        }
        return RuneStats.rollForApplication(template, RandomSource.create());
    }

    public static RuneStats getRuneStats(ItemStack stack) {
        return RuneStats.get(stack);
    }


    // gets primary effect enchantment
    public static Holder<Enchantment> getPrimaryEffectEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // books use stored enchants and finished runes use direct enchants
        ItemEnchantments enchants = !stored.isEmpty() ? stored : direct;

        if (enchants.isEmpty()) {
            return null;
        }

        List<Holder<Enchantment>> effects = new ArrayList<>();
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (isEffectEnchantment(h)) {
                effects.add(h);
            }
        }

        if (!effects.isEmpty()) {
            return effects.get(0);
        }

        return enchants.keySet().iterator().next();
    }

    public static ItemStack createMythicRune(ResourceLocation id) {
        if (!MythicRuneRegistry.isKnown(id)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        MythicRuneRegistry.setItemRuneId(stack, id);
        return stack;
    }

    public static ItemStack createSynergyRune(ResourceLocation id) {
        if (!SynergyRegistry.isRegisteredResult(id)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        RunicItemData.addSynergy(stack, id);
        return stack;
    }

    public static ResourceLocation getItemSynergyId(ItemStack stack) {
        List<ResourceLocation> synergies = RunicItemData.getSynergies(stack);
        if (synergies.size() != 1) {
            return null;
        }

        // only a single registered result counts as a real synergy rune
        ResourceLocation id = synergies.get(0);
        return SynergyRegistry.isRegisteredResult(id) ? id : null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEnchanted()) {
            return true;
        }
        if (MythicRuneRegistry.isMythicRune(stack)) {
            return true;
        }
        if (getItemSynergyId(stack) != null) {
            return true;
        }
        RuneStats stats = RuneStats.get(stack);
        return stats != null && !stats.isEmpty();
    }
}
