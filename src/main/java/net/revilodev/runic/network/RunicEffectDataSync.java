package net.revilodev.runic.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.runes.RunicEffectEnchantments;

import java.util.HashSet;
import java.util.Set;


public record RunicEffectDataSync(Set<ResourceLocation> effects) implements CustomPacketPayload {
    // sync packet for allowed effect ids
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "runic_effects");
    public static final Type<RunicEffectDataSync> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RunicEffectDataSync> CODEC =
            new StreamCodec<>() {
                @Override
                public RunicEffectDataSync decode(RegistryFriendlyByteBuf buf) {
                    // flat list of enchant ids
                    int count = buf.readVarInt();
                    Set<ResourceLocation> ids = new HashSet<>();
                    for (int i = 0; i < count; i++) {
                        ids.add(ResourceLocation.parse(buf.readUtf()));
                    }
                    return new RunicEffectDataSync(ids);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RunicEffectDataSync msg) {
                    buf.writeVarInt(msg.effects().size());
                    msg.effects().forEach(id -> buf.writeUtf(id.toString()));
                }
            };

    public static final IPayloadHandler<RunicEffectDataSync> HANDLER = (msg, ctx) -> {
        if (Minecraft.getInstance().level != null) {
            // client keeps the same whitelist as the server
            RunicEffectEnchantments.importFromNetwork(msg.effects());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
