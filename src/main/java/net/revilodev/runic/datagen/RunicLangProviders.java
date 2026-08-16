package net.revilodev.runic.datagen;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.revilodev.runic.stat.RuneStatType;


public final class RunicLangProviders {
    private RunicLangProviders() {}

    public static final class EN_US extends Base {
        public EN_US(PackOutput output) { super(output, "en_us"); }
    }

    public static final class EN_GB extends Base {
        public EN_GB(PackOutput output) { super(output, "en_gb"); }
    }

    public static final class EN_AU extends Base {
        public EN_AU(PackOutput output) { super(output, "en_au"); }
    }

    public static final class EN_CA extends Base {
        public EN_CA(PackOutput output) { super(output, "en_ca"); }
    }

    public static final class EN_NZ extends Base {
        public EN_NZ(PackOutput output) { super(output, "en_nz"); }
    }

    private abstract static class Base extends LanguageProvider {
        protected Base(PackOutput output, String locale) {
            super(output, "runic", locale); // MODID
        }


        protected void addTranslations() {
            add("creativetab.runicmod.runic_items", "RUNIC");

            // Items
            add("item.runic.repair_rune", "Restoration Inscription");
            add("item.runic.expansion_rune", "Expansion Inscription");
            add("item.runic.upgrade_rune", "Upgrade Inscription");
            add("item.runic.nullification_rune", "Nullification Inscription");
            add("item.runic.reroll_inscription", "Reroll Inscription");
            add("item.runic.wild_inscription", "Wild Inscription");
            add("item.runic.extraction_inscription", "Extraction Inscription");
            add("item.runic.cursed_inscription", "Cursed Inscription");
            add("item.runic.resonance_inscription", "Resonance Inscription");
            add("item.runic.purification_inscription", "Purification Inscription");
            add("item.runic.stabilization_inscription", "Stabilization Inscription");
            add("item.runic.tempering_inscription", "Tempering Inscription");
            add("item.runic.relic_socket_inscription", "Relic Socket Inscription");
            add("item.runic.dissonant_inscription", "Dissonant Inscription");
            add("item.runic.blank_inscription", "Blank Inscription");
            add("item.runic.enhanced_rune", "Rune");
            add("item.runic.etching", "Etching");
            add("item.runic.blank_etching", "Etching");

            // Blocks
            add("block.runic.artisans_workbench", "Artisans Workbench");
            add("block.runic.etching_table", "Inscription Table");

            // Vanilla
            add("minecraft:experience_bottle", "Bottle o' Experience");

            // Effects
            add("effect.runic.bleeding", "Bleeding");
            add("effect.runic.stunning", "Stunned");
            add("effect.runic.frozen", "Frozen");

            // Tooltip stats
            add("tooltip.runic.stat.attack_speed", "\uefe5 Attack Speed");
            add("tooltip.runic.stat.attack_damage", "\uefe5 Attack Damage");
            add("tooltip.runic.stat.attack_range", "\uefe5 Attack Range");
            add("tooltip.runic.stat.movement_speed", "\uefe4 Movement Speed");
            add("tooltip.runic.stat.sweeping_range", "\uefe5 Sweeping Range");
            add("tooltip.runic.stat.durability", "\uefe2 Durability");
            add("tooltip.runic.stat.resistance", "\uefe2 Resistance");
            add("tooltip.runic.stat.fire_resistance", "\uefe2 Fire Resistance");
            add("tooltip.runic.stat.blast_resistance", "\uefe2 Blast Resistance");
            add("tooltip.runic.stat.projectile_resistance", "\uefe2 Projectile Resistance");
            add("tooltip.runic.stat.knockback_resistance", "\uefe2 Knockback Resistance");
            add("tooltip.runic.stat.mining_speed", "\uefe6 Mining Speed");
            add("tooltip.runic.stat.swimming_speed", "\uefe7 Swimming Speed");
            add("tooltip.runic.stat.fall_reduction", "\uefe4 Fall Damage Reduction");
            add("tooltip.runic.stat.undead_damage", "\ueef3 Undead Damage");
            add("tooltip.runic.stat.nether_damage", "\ueef3 Nether Damage");
            add("tooltip.runic.stat.health", "\ueef4 Health Boost");
            add("tooltip.runic.stat.stun_chance", "\ueef4 Stunning");
            add("tooltip.runic.stat.flame_chance", "\ueef4 Fire Aspect");
            add("tooltip.runic.stat.bleeding_chance", "\ueef4 Bleeding");
            add("tooltip.runic.stat.shocking_chance", "\ueef4 Shocking");
            add("tooltip.runic.stat.poison_chance", "\ueef4 Toxic");
            add("tooltip.runic.stat.withering_chance", "\ueef4 Withering");
            add("tooltip.runic.stat.weakening_chance", "\ueef4 Diminishing");
            add("tooltip.runic.stat.water_breathing", "\uefe1 Water Breathing");
            add("tooltip.runic.stat.draw_speed", "\uefe8 Draw Speed");
            add("tooltip.runic.stat.jump_height", "\uefe4 Leaping");

            add("tooltip.runic.stat.toughness", "\uefe2 Toughness");
            add("tooltip.runic.stat.freezing_chance", "\ueef4 Freezing");
            add("tooltip.runic.stat.leeching_chance", "\ueef4 Leeching");
            add("tooltip.runic.stat.looting", "\ueef3 Looting");
            add("tooltip.runic.stat.fangs", "\ueef4 Fangs");
            add("tooltip.runic.stat.stone", "\uefe2 Stone Skin");
            add("tooltip.runic.stat.aegis", "\uefe2 Aegis");
            add("tooltip.runic.stat.power", "\uefe8 Power");
            add("tooltip.runic.stat.ability_power", "\ueef4 Ability Power");

            // Stat descriptions
            add("tooltip.runic.stat_desc.attack_speed", "Increases attack speed.");
            add("tooltip.runic.stat_desc.attack_damage", "Increases attack damage.");
            add("tooltip.runic.stat_desc.attack_range", "Increases melee reach.");
            add("tooltip.runic.stat_desc.movement_speed", "Increases movement speed.");
            add("tooltip.runic.stat_desc.sweeping_range", "Increases sweeping attack range.");
            add("tooltip.runic.stat_desc.durability", "Increases maximum durability.");
            add("tooltip.runic.stat_desc.resistance", "Reduces incoming damage.");
            add("tooltip.runic.stat_desc.fire_resistance", "Reduces fire damage.");
            add("tooltip.runic.stat_desc.blast_resistance", "Reduces explosion damage.");
            add("tooltip.runic.stat_desc.projectile_resistance", "Reduces projectile damage.");
            add("tooltip.runic.stat_desc.knockback_resistance", "Reduces knockback taken.");
            add("tooltip.runic.stat_desc.mining_speed", "Increases mining speed.");
            add("tooltip.runic.stat_desc.swimming_speed", "Increases swimming speed.");
            add("tooltip.runic.stat_desc.undead_damage", "Increases damage to undead.");
            add("tooltip.runic.stat_desc.nether_damage", "Increases damage to nether mobs.");
            add("tooltip.runic.stat_desc.health", "Increases maximum health.");
            add("tooltip.runic.stat_desc.stun_chance", "Chance to apply stunning.");
            add("tooltip.runic.stat_desc.flame_chance", "Chance to apply fire aspect.");
            add("tooltip.runic.stat_desc.bleeding_chance", "Chance to apply bleeding.");
            add("tooltip.runic.stat_desc.shocking_chance", "Chance to apply shocking.");
            add("tooltip.runic.stat_desc.poison_chance", "Chance to apply toxic.");
            add("tooltip.runic.stat_desc.withering_chance", "Chance to apply withering.");
            add("tooltip.runic.stat_desc.weakening_chance", "Chance to apply diminishing.");
            add("tooltip.runic.stat_desc.water_breathing", "Increases underwater breathing duration.");
            add("tooltip.runic.stat_desc.draw_speed", "Increases bow draw speed.");
            add("tooltip.runic.stat_desc.toughness", "Increases toughness.");
            add("tooltip.runic.stat_desc.freezing_chance", "Chance to apply freezing.");
            add("tooltip.runic.stat_desc.leeching_chance", "Chance to leach 10% of enemy max health on critical hit.");
            add("tooltip.runic.stat_desc.fangs", "Chance to summon a line of evoker fangs on hit.");
            add("tooltip.runic.stat_desc.stone", "Gain temporary resistance after a heavy hit.");
            add("tooltip.runic.stat_desc.aegis", "Chance to negate an incoming hit.");
            add("tooltip.runic.stat_desc.jump_height", "Increases leaping height.");
            add("tooltip.runic.stat_desc.power", "Increases ranged damage.");
            add("tooltip.runic.stat_desc.ability_power", "Increases ability damage and scaling.");

            // Enchants
            add("enchantment.runic.poison_cloud", "Poison Cloud");
            add("enchantment.runic.pain_cycle", "Pain Cycle");
            add("enchantment.runic.committed", "Committed");

            // Inscription
            add("tooltip.runic.resonance_inscription", "Increases Synergy Potential by 1, but fractures the item and increases corruption.");
            add("tooltip.runic.repair_rune", "Reduces corruption at the cost of maximum durability.");
            add("tooltip.runic.expansion_rune", "Adds an enhancement slot at the cost of durability and corruption.");
            add("tooltip.runic.nullification_rune", "Removes enhancements, but damages the item's capacity and increases corruption.");
            add("tooltip.runic.upgrade_rune", "Pushes a stat enhancement higher, increasing corruption and marking the item as Overforged.");
            add("tooltip.runic.reroll_inscription", "Rerolls a stat enhancement. Better outcomes may destabilize the item.");
            add("tooltip.runic.wild_inscription", "Mutates one enhancement into another of the same category, adding corruption and chaos.");
            add("tooltip.runic.cursed_inscription", "Attempts to force a stat beyond its limit. Failure curses and damages the item.");
            add("tooltip.runic.extraction_inscription", "Extracts one enhancement, sealing the item and increasing corruption.");
            add("tooltip.runic.purification_inscription", "Removes one negative attribute, but adds brittleness, increases corruption, and may reduce durability.");
            add("tooltip.runic.stabilization_inscription", "Stabilizes an item at the cost of brittleness and corruption.");
            add("tooltip.runic.tempering_inscription", "Reinforces an item, reducing durability loss but increasing corruption.");
            add("tooltip.runic.relic_socket_inscription", "Adds a relic socket, allowing one future relic to be bound to the item.");
            add("tooltip.runic.dissonant_inscription", "Sets Synergy Potential to 0 and prevents this item from holding mythic runes.");
            add("tooltip.runic.use_artisans_workbench", "Apply in an Artisan's Workbench");
            add("tooltip.runic.inscription_attributes", "Attributes:");
            add("tooltip.runic.rarity.common", "Common");
            add("tooltip.runic.rarity.uncommon", "Uncommon");
            add("tooltip.runic.rarity.rare", "Rare");
            add("tooltip.runic.rarity.epic", "Epic");
            add("tooltip.runic.rarity.legendary", "Legendary");
            add("tooltip.runic.rarity.synergy", "Synergy");
            add("tooltip.runic.rarity.mythic", "Mythic");
            add("tooltip.runic.rarity.cursed", "Cursed");

            add("tooltip.runic.attribute.sealed", "Sealed");
            add("tooltip.runic.attribute.cursed", "Cursed");
            add("tooltip.runic.attribute.instable", "Unstable");
            add("tooltip.runic.attribute.negative", "Negative");
            add("tooltip.runic.attribute.ancient", "Ancient");
            add("tooltip.runic.attribute.brittle", "Brittle");
            add("tooltip.runic.attribute.fractured", "Fractured");
            add("tooltip.runic.attribute.exhausted", "Exhausted");
            add("tooltip.runic.attribute.overforged", "Overforged");
            add("tooltip.runic.attribute.chaotic", "Chaotic");
            add("tooltip.runic.attribute.reinforced", "Reinforced");
            add("tooltip.runic.attribute.tempered", "Tempered");
            add("tooltip.runic.attribute.harmonized", "Harmonized");
            add("tooltip.runic.attribute.dissonant", "Dissonant");
            add("tooltip.runic.inscription_corruption", "Corruption: %s");
            add("tooltip.runic.attribute_desc.sealed", "Prevents further modification at the Artisan's Workbench.");
            add("tooltip.runic.attribute_desc.cursed", "Reduces runic stat values and makes future changes more dangerous.");
            add("tooltip.runic.attribute_desc.instable", "Raises forging risk and weakens future rerolls.");
            add("tooltip.runic.attribute_desc.negative", "Reduces effective rune slot capacity by 1 per stack.");
            add("tooltip.runic.attribute_desc.ancient", "Boosts all rune power on the item.");
            add("tooltip.runic.attribute_desc.brittle", "This item loses durability faster.");
            add("tooltip.runic.attribute_desc.fractured", "Failed synergy attempts add additional corruption.");
            add("tooltip.runic.attribute_desc.exhausted", "Maximum corruption reached. Runic modification is blocked.");
            add("tooltip.runic.attribute_desc.overforged", "Further stat upgrades add additional corruption.");
            add("tooltip.runic.attribute_desc.chaotic", "Wild modifications have made this item unpredictable.");
            add("tooltip.runic.attribute_desc.reinforced", "Durability loss reduced.");
            add("tooltip.runic.attribute_desc.tempered", "Inscription corruption gain reduced.");
            add("tooltip.runic.attribute_desc.harmonized", "Synergy effects are stronger.");
            add("tooltip.runic.attribute_desc.dissonant", "Synergy Potential is fixed at 0 and mythic runes cannot be held.");
            add("tooltip.runic.relic_socket.empty", "Relic Socket: Empty");
            add("tooltip.runic.relic_socket.bound", "Relic Socket: Bound");
            add("tooltip.runic.relic", "Relic: %s");
            add("tooltip.runic.relic_unknown", "Unknown");
            add("tooltip.runic.relic_durability_use", "Durability Use: +%s%%");
            add("tooltip.runic.relic_corruption", "Corruption: +%s%%");
            add("tooltip.runic.requires_empty_relic_socket", "Requires an empty relic socket.");
            add("message.runic.exhausted", "This item is exhausted and can no longer be modified.");
            add("message.runic.no_relic_socket", "This item has no relic socket.");
            add("message.runic.relic_socket_filled", "This item already contains a relic.");
            add("warning.runic.this_will_exhaust", "Warning: This will Exhaust the item.");
            add("message.runic.no_corruption", "This item has no corruption to restore.");
            add("tooltip.runic.corruption", "Corruption: %s%%");
            add("tooltip.runic.corruption_band_line", "Corruption: %s%%");
            add("tooltip.runic.corruption_band.stable", "Stable");
            add("tooltip.runic.corruption_band.tainted", "Tainted");
            add("tooltip.runic.corruption_band.corrupted", "Corrupted");
            add("tooltip.runic.corruption_band.critical", "Critical");
            add("tooltip.runic.corruption_band.exhausted", "Exhausted");
            add("tooltip.runic.enhancements_header", "Enhancements");
            add("tooltip.runic.enhancement_slots", "%s / %s");
            add("tooltip.runic.attributes_header", "Attributes");
            add("tooltip.runic.details_hint", "(Ctrl for details)");
            add("tooltip.runic.preview_changes_header", "Changes");
            add("tooltip.runic.preview_stat_roll_header", "Stat Roll");
            add("tooltip.runic.preview_corruption_delta", "Corruption: %s");
            add("tooltip.runic.preview_result_band", "%s%%");
            add("tooltip.runic.preview_slots_result", "%s");
            add("tooltip.runic.preview_synergy_potential_result", "Synergy Potential: %s -> %s");
            add("tooltip.runic.preview_synergy_failure", "Failure: +%s%% Corruption");
            add("tooltip.runic.preview_synergy_failure_fractured", "Fractured Failure: +%s%% Corruption");
            add("tooltip.runic.preview_stat_roll_range", "%s: %s to %s");
            add("tooltip.runic.preview_risk_negative", "  Risk: May gain negative attributes");
            add("tooltip.runic.preview_risk_positive", "  Opportunity: May gain positive attributes");
            add("tooltip.runic.synergy_potential", "Synergy Potential: %s");
            add("tooltip.runic.synergy_chance", "Synergy Chance: %s%%");
            add("tooltip.runic.synergies", "Synergies:");
            add("tooltip.runic.synergy_rune", "Synergy Rune");
            add("tooltip.runic.synergy_influenced_by", "Influenced by:");
            add("tooltip.runic.synergy_enhancement", "Synergy");
            add("tooltip.runic.category_line", "Category: %s");
            add("tooltip.runic.category.offensive", "Offensive");
            add("tooltip.runic.category.defensive", "Defensive");
            add("tooltip.runic.category.elemental", "Elemental");
            add("tooltip.runic.category.utility", "Utility");
            add("tooltip.runic.category.forbidden", "Forbidden");
            add("tooltip.runic.category.synergy", "Synergy");
            add("tooltip.runic.synergy.shatter", "Shatter");
            add("tooltip.runic.synergy.bloodfire", "Bloodfire");
            add("tooltip.runic.synergy.corrosion", "Corrosion");
            add("tooltip.runic.synergy.fury", "Fury");
            add("tooltip.runic.synergy.juggernaut", "Juggernaut");
            add("tooltip.runic.synergy.tempest", "Tempest");
            add("tooltip.runic.synergy.reaper", "Reaper");
            add("tooltip.runic.synergy.soulburn", "Soulburn");
            add("tooltip.runic.synergy.frostbite", "Frostbite");
            add("tooltip.runic.synergy.venom_burst", "Venom Burst");
            add("tooltip.runic.synergy.berserk", "Berserk");
            add("tooltip.runic.synergy.ice_burst", "Ice Burst");
            add("tooltip.runic.synergy_desc.shatter", "Freezing and fire aspect shatter frozen enemies, dealing 50% max health or their remaining health, whichever is greater.");
            add("tooltip.runic.synergy_desc.bloodfire", "Bleeding and fire aspect create soul fire damage that spreads for 1 second to touching enemies and bursts into blood particles on death.");
            add("tooltip.runic.synergy_desc.corrosion", "Toxic and diminishing make poisoned or weakened enemies take bonus damage that partially ignores armor.");
            add("tooltip.runic.synergy_desc.fury", "Attack damage and attack speed increase damage against enemies below 10% health.");
            add("tooltip.runic.synergy_desc.juggernaut", "Taking a heavy hit briefly grants strong resistance and knockback control.");
            add("tooltip.runic.synergy_desc.tempest", "Shocking attacks build charge on enemies; when they die, the charge splits into zaps across nearby enemies.");
            add("tooltip.runic.synergy_desc.reaper", "Leeching and diminishing drain weakened enemies harder while healing you for the normal leech amount.");
            add("tooltip.runic.synergy_desc.soulburn", "Fire aspect and withering feed each other, spreading burn and wither to nearby enemies.");
            add("tooltip.runic.synergy_desc.frostbite", "Bleeding enemies freeze more easily and take bonus pressure while chilled.");
            add("tooltip.runic.synergy_desc.venom_burst", "Poisoned enemies can erupt, spreading poison and lightning-like damage to nearby targets.");
            add("tooltip.runic.synergy_desc.berserk", "After 3 critical hits on one enemy, gain Strength II and Speed III for 2 seconds while continuing to crit that enemy.");
            add("tooltip.runic.synergy_desc.ice_burst", "Freezing and shocking make frozen enemies burst into lightning that strikes nearby enemies.");
            add("item.runic.dragon_heart", "Dragon Heart");
            add("item.runic.elder_guardians_eye", "Elder Guardian's Eye");
            add("item.runic.wither_charge", "Wither Charge");
            add("item.runic.wardens_soul", "Warden's Soul");
            add("tooltip.runic.relic_desc.dragon_heart", "Empowers fire-based effects and rewards aggressive burning builds, but increases durability use and corruption.");
            add("tooltip.runic.relic_desc.elder_guardians_eye", "Improves underwater combat and mining, with stronger benefits while fighting in water.");
            add("tooltip.runic.relic_desc.wither_charge", "Extends wither effects and increases damage against withered enemies.");
            add("tooltip.runic.relic_desc.wardens_soul", "Improves damage against powerful enemies and rewards fighting high-health targets.");
            add("tooltip.runic.relic_base.dragon_heart", "Base Effect: improves fire damage and burn pressure.");
            add("tooltip.runic.relic_base.elder_guardians_eye", "Base Effect: improves underwater combat and mining.");
            add("tooltip.runic.relic_base.wither_charge", "Base Effect: improves damage and duration against withered enemies.");
            add("tooltip.runic.relic_base.wardens_soul", "Base Effect: improves damage against high-health enemies.");
            add("tooltip.runic.relic_set.dragon_heart", "Full Set: press the relic keybind to breathe dragon fire.");
            add("tooltip.runic.relic_set.elder_guardians_eye", "Full Set: press the relic keybind to fire a guardian beam.");
            add("tooltip.runic.relic_set.wither_charge", "Full Set: press the relic keybind to launch a wither bullet.");
            add("tooltip.runic.relic_set.wardens_soul", "Full Set: press the relic keybind to release a sonic boom.");
            add("tooltip.runic.hold_ctrl_more", "Hold Ctrl to read more.");
            add("key.categories.runic", "Runic");
            add("key.runic.relic_power", "Use Relic Power");
            add("tooltip.runic.mythic_rune", "Mythic Rune");
            add("tooltip.runic.mythic_runes", "Mythic Runes");
            add("tooltip.runic.mythic_unknown", "Unknown Mythic Rune");
            add("tooltip.runic.mythic_name.ruin", "\uefe5 Ruin");
            add("tooltip.runic.mythic_name.dominion", "\uefe2 Dominion");
            add("tooltip.runic.mythic_name.hunger", "\uefe5 Hunger");
            add("tooltip.runic.mythic_name.void", "\uefe5 Void");
            add("tooltip.runic.mythic_name.ascendance", "\uefe5 Ascendance");
            add("tooltip.runic.mythic.ruin", "\uefe5 Ruin");
            add("tooltip.runic.mythic.dominion", "\uefe2 Dominion");
            add("tooltip.runic.mythic.hunger", "\uefe5 Hunger");
            add("tooltip.runic.mythic.void", "\uefe5 Void");
            add("tooltip.runic.mythic.ascendance", "\uefe5 Ascendance");
            add("tooltip.runic.mythic_desc.ruin", "Greatly increases weapon damage. Hits may add extra corruption, and the item uses more durability.");
            add("tooltip.runic.mythic_desc.dominion", "Amplifies the power of other enhancements and synergies on this item.");
            add("tooltip.runic.mythic_desc.hunger", "Restores durability on kills, but attacks can add extra corruption as the rune feeds.");
            add("tooltip.runic.mythic_desc.void", "Deals more damage to weakened targets and builds corruption while the item remains in combat.");
            add("tooltip.runic.mythic_desc.ascendance", "Defeating powerful enemies briefly grants strength and speed.");
            add("message.runic.mythic_disabled", "Mythic runes are disabled.");
            add("message.runic.mythic_already_present", "This item already has that Mythic rune.");
            add("message.runic.mythic_wrong_item", "That Mythic rune cannot be applied to this item.");
            add("message.runic.mythic_blocked_dissonant", "Dissonant items cannot hold mythic runes.");

            // tooltips
            add("tooltip.runic.use_inscription_table", "Used in an Inscription Table");
            add("tooltip.runic.use_enchanting_table", "Used in an Enchanting Table");
            add("button.runic.reroll_etching", "Reroll");
            add("tooltip.runic.reroll_etching", "Use 1 level and 1 lapis to roll a random etching.");
            for (RuneStatType stat : RuneStatType.values()) {
                add("enchantment.runic.stat." + stat.id(), titleCase(stat.id()));
            }
            add("tooltip.runic.aqua_affinity", "Increases underwater mining speed");
            add("tooltip.runic.bane_of_arthropods", "Deal more damage to arthropod type enemies");
            add("tooltip.runic.binding_curse", "Item cannot be unequipped");
            add("tooltip.runic.blast_protection", "Reduces explosion type damage");
            add("tooltip.runic.breach", "Reduces armour effectiveness");
            add("tooltip.runic.channeling", "Strikes lightning during thunderstorms");
            add("tooltip.runic.depth_strider", "Increases underwater movement speed");
            add("tooltip.runic.efficiency", "Increases mining speed");
            add("tooltip.runic.feather_falling", "Reduces fall damage");
            add("tooltip.runic.fire_aspect", "Sets targets alight on hit");
            add("tooltip.runic.fire_protection", "Reduces fire damage and burn time");
            add("tooltip.runic.flame", "Sets arrows on fire");
            add("tooltip.runic.fortune", "Increases block drops");
            add("tooltip.runic.frost_walker", "Turns water into ice");
            add("tooltip.runic.impaling", "Extra damage against aquatic mobs");
            add("tooltip.runic.infinity", "50/50 chance to not consume an arrow");
            add("tooltip.runic.knockback", "Increases knockback distance");
            add("tooltip.runic.looting", "Increases loot drops");
            add("tooltip.runic.loyalty", "Returns after being thrown");
            add("tooltip.runic.luck_of_the_sea", "Improves rarity of fishing loot");
            add("tooltip.runic.lure", "Decreases time to catch fish");
            add("tooltip.runic.mending", "Uses XP to repair the item");
            add("tooltip.runic.multishot", "Shoot an extra 2 arrows");
            add("tooltip.runic.piercing", "Arrows pass through multiple mobs");
            add("tooltip.runic.power", "Increases arrow damage");
            add("tooltip.runic.projectile_protection", "Reduces projectile damage");
            add("tooltip.runic.protection", "Reduces melee damage");
            add("tooltip.runic.punch", "Increases arrow knockback");
            add("tooltip.runic.quick_charge", "Reduces reload time");
            add("tooltip.runic.respiration", "Extends underwater breathing time");
            add("tooltip.runic.riptide", "Propels the player in water or rain");
            add("tooltip.runic.sharpness", "+1 Attack Damage per level");
            add("tooltip.runic.silk_touch", "Mined blocks drop themselves");
            add("tooltip.runic.smite", "Deals extra damage to undead");
            add("tooltip.runic.soul_speed", "Increases movement on soul blocks");
            add("tooltip.runic.sweeping_edge", "Increases sweeping attack damage");
            add("tooltip.runic.swift_sneak", "Increases sneaking speed");
            add("tooltip.runic.thorns", "Damages attackers");
            add("tooltip.runic.unbreaking", "Increases durability");
            add("tooltip.runic.vanishing_curse", "The item vanishes on death");
            add("tooltip.runic.wind_burst", "Summons a wind charge on attack");

            // descriptions
            add("tooltip.runic.skulk_smite", "Deals extra damage to sculk creatures");
            add("tooltip.runic.capacity", "Increases maximum capacity");
            add("tooltip.runic.soul_siphoner", "% chance to siphon extra soul essence");
            add("tooltip.runic.fire_react", "% chance to ignite attackers");
            add("tooltip.runic.catalysis", "Enhances potion and catalyst effects");
            add("tooltip.runic.destruction", "+% attack damage");
            add("tooltip.runic.mystical_enlightenment", "Boosts enchanting power of nearby tables");
            add("tooltip.runic.renewal", "Gradually repairs the item");
            add("tooltip.runic.chill_aura", "Slows nearby enemies with freezing aura");
            add("tooltip.runic.potato_recovery", "% chance to not consume a potato");
            add("tooltip.runic.acrobat", "Reduces dodge roll cooldown");
            add("tooltip.runic.longfooted", "Increases dodge roll distance");
            add("tooltip.runic.multi_roll", "Allows multiple rolls before cooldown");
        }

        private static String titleCase(String id) {
            StringBuilder out = new StringBuilder(id.length());
            boolean capitalize = true;
            for (char character : id.toCharArray()) {
                if (character == '_') {
                    out.append(' ');
                    capitalize = true;
                } else {
                    out.append(capitalize ? Character.toUpperCase(character) : character);
                    capitalize = false;
                }
            }
            return out.toString();
        }
    }
}
