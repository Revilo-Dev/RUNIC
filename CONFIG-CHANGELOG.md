# Config Changelog

- Renamed `loot.enchanted_book_whitelist` to `enchantments.whitelist`.
- Removed `enchanting_table.book_enchantments`; enchanting books are handled by `enchantments.whitelist`.
- Changed the enchantment whitelist to accept multiple entries without resetting the entire list when one entry is invalid.
- Changed whitelisted enchantments so they can become RUNIC effects, appear in enchanting-table offers, remain on enchanted-book loot, and bypass RUNIC's enchantment blacklist.
- Added `rune_slots.blacklist` for items that must never have rune slots.
- Added `rune_slots.whitelist` with `modid:item=count` entries; configured counts override datapack, detected, and stored counts.
- Removed `mechanics.disable_stat_caps`; stat caps are always enforced.
- Renamed `loot.removed_etchings_enabled` to `loot.all_runes_have_etchings`, changed its description to "Adds etching variants for all runes, even overpowered ones", and changed its default to `false`.
- Renamed every public `weight` config to `rarity`.
- Renamed `crafting.disable_etching_crafting` to `crafting.disable_inscription_crafting`.
- Renamed the `update_5` section to `forging`.
- Renamed `update_5.synergy` to `forging.synergy_effects`.
- Renamed `update_5.relic` to `forging.relics`.
- Renamed `update_5.mythic` to `forging.mythics`.
- Renamed `update_5.corruption` to `forging.corruption`.
- Renamed `update_5.attributes` to `forging.attributes`.
- Changed Expansion Inscription corruption to use `forging.expansion_inscription_corruption` instead of a hard-coded value.
