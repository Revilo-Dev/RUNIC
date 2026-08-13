package net.revilodev.runic.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.revilodev.runic.network.payload.UseRelicPowerPayload;
import net.revilodev.runic.particle.BloodDropParticle;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.particle.StunStarParticle;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.revilodev.runic.screen.ModMenuTypes;
import net.revilodev.runic.screen.custom.ArtisansWorkbenchScreen;
import net.revilodev.runic.screen.custom.EtchingTableScreen;

public final class RunicClient {
    private static final KeyMapping RELIC_POWER_KEY = new KeyMapping(
            "key.runic.relic_power",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.categories.runic"
    );
    private static long relicDurationEndMillis;
    private static long relicCooldownEndMillis;
    private static int relicMaxDurationTicks;
    private static int relicMaxCooldownTicks;

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

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RELIC_POWER_KEY);
    }

    public static void onClientKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;
        while (RELIC_POWER_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new UseRelicPowerPayload());
        }
    }

    public static void updateRelicPowerHud(int durationTicks, int maxDurationTicks, int cooldownTicks, int maxCooldownTicks) {
        long now = System.currentTimeMillis();
        relicDurationEndMillis = durationTicks > 0 ? now + durationTicks * 50L : 0L;
        relicCooldownEndMillis = cooldownTicks > 0 ? now + cooldownTicks * 50L : 0L;
        relicMaxDurationTicks = Math.max(0, maxDurationTicks);
        relicMaxCooldownTicks = Math.max(0, maxCooldownTicks);
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        long now = System.currentTimeMillis();
        int durationRemaining = remainingTicks(relicDurationEndMillis, now);
        int cooldownRemaining = remainingTicks(relicCooldownEndMillis, now);
        if (durationRemaining <= 0 && cooldownRemaining <= 0) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int barWidth = 44;
        int barHeight = 4;
        int x = (width - barWidth) / 2;
        int y = height / 2 + 16;

        if (durationRemaining > 0 && relicMaxDurationTicks > 0) {
            String seconds = secondsText(durationRemaining);
            graphics.drawCenteredString(mc.font, seconds, width / 2, y - 10, 0xFF50D8FF);
            drawBar(graphics, x, y, barWidth, barHeight, (float) durationRemaining / (float) relicMaxDurationTicks, 0xFF50D8FF);
            y += 8;
        }

        if (cooldownRemaining > 0 && relicMaxCooldownTicks > 0) {
            drawBar(graphics, x, y, barWidth, barHeight, (float) cooldownRemaining / (float) relicMaxCooldownTicks, 0xFFEFEFEF);
            graphics.drawCenteredString(mc.font, secondsText(cooldownRemaining), width / 2, y + 6, 0xFFFFFFFF);
        }
    }

    private static int remainingTicks(long endMillis, long nowMillis) {
        if (endMillis <= nowMillis) return 0;
        return Mth.ceil((endMillis - nowMillis) / 50.0D);
    }

    private static String secondsText(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1fs", Math.max(0.0D, ticks / 20.0D));
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int fillColor) {
        int clamped = Math.max(0, Math.min(width, Math.round(width * progress)));
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xAA000000);
        graphics.fill(x, y, x + width, y + height, 0xAA202020);
        if (clamped > 0) {
            graphics.fill(x, y, x + clamped, y + height, fillColor);
        }
    }

}
