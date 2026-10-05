# Inscriptions

Inscriptions are one-use forging tools used in the Artisan's Workbench. Defaults below come from the `forging` config section.

| Inscription | Exact default behavior |
|---|---|
| Restoration/Repair Rune | Removes 10 corruption, reduces maximum durability by 15%, and adds Brittle. Requires damageable gear with corruption. |
| Expansion Rune | Adds one rune slot, records one expansion, reduces maximum durability by 10%, and adds 8 corruption. |
| Nullification Rune | Removes all stat and effect enhancements, clears synergies, removes one slot, and adds 10 corruption. |
| Upgrade Rune | Raises one eligible stat by up to 10 points without exceeding its cap, adds Overforged, and adds 5 corruption plus another 5 if already Overforged. |
| Reroll Inscription | Rerolls one stat using its rune range, adds 3 corruption, and adds Unstable when the new roll is higher. Each Unstable level lowers future roll bounds by 2. |
| Cursed Inscription | Has a 50% base success chance, reduced by 10 percentage points per Cursed level. Success adds 25 to one capped stat and adds Cursed/Overforged. Failure adds Cursed, reduces stats by 5%, and adds Brittle. Always adds 10 corruption. |
| Wild Inscription | Replaces one enhancement with another of the same category, adds Chaotic, and adds 12 corruption. Synergy mutation is disabled by default. |
| Extraction Inscription | Extracts one eligible enhancement, seals the gear, and adds 8 corruption. Synergy and mythic extraction are disabled by default. |
| Resonance Inscription | Adds one Synergy Potential, adds Fractured, adds 6 corruption, and attempts an available synergy. |
| Purification Inscription | Removes one negative attribute level, adds Brittle, adds 10 corruption, and has a 50% chance to reduce maximum durability by 10%. |
| Stabilization Inscription | Removes one Unstable level, adds Brittle, and adds 5 corruption. |
| Tempering Inscription | Adds one Reinforced level and 5 corruption. Each Reinforced level reduces durability loss by 10% by default. |
| Relic Socket Inscription | Adds an empty relic socket, adds Brittle, and adds 10 corruption. |
| Dissonant Inscription | Creative-only. Clears corruption, synergies, mythic runes, and negative conditions; sets Dissonant, which blocks future synergy potential and mythic runes. |

`crafting.disable_inscription_crafting = true` disables inscription recipes in the Etching Table without disabling enchanting-table etchings.
