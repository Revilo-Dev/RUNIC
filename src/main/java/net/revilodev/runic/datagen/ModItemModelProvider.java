package net.revilodev.runic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.RuneModelMappings;

public class ModItemModelProvider extends ItemModelProvider {

    private static final ResourceLocation RUNE_MODEL_PRED =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "rune_model");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RunicMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.EXPANSION_INSCRIPTION.get());
        basicItem(ModItems.REPAIR_INSCRIPTION.get());
        basicItem(ModItems.UPGRADE_INSCRIPTION.get());
        basicItem(ModItems.NULLIFICATION_INSCRIPTION.get());
        basicItem(ModItems.REROLL_INSCRIPTION.get());
        basicItem(ModItems.WILD_INSCRIPTION.get());
        basicItem(ModItems.CURSED_INSCRIPTION.get());
        basicItem(ModItems.EXTRACTION_INSCRIPTION.get());

        generateLayeredRuneAndEtchingModels();
    }

    private void generateLayeredRuneAndEtchingModels() {
        generateLayeredSet("enhanced_rune", "item/rune_base", "rune");
        generateLayeredSet("etching", "item/etching_base", "etching");
    }

    private void generateLayeredSet(String itemModelName, String baseTexture, String folderName) {
        for (RuneModelMappings.ModelDef def : RuneModelMappings.modelDefs()) {
            String modelPath = "item/" + folderName + "/" + def.subPath();
            String iconTex = "item/icons/" + def.subPath();

            withExistingParent(modelPath, "minecraft:item/generated")
                    .texture("layer0", modLoc(baseTexture))
                    .texture("layer1", modLoc(iconTex));
        }

        ItemModelBuilder base = withExistingParent("item/" + itemModelName, "minecraft:item/generated")
                .texture("layer0", modLoc(baseTexture));

        for (RuneModelMappings.ModelDef def : RuneModelMappings.modelDefs()) {
            String modelPath = "item/" + folderName + "/" + def.subPath();
            base.override()
                    .predicate(RUNE_MODEL_PRED, def.predicateValue())
                    .model(getExistingFile(modLoc(modelPath)))
                    .end();
        }
    }
}
