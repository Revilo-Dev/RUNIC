package net.revilodev.runic.runes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.revilodev.runic.RunicMod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// stores runic effect enchantment data

// stores runic effect enchantment data
public final class RunicEffectEnchantmentData extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String FOLDER = "runic_effects";

    public RunicEffectEnchantmentData() {
        super(GSON, FOLDER);
    }

    @Override
    // runs apply
    protected void apply(Map<ResourceLocation, JsonElement> objects,
                         ResourceManager manager,
                         ProfilerFiller profiler) {
        Set<ResourceLocation> additions = new HashSet<>();
        Set<ResourceLocation> removals = new HashSet<>();

        objects.forEach((source, element) -> {
            if (!element.isJsonObject()) {
                RunicMod.LOGGER.warn("RunicEffects: {} is not a JSON object", source);
                return;
            }
            var json = element.getAsJsonObject();
            readIdArray(json.get("effects"), additions, source, "effects");
            readIdArray(json.get("add"), additions, source, "add");
            readIdArray(json.get("remove"), removals, source, "remove");
        });

        RunicEffectEnchantments.replaceDatapackEffects(additions, removals);
        RunicMod.LOGGER.info("Loaded {} datapack runic effect enchantments and {} removals.",
                additions.size(), removals.size());
    }

    // reads id array
    private static void readIdArray(JsonElement element,
                                    Set<ResourceLocation> target,
                                    ResourceLocation source,
                                    String field) {
        if (element == null) {
            return;
        }
        if (!element.isJsonArray()) {
            RunicMod.LOGGER.warn("RunicEffects: '{}' in {} must be an array", field, source);
            return;
        }
        element.getAsJsonArray().forEach(value -> {
            if (!value.isJsonPrimitive()) {
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id == null) {
                RunicMod.LOGGER.warn("RunicEffects: invalid enchantment id '{}' in {}", value.getAsString(), source);
                return;
            }
            target.add(id);
        });
    }
}
