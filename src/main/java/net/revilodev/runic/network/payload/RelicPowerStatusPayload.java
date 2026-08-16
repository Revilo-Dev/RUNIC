package net.revilodev.runic.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.client.RunicClient;

public record RelicPowerStatusPayload(int durationTicks, int maxDurationTicks, int cooldownTicks, int maxCooldownTicks) implements CustomPacketPayload {
    public static final Type<RelicPowerStatusPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "relic_power_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RelicPowerStatusPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RelicPowerStatusPayload::durationTicks,
            ByteBufCodecs.VAR_INT, RelicPowerStatusPayload::maxDurationTicks,
            ByteBufCodecs.VAR_INT, RelicPowerStatusPayload::cooldownTicks,
            ByteBufCodecs.VAR_INT, RelicPowerStatusPayload::maxCooldownTicks,
            RelicPowerStatusPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RelicPowerStatusPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> RunicClient.updateRelicPowerHud(payload.durationTicks(), payload.maxDurationTicks(), payload.cooldownTicks(), payload.maxCooldownTicks()));
    }
}
