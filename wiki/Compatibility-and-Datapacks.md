# Compatibility and Datapacks

RUNIC supports datapack-driven compatibility for rune slots, custom gear categories, rarity weights, and effect enchantments.

This page is for players and pack makers. For mod developers, see [Developer Integration](Developer-Integration.md).

## Rune Slot Files

Rune slot files live in:

```text
data/<namespace>/rune_slots/<file>.json
```

They can give items or item tags enhancement slots.

```json
{
  "items": {
    "examplemod:crystal_sword": 5
  },
  "tags": {
    "examplemod:runic_greatswords": 5
  }
}
```

## Gear Type Files

The same rune slot file can tell RUNIC what kind of gear a custom item is.

```json
{
  "item_types": {
    "examplemod:crystal_sword": "sword"
  },
  "tag_types": {
    "examplemod:runic_longbows": "bow"
  }
}
```

Valid types are:

`helmet`, `chestplate`, `leggings`, `boots`, `sword`, `pickaxe`, `axe`, `shovel`, `hoe`, `bow`, `crossbow`, `shield`, `trident`, `elytra`, `fishing_rod`, `mace`.

## Effect Enchantment Files

Effect enchantment files live in:

```text
data/<namespace>/runic_effects/<file>.json
```

They allow enchantments to appear as RUNIC effect runes and etchings.

```json
{
  "effects": [
    "examplemod:storm_edge",
    "examplemod:lifesteal"
  ]
}
```

## Removing Built-In Effect Support

Packs can remove an effect from RUNIC's allowed list:

```json
{
  "remove": [
    "minecraft:mending"
  ]
}
```

## Reloading

After changing datapacks, reload the world or run `/reload`.

