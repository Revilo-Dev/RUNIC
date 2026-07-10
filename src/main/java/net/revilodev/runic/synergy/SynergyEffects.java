package net.revilodev.runic.synergy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.gear.RunicItemData;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public final class SynergyEffects {
    private static final String INTERNAL_DAMAGE = "runic_synergy_internal_damage";
    private static final String FROZEN_UNTIL = "runic_frozen_until";
    private static final String SHATTER_COOLDOWN = "runic_shatter_cd";
    private static final String ICE_PRISON_COOLDOWN = "runic_ice_prison_cd";
    private static final String SOULBURN_COOLDOWN = "runic_soulburn_cd";
    private static final String TEMPEST_HITS = "runic_tempest_hits";
    private static final String BERSERK_HITS = "runic_berserk_hits";
    private static final String EXECUTION_MARK = "runic_execution_mark";
    private static final String REAPER_MARK = "runic_reaper_mark";
    private static final String SOULBURN_MARK = "runic_soulburn_mark";
    private static final String JUGGERNAUT_COOLDOWN = "runic_juggernaut_cd";
    private static final String FURY_UNTIL = "runic_executioners_fury_until";

    private SynergyEffects() {}

    public static void markFrozen(LivingEntity target, int durationTicks) {
        if (target == null || target.level().isClientSide) return;
        target.getPersistentData().putLong(FROZEN_UNTIL, target.level().getGameTime() + Math.max(0, durationTicks));
    }

    public static boolean isFrozenOrChilled(LivingEntity target) {
        if (target == null) return false;
        long now = target.level().getGameTime();
        if (target.getPersistentData().getLong(FROZEN_UNTIL) > now) return true;
        return target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    public static boolean isInternalDamage(LivingEntity entity) {
        return entity != null && entity.getPersistentData().getBoolean(INTERNAL_DAMAGE);
    }

    public static void onWeaponAttack(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (attacker == null || target == null || weapon.isEmpty() || attacker.level().isClientSide) return;

        if (has(weapon, SynergyRegistry.BLOODFIRE)) applyBloodfire(attacker, target);
        if (has(weapon, SynergyRegistry.FROSTBITE)) applyFrostbite(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.VENOM_BURST)) applyVenomBurst(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.SHATTER)) applyShatter(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.ICE_PRISON)) applyIcePrison(attacker, target, weapon);
        if (has(weapon, SynergyRegistry.TEMPEST)) applyTempest(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.BERSERK)) applyBerserk(attacker);
        if (has(weapon, SynergyRegistry.SOULBURN) && target.isOnFire()) {
            markUuid(target.getPersistentData(), SOULBURN_MARK, attacker.getUUID());
        }
    }

    public static float onIncomingWeaponDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource source, float amount) {
        if (attacker == null || target == null || weapon.isEmpty() || source == null || isInternalDamage(attacker)) {
            return amount;
        }

        float out = amount;
        if (has(weapon, SynergyRegistry.CORROSION) && target.hasEffect(MobEffects.POISON)) {
            double bonus = RunicConfig.corrosionBonusDamageMultiplier() + RunicConfig.corrosionArmorIgnorePercent();
            out *= (float) (1.0D + Math.max(0.0D, bonus) * synergyMultiplier(weapon));
        }

        long now = attacker.level().getGameTime();
        if (has(weapon, SynergyRegistry.EXECUTIONERS_FURY)
                && attacker.getPersistentData().getLong(FURY_UNTIL) > now) {
            out *= (float) (1.0D + RunicConfig.executionersFuryDamageBonusPercent());
        }

        if (target.getMaxHealth() > 0.0F) {
            double ratio = target.getHealth() / target.getMaxHealth();
            if (has(weapon, SynergyRegistry.EXECUTIONERS_FURY)
                    && ratio <= RunicConfig.executionersFuryExecutionHealthThreshold()) {
                markUuid(target.getPersistentData(), EXECUTION_MARK, attacker.getUUID());
            }
            if (has(weapon, SynergyRegistry.REAPER)
                    && ratio <= RunicConfig.reaperExecutionHealthThreshold()) {
                markUuid(target.getPersistentData(), REAPER_MARK, attacker.getUUID());
            }
        }

        return out;
    }

    public static void onIncomingDefensiveDamage(LivingEntity target, float amount) {
        if (target == null || target.level().isClientSide || amount <= 0.0F || target.getMaxHealth() <= 0.0F) return;
        if (!hasEquippedSynergy(target, SynergyRegistry.JUGGERNAUT)) return;
        if (amount < target.getMaxHealth() * RunicConfig.juggernautDamageThresholdPercent()) return;

        CompoundTag data = target.getPersistentData();
        long now = target.level().getGameTime();
        if (data.getLong(JUGGERNAUT_COOLDOWN) > now) return;

        data.putLong(JUGGERNAUT_COOLDOWN, now + RunicConfig.juggernautCooldownTicks());
        int amplifier = RunicConfig.juggernautArmorBonus() >= 4.0D ? 1 : 0;
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                RunicConfig.juggernautDurationTicks(), amplifier, false, false, true));
    }

    public static void onLivingDeath(LivingEntity target, DamageSource source) {
        if (target == null || target.level().isClientSide) return;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        CompoundTag data = target.getPersistentData();
        UUID attackerId = attacker.getUUID();

        if (markedFor(data, EXECUTION_MARK, attackerId)) {
            attacker.getPersistentData().putLong(FURY_UNTIL,
                    attacker.level().getGameTime() + RunicConfig.executionersFuryDurationTicks());
            attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                    RunicConfig.executionersFuryDurationTicks(), 0, false, false, true));
        }

        if (markedFor(data, REAPER_MARK, attackerId)) {
            attacker.heal((float) RunicConfig.reaperHealAmount());
            attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,
                    RunicConfig.reaperDurationTicks(), 0, false, false, true));
        }

        if (markedFor(data, SOULBURN_MARK, attackerId) && target.isOnFire()) {
            applySoulburn(attacker, target);
        }
    }

    private static void applyBloodfire(Player attacker, LivingEntity target) {
        if (target.hasEffect(ModMobEffects.BLEEDING)) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), RunicConfig.bloodfireFireSeconds() * 20));
        }
        if (target.isOnFire() && attacker.getRandom().nextDouble() <= RunicConfig.bloodfireBleedChance()) {
            target.addEffect(new MobEffectInstance(ModMobEffects.BLEEDING,
                    RunicConfig.bloodfireBleedDurationTicks(), 0, false, false, true));
        }
    }

    private static void applyFrostbite(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (!target.hasEffect(ModMobEffects.BLEEDING)) return;
        int duration = (int) Math.round(80.0D * Math.max(0.0D, RunicConfig.frostbiteFreezeBonusMultiplier()) * synergyMultiplier(weapon));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false, true));
        markFrozen(target, duration);
        if (isFrozenOrChilled(target)) {
            dealInternal(attacker, target, baseDamage * (float) (RunicConfig.frostbiteChilledDamageMultiplier() * synergyMultiplier(weapon)));
        }
    }

    private static void applyShatter(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (!isFrozenOrChilled(target)) return;
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (data.getLong(SHATTER_COOLDOWN) > now) return;
        data.putLong(SHATTER_COOLDOWN, now + RunicConfig.shatterCooldownTicks());

        float damage = baseDamage * (float) (RunicConfig.shatterDamageMultiplier() * synergyMultiplier(weapon));
        dealInternal(attacker, target, damage);
        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.shatterRadius() * synergyMultiplier(weapon))) {
            dealInternal(attacker, nearby, damage);
        }
    }

    private static void applyIcePrison(Player attacker, LivingEntity target, ItemStack weapon) {
        if (!isFrozenOrChilled(target)) return;
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (data.getLong(ICE_PRISON_COOLDOWN) > now) return;
        data.putLong(ICE_PRISON_COOLDOWN, now + RunicConfig.icePrisonCooldownTicks());

        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.icePrisonRadius() * synergyMultiplier(weapon))) {
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    RunicConfig.icePrisonDurationTicks(), 4, false, false, true));
            markFrozen(nearby, RunicConfig.icePrisonDurationTicks());
        }
    }

    private static void applyVenomBurst(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (!target.hasEffect(MobEffects.POISON)) return;
        if (attacker.getRandom().nextDouble() > RunicConfig.venomBurstChance()) return;
        float damage = baseDamage * (float) (RunicConfig.venomBurstDamageMultiplier() * synergyMultiplier(weapon));
        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.venomBurstRadius() * synergyMultiplier(weapon))) {
            nearby.addEffect(new MobEffectInstance(MobEffects.POISON,
                    RunicConfig.venomBurstPoisonDurationTicks(), 0, false, false, true));
            dealInternal(attacker, nearby, damage);
        }
    }

    private static void applyTempest(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getInt(TEMPEST_HITS) + 1;
        if (hits < RunicConfig.tempestHitsRequired()) {
            data.putInt(TEMPEST_HITS, hits);
            return;
        }
        data.remove(TEMPEST_HITS);

        int left = RunicConfig.tempestChainTargets();
        float damage = baseDamage * (float) (RunicConfig.tempestDamageMultiplier() * synergyMultiplier(weapon));
        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.tempestRadius() * synergyMultiplier(weapon))) {
            if (left-- <= 0) break;
            dealInternal(attacker, nearby, damage);
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
        }
    }

    private static void applyBerserk(Player attacker) {
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getInt(BERSERK_HITS) + 1;
        if (hits < RunicConfig.berserkHitsRequired()) {
            data.putInt(BERSERK_HITS, hits);
            return;
        }
        data.remove(BERSERK_HITS);
        int moveAmp = RunicConfig.berserkMovementSpeedBonusPercent() >= 0.20D ? 1 : 0;
        int hasteAmp = RunicConfig.berserkAttackSpeedBonusPercent() >= 0.20D ? 1 : 0;
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                RunicConfig.berserkDurationTicks(), moveAmp, false, false, true));
        attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,
                RunicConfig.berserkDurationTicks(), hasteAmp, false, false, true));
    }

    private static void applySoulburn(LivingEntity attacker, LivingEntity target) {
        long now = target.level().getGameTime();
        CompoundTag data = attacker.getPersistentData();
        if (data.getLong(SOULBURN_COOLDOWN) > now) return;
        data.putLong(SOULBURN_COOLDOWN, now + RunicConfig.soulburnCooldownTicks());

        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.soulburnRadius())) {
            nearby.addEffect(new MobEffectInstance(MobEffects.WITHER,
                    RunicConfig.soulburnWitherDurationTicks(),
                    RunicConfig.soulburnWitherAmplifier(),
                    false,
                    false,
                    true));
        }
    }

    private static void dealInternal(LivingEntity attacker, LivingEntity target, float amount) {
        if (amount <= 0.0F || attacker == null || target == null || !target.isAlive()) return;
        CompoundTag data = attacker.getPersistentData();
        data.putBoolean(INTERNAL_DAMAGE, true);
        try {
            target.hurt(target.damageSources().magic(), amount);
        } finally {
            data.remove(INTERNAL_DAMAGE);
        }
    }

    private static List<LivingEntity> nearbyHostiles(LivingEntity attacker, LivingEntity center, double radius) {
        if (!(center.level() instanceof ServerLevel level) || radius <= 0.0D) return List.of();
        AABB box = center.getBoundingBox().inflate(radius);
        Predicate<LivingEntity> filter = entity ->
                entity != center
                        && entity.isAlive()
                        && entity instanceof Monster
                        && (attacker == null || !entity.isAlliedTo(attacker));
        return level.getEntitiesOfClass(LivingEntity.class, box, filter);
    }

    private static boolean has(ItemStack stack, net.minecraft.resources.ResourceLocation id) {
        return RunicItemData.hasSynergy(stack, id);
    }

    private static boolean hasEquippedSynergy(LivingEntity entity, net.minecraft.resources.ResourceLocation id) {
        for (ItemStack stack : entity.getAllSlots()) {
            if (RunicItemData.hasSynergy(stack, id)) return true;
        }
        return false;
    }

    private static void markUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putUUID(key, uuid);
    }

    private static boolean markedFor(CompoundTag tag, String key, UUID uuid) {
        return tag.hasUUID(key) && tag.getUUID(key).equals(uuid);
    }

    private static double synergyMultiplier(ItemStack stack) {
        return stack == null || stack.isEmpty() ? 1.0D : GearAttributes.harmonizedMultiplier(stack);
    }
}
