package net.revilodev.runic.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.revilodev.runic.enchanting.EnchantingResource;
import net.revilodev.runic.item.EnhancementCategory;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.network.payload.RerollEtchingPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentScreen.class)
abstract class EnchantmentScreenMixin {
    private static final ResourceLocation REROLL_BUTTON = ResourceLocation.fromNamespaceAndPath(
            "runic", "textures/gui/enchanting_table/re-roll.png"
    );
    private static final ResourceLocation REROLL_BUTTON_DISABLED = ResourceLocation.fromNamespaceAndPath(
            "runic", "textures/gui/enchanting_table/re-roll-disabled.png"
    );
    private static final int REROLL_BUTTON_SIZE = 16;
    private static final int REROLL_BUTTON_X = 25;
    private static final int REROLL_BUTTON_Y = 23;
    private static final int REROLL_COST_LEVELS = 3;
    private static final int REROLL_DELAY_TICKS = 10;

    @Shadow public int time;
    @Shadow public float open;

    private ItemStack runic$rerollInput = ItemStack.EMPTY;
    private int runic$rerollButtonVisibleAt = Integer.MAX_VALUE;

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void runic$renderEnchantingResource(GuiGraphics graphics, float partialTick, int mouseX, int mouseY,
                                                CallbackInfo callback) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        EnchantmentMenu menu = (EnchantmentMenu) accessor.runic$getMenu();
        if (menu.getSlot(1).hasItem()) return;
        graphics.blit(
                EnchantingResource.emptySlotSprite(System.currentTimeMillis() / 50L),
                accessor.runic$getLeftPos() + 35,
                accessor.runic$getTopPos() + 47,
                0,
                0,
                16,
                16,
                16,
                16
        );
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void runic$renderRerollButton(GuiGraphics graphics, float partialTick, int mouseX, int mouseY,
                                           CallbackInfo callback) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        EnchantmentMenu menu = (EnchantmentMenu) accessor.runic$getMenu();
        if (!runic$isRerollButtonVisible(menu)) return;

        Minecraft minecraft = Minecraft.getInstance();
        boolean enabled = minecraft.player != null
                && (minecraft.player.getAbilities().instabuild || minecraft.player.experienceLevel >= REROLL_COST_LEVELS);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        graphics.blit(
                enabled ? REROLL_BUTTON : REROLL_BUTTON_DISABLED,
                rerollButtonX(accessor),
                rerollButtonY(accessor),
                0,
                0,
                REROLL_BUTTON_SIZE,
                REROLL_BUTTON_SIZE,
                REROLL_BUTTON_SIZE,
                REROLL_BUTTON_SIZE
        );
        graphics.pose().popPose();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void runic$clickRerollButton(double mouseX, double mouseY, int button,
                                         CallbackInfoReturnable<Boolean> callback) {
        if (button != 0) return;

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        EnchantmentMenu menu = (EnchantmentMenu) accessor.runic$getMenu();
        if (!runic$isRerollButtonVisible(menu) || !isRerollButtonHovered(accessor, mouseX, mouseY)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && (minecraft.player.getAbilities().instabuild || minecraft.player.experienceLevel >= REROLL_COST_LEVELS)) {
            PacketDistributor.sendToServer(new RerollEtchingPayload());
        }
        callback.setReturnValue(true);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void runic$renderRerollTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                            CallbackInfo callback) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        EnchantmentMenu menu = (EnchantmentMenu) accessor.runic$getMenu();
        if (!runic$isRerollButtonVisible(menu) || !isRerollButtonHovered(accessor, mouseX, mouseY)) return;

        graphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                List.of(
                        Component.translatable("button.runic.reroll_etching").withStyle(ChatFormatting.GOLD),
                        Component.translatable("tooltip.runic.reroll_etching").withStyle(ChatFormatting.GRAY)
                ),
                mouseX,
                mouseY
        );
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderComponentTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V")
    )
    private void runic$renderEnchantmentTooltip(GuiGraphics graphics, Font font, List<Component> vanillaTooltip,
                                                 int mouseX, int mouseY) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        EnchantmentMenu menu = (EnchantmentMenu) accessor.runic$getMenu();
        int offerSlot = offerSlot(accessor, mouseX, mouseY);
        if (offerSlot < 0 || menu.enchantClue[offerSlot] < 0) {
            graphics.renderComponentTooltip(font, vanillaTooltip, mouseX, mouseY);
            return;
        }

        Holder.Reference<Enchantment> enchantment = net.minecraft.client.Minecraft.getInstance()
                .level
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(menu.enchantClue[offerSlot])
                .orElse(null);
        if (enchantment == null) {
            graphics.renderComponentTooltip(font, vanillaTooltip, mouseX, mouseY);
            return;
        }

        graphics.renderComponentTooltip(font, buildTooltip(enchantment), mouseX, mouseY);
    }

    private static int offerSlot(AbstractContainerScreenAccessor accessor, int mouseX, int mouseY) {
        int relativeX = mouseX - accessor.runic$getLeftPos() - 60;
        int relativeY = mouseY - accessor.runic$getTopPos() - 14;
        if (relativeX < 0 || relativeX >= 108 || relativeY < 0 || relativeY >= 57) {
            return -1;
        }
        return relativeY / 19;
    }

    private boolean runic$isRerollButtonVisible(EnchantmentMenu menu) {
        ItemStack input = menu.getSlot(0).getItem();
        if (!ItemStack.matches(input, runic$rerollInput)) {
            runic$rerollInput = input.copy();
            runic$rerollButtonVisibleAt = input.isEmpty() ? Integer.MAX_VALUE : time + REROLL_DELAY_TICKS;
        }
        return !input.isEmpty() && input.isEnchantable() && open >= 1.0F && time >= runic$rerollButtonVisibleAt;
    }

    private static int rerollButtonX(AbstractContainerScreenAccessor accessor) {
        return accessor.runic$getLeftPos() + REROLL_BUTTON_X;
    }

    private static int rerollButtonY(AbstractContainerScreenAccessor accessor) {
        return accessor.runic$getTopPos() + REROLL_BUTTON_Y;
    }

    private static boolean isRerollButtonHovered(AbstractContainerScreenAccessor accessor, double mouseX, double mouseY) {
        int buttonX = rerollButtonX(accessor);
        int buttonY = rerollButtonY(accessor);
        return mouseX >= buttonX && mouseX < buttonX + REROLL_BUTTON_SIZE
                && mouseY >= buttonY && mouseY < buttonY + REROLL_BUTTON_SIZE;
    }

    private static List<Component> buildTooltip(Holder<Enchantment> enchantment) {
        List<Component> tooltip = new ArrayList<>();
        EnhancementCategory category = EtchingItem.categoryForTableEnchantment(enchantment);
        EnhancementRarity rarity = EnhancementRarities.get(enchantment);
        Enchantment value = enchantment.value();

        tooltip.add(value.description().copy().withStyle(category.color()));
        tooltip.add(Component.literal("\u2605".repeat(rarity.stars() + 1)).withStyle(rarity.color()));
        appendDescription(tooltip, enchantment);
        return tooltip;
    }

    private static void appendDescription(List<Component> tooltip, Holder<Enchantment> enchantment) {
        if (!Screen.hasAltDown()) {
            tooltip.add(Component.literal("Hold Alt to read more").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        ResourceLocation id = enchantment.unwrapKey().map(key -> key.location()).orElse(null);
        if (id == null) return;
        String descriptionKey = id.getNamespace().equals("runic") && id.getPath().startsWith("stat/")
                ? "tooltip.runic.stat_desc." + id.getPath().substring("stat/".length())
                : "tooltip.runic." + id.getPath();
        if (I18n.exists(descriptionKey)) {
            tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }
    }

}
