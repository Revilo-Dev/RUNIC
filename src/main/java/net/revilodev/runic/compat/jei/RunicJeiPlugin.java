package net.revilodev.runic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.compat.RunicCompat;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.relic.RelicRegistry;
import net.revilodev.runic.runes.UniqueRuneSources;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.recipe.ModRecipeTypes;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public final class RunicJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EtchingTableCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            List<EtchingTableRecipe> recipes = mc.level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.ETCHING_TABLE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();
            registration.addRecipes(EtchingTableCategory.TYPE, recipes);
        }

        registerRuneInfo(registration);
        registerRelicInfo(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ETCHING_TABLE.get()), EtchingTableCategory.TYPE);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var registry = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<ItemStack> extra = new ArrayList<>();
        extra.addAll(buildRuneStacks(registry));
        extra.addAll(buildEtchingStacks(registry));

        registration.addExtraItemStacks(extra);
    }


    private static void registerRuneInfo(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var registry = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (RuneStatType type : RuneStatType.values()) {
            if (!RunicCompat.isStatAvailable(type)) continue;
            ItemStack stack = RuneItem.createStatRune(RandomSource.create(), type);
            if (stack.isEmpty()) continue;

            String key = "tooltip.runic.stat_desc." + type.id();
            registration.addIngredientInfo(
                    List.of(stack),
                    VanillaTypes.ITEM_STACK,
                    Component.translatable(key)
            );
        }

        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
            if (holder == null) continue;

            ItemStack stack = RuneItem.createEffectRune(holder);
            if (stack.isEmpty()) continue;

            String descKey = "tooltip.runic." + id.getPath();
            Component desc = I18n.exists(descKey)
                    ? Component.translatable(descKey)
                    : Component.literal("Applies " + holder.value().description().getString() + ".");

            registration.addIngredientInfo(
                    List.of(stack),
                    VanillaTypes.ITEM_STACK,
                    desc
            );
        }

        for (ResourceLocation id : MythicRuneRegistry.ids()) {
            ItemStack stack = RuneItem.createMythicRune(id);
            if (!stack.isEmpty()) {
                registration.addIngredientInfo(
                        List.of(stack),
                        VanillaTypes.ITEM_STACK,
                        Component.translatable("tooltip.runic.mythic_desc." + id.getPath().substring("mythic/".length()))
                );
            }
        }
    }

    // registers relic info
    private static void registerRelicInfo(IRecipeRegistration registration) {
        List<ItemStack> relics = List.of(
                new ItemStack(ModItems.DRAGON_HEART.get()),
                new ItemStack(ModItems.ELDER_GUARDIANS_EYE.get()),
                new ItemStack(ModItems.WITHER_CHARGE.get()),
                new ItemStack(ModItems.WARDENS_SOUL.get())
        );

        for (ItemStack stack : relics) {
            ResourceLocation relicId = RelicRegistry.relicItemId(stack);
            if (relicId == null) continue;

            List<Component> lines = new ArrayList<>();
            RelicRegistry.appendRelicItemTooltip(relicId, lines, true);
            registration.addIngredientInfo(List.of(stack), VanillaTypes.ITEM_STACK, lines.toArray(Component[]::new));
        }
    }


    private static List<ItemStack> buildRuneStacks(net.minecraft.core.Registry<Enchantment> registry) {
        List<ItemStack> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            if (!RunicCompat.isStatAvailable(type)) continue;
            ItemStack stack = RuneItem.createStatRune(RandomSource.create(), type);
            if (!stack.isEmpty()) out.add(stack);
        }

        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
            if (holder == null) continue;

            ItemStack stack = RuneItem.createEffectRune(holder);
            if (!stack.isEmpty()) out.add(stack);
        }

        for (ResourceLocation id : MythicRuneRegistry.ids()) {
            ItemStack stack = RuneItem.createMythicRune(id);
            if (!stack.isEmpty()) out.add(stack);
        }

        return out;
    }

    // builds etching stacks
    private static List<ItemStack> buildEtchingStacks(net.minecraft.core.Registry<Enchantment> registry) {
        List<ItemStack> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            if (!RunicCompat.isStatAvailable(type)) continue;
            if (UniqueRuneSources.isUniqueEtchingStat(type)) continue;
            ItemStack stack = EtchingItem.createStatEtching(RandomSource.create(), type);
            if (!stack.isEmpty()) out.add(stack);
        }

        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            if (UniqueRuneSources.isUniqueEtchingEffect(id)) continue;
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
            if (holder == null) continue;

            ItemStack stack = EtchingItem.createEffectEtching(holder);
            if (!stack.isEmpty()) out.add(stack);
        }

        return out;
    }
}
