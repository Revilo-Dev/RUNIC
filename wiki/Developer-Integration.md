# Developer Integration

Use configuration for pack-local overrides, datapacks for distributable data integration, and Java only when adding new mechanics.

## Add or Remove Rune Slots

For a server/pack config override, use `rune_slots.whitelist = ["yourmod:item=5"]` or `rune_slots.blacklist = ["yourmod:item"]`. The blacklist wins, and a whitelist count overrides all detected, datapack, or stored capacities.

For a datapack, create `data/<namespace>/rune_slots/<file>.json`:

```json
{
  "defaults": { "sword": 4 },
  "items": { "yourmod:steel_greatsword": 5 },
  "tags": { "yourmod:runic_weapons": 5 },
  "item_types": { "yourmod:steel_greatsword": "sword" },
  "tag_types": { "yourmod:runic_weapons": "sword" }
}
```

Supported types are `helmet`, `chestplate`, `leggings`, `boots`, `sword`, `pickaxe`, `axe`, `shovel`, `hoe`, `bow`, `crossbow`, `shield`, `trident`, `elytra`, `fishing_rod`, and `mace`. Direct items beat tags; tags beat defaults. Vanilla classes and item attribute modifiers provide a final automatic fallback.

Legacy `{ "item": "id", "slots": 4 }` and `{ "list": [{"item":"id","slots":4}] }` files are also accepted.

## Add Effect Enchantments

The easiest runtime option is the config:

```toml
[enchantments]
whitelist = ["yourmod:storm_edge"]
```

This makes the enchantment a RUNIC effect, permits it on any workbench item, adds it to enchanting-table candidates, and preserves it in book loot.

For a distributable datapack list, create `data/<namespace>/runic_effects/<file>.json`:

```json
{
  "add": ["yourmod:storm_edge", "yourmod:lifesteal"],
  "remove": ["minecraft:mending"]
}
```

`effects` is an alias of `add`. Datapack removal affects the built-in effect set, but a config whitelist explicitly adds its ids back.

## Add Etching Table Recipes

Create `data/<namespace>/recipe/etching_table/<name>.json`:

```json
{
  "type": "runic:etching_table",
  "base": { "item": "runic:blank_inscription" },
  "material": { "item": "minecraft:lightning_rod" },
  "result": { "id": "runic:etching", "count": 1 },
  "effect": "yourmod:storm_edge"
}
```

Use `stat: "attack_damage"` for a stat template or `mythic: "runic:mythic/ruin"` for a mythic item. The menu shows only Blank Inscription-based recipes. Blank Etchings are produced through the Enchanting Table.

## Add or Change Rarities

Create `data/<namespace>/rarities/<file>.json`:

```json
{
  "default": "common",
  "entries": {
    "yourmod:storm_edge": "epic",
    "runic:stat/attack_damage": "uncommon"
  }
}
```

Valid built-in rarity keys are `common`, `uncommon`, `rare`, `epic`, `legendary`, `mythic`, and `cursed`. Rarity affects color, offer tiers, corruption category, and relative loot selection.

## Add Items Through Tags

Any item tag referenced by `tags` or `tag_types` must be a normal item tag at `data/<namespace>/tags/item/<name>.json`. This is the recommended way to integrate families of gear without enumerating every id.

## Java APIs

Important APIs are:

- `RuneSlotCapacityData` for reloadable slot/type data.
- `RuneSlots` for effective capacity, usage, expansion, and synchronization.
- `RuneStats` and `RuneStatType` for stat storage and capped combination.
- `RunicEffectEnchantments` for the live effect set.
- `RuneItem` and `EtchingItem` for enhancement item creation.
- `RunicItemData` for corruption, synergies, relic sockets, and mythic ids.
- `GearAttributes` for levelled item attributes.
- `SynergyRegistry`, `MythicRuneRegistry`, and `RelicRegistry` for built-in definitions.

Always mutate items on the logical server and call `RuneSlots.syncUsedToContents(stack)` after directly changing stats or enchantments.

```java
RuneStats current = RuneStats.get(stack);
RuneStats added = RuneStats.single(RuneStatType.ATTACK_DAMAGE, 2.0F);
RuneStats.set(stack, RuneStats.combine(current, added));
RuneSlots.syncUsedToContents(stack);
```

## Adding New Built-In Mechanics

- New stat: add a `RuneStatType`, translation/model/category/rarity data, application behavior if it is not a generic attribute, and optional recipe/loot source.
- New synergy: register its input pair and id in `SynergyRegistry`, implement behavior in `SynergyEffects`, add config values, tooltip text, model mapping, and tests or validation.
- New mythic rune: add a `MythicRuneDefinition`, application target rules, behavior in `MythicRuneEffects`, config values, translations, recipe/loot source, and model mapping.
- New relic: register its item and `RelicDefinition`, boss/loot source, passive and active logic in `RelicEffects`, config values, translations, and models.
- New inscription: register the item, add an Etching Table recipe, preview/apply rules in `ArtisansWorkbenchMenu`, tooltip translations, and config values.
- New compatibility pack: prefer rune-slot, effect, rarity, tag, and recipe JSON so it works without a hard dependency.

Datapack reload listeners sync rune slots, types, effect ids, and rarities to clients. Custom gameplay registries implemented only in Java require both sides to ship the same mod version.
