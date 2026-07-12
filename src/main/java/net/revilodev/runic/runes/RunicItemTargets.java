package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

public final class RunicItemTargets {
    private RunicItemTargets() {}

    public static boolean isWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return RuneSlotCapacityData.isCategory(stack, "sword")
                || RuneSlotCapacityData.isCategory(stack, "axe")
                || RuneSlotCapacityData.isCategory(stack, "trident")
                || RuneSlotCapacityData.isCategory(stack, "mace")
                || item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof TridentItem
                || item instanceof MaceItem
                || hasMainhandModifier(stack, Attributes.ATTACK_DAMAGE)
                || hasMainhandModifier(stack, Attributes.ATTACK_SPEED);
    }

    public static boolean isRangedWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return RuneSlotCapacityData.isCategory(stack, "bow")
                || RuneSlotCapacityData.isCategory(stack, "crossbow")
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }

    public static boolean isMiningTool(ItemStack stack) {
        return RuneSlotCapacityData.isCategory(stack, "pickaxe")
                || RuneSlotCapacityData.isCategory(stack, "axe")
                || RuneSlotCapacityData.isCategory(stack, "shovel")
                || RuneSlotCapacityData.isCategory(stack, "hoe")
                || stack.getItem() instanceof DiggerItem
                || hasMainhandModifier(stack, Attributes.BLOCK_BREAK_SPEED);
    }

    public static boolean isArmor(ItemStack stack) {
        return armorSlot(stack) != null;
    }

    public static EquipmentSlot armorSlot(ItemStack stack) {
        Item item = stack.getItem();
        if (RuneSlotCapacityData.isCategory(stack, "helmet")) return EquipmentSlot.HEAD;
        if (RuneSlotCapacityData.isCategory(stack, "chestplate")) return EquipmentSlot.CHEST;
        if (RuneSlotCapacityData.isCategory(stack, "leggings")) return EquipmentSlot.LEGS;
        if (RuneSlotCapacityData.isCategory(stack, "boots")) return EquipmentSlot.FEET;
        if (item instanceof ArmorItem armor) return armor.getEquipmentSlot();
        if (hasSlotModifier(stack, EquipmentSlot.HEAD, Attributes.ARMOR)) return EquipmentSlot.HEAD;
        if (hasSlotModifier(stack, EquipmentSlot.CHEST, Attributes.ARMOR)) return EquipmentSlot.CHEST;
        if (hasSlotModifier(stack, EquipmentSlot.LEGS, Attributes.ARMOR)) return EquipmentSlot.LEGS;
        if (hasSlotModifier(stack, EquipmentSlot.FEET, Attributes.ARMOR)) return EquipmentSlot.FEET;
        if (hasSlotModifier(stack, EquipmentSlot.HEAD, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.HEAD;
        if (hasSlotModifier(stack, EquipmentSlot.CHEST, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.CHEST;
        if (hasSlotModifier(stack, EquipmentSlot.LEGS, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.LEGS;
        if (hasSlotModifier(stack, EquipmentSlot.FEET, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.FEET;
        return null;
    }

    public static boolean isRunicGear(ItemStack stack) {
        Item item = stack.getItem();
        return isWeapon(stack)
                || isRangedWeapon(stack)
                || isMiningTool(stack)
                || isArmor(stack)
                || item instanceof ShieldItem
                || item instanceof ElytraItem
                || item instanceof FishingRodItem;
    }

    private static boolean hasMainhandModifier(ItemStack stack, Holder<Attribute> attribute) {
        return hasSlotModifier(stack, EquipmentSlot.MAINHAND, attribute);
    }

    private static boolean hasSlotModifier(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute) {
        final boolean[] found = {false};
        stack.forEachModifier(slot, (holder, modifier) -> {
            if (holder.unwrapKey().equals(attribute.unwrapKey()) && modifier.amount() != 0.0D) {
                found[0] = true;
            }
        });
        return found[0];
    }
}
