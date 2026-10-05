# Forging

Forging is the process of changing gear in the Artisan's Workbench. Put the gear in the target slot and a rune, etching, inscription, mythic rune, or relic in the enhancement slot. The preview shows the exact resulting item before the operation is accepted.

## Capacity

Normal runes and etchings consume one rune slot per stat or effect. Synergies do not consume slots. Mythic runes require an available slot. Relics use a separate relic socket. An item at zero capacity cannot accept normal enhancements.

Slot priority is: global rune-slot disable, item blacklist, config whitelist override, stored capacity, datapack item capacity, datapack tag capacity, then detected/default gear type. A blacklist always wins. A whitelist entry such as `minecraft:stick=2` gives that item exactly two slots and overrides any existing allocation.

## Applying Enhancements

- Stat runes roll from the rune range; stat etchings roll from the etching range.
- Effect runes apply level 2; effect etchings apply level 1; both clamp to the enchantment maximum.
- Existing incompatible effect enchantments can block an application.
- Rune applications can trigger a compatible synergy roll. Etchings cannot create synergies.
- Each rarity adds its configured corruption: Common 1, Uncommon 1, Rare 2, Epic 2, Legendary 3, Mythic 20, and an etching adds 1 by default.

## Synergy Rolls

The default synergy chance is 20%, plus 20 percentage points per Synergy Potential level, capped at 80%. Success adds 5 corruption. Failure adds 2; Fractured gear adds another 5. Resonance inscriptions raise Synergy Potential and immediately attempt available synergies.

## Limits

Exhausted gear cannot be forged. Dissonant gear cannot gain Synergy Potential or mythic runes. Stat caps are always enforced; Upgrade and Cursed inscriptions are the controlled ways to reach or exceed them.
