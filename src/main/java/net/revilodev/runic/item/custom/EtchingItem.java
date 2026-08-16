package net.revilodev.runic.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.compat.RunicCompat;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class EtchingItem extends Item {

    public EtchingItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.is(ModItems.BLANK_ETCHING.get());
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (!stack.is(ModItems.BLANK_ETCHING.get())) {
            return false;
        }

        RuneStatType stat = statTypeForTableEnchantment(enchantment);
        if (stat != null) {
            return !EnchantBlacklist.isStatBlacklisted(stat) && RunicCompat.isStatAvailable(stat);
        }

        return isEffectEnchantment(enchantment) && !EnchantBlacklist.isBlacklisted(enchantment);
    }

    @Override
    public ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        if (!stack.is(ModItems.BLANK_ETCHING.get())) {
            return super.applyEnchantments(stack, enchantments);
        }
        for (EnchantmentInstance enchantment : enchantments) {
            RuneStatType stat = statTypeForTableEnchantment(enchantment.enchantment);
            if (stat != null) {
                ItemStack etching = createStatEtching(RandomSource.create(), stat);
                if (!etching.isEmpty()) {
                    return etching;
                }
            }

            ItemStack etching = createEffectEtching(enchantment.enchantment);
            if (!etching.isEmpty()) {
                return etching;
            }
        }
        return stack;
    }

    public static Set<ResourceLocation> allowedEffectIds() {
        return RuneItem.allowedEffectIds();
    }

    public static boolean isEffectEnchantment(Holder<Enchantment> holder) {
        return RuneItem.isEffectEnchantment(holder);
    }

    public static ItemStack createEffectEtching(Holder<Enchantment> enchantment) {
        if (!isEffectEnchantment(enchantment) || EnchantBlacklist.isBlacklisted(enchantment)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.ETCHING.get());
        setStoredEnchantment(stack, enchantment, RuneItem.forcedEtchingEffectLevel(enchantment));
        return stack;
    }

    public static void setStoredEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, level);
        stack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
    }

    public static ItemStack createStatEtching(RandomSource random, RuneStatType type) {
        if (type == null || EnchantBlacklist.isStatBlacklisted(type) || !RunicCompat.isStatAvailable(type)) {
            return ItemStack.EMPTY;
        }
        RuneStats stats = RuneStats.singleUnrolled(type);
        ItemStack stack = new ItemStack(ModItems.ETCHING.get());
        RuneStats.set(stack, stats);
        return stack;
    }

    private static RuneStatType statTypeForTableEnchantment(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey()
                .map(ResourceKey::location)
                .filter(id -> id.getNamespace().equals(RunicMod.MOD_ID))
                .map(ResourceLocation::getPath)
                .filter(path -> path.startsWith("stat/"))
                .map(path -> RuneStatType.byId(path.substring("stat/".length())))
                .orElse(null);
    }

    public static ItemStack createRandomStatEtching(RandomSource random) {
        RuneStatType[] all = RuneStatType.values();
        List<RuneStatType> allowed = new ArrayList<>();
        for (RuneStatType type : all) {
            if (!EnchantBlacklist.isStatBlacklisted(type) && RunicCompat.isStatAvailable(type)) {
                allowed.add(type);
            }
        }
        if (allowed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return createStatEtching(random, allowed.get(random.nextInt(allowed.size())));
    }

    public static RuneStats getRolledStatsForTooltip(ItemStack etching) {
        RuneStats template = RuneStats.get(etching);
        if (template == null || template.isEmpty()) {
            return RuneStats.empty();
        }
        return RuneStats.rollForApplication(template, RandomSource.create(), true);
    }

    public static RuneStats getEtchingStats(ItemStack stack) {
        return RuneStats.get(stack);
    }

    // gets primary effect enchantment
    public static Holder<Enchantment> getPrimaryEffectEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
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

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEnchanted()) {
            return true;
        }
        if (!stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return true;
        }
        RuneStats stats = RuneStats.get(stack);
        return stats != null && !stats.isEmpty();
    }
}
