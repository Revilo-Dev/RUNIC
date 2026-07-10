package net.revilodev.runic.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.runes.UniqueRuneSources;
import net.revilodev.runic.stat.RuneStatType;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RunicMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RUNIC_ITEMS_TAB =
            CREATIVE_MODE_TABS.register("runic_items_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ARTISANS_WORKBENCH.get()))
                    .title(Component.translatable("creativetab.runicmod.runic_items"))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.ARTISANS_WORKBENCH.get());
                        output.accept(ModBlocks.ETCHING_TABLE.get());
                        output.accept(ModItems.BLANK_ETCHING.get());
                        output.accept(ModItems.BLANK_INSCRIPTION.get());
                        output.accept(ModItems.EXPANSION_INSCRIPTION.get());
                        output.accept(ModItems.REPAIR_INSCRIPTION.get());
                        output.accept(ModItems.NULLIFICATION_INSCRIPTION.get());
                        output.accept(ModItems.UPGRADE_INSCRIPTION.get());
                        output.accept(ModItems.REROLL_INSCRIPTION.get());
                        output.accept(ModItems.WILD_INSCRIPTION.get());
                        output.accept(ModItems.CURSED_INSCRIPTION.get());
                        output.accept(ModItems.EXTRACTION_INSCRIPTION.get());
                        output.accept(ModItems.RESONANCE_INSCRIPTION.get());
                        output.accept(ModItems.PURIFICATION_INSCRIPTION.get());
                        output.accept(ModItems.STABILIZATION_INSCRIPTION.get());
                        output.accept(ModItems.TEMPERING_INSCRIPTION.get());
                        output.accept(ModItems.RELIC_SOCKET_INSCRIPTION.get());
                        output.accept(ModItems.DRAGON_HEART.get());
                        output.accept(ModItems.ELDER_GUARDIANS_EYE.get());
                        output.accept(ModItems.WITHER_CHARGE.get());
                        output.accept(ModItems.WARDENS_SOUL.get());
                        for (ResourceLocation id : MythicRuneRegistry.ids()) {
                            ItemStack mythic = RuneItem.createMythicRune(id);
                            if (!mythic.isEmpty()) output.accept(mythic);
                        }
                        RandomSource random = RandomSource.create();

                        for (RuneStatType type : RuneStatType.values()) {
                            ItemStack statRune = RuneItem.createStatRune(random, type);
                            if (!statRune.isEmpty()) output.accept(statRune);

                            if (!UniqueRuneSources.isUniqueEtchingStat(type)) {
                                ItemStack statEtching = EtchingItem.createStatEtching(random, type);
                                if (!statEtching.isEmpty()) output.accept(statEtching);
                            }
                        }

                        params.holders()
                                .lookup(Registries.ENCHANTMENT)
                                .ifPresent((HolderLookup.RegistryLookup<Enchantment> enchants) -> {
                                    for (ResourceLocation id : RuneItem.allowedEffectIds()) {
                                        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
                                        enchants.get(key).ifPresent((Holder<Enchantment> holder) -> {
                                            ItemStack effectRune = RuneItem.createEffectRune(holder);
                                            if (!effectRune.isEmpty()) output.accept(effectRune);

                                            if (!UniqueRuneSources.isUniqueEtchingEffect(id)) {
                                                ItemStack effectEtching = EtchingItem.createEffectEtching(holder);
                                                if (!effectEtching.isEmpty()) output.accept(effectEtching);
                                            }
                                        });
                                    }
                                });
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
