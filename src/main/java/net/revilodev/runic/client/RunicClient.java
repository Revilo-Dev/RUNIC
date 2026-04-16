package net.revilodev.runic.client;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.revilodev.runic.particle.BloodDropParticle;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.particle.StunStarParticle;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.revilodev.runic.screen.ModMenuTypes;
import net.revilodev.runic.screen.custom.ArtisansWorkbenchScreen;
import net.revilodev.runic.screen.custom.EtchingTableScreen;

public final class RunicClient {
    private RunicClient() {
    }

    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ARTISANS_WORKBENCH.get(), ArtisansWorkbenchScreen::new);
        event.register(ModMenuTypes.ETCHING_TABLE.get(), EtchingTableScreen::new);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.STUN_STAR.get(), StunStarParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLOOD_DROP.get(), BloodDropParticle.Provider::new);
    }
}
