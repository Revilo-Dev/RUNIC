package net.revilodev.runic.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.util.RandomSource;
import net.revilodev.runic.enchanting.EnchantingResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentMenu.class)
abstract class EnchantmentMenuMixin {
    @Shadow private Container enchantSlots;
    @Shadow public int[] costs;
    private int runic$activeOfferSlot;

    @Redirect(
            method = "quickMoveStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean runic$quickMoveEnchantingResources(ItemStack stack, net.minecraft.world.item.Item item) {
        return item == Items.LAPIS_LAZULI && EnchantingResource.isValid(stack);
    }

    @Inject(method = "getEnchantmentList", at = @At("HEAD"))
    private void runic$setActiveOfferSlot(net.minecraft.core.RegistryAccess registries, ItemStack stack, int slot, int cost,
                                           CallbackInfoReturnable<List<EnchantmentInstance>> callback) {
        runic$activeOfferSlot = slot;
        if (EnchantingResource.isRunicEtching(stack) && EnchantingResource.isEchoShard(enchantSlots.getItem(1))) {
            Arrays.fill(costs, 25);
        }
    }

    @Redirect(
            method = "getEnchantmentList",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;")
    )
    private List<EnchantmentInstance> runic$selectTieredEnchantments(RandomSource random, ItemStack stack, int cost,
                                                                       Stream<Holder<Enchantment>> candidates) {
        ItemStack resource = enchantSlots.getItem(1);
        Stream<Holder<Enchantment>> filtered = candidates;

        if (stack.is(Items.BOOK)) {
            filtered = filtered.filter(EnchantingResource::allowsBook);
        } else if (EnchantingResource.isRunicEtching(stack)) {
            filtered = filtered.filter(enchantment -> EnchantingResource.allows(resource, enchantment));
        }

        List<Holder<Enchantment>> allowedEnchantments = filtered.toList();
        ItemStack tierResource = EnchantingResource.isRunicEtching(stack) ? resource : ItemStack.EMPTY;
        List<Holder<Enchantment>> tierEnchantments = allowedEnchantments.stream()
                .filter(enchantment -> EnchantingResource.allowsOfferLevel(tierResource, runic$activeOfferSlot, enchantment))
                .toList();

        Stream<Holder<Enchantment>> selectedEnchantments = tierEnchantments.isEmpty() && !EnchantingResource.isEchoShard(tierResource)
                ? allowedEnchantments.stream()
                : tierEnchantments.stream();
        int selectionCost = EnchantingResource.isRunicEtching(stack) && EnchantingResource.isEchoShard(resource) ? 25 : cost;
        return EnchantmentHelper.selectEnchantment(random, stack, selectionCost, selectedEnchantments);
    }
}
