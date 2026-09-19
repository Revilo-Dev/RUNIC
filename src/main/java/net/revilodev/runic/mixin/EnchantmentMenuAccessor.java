package net.revilodev.runic.mixin;

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Provides the enchanting-table reroll action access to the synchronized seed. */
@Mixin(EnchantmentMenu.class)
public interface EnchantmentMenuAccessor {
    @Accessor("enchantmentSeed")
    DataSlot runic$getEnchantmentSeed();
}
