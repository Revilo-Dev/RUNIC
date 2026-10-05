# Synergies

Synergies are bonus powers that unlock when the right rune effects are on the same item.

Synergies do not replace the runes that created them. The original runes stay on the item, keep using their rune slots, and continue to work normally. The synergy is an extra bonus layered on top.

Etchings cannot create synergies. Synergies come from runes, resonance effects, or creative-only Synergy Runes.

## Reading Synergies

On gear tooltips, synergy runes that influence a synergy are shown indented below it in yellow. Their rolled values affect how strong the synergy is, so a stronger Freezing rune will make Freezing-based synergies better.

Synergies do not consume rune slots.

## Quick Recipe Chart

```text
Rune A + Rune B ──synergy roll──▶ Free bonus effect
       └─ both original runes remain and keep working
```

| Synergy | Required runes | Main purpose |
|---|---|---|
| Berserk | Attack Speed + Attack Damage | Sustained combat speed |
| Bloodfire | Flame Chance + Bleeding Chance | Fire and bleed pressure |
| Corrosion | Poison Chance + Weakening Chance | Armor penetration |
| Frostbite | Freezing Chance + Bleeding Chance | Stronger freezing and chilled damage |
| Ice Burst / Ice Prison | Freezing Chance + Shocking Chance | Area control |
| Shatter | Freezing Chance + Flame Chance | Area burst damage |
| Reaper | Leeching Chance + Weakening Chance | Healing and finishing weakened targets |
| Soulburn | Flame Chance + Withering Chance | Spreading damage over time |
| Tempest | Shocking Chance + Attack Speed | Chained lightning |
| Venom Burst | Poison Chance + Shocking Chance | Poison explosion |
| Juggernaut | Stone + Resistance | Defensive armor response |

The rune roll ranges are listed in [Enchantments and Base Stats](Enchantments-and-Base-Stats.md). Synergy unlock chances and corruption costs are explained in [Forging](Forging.md#synergy-rolls).

---

## Berserk

**Recipe:** `attack_speed` + `attack_damage`

After landing three qualifying hits, the wielder enters a short combat rush. By default, Berserk increases attack speed by 20% and movement speed by 10% for **5 seconds**. Continuing to fight can refresh the effect.

**Best for:** fast melee weapons and aggressive builds that stay on one target.

---

## Bloodfire

**Recipe:** `flame_chance` + `bleeding_chance`

Bloodfire combines burning and bleeding into sustained pressure. It applies **4 seconds of fire** and has a **35% bleed chance**, with the bleed lasting **4 seconds** by default.

**Best for:** damage-over-time builds and groups of enemies that remain close together.

---

## Corrosion

**Recipe:** `poison_chance` + `weakening_chance`

Corrosion makes poisoned or weakened enemies easier to break through. By default, it ignores **25% of the target's armor** and adds **20% bonus damage** through its configured damage multiplier.

**Best for:** heavily armored enemies and durable bosses.

---

## Frostbite

**Recipe:** `freezing_chance` + `bleeding_chance`

Frostbite makes bleeding targets freeze faster and makes chilled targets more vulnerable. The default freeze buildup is multiplied by **1.5**, while chilled enemies take **15% additional damage**.

**Best for:** control builds that repeatedly apply Freezing and Bleeding.

---

## Ice Burst / Ice Prison

**Recipe:** `freezing_chance` + `shocking_chance`

This synergy creates an area-control burst around a frozen or shocked target. Its default radius is **3 blocks** and its normal duration is **2 seconds**. Bosses receive one quarter of that duration—about **0.5 seconds**—and the effect has a **5-second cooldown**.

**Best for:** interrupting groups and briefly controlling dangerous enemies.

---

## Shatter

**Recipe:** `freezing_chance` + `flame_chance`

Shatter turns the clash between freezing and fire into an area burst. It reaches **3 blocks**, deals damage using a **35% multiplier**, and can trigger once every **2 seconds** by default.

**Best for:** burst damage against packed enemies.

---

## Reaper

**Recipe:** `leeching_chance` + `weakening_chance`

Reaper rewards finishing weakened enemies. It heals **3 health**, grants **15% attack speed for 4 seconds**, and becomes especially effective when the target falls below **30% health**.

**Best for:** sustain-focused melee builds and long fights.

---

## Soulburn

**Recipe:** `flame_chance` + `withering_chance`

Soulburn spreads burning and Wither between nearby enemies. The default spread reaches **4 blocks**, applies Wither for **5 seconds**, and can trigger once every **2 seconds**.

**Best for:** clearing clustered enemies with overlapping damage-over-time effects.

---

## Tempest

**Recipe:** `shocking_chance` + `attack_speed`

Tempest builds electrical charge through repeated attacks. After **5 hits**, it chains lightning to as many as **3 nearby targets** within **5 blocks**, dealing damage with a **25% multiplier**.

**Best for:** fast weapons fighting groups.

---

## Venom Burst

**Recipe:** `poison_chance` + `shocking_chance`

Poisoned enemies have a **25% chance** to erupt. The burst reaches **3.5 blocks**, deals damage with a **20% multiplier**, and applies poison for **4 seconds**.

**Best for:** spreading poison through tightly grouped enemies.

---

## Juggernaut

**Recipe:** `stone` + `resistance` — armor only

Juggernaut reacts when a hit deals at least **20% of the wearer's health**. It grants **4 armor** and **50% knockback resistance** for **5 seconds**, followed by a **15-second cooldown**.

**Best for:** tank builds that need protection after taking a heavy hit.

---

## Related Pages

- [Enchantments and Base Stats](Enchantments-and-Base-Stats.md) — rune ranges, etching ranges, and stat caps.
- [Forging](Forging.md#synergy-rolls) — unlock chance, Synergy Potential, success corruption, and failure corruption.
- [Corruption and Attributes](Corruption-and-Attributes.md) — Fractured and Harmonized interactions.
- [Configuration Reference](Configuration.md#forging) — every `forging.synergy_effects` setting and default.

## Creative Synergy Runes

Synergy Runes are creative-only testing items. Applying one also adds the two relevant base rune effects so the synergy can be tested immediately.

## Synergy Potential

Some items can gain Synergy Potential. More potential gives more chances to unlock synergy bonuses, but failed attempts can add corruption.

## Dissonant Items

The Dissonant Inscription is a creative-only testing inscription. It sets an item's Synergy Potential to 0 and prevents that item from holding Mythic Runes.
