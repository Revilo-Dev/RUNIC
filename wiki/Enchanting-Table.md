# Enchanting Table

RUNIC repurposes the enchanting table for Blank Etchings and explicitly whitelisted enchantments.

## Blank Etchings

Place a Blank Etching in the item slot. The resource slot accepts Lapis Lazuli, Diamond, Amethyst Shard, Emerald, Gold Ingot, or Echo Shard.

| Resource | Category |
|---|---|
| Lapis Lazuli | any category |
| Diamond | offensive |
| Amethyst Shard | defensive |
| Emerald | elemental |
| Gold Ingot | utility |
| Echo Shard | forbidden; only Epic or Legendary offers |

Offer one prefers Common/Uncommon, offer two prefers Uncommon/Rare/Epic, and offer three prefers Epic/Legendary. If a normal tier has no candidates, RUNIC falls back to the allowed category pool. Etching costs are capped at 25; the second offer is at least 15 and the third is always 25.

The selected stat or effect is written to the resulting Etching. Stat values remain unrolled until forging; effect etchings apply at level 1 when forged.

## Whitelisted Enchantments

`enchantments.whitelist` is the single config list for normal enchantments. Every listed enchantment can appear on books and enchantable items, is allowed as a RUNIC effect on any workbench target, bypasses RUNIC stripping, and is preserved in enchanted-book loot. Non-whitelisted normal item offers remain disabled.

Use valid TOML list syntax:

```toml
[enchantments]
whitelist = ["minecraft:sharpness", "minecraft:protection", "othermod:custom_enchant"]
```

Invalid ids are ignored individually instead of causing the whole list to be reset.
