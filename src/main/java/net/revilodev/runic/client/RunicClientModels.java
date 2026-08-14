package net.revilodev.runic.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.RuneModelMappings;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

// supports runic client models

// supports runic client models
public final class RunicClientModels {

    private RunicClientModels() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, path);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(RunicClientModels::registerAllRuneLikeItems);
    }

    private static void registerAllRuneLikeItems() {
        ResourceLocation pred = id("rune_model");
        register(ModItems.ENHANCED_RUNE.get(), pred);
        register(ModItems.ETCHING.get(), pred);
    }

    private static void register(Item item, ResourceLocation predicateId) {
        ItemProperties.register(item, predicateId, RunicClientModels::runePredicate);
    }


    private static float runePredicate(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        if (stack.isEmpty()) return 0.0F;

        ResourceLocation synergyId = RuneItem.getItemSynergyId(stack);
        if (synergyId != null) {
            return RuneModelMappings.predicateForSynergy(synergyId);
        }

        ResourceLocation mythicId = MythicRuneRegistry.getItemRuneId(stack);
        if (mythicId != null) {
            return RuneModelMappings.predicateForMythic(mythicId);
        }

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            RuneStatType type = stats.view().keySet().iterator().next();
            return RuneModelMappings.predicateForStat(type);
        }

        ItemEnchantments enchants = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) return 0.0F;

        Holder<Enchantment> ench = enchants.keySet().iterator().next();
        ResourceLocation rl = ench.unwrapKey().map(k -> k.location()).orElse(null);
        if (rl == null) return 0.0F;

        return RuneModelMappings.predicateForEnchant(rl);
    }
}
