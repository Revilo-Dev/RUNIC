# Configuration Reference

The common config is `config/runic-common.toml`. Stop the game/server before large edits, keep TOML strings quoted, and restart or reload the config afterward. Decimal chances use `0.0` to `1.0`; `0.25` means 25%. Rarity values are relative weights: increasing one makes it more common compared with the others.

## Lists

```toml
[enchantments]
whitelist = ["minecraft:sharpness", "minecraft:protection"]

[rune_slots]
blacklist = ["storagedrawers:oak_full_drawers_1"]
whitelist = ["minecraft:stick=2", "othermod:special_sword=6"]
```

`rune_slots.blacklist` always wins. `rune_slots.whitelist` adds unsupported items and overrides existing counts; duplicate ids use the last count. Enchantment ids that do not parse are ignored individually.

## Every Key and Default

### `crafting`

| Key | Default |
|---|---:|
| `crafting.disable_inscription_crafting` | `false` |

### `enchant_blacklist`

| Key | Default |
|---|---:|
| `enchant_blacklist.blacklisted` | `[]` |
| `enchant_blacklist.disable_all` | `false` |

### `enchantments`

| Key | Default |
|---|---:|
| `enchantments.whitelist` | `[]` |

### `enhancement_blacklist`

| Key | Default |
|---|---:|
| `enhancement_blacklist.stats` | `[]` |

### `forging`

| Key | Default |
|---|---:|
| `forging.attributes.ancient_enhancement_power_bonus_percent` | `5.0` |
| `forging.attributes.harmonized_synergy_power_bonus_percent` | `10.0` |
| `forging.attributes.reinforced_durability_loss_reduction_percent` | `10.0` |
| `forging.attributes.tempered_inscription_corruption_reduction_percent` | `10.0` |
| `forging.base_synergy_chance` | `0.20` |
| `forging.common_corruption` | `1` |
| `forging.corruption.corrupted_negative_attribute_roll_chance` | `0.10` |
| `forging.corruption.corrupted_positive_attribute_roll_chance` | `0.03` |
| `forging.corruption.critical_negative_attribute_roll_chance` | `0.20` |
| `forging.corruption.critical_positive_attribute_roll_chance` | `0.05` |
| `forging.corruption.enable_negative_attributes` | `true` |
| `forging.corruption.enable_positive_attributes` | `true` |
| `forging.corruption.stable_attribute_roll_chance` | `0.0` |
| `forging.corruption.tainted_negative_attribute_roll_chance` | `0.05` |
| `forging.corruption.tainted_positive_attribute_roll_chance` | `0.0` |
| `forging.cursed_inscription_corruption` | `10` |
| `forging.cursed_inscription_failure_adds_brittle` | `true` |
| `forging.cursed_inscription_overupgrade_percent` | `25.0` |
| `forging.cursed_inscription_success_chance` | `0.50` |
| `forging.epic_corruption` | `2` |
| `forging.etching_corruption` | `1` |
| `forging.exhausted_corruption_threshold` | `100` |
| `forging.expansion_inscription_corruption` | `8` |
| `forging.expansion_inscription_max_durability_loss_percent` | `10.0` |
| `forging.extraction_inscription_can_extract_mythic` | `false` |
| `forging.extraction_inscription_can_extract_synergies` | `false` |
| `forging.extraction_inscription_corruption` | `8` |
| `forging.failed_synergy_corruption` | `2` |
| `forging.fractured_extra_failure_corruption` | `5` |
| `forging.legendary_corruption` | `3` |
| `forging.max_synergy_chance` | `0.80` |
| `forging.max_synergy_potential` | `3` |
| `forging.mythic_corruption` | `20` |
| `forging.mythics.apply_curse_on_success` | `true` |
| `forging.mythics.ascendance.damage_bonus_percent` | `15.0` |
| `forging.mythics.ascendance.duration_ticks` | `200` |
| `forging.mythics.ascendance.speed_bonus_percent` | `10.0` |
| `forging.mythics.ascendance.target_max_health_threshold` | `50.0` |
| `forging.mythics.can_be_extracted` | `false` |
| `forging.mythics.can_be_mutated_by_wild` | `false` |
| `forging.mythics.dominion.enhancement_power_bonus_percent` | `10.0` |
| `forging.mythics.dominion.synergy_power_bonus_percent` | `5.0` |
| `forging.mythics.enabled` | `true` |
| `forging.mythics.extra_curse_chance` | `0.25` |
| `forging.mythics.hunger.durability_restore_on_kill` | `2` |
| `forging.mythics.hunger.extra_corruption_amount` | `1` |
| `forging.mythics.hunger.extra_corruption_on_hit_chance` | `0.03` |
| `forging.mythics.loot_enabled` | `true` |
| `forging.mythics.min_loot_difficulty` | `4` |
| `forging.mythics.rarity` | `1` |
| `forging.mythics.ruin.damage_bonus_percent` | `20.0` |
| `forging.mythics.ruin.durability_use_increase_percent` | `20.0` |
| `forging.mythics.ruin.extra_corruption_amount` | `1` |
| `forging.mythics.ruin.extra_corruption_chance` | `0.05` |
| `forging.mythics.void.combat_corruption_amount` | `1` |
| `forging.mythics.void.combat_corruption_interval_ticks` | `200` |
| `forging.mythics.void.damage_bonus_percent` | `25.0` |
| `forging.mythics.void.low_health_threshold` | `0.35` |
| `forging.nullification_inscription_can_remove_synergies` | `true` |
| `forging.nullification_inscription_corruption` | `10` |
| `forging.nullification_inscription_removes_slot` | `true` |
| `forging.purification_inscription_corruption` | `10` |
| `forging.purification_inscription_durability_loss_chance` | `0.50` |
| `forging.purification_inscription_max_durability_loss_percent` | `10.0` |
| `forging.rare_corruption` | `2` |
| `forging.relic_loot_injection_enabled` | `true` |
| `forging.relic_socket_inscription_adds_brittle` | `true` |
| `forging.relic_socket_inscription_corruption` | `10` |
| `forging.relics.default_corruption` | `10` |
| `forging.relics.default_durability_use_increase_percent` | `20.0` |
| `forging.relics.dragon_heart.burn_duration_bonus_seconds` | `2` |
| `forging.relics.dragon_heart.corruption` | `10` |

