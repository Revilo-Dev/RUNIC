package net.revilodev.runic.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.runes.RuneSlotCapacityData;

import java.util.HashMap;
import java.util.Map;

public record RuneSlotDataSync(Map<ResourceLocation,Integer> items,
                               Map<String,Integer> defaults,
                               Map<ResourceLocation,Integer> tagCapacities,
                               Map<ResourceLocation,String> itemTypes,
                               Map<ResourceLocation,String> tagTypes) implements CustomPacketPayload {

    // sync packet for rune slot rules
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "rune_slots");
    public static final Type<RuneSlotDataSync> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RuneSlotDataSync> CODEC =
            new StreamCodec<>() {
                @Override
                public RuneSlotDataSync decode(RegistryFriendlyByteBuf buf) {
                    // item capacities
                    int count = buf.readVarInt();
                    Map<ResourceLocation,Integer> items = new HashMap<>();
                    for (int i=0;i<count;i++) {
                        items.put(ResourceLocation.parse(buf.readUtf()), buf.readVarInt());
                    }

                    // default type capacities
                    int dCount = buf.readVarInt();
                    Map<String,Integer> defs = new HashMap<>();
                    for (int i=0;i<dCount;i++) {
                        defs.put(buf.readUtf(), buf.readVarInt());
                    }

                    // tag overrides
                    int tagCount = buf.readVarInt();
                    Map<ResourceLocation,Integer> tagCapacities = new HashMap<>();
                    for (int i = 0; i < tagCount; i++) {
                        tagCapacities.put(ResourceLocation.parse(buf.readUtf()), buf.readVarInt());
                    }

                    // explicit item types
                    int itemTypeCount = buf.readVarInt();
                    Map<ResourceLocation,String> itemTypes = new HashMap<>();
                    for (int i = 0; i < itemTypeCount; i++) {
                        itemTypes.put(ResourceLocation.parse(buf.readUtf()), buf.readUtf());
                    }

                    // tag based item types
                    int tagTypeCount = buf.readVarInt();
                    Map<ResourceLocation,String> tagTypes = new HashMap<>();
                    for (int i = 0; i < tagTypeCount; i++) {
                        tagTypes.put(ResourceLocation.parse(buf.readUtf()), buf.readUtf());
                    }
                    return new RuneSlotDataSync(items, defs, tagCapacities, itemTypes, tagTypes);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RuneSlotDataSync msg) {
                    // item capacities
                    buf.writeVarInt(msg.items().size());
                    msg.items().forEach((id,v)->{
                        buf.writeUtf(id.toString());
                        buf.writeVarInt(v);
                    });

                    // default type capacities
                    buf.writeVarInt(msg.defaults().size());
                    msg.defaults().forEach((k,v)->{
                        buf.writeUtf(k);
                        buf.writeVarInt(v);
                    });

                    // tag overrides
                    buf.writeVarInt(msg.tagCapacities().size());
                    msg.tagCapacities().forEach((id,v)->{
                        buf.writeUtf(id.toString());
                        buf.writeVarInt(v);
                    });

                    // explicit item types
                    buf.writeVarInt(msg.itemTypes().size());
                    msg.itemTypes().forEach((id,type)->{
                        buf.writeUtf(id.toString());
                        buf.writeUtf(type);
                    });

                    // tag based item types
                    buf.writeVarInt(msg.tagTypes().size());
                    msg.tagTypes().forEach((id,type)->{
                        buf.writeUtf(id.toString());
                        buf.writeUtf(type);
                    });
                }
            };

    public static final IPayloadHandler<RuneSlotDataSync> HANDLER = (msg, ctx) -> {
        if (Minecraft.getInstance().level != null) {
            // client cache mirrors the server rules
            RuneSlotCapacityData.importFromNetwork(
                    msg.items(),
                    msg.defaults(),
                    msg.tagCapacities(),
                    msg.itemTypes(),
                    msg.tagTypes()
            );
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
