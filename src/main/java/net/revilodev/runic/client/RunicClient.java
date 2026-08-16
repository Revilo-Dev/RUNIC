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
    private static long relicEndMs;
    private static long cdEndMs;
    private static int relicTicks;
    private static int cdTicks;

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

    public static void updateRelicPowerHud(int dur, int maxDur, int cd, int maxCd) {
        long now = System.currentTimeMillis();
        relicEndMs = dur > 0 ? now + dur * 50L : 0L;
        cdEndMs = cd > 0 ? now + cd * 50L : 0L;
        relicTicks = Math.max(0, maxDur);
        cdTicks = Math.max(0, maxCd);
    }


    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        long now = System.currentTimeMillis();
        int durLeft = remainingTicks(relicEndMs, now);
        int cdLeft = remainingTicks(cdEndMs, now);
        if (durLeft <= 0 && cdLeft <= 0) return;

        GuiGraphics gg = event.getGuiGraphics();
        int w = gg.guiWidth();
        int h = gg.guiHeight();
        int barW = 44;
        int barH = 4;
        int x = (w - barW) / 2;
        int y = h / 2 + 16;

        if (durLeft > 0 && relicTicks > 0) {
            String secs = secondsText(durLeft);
            gg.drawCenteredString(mc.font, secs, w / 2, y - 10, 0xFF50D8FF);
            drawBar(gg, x, y, barW, barH, (float) durLeft / relicTicks, 0xFF50D8FF);
            y += 8;
        }

        if (cdLeft > 0 && cdTicks > 0) {
            drawBar(gg, x, y, barW, barH, (float) cdLeft / cdTicks, 0xFFEFEFEF);
            gg.drawCenteredString(mc.font, secondsText(cdLeft), w / 2, y + 6, 0xFFFFFFFF);
        }
    }

    private static int remainingTicks(long endMs, long nowMs) {
        if (endMs <= nowMs) return 0;
        return Mth.ceil((endMs - nowMs) / 50.0D);
    }

    private static String secondsText(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1fs", Math.max(0.0D, ticks / 20.0D));
    }

    private static void drawBar(GuiGraphics gg, int x, int y, int w, int h, float pct, int color) {
        int fill = Math.max(0, Math.min(w, Math.round(w * pct)));
        gg.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xAA000000);
        gg.fill(x, y, x + w, y + h, 0xAA202020);
        if (fill > 0) {
            gg.fill(x, y, x + fill, y + h, color);
        }
    }

}
