package net.revilodev.runic.stat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.runes.RuneAttributeApplier;

import java.util.EnumMap;
import java.util.Map;



// supports rune stats
public final class RuneStats {

    // custom data keys
    public static final String NBT_KEY = "runic_stats";
    private static final String BASE_ITEM_KEY = "runic_base_item";
    private static final RuneStats EMPTY = new RuneStats(new EnumMap<>(RuneStatType.class));

    // active stat values by type
    final EnumMap<RuneStatType, Float> values;

    public RuneStats(EnumMap<RuneStatType, Float> values) {
        this.values = values;
    }

    public float get(RuneStatType type) {
        return values.getOrDefault(type, 0.0F);
    }

    // exact key presence
    public boolean has(RuneStatType type) {
        return values.containsKey(type);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Map<RuneStatType, Float> view() {
        return Map.copyOf(values);
    }

    // preview rolls without storing them
    public RuneStats rolledForTooltip() {
        return rollForApplication(this, RandomSource.create());
    }

    // save only live stat values
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<RuneStatType, Float> e : values.entrySet()) {
            float v = e.getValue();
            if (v != 0.0F) {
                tag.putFloat(e.getKey().id(), v);
            }
        }
        return tag;
    }

    // rebuild enum keyed stats from saved ids
    public static RuneStats load(CompoundTag tag) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (String key : tag.getAllKeys()) {
            RuneStatType type = RuneStatType.byId(key);
            if (type != null) {
                map.put(type, tag.getFloat(key));
            }
        }
        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    public static RuneStats empty() {
        return EMPTY;
    }

    public static RuneStats single(RuneStatType type, float value) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.put(type, value);
        return new RuneStats(map);
    }

    // negative value marks a pending roll
    public static RuneStats singleUnrolled(RuneStatType type) {
        return single(type, -1.0F);
    }

    // normal rune application path
    public static RuneStats rollForApplication(RuneStats template, RandomSource random) {
        return rollForApplication(template, random, false);
    }

    // rolls for application
    public static RuneStats rollForApplication(RuneStats template, RandomSource random, boolean etching) {
        if (template == null || template.isEmpty()) {
            return EMPTY;
        }

        // roll unresolved values when the rune is applied
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (Map.Entry<RuneStatType, Float> e : template.values.entrySet()) {
            RuneStatType type = e.getKey();
            float v = e.getValue();

            // unresolved entries roll here
            if (v < 0.0F) {
                v = etching ? type.rollEtching(random) : type.roll(random);
            }
            if (v != 0.0F) {
                map.put(type, v);
            }
        }
        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    public static RuneStats combine(RuneStats base, RuneStats add) {
        return combine(base, add, !RunicConfig.disableStatCaps());
    }

    // optional cap aware merge
    public static RuneStats combine(RuneStats base, RuneStats add, boolean respectCaps) {
        if ((base == null || base.isEmpty()) && (add == null || add.isEmpty())) {
            return EMPTY;
        }

        // start from existing stats then merge new ones
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);

        if (base != null && !base.isEmpty()) {
            map.putAll(base.values);
        }

        // add onto existing values one type at a time
        if (add != null && !add.isEmpty()) {
            for (Map.Entry<RuneStatType, Float> e : add.values.entrySet()) {
                RuneStatType type = e.getKey();
                float existing = map.getOrDefault(type, 0.0F);
                float added = e.getValue();
                float sum = existing + added;

                // optional per stat cap
                float cap = type.cap();
                if (respectCaps && cap > 0.0F && sum > cap) {
                    sum = cap;
                }

                map.put(type, sum);
            }
        }

        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    public static RuneStats get(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = data.copyTag();
        if (root == null || !root.contains(NBT_KEY)) {
            return EMPTY;
        }
        // stats live under one custom data key
        return load(root.getCompound(NBT_KEY));
    }

    public static void set(ItemStack stack, RuneStats stats) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = existing.copyTag();
        if (root == null) {
            root = new CompoundTag();
        }

        // keep raw stats and the source item id together
        if (stats == null || stats.isEmpty()) {
            root.remove(NBT_KEY);
            root.remove(BASE_ITEM_KEY);
        } else {
            root.put(NBT_KEY, stats.save());
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId != null) {
                root.putString(BASE_ITEM_KEY, itemId.toString());
            }
        }

        // rebuild derived item state from the stored stats
        RuneAttributeApplier.clearRunicAttributes(stack);
        RuneAttributeApplier.clearDurability(stack, root);

        if (stats != null && !stats.isEmpty()) {
            RuneAttributeApplier.rebuildAttributes(stack, stats);
            RuneAttributeApplier.applyDurability(stack, stats, root);
        }

        // write the final custom data back once
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static boolean needsRebuildForCurrentItem(ItemStack stack) {
        RuneStats stats = get(stack);
        if (stats == null || stats.isEmpty()) return false;

        // item swaps can leave old derived data behind
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = data.copyTag();
        if (root == null || !root.contains(BASE_ITEM_KEY)) return true;

        ResourceLocation currentId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (currentId == null) return true;

        return !currentId.toString().equals(root.getString(BASE_ITEM_KEY));
    }

    public static float getTotalFromEquipment(LivingEntity entity, RuneStatType type) {
        if (entity == null || type == null) return 0.0F;
        float total = 0.0F;
        // sum every equipped item with runic stats
        for (ItemStack stack : entity.getAllSlots()) {
            if (!stack.isEmpty()) {
                total += get(stack).get(type);
            }
        }
        return total;
    }
}
