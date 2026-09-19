package net.revilodev.runic.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.revilodev.runic.enchanting.EnchantingResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the enchanting table's lapis-only resource slot. */
@Mixin(targets = "net.minecraft.world.inventory.EnchantmentMenu$3")
abstract class EnchantmentResourceSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void runic$acceptEnchantingResources(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(EnchantingResource.isValid(stack));
    }

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void runic$removeVanillaLapisOverlay(CallbackInfoReturnable<Pair<ResourceLocation, ResourceLocation>> callback) {
        callback.setReturnValue(null);
    }
}
