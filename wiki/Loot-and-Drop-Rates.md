# Loot and Drop Rates

RUNIC injects loot into structure/chest-style tables and excludes villager, fishing, ordinary entity, block, gameplay, and trade tables unless the table is a named unique-rune source.

## Structure Roll

The default injection chance is 35% per eligible loot table. A successful table produces one roll, or two rolls in Bastions and Ancient Cities.

For each normal roll:

- 25% selects a utility item: Repair 50%, Expansion 25%, Nullification 16.67%, Upgrade 8.33%.
- The remaining 75% selects a stat rune 80% of the time or an effect rune 20% of the time. Across all successful rolls, that is 60% stat and 15% effect.
- Stat/effect selection is weighted by enhancement rarity. Higher `loot.*_rarity` values make that rarity more common relative to the others.

Default rarity values are Common 60, Uncommon 35, Rare 18, Epic 8, Legendary 3, and Mythic 1. These are relative selection weights, not direct percentages.

## Mythic Loot

Before a normal/unique roll, eligible high-difficulty sources attempt a mythic roll. Ancient City, End City, Bastion, Fortress, Trial Chamber/Vault, and Deep Dark qualify; the default minimum difficulty is 4. The default check is `rarity / 24`, so rarity 1 is about 4.17% after the table's 35% injection succeeds.

## Unique Sources

- Mansion, Outpost, and raider sources include Leeching, Multishot, Binding Curse, Vanishing Curse, and Stun.
- Evokers add Fangs.
- Trial Chamber/Vault sources include Wind Burst, Density, and Breach.
- Ancient Cities include Swift Sneak, Leeching, Stun, Binding Curse, and Vanishing Curse.
- Nether Fortress/Bastion/bartering sources include Nether Damage, Soul Speed, Health, Fire Resistance, Blast Resistance, and Withering.

Unique rune choices use `loot.loot_only_etching_rarity` as their relative repetition value. Their rune drops remain available regardless of the etching toggle. Unique/overpowered etching variants are excluded by default; `loot.all_runes_have_etchings = true` makes those same enhancements available as etchings.

## Relics and Books

Boss relics are guaranteed: Dragon Heart from the Ender Dragon, Wither Charge from the Wither, Elder Guardian's Eye from Elder Guardians, and Warden's Soul from Wardens. Enchanted books are removed unless they contain at least one enchantment in `enchantments.whitelist`; mixed books keep only whitelisted entries.
