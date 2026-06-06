# Runic Enchant Integration Guide (For External Mods)

This document explains how another mod can apply and upgrade RUNIC enhancements on `ItemStack`s without using RUNIC's block GUIs.

## Scope

Use this when you want:
- pre-configured dungeon loadouts (items spawned with runic stats/effects already applied),
- a custom "virtual upgrades" screen where players pick which stat on a weapon to improve.

This guide is based on current code in:
- `net.revilodev.runic.stat.RuneStats`
- `net.revilodev.runic.runes.RuneSlots`
- `net.revilodev.runic.item.custom.RuneItem`
- `net.revilodev.runic.item.custom.EtchingItem`
- `net.revilodev.runic.gear.GearAttributes`

## Core Data Model

RUNIC stores enhancement data in two places:

1. Stat runes: `RuneStats` in `DataComponents.CUSTOM_DATA` (`runic_stats` tag).
2. Effect runes: regular enchantments in `DataComponents.ENCHANTMENTS` (filtered to RUNIC effect whitelist via `RuneItem.isEffectEnchantment`).

Slots/capacity are tracked using data components from `ModDataComponents`:
- `RUNE_SLOTS_CAPACITY`
- `RUNE_SLOTS_USED`
- `RUNE_EXPANSIONS_USED`

Important: always mutate runic stats through `RuneStats.set(stack, stats)`, not by raw NBT writes.  
`RuneStats.set` rebuilds attributes and durability modifiers through `RuneAttributeApplier`.

## Loadout Flow (Pre-Applied Gear)

Recommended server-side flow for each generated loadout item:

1. Create base item stack (`ItemStack sword = new ItemStack(Items.DIAMOND_SWORD)`).
2. Ensure slot capacity exists:
   - either rely on default (`RuneSlotCapacityData`) or explicitly set `RUNE_SLOTS_CAPACITY`.
3. Apply stat runes:
   - build one or more `RuneStats` values,
   - combine into current stats with `RuneStats.combine(...)`,
   - call `RuneStats.set(stack, combinedStats)`.
4. Apply effect runes (enchants):
   - add only allowed effects (`RuneItem.isEffectEnchantment(holder)`),
   - cap level with `RuneItem.clampEffectLevel(holder, requestedLevel)` or use forced levels (`forcedEffectLevel` / `forcedEtchingEffectLevel`).
5. Recompute used slots:
   - call `RuneSlots.syncUsedToContents(stack)`.
6. (Optional) consume additional slot count manually with `RuneSlots.tryConsumeSlot(stack)` if you apply custom non-standard modifiers that should cost a slot.

## Virtual Upgrade GUI Flow

For your custom upgrade screen ("select sword -> pick an existing stat -> upgrade it"), keep all logic authoritative on server.

### Suggested server validation

Given `targetStack` and selected `RuneStatType selectedStat`:

1. `RuneStats current = RuneStats.get(targetStack)` must be non-empty.
2. `current.has(selectedStat)` must be true.
3. Current stat value must be `> 0`.
4. Stat must have upgrade cap (`selectedStat.cap() > 0`).
5. Respect curse multiplier: `effectiveCap = selectedStat.cap() * GearAttributes.cursedMultiplier(targetStack)`.
6. Ensure `currentValue < effectiveCap`.
7. Apply your own economy/cost rule (currency/materials/etc).

### Applying the upgrade

1. Copy current map (`new EnumMap<>(current.view())`).
2. Increase selected stat by chosen amount.
3. Clamp to `effectiveCap`.
4. Write back with `RuneStats.set(targetStack, new RuneStats(updatedMap))`.
5. If your design wants durability tradeoff (like RUNIC upgrade inscription), apply damage cost yourself after stat write:
   - `targetStack.set(DataComponents.DAMAGE, newDamageValue)`.

`RuneStats.set(...)` will keep item attributes in sync automatically.

## Reference Pattern (Code Skeleton)

```java
ItemStack gear = ...; // server-owned stack

// 1) Add or merge stats
RuneStats base = RuneStats.get(gear);
RuneStats add = RuneStats.single(RuneStatType.ATTACK_DAMAGE, 2.0F);
RuneStats merged = RuneStats.combine(base, add);
RuneStats.set(gear, merged);

// 2) Add an allowed effect enchant
Holder<Enchantment> effect = ...;
if (RuneItem.isEffectEnchantment(effect)) {
    int lvl = RuneItem.clampEffectLevel(effect, 2);
    ItemEnchantments current = gear.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(current);
    mut.set(effect, Math.max(mut.getLevel(effect), lvl));
    gear.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
}

// 3) Sync slot usage after content changes
RuneSlots.syncUsedToContents(gear);
```

## Compatibility Notes

- Apply on server only; treat client UI as request/preview.
- Call `RuneSlots.syncUsedToContents` after any direct enchant/stat edits.
- Do not bypass `RuneStats.set`; direct NBT edits can desync attributes/durability.
- Etching vs rune stats:
  - etching roll ranges are lower (`RuneStatType.etchingMinPercent`/`etchingMaxPercent`).
  - rune roll ranges are normal (`minPercent`/`maxPercent`).
- If you need deterministic pre-rolls for loadouts, store explicit numeric stat values (not `-1` unrolled templates).

## Recommended Integration Contract Between Mods

For your "dungeon loadout + upgrades" mod, expose two internal services:

1. `RunicLoadoutService`
   - `ItemStack createRunicPreset(ItemStack base, List<StatSpec>, List<EffectSpec>)`
   - performs apply + validation + slot sync.

2. `RunicUpgradeService`
   - `boolean upgradeExistingStat(ItemStack gear, RuneStatType type, float amount, UpgradeContext ctx)`
   - validates, clamps, applies, and returns success/failure.

This keeps all RUNIC-specific behavior in one adapter layer, so future RUNIC changes only require updates in one place.
