# Etching Table

The Etching Table is RUNIC's inscription crafting station. Blank Etchings themselves are enchanted at the normal Enchanting Table; this block consumes a Blank Inscription plus a recipe material to make utility inscriptions and special rune items.

## Operation

1. Put the recipe base in the first input and its material in the second.
2. Inspect the server-generated output preview.
3. Pay the five-level crafting cost and take the result.

Only recipes whose base accepts `runic:blank_inscription` appear in this menu. Blacklisted enchantments, disabled stats, missing compatibility stats, and unknown mythic ids invalidate a recipe. JEI displays registered recipes when installed.

`crafting.disable_inscription_crafting = true` clears the result and prevents the five-level payment/craft. It does not disable Blank Etching enchanting.

## Datapack Recipe

```json
{
  "type": "runic:etching_table",
  "base": { "item": "runic:blank_inscription" },
  "material": { "item": "minecraft:diamond" },
  "result": { "id": "runic:upgrade_rune", "count": 1 }
}
```

Recipes may also declare `stat`, `effect`, or `mythic` to write enhancement data to the result.
