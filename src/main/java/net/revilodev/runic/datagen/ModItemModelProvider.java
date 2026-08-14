package net.revilodev.runic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.RuneModelMappings;

// generates mod item model provider

// generates mod item model provider
public class ModItemModelProvider extends ItemModelProvider {

    private static final ResourceLocation RUNE_MODEL_PRED =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "rune_model");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RunicMod.MOD_ID, existingFileHelper);
    }

    @Override
    // registers models
    protected void registerModels() {
        basicItem(ModItems.BLANK_INSCRIPTION.get());
        withExistingParent("item/blank_etching", "minecraft:item/generated")
                .texture("layer0", modLoc("item/blank_etching"));
        basicItem(ModItems.EXPANSION_INSCRIPTION.get());
        basicItem(ModItems.REPAIR_INSCRIPTION.get());
        basicItem(ModItems.UPGRADE_INSCRIPTION.get());
        basicItem(ModItems.NULLIFICATION_INSCRIPTION.get());
        basicItem(ModItems.REROLL_INSCRIPTION.get());
        basicItem(ModItems.WILD_INSCRIPTION.get());
        basicItem(ModItems.CURSED_INSCRIPTION.get());
        basicItem(ModItems.EXTRACTION_INSCRIPTION.get());
        withExistingParent("item/resonance_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/resonance-inscription"));
        withExistingParent("item/purification_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/purification-inscription"));
        withExistingParent("item/stabilization_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/stabilisation-inscription"));
        withExistingParent("item/tempering_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/tempering-inscription"));
        withExistingParent("item/relic_socket_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/relic-inscription"));
        withExistingParent("item/dissonant_inscription", "minecraft:item/generated")
                .texture("layer0", modLoc("item/inscriptions/dissonant-inscription"));
        withExistingParent("item/dragon_heart", "minecraft:item/generated")
                .texture("layer0", modLoc("item/relic/dragon-heart"));
        withExistingParent("item/elder_guardians_eye", "minecraft:item/generated")
                .texture("layer0", modLoc("item/relic/elder-gardian-eye"));
        withExistingParent("item/wither_charge", "minecraft:item/generated")
                .texture("layer0", modLoc("item/relic/wither-charge"));
        withExistingParent("item/wardens_soul", "minecraft:item/generated")
                .texture("layer0", modLoc("item/relic/warden-soul"));

        generateLayeredRuneAndEtchingModels();
    }

    private void generateLayeredRuneAndEtchingModels() {
        generateLayeredSet("enhanced_rune", "item/rune_base", "rune");
        generateLayeredSet("etching", "item/etching_base", "etching");
    }


    private void generateLayeredSet(String itemModelName, String baseTexture, String folderName) {
        java.util.Set<String> generatedModels = new java.util.HashSet<>();
        for (RuneModelMappings.ModelDef def : RuneModelMappings.modelDefs()) {
            String modelPath = "item/" + folderName + "/" + def.subPath();
            String iconTex = iconTextureFor(def.subPath());
            if (!generatedModels.add(modelPath)) {
                continue;
            }

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

    private String iconTextureFor(String subPath) {
        return switch (subPath) {
            case "mythic" -> "item/inscriptions/mythic-inscription";
            case "synergy" -> "item/inscriptions/synergy-inscription";
            default -> "item/icons/" + subPath;
        };
    }
}