| `forging.relics.dragon_heart.durability_use_increase_percent` | `20.0` |
| `forging.relics.dragon_heart.fire_damage_bonus_percent` | `10.0` |
| `forging.relics.dragon_heart.full_set_fire_damage_bonus_percent` | `25.0` |
| `forging.relics.dragon_heart.full_set_ignite_aura_chance` | `0.15` |
| `forging.relics.dragon_heart.full_set_ignite_aura_radius` | `4.0` |
| `forging.relics.effects_require_equipped` | `true` |
| `forging.relics.elder_guardians_eye.corruption` | `10` |
| `forging.relics.elder_guardians_eye.durability_use_increase_percent` | `20.0` |
| `forging.relics.elder_guardians_eye.full_set_slow_chance` | `0.20` |
| `forging.relics.elder_guardians_eye.full_set_slow_duration_ticks` | `60` |
| `forging.relics.elder_guardians_eye.mining_speed_bonus_percent` | `10.0` |
| `forging.relics.elder_guardians_eye.underwater_damage_bonus_percent` | `15.0` |
| `forging.relics.enable_set_bonuses` | `true` |
| `forging.relics.full_set_required_count` | `4` |
| `forging.relics.wardens_soul.boss_damage_bonus_percent` | `10.0` |
| `forging.relics.wardens_soul.corruption` | `15` |
| `forging.relics.wardens_soul.durability_use_increase_percent` | `35.0` |
| `forging.relics.wardens_soul.full_set_sonic_pulse_cooldown_ticks` | `300` |
| `forging.relics.wardens_soul.full_set_sonic_pulse_damage` | `6.0` |
| `forging.relics.wardens_soul.heavy_damage_pulse_chance` | `0.20` |
| `forging.relics.wardens_soul.high_health_threshold` | `100.0` |
| `forging.relics.wither_charge.corruption` | `12` |
| `forging.relics.wither_charge.damage_to_withered_bonus_percent` | `10.0` |
| `forging.relics.wither_charge.durability_use_increase_percent` | `25.0` |
| `forging.relics.wither_charge.full_set_wither_pulse_chance` | `0.15` |
| `forging.relics.wither_charge.full_set_wither_pulse_radius` | `4.0` |
| `forging.relics.wither_charge.wither_duration_bonus_percent` | `20.0` |
| `forging.reroll_inscription_add_unstable_on_higher_roll` | `true` |
| `forging.reroll_inscription_corruption` | `3` |
| `forging.resonance_inscription_corruption` | `6` |
| `forging.restoration_inscription_adds_brittle` | `true` |
| `forging.restoration_inscription_corruption_reduction` | `10` |
| `forging.restoration_inscription_max_durability_loss_percent` | `15.0` |
| `forging.stabilization_inscription_adds_brittle` | `true` |
| `forging.stabilization_inscription_corruption` | `5` |
| `forging.successful_synergy_corruption` | `5` |
| `forging.synergy_effects.berserk_attack_speed_bonus_percent` | `0.20` |
| `forging.synergy_effects.berserk_duration_ticks` | `100` |
| `forging.synergy_effects.berserk_hits_required` | `3` |
| `forging.synergy_effects.berserk_movement_speed_bonus_percent` | `0.10` |
| `forging.synergy_effects.bloodfire_bleed_chance` | `0.35` |
| `forging.synergy_effects.bloodfire_bleed_duration_ticks` | `80` |
| `forging.synergy_effects.bloodfire_fire_seconds` | `4` |
| `forging.synergy_effects.corrosion_armor_ignore_percent` | `0.25` |
| `forging.synergy_effects.corrosion_bonus_damage_multiplier` | `0.20` |
| `forging.synergy_effects.executioners_fury_damage_bonus_percent` | `0.15` |
| `forging.synergy_effects.executioners_fury_duration_ticks` | `100` |
| `forging.synergy_effects.executioners_fury_execution_health_threshold` | `0.30` |
| `forging.synergy_effects.frostbite_chilled_damage_multiplier` | `0.15` |
| `forging.synergy_effects.frostbite_freeze_bonus_multiplier` | `1.5` |
| `forging.synergy_effects.ice_prison_boss_duration_multiplier` | `0.25` |
| `forging.synergy_effects.ice_prison_cooldown_ticks` | `100` |
| `forging.synergy_effects.ice_prison_duration_ticks` | `40` |
| `forging.synergy_effects.ice_prison_radius` | `3.0` |
| `forging.synergy_effects.juggernaut_armor_bonus` | `4.0` |
| `forging.synergy_effects.juggernaut_cooldown_ticks` | `300` |
| `forging.synergy_effects.juggernaut_damage_threshold_percent` | `0.20` |
| `forging.synergy_effects.juggernaut_duration_ticks` | `100` |
| `forging.synergy_effects.juggernaut_knockback_resistance_bonus` | `0.5` |
| `forging.synergy_effects.reaper_attack_speed_bonus_percent` | `0.15` |
| `forging.synergy_effects.reaper_duration_ticks` | `80` |
| `forging.synergy_effects.reaper_execution_health_threshold` | `0.30` |
| `forging.synergy_effects.reaper_heal_amount` | `3.0` |
| `forging.synergy_effects.shatter_cooldown_ticks` | `40` |
| `forging.synergy_effects.shatter_damage_multiplier` | `0.35` |
| `forging.synergy_effects.shatter_radius` | `3.0` |
| `forging.synergy_effects.soulburn_cooldown_ticks` | `40` |
| `forging.synergy_effects.soulburn_radius` | `4.0` |
| `forging.synergy_effects.soulburn_wither_amplifier` | `0` |
| `forging.synergy_effects.soulburn_wither_duration_ticks` | `100` |
| `forging.synergy_effects.tempest_chain_targets` | `3` |
| `forging.synergy_effects.tempest_damage_multiplier` | `0.25` |
| `forging.synergy_effects.tempest_hits_required` | `5` |
| `forging.synergy_effects.tempest_radius` | `5.0` |
| `forging.synergy_effects.venom_burst_chance` | `0.25` |
| `forging.synergy_effects.venom_burst_damage_multiplier` | `0.20` |
| `forging.synergy_effects.venom_burst_poison_duration_ticks` | `80` |
| `forging.synergy_effects.venom_burst_radius` | `3.5` |
| `forging.synergy_potential_bonus` | `0.20` |
| `forging.tempering_inscription_corruption` | `5` |
| `forging.tempering_inscription_durability_loss_reduction_percent` | `10.0` |
| `forging.uncommon_corruption` | `1` |
| `forging.upgrade_inscription_corruption` | `5` |
| `forging.upgrade_inscription_extra_corruption_if_overforged` | `5` |
| `forging.upgrade_inscription_stat_increase_percent` | `10.0` |
| `forging.wild_inscription_can_mutate_synergies` | `false` |
| `forging.wild_inscription_corruption` | `12` |

