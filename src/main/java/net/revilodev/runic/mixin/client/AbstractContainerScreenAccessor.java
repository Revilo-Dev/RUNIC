package net.revilodev.runic.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("menu")
    AbstractContainerMenu runic$getMenu();

    @Accessor("leftPos")
    int runic$getLeftPos();

    @Accessor("topPos")
    int runic$getTopPos();
}
