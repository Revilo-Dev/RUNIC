package net.revilodev.runic.relic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;
import java.util.function.Supplier;

// stores relic definition

// stores relic definition
public record RelicDefinition(
        ResourceLocation id,
        String translationKey,
        Supplier<Integer> corruption,
        Supplier<Double> durabilityUseIncreasePercent,
        RelicDefinition.Effect effect,
        RelicDefinition.SetBonus setBonus,
        Predicate<ItemStack> targetPredicate
) {
    public int corruptionValue() {
        return Math.max(0, corruption.get());
    }

    public double durabilityUseIncreasePercentValue() {
        return Math.max(0.0D, durabilityUseIncreasePercent.get());
    }

    public boolean canApplyTo(ItemStack stack) {
        return stack != null && !stack.isEmpty() && targetPredicate.test(stack);
    }

    public interface Effect {
    }

    public interface SetBonus {
    }
}
