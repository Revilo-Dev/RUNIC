package net.revilodev.runic.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.relic.RelicEffects;

public record UseRelicPowerPayload() implements CustomPacketPayload {
    public static final Type<UseRelicPowerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "use_relic_power"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UseRelicPowerPayload> STREAM_CODEC =
            StreamCodec.unit(new UseRelicPowerPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseRelicPowerPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                RelicEffects.useRelicPower(player);
            }
        });
    }
}
