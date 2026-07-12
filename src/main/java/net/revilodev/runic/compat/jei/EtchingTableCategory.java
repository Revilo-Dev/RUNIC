package net.revilodev.runic.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStats;

public final class EtchingTableCategory implements IRecipeCategory<EtchingTableRecipe> {
    public static final RecipeType<EtchingTableRecipe> RECIPE_TYPE =
            RecipeType.create(RunicMod.MOD_ID, "etching_table", EtchingTableRecipe.class);

    private static final int WIDTH = 118;
    private static final int HEIGHT = 32;

    private final IDrawable icon;

    public EtchingTableCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ETCHING_TABLE.get()));
    }

    @Override
    public RecipeType<EtchingTableRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.runic.etching_table");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EtchingTableRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 8)
                .addIngredients(recipe.base());

        builder.addSlot(RecipeIngredientRole.INPUT, 26, 8)
                .addIngredients(recipe.material());

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 8)
                .addItemStack(new ItemStack(Items.LAPIS_LAZULI, 1));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 8)
                .addItemStack(buildOutput(recipe));
    }

    private static ItemStack buildOutput(EtchingTableRecipe recipe) {
        ItemStack out = recipe.result().copy();

        recipe.stat().ifPresent(stat -> RuneStats.set(out, RuneStats.singleUnrolled(stat)));
        recipe.effect().ifPresent(effect -> applyEffect(out, effect));
        recipe.mythic().ifPresent(id -> MythicRuneRegistry.setItemRuneId(out, id));

        return out;
    }

    private static void applyEffect(ItemStack out, ResourceLocation id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var registry = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
        Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
        if (holder == null || !EtchingItem.isEffectEnchantment(holder)) return;

        out.enchant(holder, RuneItem.forcedEtchingEffectLevel(holder));
    }
}
