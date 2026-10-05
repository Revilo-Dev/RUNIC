# Corruption and Attributes

Corruption is a stored risk score. The default Exhausted threshold is 100, and the displayed percentage is `corruption / threshold × 100`. Corruption is clamped between zero and the threshold.

## Bands and Roll Chances

Every operation that increases corruption performs independent negative and positive attribute rolls using the band reached after that increase.

| Band | Percent | Negative roll | Positive roll | Possible negative attributes | Possible positive attributes |
|---|---:|---:|---:|---|---|
| Stable | 0–24% | 0% | 0% | none | none |
| Tainted | 25–49% | 5% | 0% | Brittle, Fractured, Unstable | none |
| Corrupted | 50–74% | 10% | 3% | Brittle, Fractured, Unstable, Chaotic | Reinforced, Tempered |
| Critical | 75–99% | 20% | 5% | Brittle, Fractured, Unstable, Chaotic, Cursed | Reinforced, Tempered, Ancient, Harmonized |
| Exhausted | 100% | no roll | no roll | Exhausted is applied | none |

The two rolls are independent, so one corruption gain can add both a negative and a positive attribute. Negative attributes are normally added only once by random corruption rolls; positive attributes can gain levels up to 10.

## Attribute Effects

| Attribute | Effect |
|---|---|
| Sealed | Prevents another extraction. |
| Cursed | Multiplies enhancement power and effective stat caps by `0.95^level`; also reduces the next Cursed Inscription success chance by 10 percentage points per level. |
| Unstable | Lowers both ends of future stat reroll ranges by 2 per level. |
| Negative | Reduces effective rune-slot capacity by one per level. |
| Ancient | Adds 5% enhancement power per level by default. |
| Brittle | Increases durability loss through the durability handling system. |
| Fractured | Adds 5 extra corruption after a failed synergy roll by default. |
| Exhausted | Blocks further workbench modification. |
| Overforged | Marks upgraded gear; another Upgrade adds extra corruption. |
| Chaotic | Marks gear changed by a Wild Inscription. |
| Reinforced | Reduces durability loss by 10% per level by default. |
| Tempered | Reduces inscription corruption by 10% per level by default. |
| Harmonized | Adds 10% synergy power per level by default, stacking with Dominion. |
| Dissonant | Forces Synergy Potential to zero and prevents mythic runes. |

## Managing Corruption

Restoration removes corruption at the cost of maximum durability and Brittle. Purification removes one removable negative attribute but adds corruption and Brittle. Stabilization trades Unstable for Brittle. Reaching the threshold permanently applies Exhausted even if corruption is later lowered.
