package net.revilodev.runic.synergy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.item.EnhancementCategory;
import net.revilodev.runic.runes.RunicItemTargets;
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
    public static final ResourceLocation FURY = synergyId("fury");
    public static final ResourceLocation EXECUTIONERS_FURY = FURY;
    public static final ResourceLocation JUGGERNAUT = synergyId("juggernaut");
    public static final ResourceLocation TEMPEST = synergyId("tempest");
    public static final ResourceLocation REAPER = synergyId("reaper");
    public static final ResourceLocation SOULBURN = synergyId("soulburn");
    public static final ResourceLocation FROSTBITE = synergyId("frostbite");
    public static final ResourceLocation VENOM_BURST = synergyId("venom_burst");
    public static final ResourceLocation BERSERK = synergyId("berserk");
    public static final ResourceLocation ICE_BURST = synergyId("ice_burst");
    public static final ResourceLocation ICE_PRISON = ICE_BURST;

    private static final Map<PairKey, Definition> DEFINITIONS = new ConcurrentHashMap<>();

    static {
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.FLAME_CHANCE), SHATTER);
        register(statId(RuneStatType.FLAME_CHANCE), statId(RuneStatType.BLEEDING_CHANCE), BLOODFIRE);
        register(statId(RuneStatType.POISON_CHANCE), statId(RuneStatType.WEAKENING_CHANCE), CORROSION);
        register(statId(RuneStatType.ATTACK_DAMAGE), statId(RuneStatType.ATTACK_SPEED), EXECUTIONERS_FURY);
        register(statId(RuneStatType.STONE), statId(RuneStatType.RESISTANCE), JUGGERNAUT);
        register(statId(RuneStatType.SHOCKING_CHANCE), statId(RuneStatType.ATTACK_SPEED), TEMPEST);
        register(statId(RuneStatType.LEECHING_CHANCE), statId(RuneStatType.WEAKENING_CHANCE), REAPER);
        register(statId(RuneStatType.FLAME_CHANCE), statId(RuneStatType.WITHERING_CHANCE), SOULBURN);
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.BLEEDING_CHANCE), FROSTBITE);
        register(statId(RuneStatType.POISON_CHANCE), statId(RuneStatType.SHOCKING_CHANCE), VENOM_BURST);
        register(statId(RuneStatType.ATTACK_SPEED), statId(RuneStatType.ATTACK_DAMAGE), BERSERK);
        register(statId(RuneStatType.FREEZING_CHANCE), statId(RuneStatType.SHOCKING_CHANCE), ICE_PRISON);
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

    public static List<ResourceLocation> ids() {
        return DEFINITIONS.values().stream()
                .map(Definition::result)
                .distinct()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .toList();
    }

    public static List<Definition> possibleSynergies(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return List.of();
        List<ResourceLocation> applied = EnhancementRefs.collectApplied(stack);
        if (applied.isEmpty()) return List.of();

        List<Definition> out = new ArrayList<>();
        for (Definition def : DEFINITIONS.values()) {
            if (!def.enabled() || RunicItemData.hasSynergy(stack, def.result()) || !canApplyTo(stack, def.result())) continue;
            if (applied.contains(def.inputA()) && applied.contains(def.inputB())) {
                out.add(def);
            }
        }
        return List.copyOf(out);
    }

    public static List<Definition> possibleSynergies(ResourceLocation newlyApplied, ItemStack stack) {
        if (newlyApplied == null || stack == null || stack.isEmpty()) return List.of();
        List<Definition> out = new ArrayList<>();
        for (ResourceLocation existing : EnhancementRefs.collectApplied(stack)) {
            if (existing.equals(newlyApplied)) continue;
            Definition def = DEFINITIONS.get(PairKey.of(newlyApplied, existing));
            if (def != null && def.enabled() && !RunicItemData.hasSynergy(stack, def.result()) && canApplyTo(stack, def.result())) out.add(def);
        }
        return List.copyOf(out);
    }

    public static boolean canApplyTo(ItemStack stack, ResourceLocation result) {
        if (stack == null || stack.isEmpty() || result == null) return false;
        if (JUGGERNAUT.equals(result)) {
            return RunicItemTargets.isArmor(stack);
        }
        return true;
    }

    public static boolean isSynergy(ResourceLocation id) {
        return id != null && RunicMod.MOD_ID.equals(id.getNamespace()) && id.getPath().startsWith("synergy/");
    }

    public static boolean isRegisteredResult(ResourceLocation id) {
        if (!isSynergy(id)) return false;
        return DEFINITIONS.values().stream().anyMatch(def -> def.result().equals(id));
    }

    public static Optional<Definition> definitionForResult(ResourceLocation id) {
        if (!isSynergy(id)) return Optional.empty();
        return DEFINITIONS.values().stream().filter(def -> def.result().equals(id)).findFirst();
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

            return out;
        }
    }
}
