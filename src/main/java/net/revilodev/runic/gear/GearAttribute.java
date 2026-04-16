package net.revilodev.runic.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GearAttribute {
    SEALED("sealed", Component.literal("Sealed"), ChatFormatting.WHITE),
    CURSED("cursed", Component.literal("Cursed"), ChatFormatting.DARK_RED),
    INSTABLE("instable", Component.literal("Unstable"), ChatFormatting.GRAY),
    NEGATIVE("negative", Component.literal("Negative"), ChatFormatting.WHITE),
    ANCIENT("ancient", Component.literal("Ancient"), ChatFormatting.GOLD),
    BRITTLE("brittle", Component.literal("Brittle"), ChatFormatting.RED);

    private final String id;
    private final Component displayName;
    private final ChatFormatting color;

    GearAttribute(String id, Component displayName, ChatFormatting color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return displayName;
    }

    public ChatFormatting color() {
        return color;
    }
}