### `loot`

| Key | Default |
|---|---:|
| `loot.all_runes_have_etchings` | `false` |
| `loot.common_rune_rarity` | `60` |
| `loot.disable_runic_loot` | `false` |
| `loot.epic_rune_rarity` | `8` |
| `loot.legendary_rune_rarity` | `3` |
| `loot.loot_only_etching_rarity` | `4` |
| `loot.mythic_rune_rarity` | `1` |
| `loot.rare_rune_rarity` | `18` |
| `loot.relic_rarity` | `2` |
| `loot.uncommon_rune_rarity` | `35` |

### `mechanics`

| Key | Default |
|---|---:|
| `mechanics.default_weapon_rune_slots` | `4` |
| `mechanics.disable_rune_slots` | `false` |

### `rune_slots`

| Key | Default |
|---|---:|
| `rune_slots.blacklist` | `[]` |
| `rune_slots.whitelist` | `[]` |

## Important Behavior

- `enchant_blacklist.disable_all` disables all non-whitelisted enchantments.

- `enchant_blacklist.blacklisted` disables selected enchantment ids; the whitelist overrides it.
- `enhancement_blacklist.stats` accepts RUNIC stat ids such as `attack_damage`.
- `mechanics.disable_rune_slots` removes slot limits globally.
- `loot.disable_runic_loot` disables structure injection and enchanted-book filtering.
- `loot.all_runes_have_etchings` adds etching variants for all runes, even overpowered ones.
- `crafting.disable_inscription_crafting` disables Etching Table inscription crafting only.
- `forging.corruption.*` controls attribute-roll chances by corruption band.
- `forging.attributes.*` controls per-level positive attribute bonuses.
- `forging.synergy_effects.*`, `forging.relics.*`, and `forging.mythics.*` tune their named systems.
