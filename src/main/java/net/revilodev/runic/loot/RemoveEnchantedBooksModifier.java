package net.revilodev.runic.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.RunicConfig;

public class RemoveEnchantedBooksModifier extends LootModifier {
    public static final MapCodec<RemoveEnchantedBooksModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, RemoveEnchantedBooksModifier::new));

    public RemoveEnchantedBooksModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (RunicConfig.disableRunicLoot()) {
            return generatedLoot;
        }
        generatedLoot.removeIf(stack -> stack.is(Items.ENCHANTED_BOOK) && !keepWhitelistedEnchantments(stack));
        return generatedLoot;
    }

    private static boolean keepWhitelistedEnchantments(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty() || RunicConfig.enchantmentWhitelist().isEmpty()) {
            return false;
        }

        ItemEnchantments.Mutable kept = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            ResourceLocation id = enchantment.unwrapKey().map(ResourceKey::location).orElse(null);
            if (RunicConfig.isEnchantmentWhitelisted(id)) {
                kept.set(enchantment, entry.getIntValue());
            }
        }
        ItemEnchantments result = kept.toImmutable();
        if (result.isEmpty()) return false;
        stack.set(DataComponents.STORED_ENCHANTMENTS, result);
        return true;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
