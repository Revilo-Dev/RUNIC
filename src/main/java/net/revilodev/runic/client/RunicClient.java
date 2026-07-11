package net.revilodev.runic.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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

    public static void onAddEntityLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            addFrozenLayer(event.getRenderer(type));
        }

        for (String skin : event.getSkins()) {
            addFrozenLayer(event.getSkin(skin));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addFrozenLayer(EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new FrozenIceLayer<>((RenderLayerParent<LivingEntity, EntityModel<LivingEntity>>) livingRenderer));
        }
    }
}
