package net.revilodev.runic.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.synergy.SynergyEffects;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class FrozenClientEffects {
    private static final ResourceLocation SNOW_OUTLINE =
            ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static final ResourceLocation ICE_TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/renderlayer/ice.png");

    private FrozenClientEffects() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        MobEffectInstance effect = mc.player.getEffect(ModMobEffects.FROZEN);
        if (effect != null) {
            mc.player.setTicksFrozen(mc.player.getTicksRequiredToFreeze());
        } else if (mc.player.getTicksFrozen() > 0) {
            mc.player.setTicksFrozen(0);
        }
    }

    @SubscribeEvent
    public static void onFrozenOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || SynergyEffects.frozenPhase(mc.player) <= 0) return;

        GuiGraphics gg = event.getGuiGraphics();
        int w = gg.guiWidth();
        int h = gg.guiHeight();

        RenderSystem.enableBlend();
        gg.setColor(1.0F, 1.0F, 1.0F, 0.45F);
        gg.blit(SNOW_OUTLINE, 0, 0, 0, 0.0F, 0.0F, w, h, w, h);
        gg.setColor(0.82F, 0.93F, 1.0F, 1.0F);
        gg.fill(0, 0, w, h, 0x30A6D8FF);
        gg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        int phase = frozenPhase(entity);
        if (phase <= 0) return;

        float strength = phase == 1 ? 0.025F : 0.0125F;
        double time = (entity.tickCount + event.getPartialTick()) * 0.9D;
        double x = Math.sin(time * 2.7D) * strength;
        double z = Math.cos(time * 2.1D) * strength;
        event.getPoseStack().translate(x, 0.0D, z);
    }

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void onLivingPost(RenderLivingEvent.Post<T, M> event) {
        LivingEntity entity = event.getEntity();
        if (frozenPhase(entity) <= 0) return;

        MultiBufferSource buffers = event.getMultiBufferSource();
        var poseStack = event.getPoseStack();
        var model = event.getRenderer().getModel();

        drawIcePrison(entity, poseStack, buffers, event.getPackedLight());

        // ice layer
        var buffer = buffers.getBuffer(RenderType.entityTranslucent(ICE_TEX));

        poseStack.pushPose();
        // Slightly expand the layer so it stays visible over the entity's base texture.
        poseStack.scale(1.0125F, 1.0125F, 1.0125F);
        model.renderToBuffer(
                poseStack,
                buffer,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                0xD0C8FFFF
        );
        poseStack.popPose();
    }

    private static void drawIcePrison(LivingEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack,
                                        MultiBufferSource buffers, int packedLight) {
        float width = entity.getBbWidth() + 0.16F;
        float height = entity.getBbHeight() + 0.16F;

        poseStack.pushPose();
        poseStack.translate(-width * 0.5F, 0.0F, -width * 0.5F);
        poseStack.scale(width, height, width);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.ICE.defaultBlockState(), poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    // frozen phase
    private static int frozenPhase(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModMobEffects.FROZEN);
        return effect == null ? 0 : (effect.getAmplifier() >= 2 ? 2 : 1);
    }
}
