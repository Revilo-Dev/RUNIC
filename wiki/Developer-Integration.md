# Developer Integration

This page is for mod developers who want their items or enchantments to work with RUNIC.

## Datapack Support

The preferred integration path is datapack JSON. Put these files in your mod resources under `src/main/resources/data/<your_modid>/`.

## Add Rune Slots to Custom Gear

Create:

```text
data/<your_modid>/rune_slots/<file>.json
```

Direct item example:

```json
{
  "items": {
    "your_modid:steel_greatsword": 5
  },
  "item_types": {
    "your_modid:steel_greatsword": "sword"
  }
}
```

Tag-based example:

```json
{
  "tags": {
    "your_modid:runic_weapons": 5
  },
  "tag_types": {
    "your_modid:runic_weapons": "sword"
  }
}
```

Then create a normal item tag:

```text
data/your_modid/tags/item/runic_weapons.json
```

```json
{
  "replace": false,
  "values": [
    "your_modid:steel_greatsword",
    "your_modid:obsidian_glaive"
  ]
}
```

## Supported Gear Types

Use one of:

`helmet`, `chestplate`, `leggings`, `boots`, `sword`, `pickaxe`, `axe`, `shovel`, `hoe`, `bow`, `crossbow`, `shield`, `trident`, `elytra`, `fishing_rod`, `mace`.

Gear type affects which stats, attributes, mythic runes, and workbench behavior apply.

If your item already exposes vanilla-style attributes, RUNIC may auto-detect it. A datapack type is still recommended for predictable behavior.

## Add Custom Enchantments as RUNIC Effects

Create:

```text
data/<your_modid>/runic_effects/<file>.json
```

```json
{
  "effects": [
    "your_modid:storm_edge",
    "your_modid:lifesteal"
  ]
}
```

`effects` and `add` are aliases. `remove` can remove built-in or datapack-added entries.

```json
{
  "add": [
    "your_modid:storm_edge"
  ],
  "remove": [
    "minecraft:mending"
  ]
}
```

## Add Etching Table Recipes for Your Enchants

Whitelisting an enchantment makes it valid as a RUNIC effect, but players still need a recipe if you want it craftable.

Create an Etching Table recipe:

```text
data/<your_modid>/recipe/etching_table/effect/storm_edge.json
```

```json
{
  "type": "runic:etching_table",
  "base": {
    "item": "runic:blank_etching"
  },
  "material": {
    "item": "minecraft:lightning_rod"
  },
  "result": {
    "id": "runic:etching",
    "count": 1
  },
  "effect": "your_modid:storm_edge"
}
```

The resulting etching applies the enchantment at RUNIC's etching level. RUNIC clamps levels to the enchantment's max level.

## Code-Level Integration

If your mod directly edits RUNIC gear, use RUNIC APIs instead of raw NBT.

Important classes:

- `net.revilodev.runic.stat.RuneStats`
- `net.revilodev.runic.runes.RuneSlots`
- `net.revilodev.runic.item.custom.RuneItem`
- `net.revilodev.runic.item.custom.EtchingItem`
- `net.revilodev.runic.gear.GearAttributes`

Recommended flow:

```java
RuneStats current = RuneStats.get(stack);
RuneStats add = RuneStats.single(RuneStatType.ATTACK_DAMAGE, 2.0F);
RuneStats merged = RuneStats.combine(current, add);
RuneStats.set(stack, merged);
RuneSlots.syncUsedToContents(stack);
```

For enchant-like effects:

```java
if (RuneItem.isEffectEnchantment(enchantment)) {
    int level = RuneItem.clampEffectLevel(enchantment, 2);
    // Add the enchantment to DataComponents.ENCHANTMENTS server-side,
    // then call RuneSlots.syncUsedToContents(stack).
}
```

Always apply changes on the logical server. Client screens should only request or preview changes.

