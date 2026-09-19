package net.revilodev.runic.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.EnhancementCategory;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;

import java.util.List;

/** Defines the enchanting-table resources used to filter etching categories. */
public final class EnchantingResource {
    private static final List<Item> RESOURCES = List.of(
            Items.LAPIS_LAZULI,
            Items.DIAMOND,
            Items.AMETHYST_SHARD,
            Items.EMERALD,
            Items.GOLD_INGOT,
            Items.ECHO_SHARD
    );
    private static final List<ResourceLocation> EMPTY_SLOT_SPRITES = List.of(
            sprite("empty_slot_lapis_lazuli"),
            sprite("empty_slot_diamond"),
            sprite("empty_slot_amethyst_shard"),
            sprite("empty_slot_emerald"),
            sprite("empty_slot_ingot"),
            sprite("empty_slot_echo-shard")
    );

    private EnchantingResource() {}

    public static boolean isValid(ItemStack stack) {
        return RESOURCES.stream().anyMatch(stack::is);
    }

    public static List<ItemStack> displayStacks() {
        return RESOURCES.stream().map(ItemStack::new).toList();
    }

    public static ResourceLocation emptySlotSprite(long tick) {
        return EMPTY_SLOT_SPRITES.get((int) (tick / 20L % EMPTY_SLOT_SPRITES.size()));
    }

    public static boolean allows(ItemStack resource, Holder<Enchantment> enchantment) {
        if (resource.isEmpty() || resource.is(Items.LAPIS_LAZULI) || isEchoShard(resource)) {
            return true;
        }
        return categoryFor(resource) == EtchingItem.categoryForTableEnchantment(enchantment);
    }

    public static ItemStack itemFor(EnhancementCategory category) {
        if (category == null) {
            return new ItemStack(Items.LAPIS_LAZULI);
        }
        return new ItemStack(switch (category) {
            case OFFENSIVE -> Items.DIAMOND;
            case DEFENSIVE -> Items.AMETHYST_SHARD;
            case ELEMENTAL -> Items.EMERALD;
            case UTILITY -> Items.GOLD_INGOT;
            case FORBIDDEN -> Items.ECHO_SHARD;
            case SYNERGY -> Items.LAPIS_LAZULI;
        });
    }

    public static boolean allowsOfferLevel(ItemStack resource, int offerSlot, Holder<Enchantment> enchantment) {
        if (isEchoShard(resource)) {
            return hasRarity(enchantment, "epic", "legendary");
        }

        return switch (offerSlot) {
            case 0 -> hasRarity(enchantment, "common", "uncommon");
            case 1 -> hasRarity(enchantment, "uncommon", "rare", "epic");
            case 2 -> hasRarity(enchantment, "epic", "legendary");
            default -> false;
        };
    }

    // Allows enchanting books only through a config whitelist.
    public static boolean allowsBook(Holder<Enchantment> enchantment) {
        ResourceLocation id = enchantment.unwrapKey().map(ResourceKey::location).orElse(null);
        return RunicConfig.canEnchantBook(id);
    }

    private static EnhancementCategory categoryFor(ItemStack resource) {
        if (resource.is(Items.DIAMOND)) return EnhancementCategory.OFFENSIVE;
        if (resource.is(Items.AMETHYST_SHARD)) return EnhancementCategory.DEFENSIVE;
        if (resource.is(Items.EMERALD)) return EnhancementCategory.ELEMENTAL;
        if (resource.is(Items.GOLD_INGOT)) return EnhancementCategory.UTILITY;
        return null;
    }

    public static boolean isEchoShard(ItemStack stack) {
        return stack.is(Items.ECHO_SHARD);
    }

    public static boolean isRunicEtching(ItemStack stack) {
        return stack.is(ModItems.BLANK_ETCHING.get());
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/enchanting_table/" + path + ".png");
    }

    private static boolean hasRarity(Holder<Enchantment> enchantment, String... allowed) {
        String rarity = net.revilodev.runic.loot.rarity.EnhancementRarities.get(enchantment).key();
        return java.util.Arrays.asList(allowed).contains(rarity);
    }
}
