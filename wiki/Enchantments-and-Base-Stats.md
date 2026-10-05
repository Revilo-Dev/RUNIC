# Enchantments and Base Stats

RUNIC enhancements are either stat enhancements or effect enchantments. A rune uses the rune range; an etching uses the lower etching range. Values are rolled when applied in the Artisan's Workbench. Percent-based values display as percentages; flat stats are marked below. A cap of `0` means the stat has no fixed cap.

| Stat id | Rune roll | Etching roll | Cap | Type |
|---|---:|---:|---:|---|
| `attack_speed` | 10–28 | 5–14 | none | percent |
| `attack_damage` | 2.0–3.5 | 1.0–2.0 | none | flat, 0.1 steps |
| `attack_range` | 6–14 | 3–7 | 20 | percent |
| `movement_speed` | 8–18 | 4–9 | 30 | percent |
| `sweeping_range` | 6–12 | 3–6 | 35 | percent |
| `durability` | 12–100 | 6–50 | none | percent |
| `resistance` | 1–4 | 1–2 | 4 | percent |
| `fire_resistance` | 4–8 | 2–4 | 15 | percent |
| `blast_resistance` | 4–8 | 2–4 | 15 | percent |
| `projectile_resistance` | 4–8 | 2–4 | 15 | percent |
| `knockback_resistance` | 4–8 | 2–4 | 15 | percent |
| `mining_speed` | 12–80 | 6–40 | none | percent |
| `undead_damage` | 10–32 | 5–16 | none | percent |
| `nether_damage` | 10–32 | 5–16 | none | percent |
| `health` | 2.0–4.0 | 1.0–2.0 | none | flat, 0.1 steps |
| `stun_chance` | 6–12 | 3–6 | 35 | percent |
| `flame_chance` | 6–12 | 3–6 | 35 | percent |
| `bleeding_chance` | 6–12 | 3–6 | 60 | percent |
| `shocking_chance` | 6–12 | 3–6 | 60 | percent |
| `poison_chance` | 6–12 | 3–6 | 60 | percent |
| `withering_chance` | 6–12 | 3–6 | 60 | percent |
| `weakening_chance` | 6–12 | 3–6 | 60 | percent |
| `draw_speed` | 10–24 | 5–12 | none | percent |
| `toughness` | 2.0–4.0 | 1.0–2.0 | 20 | flat, 0.1 steps |
| `freezing_chance` | 6–12 | 3–6 | 35 | percent |
| `leeching_chance` | 2.0–6.0 | 1.0–3.0 | 18 | percent, 0.1 steps |
| `fangs` | 8–15 | 4–8 | 35 | percent |
| `stone` | 10–20 | 5–10 | 30 | percent |
| `aegis` | 3–8 | 1–4 | 12 | percent |
| `jump_height` | 6–18 | 3–9 | 25 | percent |
| `power` | 8–25 | 4–13 | none | percent |
| `ability_power` | 8–25 | 4–13 | none | percent |

## Effect Enchantments

Effect runes apply their enchantment at level 2 and etchings at level 1, clamped to that enchantment's maximum level. RUNIC ships support for selected vanilla and compatibility enchantments. Datapacks and `enchantments.whitelist` can add more.

Whitelisted enchantments are deliberately unrestricted by RUNIC: they can be represented as runes or etchings, applied to any item accepted by the workbench, offered by the enchanting table, and retained in enchanted-book loot.
