package net.revilodev.runic.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.revilodev.runic.network.payload.PlaceEtchingRecipePayload;
import net.revilodev.runic.network.payload.RerollEtchingPayload;
import net.revilodev.runic.network.payload.RelicPowerStatusPayload;
import net.revilodev.runic.network.payload.UseRelicPowerPayload;


public final class RunicNetwork {
    private RunicNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToServer(PlaceEtchingRecipePayload.TYPE, PlaceEtchingRecipePayload.STREAM_CODEC, PlaceEtchingRecipePayload::handle);
        r.playToServer(RerollEtchingPayload.TYPE, RerollEtchingPayload.STREAM_CODEC, RerollEtchingPayload::handle);
        r.playToServer(UseRelicPowerPayload.TYPE, UseRelicPowerPayload.STREAM_CODEC, UseRelicPowerPayload::handle);
        r.playToClient(RelicPowerStatusPayload.TYPE, RelicPowerStatusPayload.STREAM_CODEC, RelicPowerStatusPayload::handle);
    }
}
