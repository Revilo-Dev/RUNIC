package net.revilodev.runic.synergy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.EnhancementCategory;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SynergyRegistry {
    public static final ResourceLocation SHATTER = synergyId("shatter");
    public static final ResourceLocation BLOODFIRE = synergyId("bloodfire");
    public static final ResourceLocation CORROSION = synergyId("corrosion");
    public static final ResourceLocation EXECUTIONERS_FURY = synergyId("executioners_fury");
    public static final ResourceLocation JUGGERNAUT = synergyId("juggernaut");
    public static final ResourceLocation TEMPEST = synergyId("tempest");
    public static final ResourceLocation REAPER = synergyId("reaper");
    public static final ResourceLocation SOULBURN = synergyId("soulburn");
    public static final ResourceLocation FROSTBITE = synergyId("frostbite");
    public static final ResourceLocation VENOM_BURST = synergyId("venom_burst");
    public static final ResourceLocation BERSERK = synergyId("berserk");
    public static final ResourceLocation ICE_PRISON = synergyId("ice_prison");

    private static final Map<PairKey, Definition> DEFINITIONS = new ConcurrentHashMap<>();

    static {
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.SHOCKING_CHANCE), SHATTER);
        register(statId(RuneStatType.FLAME_CHANCE), statId(RuneStatType.BLEEDING_CHANCE), BLOODFIRE);
        register(statId(RuneStatType.POISON_CHANCE), effectId("minecraft", "piercing"), CORROSION);
        register(effectId("runic", "execution"), effectId("runic", "momentum"), EXECUTIONERS_FURY);
        register(statId(RuneStatType.STONE), statId(RuneStatType.RESISTANCE), JUGGERNAUT);
        register(statId(RuneStatType.SHOCKING_CHANCE), statId(RuneStatType.ATTACK_SPEED), TEMPEST);
        register(statId(RuneStatType.LEECHING_CHANCE), effectId("runic", "execution"), REAPER);
        register(statId(RuneStatType.FLAME_CHANCE), statId(RuneStatType.WITHERING_CHANCE), SOULBURN);
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.BLEEDING_CHANCE), FROSTBITE);
        register(statId(RuneStatType.POISON_CHANCE), statId(RuneStatType.SHOCKING_CHANCE), VENOM_BURST);
        register(statId(RuneStatType.ATTACK_SPEED), effectId("runic", "momentum"), BERSERK);
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.STONE), ICE_PRISON);
    }

    private SynergyRegistry() {}

    public static void register(ResourceLocation inputA, ResourceLocation inputB, ResourceLocation result) {
        if (inputA == null || inputB == null || result == null) return;
        DEFINITIONS.put(PairKey.of(inputA, inputB), Definition.enabled(inputA, inputB, result));
    }

    public static boolean hasSynergy(ResourceLocation inputA, ResourceLocation inputB) {
        return getResult(inputA, inputB).isPresent();
    }

    public static Optional<ResourceLocation> getResult(ResourceLocation inputA, ResourceLocation inputB) {
        Definition def = DEFINITIONS.get(PairKey.of(inputA, inputB));
        return def == null ? Optional.empty() : Optional.of(def.result());
    }

    public static List<Definition> possibleSynergies(ResourceLocation newlyApplied, ItemStack stack) {
        if (newlyApplied == null || stack == null || stack.isEmpty()) return List.of();
        List<Definition> out = new ArrayList<>();
        for (ResourceLocation existing : EnhancementRefs.collectApplied(stack)) {
            if (existing.equals(newlyApplied)) continue;
            Definition def = DEFINITIONS.get(PairKey.of(newlyApplied, existing));
            if (def != null && def.enabled() && !RunicItemData.hasSynergy(stack, def.result())) out.add(def);
        }
        return List.copyOf(out);
    }

    public static boolean isSynergy(ResourceLocation id) {
        return id != null && RunicMod.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("synergy/");
    }

    public static boolean isRegisteredResult(ResourceLocation id) {
        if (!isSynergy(id)) return false;
        return DEFINITIONS.values().stream().anyMatch(def -> def.result().equals(id));
    }

    public static ResourceLocation synergyId(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "synergy/" + path);
    }

    public static ResourceLocation statId(RuneStatType type) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stat/" + type.id());
    }

    public static ResourceLocation effectId(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public record Definition(ResourceLocation inputA,
                             ResourceLocation inputB,
                             ResourceLocation result,
                             EnhancementCategory categoryA,
                             EnhancementCategory categoryB,
                             int corruption,
                             int weight,
                             boolean enabled) {
        private static Definition enabled(ResourceLocation inputA, ResourceLocation inputB, ResourceLocation result) {
            return new Definition(
                    inputA,
                    inputB,
                    result,
                    EnhancementCategory.getCategory(inputA),
                    EnhancementCategory.getCategory(inputB),
                    0,
                    1,
                    true
            );
        }

        public ResourceLocation other(ResourceLocation input) {
            if (inputA.equals(input)) return inputB;
            if (inputB.equals(input)) return inputA;
            return null;
        }
    }

    private record PairKey(ResourceLocation first, ResourceLocation second) {
        private static PairKey of(ResourceLocation a, ResourceLocation b) {
            return compare(a, b) <= 0 ? new PairKey(a, b) : new PairKey(b, a);
        }

        private static int compare(ResourceLocation a, ResourceLocation b) {
            String as = a == null ? "" : a.toString();
            String bs = b == null ? "" : b.toString();
            return as.compareTo(bs);
        }
    }

    public static final class EnhancementRefs {
        private EnhancementRefs() {}

        public static List<ResourceLocation> collectApplied(ItemStack stack) {
            List<ResourceLocation> out = new ArrayList<>();

            RuneStats stats = RuneStats.get(stack);
            if (stats != null && !stats.isEmpty()) {
                for (RuneStatType type : stats.view().keySet()) {
                    out.add(statId(type));
                }
            }

            net.minecraft.world.item.enchantment.ItemEnchantments enchants =
                    stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                            net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            enchants.entrySet().forEach(entry -> {
                if (entry.getIntValue() <= 0) return;
                entry.getKey().unwrapKey().ifPresent(key -> out.add(key.location()));
            });

            // Mythic runes are stored as applied enhancements, but they are not synergy inputs.
            out.addAll(RunicItemData.getSynergies(stack));
            return out;
        }
    }
}
