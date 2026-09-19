package net.revilodev.runic.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.mixin.EnchantmentMenuAccessor;

/** Rerolls the current enchanting-table offers after charging the player three levels. */
public record RerollEtchingPayload() implements CustomPacketPayload {
    public static final Type<RerollEtchingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "reroll_etching"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RerollEtchingPayload> STREAM_CODEC =
            StreamCodec.unit(new RerollEtchingPayload());

    private static final int XP_COST_LEVELS = 3;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RerollEtchingPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EnchantmentMenu menu)) return;

            Slot enchantmentSlot = menu.getSlot(0);
            ItemStack input = enchantmentSlot.getItem();
            if (input.isEmpty() || !input.isEnchantable()) return;
            if (!player.getAbilities().instabuild && player.experienceLevel < XP_COST_LEVELS) return;

            player.onEnchantmentPerformed(input, XP_COST_LEVELS);
            ((EnchantmentMenuAccessor) menu).runic$getEnchantmentSeed().set(player.getEnchantmentSeed());
            enchantmentSlot.setChanged();
            menu.broadcastChanges();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        });
    }
}
