package net.revilodev.runic.mythic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

// stores mythic rune definition

// stores mythic rune definition
public record MythicRuneDefinition(
        ResourceLocation id,
        String translationKey,
        Supplier<Integer> weight,
        Supplier<Double> ruinDamageBonusPercent,
        Supplier<Double> ruinExtraCorruptionChance,
        Supplier<Integer> ruinExtraCorruptionAmount,
        Supplier<Double> ruinDurabilityUseIncreasePercent,
        Supplier<Double> dominionEnhancementPowerBonusPercent,
        Supplier<Double> dominionSynergyPowerBonusPercent,
        Supplier<Integer> hungerDurabilityRestoreOnKill,
        Supplier<Double> hungerExtraCorruptionOnHitChance,
        Supplier<Integer> hungerExtraCorruptionAmount,
        Supplier<Double> voidLowHealthThreshold,
        Supplier<Double> voidDamageBonusPercent,
        Supplier<Integer> voidCombatCorruptionIntervalTicks,
        Supplier<Integer> voidCombatCorruptionAmount,
        Supplier<Double> ascendanceTargetMaxHealthThreshold,
        Supplier<Integer> ascendanceDurationTicks,
        Supplier<Double> ascendanceDamageBonusPercent,
        Supplier<Double> ascendanceSpeedBonusPercent
) {
    public boolean canApplyTo(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isDamageableItem();
    }
}
