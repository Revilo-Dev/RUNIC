package net.revilodev.runic.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GearAttribute {
    SEALED("sealed", Component.literal("Sealed"), ChatFormatting.WHITE),
    CURSED("cursed", Component.literal("Cursed"), ChatFormatting.DARK_RED),
    INSTABLE("instable", Component.literal("Unstable"), ChatFormatting.GRAY),
    NEGATIVE("negative", Component.literal("Negative"), ChatFormatting.WHITE),
    ANCIENT("ancient", Component.literal("Ancient"), ChatFormatting.GOLD),
    BRITTLE("brittle", Component.literal("Brittle"), ChatFormatting.RED),
    FRACTURED("fractured", Component.translatable("tooltip.runic.attribute.fractured"), ChatFormatting.DARK_PURPLE),
    EXHAUSTED("exhausted", Component.translatable("tooltip.runic.attribute.exhausted"), ChatFormatting.DARK_GRAY),
    OVERFORGED("overforged", Component.translatable("tooltip.runic.attribute.overforged"), ChatFormatting.GOLD),
    CHAOTIC("chaotic", Component.translatable("tooltip.runic.attribute.chaotic"), ChatFormatting.LIGHT_PURPLE),
    REINFORCED("reinforced", Component.translatable("tooltip.runic.attribute.reinforced"), ChatFormatting.GREEN),
    TEMPERED("tempered", Component.translatable("tooltip.runic.attribute.tempered"), ChatFormatting.AQUA),
    HARMONIZED("harmonized", Component.translatable("tooltip.runic.attribute.harmonized"), ChatFormatting.BLUE);

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
