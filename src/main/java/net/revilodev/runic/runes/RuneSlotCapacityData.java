package net.revilodev.runic.runes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.revilodev.runic.RunicMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RuneSlotCapacityData extends SimpleJsonResourceReloadListener {
    // permissive parser for datapack json
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String FOLDER = "rune_slots";

    // live caches rebuilt on reload
    private static Map<Item, Integer> CAPACITIES = new HashMap<>();
    private static Map<TagKey<Item>, Integer> TAG_CAPACITIES = new HashMap<>();
    private static Map<String, Integer> DEFAULTS = new HashMap<>();
    private static Map<Item, String> ITEM_TYPES = new HashMap<>();
    private static Map<TagKey<Item>, String> TAG_TYPES = new HashMap<>();

    public RuneSlotCapacityData() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
                         ResourceManager manager,
                         ProfilerFiller profiler) {
        // rebuild everything from datapacks
        Map<Item, Integer> fresh = new HashMap<>();
        Map<TagKey<Item>, Integer> tagCapacities = new HashMap<>();
        Map<String, Integer> defaults = new HashMap<>();
        Map<Item, String> itemTypes = new HashMap<>();
        Map<TagKey<Item>, String> tagTypes = new HashMap<>();

        objects.forEach((rl, element) -> {
            if (!element.isJsonObject()) {
                RunicMod.LOGGER.warn("RuneSlots: {} is not a JSON object", rl);
                return;
            }

            // each file can contribute partial data
            JsonObject json = element.getAsJsonObject();

            // new format with grouped sections
            if (json.has("defaults") && json.get("defaults").isJsonObject()) {
                JsonObject defs = json.getAsJsonObject("defaults");
                for (String keyStr : defs.keySet()) {
                    int slots = GsonHelper.getAsInt(defs, keyStr, 0);
                    defaults.put(keyStr, Math.max(0, slots));
                }
            }

            if (json.has("items") && json.get("items").isJsonObject()) {
                JsonObject items = json.getAsJsonObject("items");
                for (String keyStr : items.keySet()) {
                    ResourceLocation key = ResourceLocation.tryParse(keyStr);
                    if (key == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item id '{}' in {}", keyStr, rl);
                        continue;
                    }
                    int slots = GsonHelper.getAsInt(items, keyStr, 0);
                    // direct item override
                    putSafe(fresh, key, slots, rl);
                }
            }

            if (json.has("tags") && json.get("tags").isJsonObject()) {
                JsonObject tags = json.getAsJsonObject("tags");
                for (String keyStr : tags.keySet()) {
                    ResourceLocation key = ResourceLocation.tryParse(keyStr);
                    if (key == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item tag id '{}' in {}", keyStr, rl);
                        continue;
                    }
                    int slots = GsonHelper.getAsInt(tags, keyStr, 0);
                    // tag wide override
                    tagCapacities.put(itemTag(key), Math.max(0, slots));
                }
            }

            if (json.has("item_types") && json.get("item_types").isJsonObject()) {
                JsonObject types = json.getAsJsonObject("item_types");
                for (String keyStr : types.keySet()) {
                    ResourceLocation key = ResourceLocation.tryParse(keyStr);
                    if (key == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item id '{}' in item_types for {}", keyStr, rl);
                        continue;
                    }
                    String type = normalizeType(GsonHelper.getAsString(types, keyStr, ""));
                    if (type == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item type '{}' for {} in {}", GsonHelper.getAsString(types, keyStr, ""), keyStr, rl);
                        continue;
                    }
                    // explicit item category
                    putTypeSafe(itemTypes, key, type, rl);
                }
            }

            if (json.has("tag_types") && json.get("tag_types").isJsonObject()) {
                JsonObject types = json.getAsJsonObject("tag_types");
                for (String keyStr : types.keySet()) {
                    ResourceLocation key = ResourceLocation.tryParse(keyStr);
                    if (key == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item tag id '{}' in tag_types for {}", keyStr, rl);
                        continue;
                    }
                    String type = normalizeType(GsonHelper.getAsString(types, keyStr, ""));
                    if (type == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid tag type '{}' for {} in {}", GsonHelper.getAsString(types, keyStr, ""), keyStr, rl);
                        continue;
                    }
                    // explicit tag category
                    tagTypes.put(itemTag(key), type);
                }
            }

            // grouped format stops here
            if (json.has("defaults") || json.has("items") || json.has("tags") || json.has("item_types") || json.has("tag_types")) {
                return;
            }

            // legacy list format
            if (json.has("list") && json.get("list").isJsonArray()) {
                json.getAsJsonArray("list").forEach(el -> {
                    if (!el.isJsonObject()) return;
                    JsonObject entry = el.getAsJsonObject();
                    String itemId = GsonHelper.getAsString(entry, "item", "");
                    int slots = GsonHelper.getAsInt(entry, "slots", 0);
                    ResourceLocation key = ResourceLocation.tryParse(itemId);
                    if (key == null) {
                        RunicMod.LOGGER.warn("RuneSlots: invalid item id '{}' in {}", itemId, rl);
                        return;
                    }
                    putSafe(fresh, key, slots, rl);
                });
                return;
            }

            // legacy single entry format
            String itemId = GsonHelper.getAsString(json, "item", "");
            int slots = GsonHelper.getAsInt(json, "slots", 0);
            ResourceLocation key = ResourceLocation.tryParse(itemId);
            if (key == null) {
                RunicMod.LOGGER.warn("RuneSlots: invalid item id '{}' in {}", itemId, rl);
                return;
            }
            putSafe(fresh, key, slots, rl);
        });

        CAPACITIES = fresh;
        TAG_CAPACITIES = tagCapacities;
        DEFAULTS = defaults;
        ITEM_TYPES = itemTypes;
        TAG_TYPES = tagTypes;
        // log only the direct tables
        RunicMod.LOGGER.info("Loaded {} rune slot capacity entries and {} defaults.",
                CAPACITIES.size(), DEFAULTS.size());
    }

    // validate ids before storing
    private static void putSafe(Map<Item, Integer> map, ResourceLocation itemId, int slots, ResourceLocation source) {
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            RunicMod.LOGGER.warn("RuneSlots: unknown item '{}' in {}", itemId, source);
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        map.put(item, Math.max(0, slots));
    }

    // validate item types against the registry
    private static void putTypeSafe(Map<Item, String> map, ResourceLocation itemId, String type, ResourceLocation source) {
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            RunicMod.LOGGER.warn("RuneSlots: unknown item '{}' in {}", itemId, source);
            return;
        }
        map.put(BuiltInRegistries.ITEM.get(itemId), type);
    }

    private static TagKey<Item> itemTag(ResourceLocation id) {
        return TagKey.create(Registries.ITEM, id);
    }

    // keep type keys normalized
    private static String normalizeType(String raw) {
        if (raw == null) return null;
        String type = raw.trim().toLowerCase();
        return DEFAULT_TYPE_KEYS.contains(type) ? type : null;
    }

    // supported fallback categories
    private static final Set<String> DEFAULT_TYPE_KEYS = Set.of(
            "helmet",
            "chestplate",
            "leggings",
            "boots",
            "sword",
            "pickaxe",
            "axe",
            "shovel",
            "hoe",
            "bow",
            "crossbow",
            "shield",
            "trident",
            "elytra",
            "fishing_rod",
            "mace"
    );

    public static int capacity(Item item) {
        Integer direct = CAPACITIES.get(item);
        if (direct != null) return direct;

        // item type overrides fallback classification
        String type = ITEM_TYPES.get(item);
        if (type == null) {
            type = classify(item);
        }
        if (type != null && DEFAULTS.containsKey(type)) {
            return DEFAULTS.get(type);
        }
        return 0;
    }

    public static int capacity(ItemStack stack) {
        Integer direct = CAPACITIES.get(stack.getItem());
        if (direct != null) return direct;

        // tag capacities beat defaults
        for (Map.Entry<TagKey<Item>, Integer> entry : TAG_CAPACITIES.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        String type = classify(stack);
        if (type != null && DEFAULTS.containsKey(type)) {
            return DEFAULTS.get(type);
        }
        return 0;
    }

    private static String classify(Item item) {
        // vanilla item classes first
        if (item instanceof ArmorItem armor) {
            return switch (armor.getType()) {
                case HELMET -> "helmet";
                case CHESTPLATE -> "chestplate";
                case LEGGINGS -> "leggings";
                case BOOTS -> "boots";
                default -> null;
            };
        }
        if (item instanceof SwordItem) return "sword";
        if (item instanceof PickaxeItem) return "pickaxe";
        if (item instanceof AxeItem) return "axe";
        if (item instanceof ShovelItem) return "shovel";
        if (item instanceof HoeItem) return "hoe";
        if (item instanceof BowItem) return "bow";
        if (item instanceof CrossbowItem) return "crossbow";
        if (item instanceof ShieldItem) return "shield";
        if (item instanceof TridentItem) return "trident";
        if (item instanceof ElytraItem) return "elytra";
        if (item instanceof FishingRodItem) return "fishing_rod";
        return null;
    }

    private static String classify(ItemStack stack) {
        Item item = stack.getItem();
        String directType = ITEM_TYPES.get(item);
        if (directType != null) return directType;

        // tag types beat vanilla guesses
        for (Map.Entry<TagKey<Item>, String> entry : TAG_TYPES.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        String vanilla = classify(item);
        if (vanilla != null) return vanilla;

        EquipmentSlot armorSlot = armorSlotByAttributes(stack);
        if (armorSlot != null) {
            // fallback for custom armor like items
            return switch (armorSlot) {
                case HEAD -> "helmet";
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                case FEET -> "boots";
                default -> null;
            };
        }

        if (hasMainhandModifier(stack, Attributes.BLOCK_BREAK_SPEED)) return "pickaxe";
        if (hasMainhandModifier(stack, Attributes.ATTACK_DAMAGE)
                || hasMainhandModifier(stack, Attributes.ATTACK_SPEED)) {
            return "sword";
        }
        return null;
    }

    private static EquipmentSlot armorSlotByAttributes(ItemStack stack) {
        // detect custom armor slots from attributes
        if (hasSlotModifier(stack, EquipmentSlot.HEAD, Attributes.ARMOR)) return EquipmentSlot.HEAD;
        if (hasSlotModifier(stack, EquipmentSlot.CHEST, Attributes.ARMOR)) return EquipmentSlot.CHEST;
        if (hasSlotModifier(stack, EquipmentSlot.LEGS, Attributes.ARMOR)) return EquipmentSlot.LEGS;
        if (hasSlotModifier(stack, EquipmentSlot.FEET, Attributes.ARMOR)) return EquipmentSlot.FEET;
        if (hasSlotModifier(stack, EquipmentSlot.HEAD, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.HEAD;
        if (hasSlotModifier(stack, EquipmentSlot.CHEST, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.CHEST;
        if (hasSlotModifier(stack, EquipmentSlot.LEGS, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.LEGS;
        if (hasSlotModifier(stack, EquipmentSlot.FEET, Attributes.ARMOR_TOUGHNESS)) return EquipmentSlot.FEET;
        return null;
    }

    private static boolean hasMainhandModifier(ItemStack stack, Holder<Attribute> attribute) {
        return hasSlotModifier(stack, EquipmentSlot.MAINHAND, attribute);
    }

    // inspect raw attribute modifiers on the stack
    private static boolean hasSlotModifier(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute) {
        ItemAttributeModifiers modifiers =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.slot().test(slot)
                    && entry.attribute().unwrapKey().equals(attribute.unwrapKey())
                    && entry.modifier().amount() != 0.0D) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCategory(ItemStack stack, String category) {
        String normalized = normalizeType(category);
        return normalized != null && normalized.equals(classify(stack));
    }

    // network export for direct item capacities
    public static Map<ResourceLocation, Integer> exportItemIdMap() {
        Map<ResourceLocation, Integer> out = new HashMap<>();
        CAPACITIES.forEach((item, v) -> {
            ResourceLocation id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
            out.put(id, v);
        });
        return out;
    }

    public static Map<ResourceLocation, Integer> exportTagCapacityMap() {
        Map<ResourceLocation, Integer> out = new HashMap<>();
        TAG_CAPACITIES.forEach((tag, v) -> out.put(tag.location(), v));
        return out;
    }

    // network export for default type capacities
    public static Map<String, Integer> exportDefaults() {
        return new HashMap<>(DEFAULTS);
    }

    // network export for direct item types
    public static Map<ResourceLocation, String> exportItemTypeMap() {
        Map<ResourceLocation, String> out = new HashMap<>();
        ITEM_TYPES.forEach((item, type) -> {
            ResourceLocation id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
            out.put(id, type);
        });
        return out;
    }

    public static Map<ResourceLocation, String> exportTagTypeMap() {
        Map<ResourceLocation, String> out = new HashMap<>();
        TAG_TYPES.forEach((tag, type) -> out.put(tag.location(), type));
        return out;
    }

    public static void importFromNetwork(Map<ResourceLocation, Integer> itemMap,
                                         Map<String, Integer> defaults,
                                         Map<ResourceLocation, Integer> tagCapacities,
                                         Map<ResourceLocation, String> itemTypes,
                                         Map<ResourceLocation, String> tagTypes) {
        // rebuild client caches from the sync payload
        Map<Item, Integer> rebuilt = new HashMap<>();
        itemMap.forEach((id, v) -> {
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                return;
            }
            // skip unknown ids on the client
            Item item = BuiltInRegistries.ITEM.get(id);
            rebuilt.put(item, Math.max(0, v));
        });

        // tag capacities rebuild by location
        Map<TagKey<Item>, Integer> rebuiltTagCapacities = new HashMap<>();
        tagCapacities.forEach((id, v) -> rebuiltTagCapacities.put(itemTag(id), Math.max(0, v)));

        // explicit item types rebuild after validation
        Map<Item, String> rebuiltItemTypes = new HashMap<>();
        itemTypes.forEach((id, type) -> {
            String normalized = normalizeType(type);
            if (normalized != null && BuiltInRegistries.ITEM.containsKey(id)) {
                rebuiltItemTypes.put(BuiltInRegistries.ITEM.get(id), normalized);
            }
        });

        // tag types rebuild after validation
        Map<TagKey<Item>, String> rebuiltTagTypes = new HashMap<>();
        tagTypes.forEach((id, type) -> {
            String normalized = normalizeType(type);
            if (normalized != null) {
                rebuiltTagTypes.put(itemTag(id), normalized);
            }
        });

        CAPACITIES = rebuilt;
        TAG_CAPACITIES = rebuiltTagCapacities;
        DEFAULTS = new HashMap<>(defaults);
        ITEM_TYPES = rebuiltItemTypes;
        TAG_TYPES = rebuiltTagTypes;
    }
}
