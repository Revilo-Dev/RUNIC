package net.revilodev.runic.synergy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.runes.RunicItemTargets;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class SynergyEffects {
    private static final String INTERNAL_DAMAGE = "runic_synergy_internal_damage";
    private static final String FROZEN_PHASE = "runic_frozen_phase";
    private static final String FROZEN_LEVEL = "runic_frozen_level";
    private static final String FROZEN_PHASE_END = "runic_frozen_phase_end";
    private static final String FROZEN_SOURCE = "runic_frozen_source";
    private static final String SHATTER_COOLDOWN = "runic_shatter_cd";
    private static final String ICE_PRISON_COOLDOWN = "runic_ice_prison_cd";
    private static final String SOULBURN_COOLDOWN = "runic_soulburn_cd";
    private static final String TEMPEST_HITS = "runic_tempest_hits";
    private static final String TEMPEST_CHARGE = "runic_tempest_charge";
    private static final String BERSERK_HITS = "runic_berserk_hits";
    private static final String EXECUTION_MARK = "runic_execution_mark";
    private static final String REAPER_MARK = "runic_reaper_mark";
    private static final String BLOODFIRE_MARK = "runic_bloodfire_mark";
    private static final String VENOM_BURST_MARK = "runic_venom_burst_mark";
    private static final String SOULBURN_MARK = "runic_soulburn_mark";
    private static final String JUGGERNAUT_COOLDOWN = "runic_juggernaut_cd";
    private static final String FURY_UNTIL = "runic_executioners_fury_until";

    private SynergyEffects() {}

    public static void markFrozen(LivingEntity target, LivingEntity attacker, int level) {
        if (target == null || target.level().isClientSide) return;
        int clampedLevel = Mth.clamp(level, 1, 2);
        int duration = phaseOneDurationTicks(target, clampedLevel);
        CompoundTag data = target.getPersistentData();
        data.putInt(FROZEN_PHASE, 1);
        data.putInt(FROZEN_LEVEL, clampedLevel);
        data.putLong(FROZEN_PHASE_END, target.level().getGameTime() + duration);
        if (attacker != null) {
            data.putUUID(FROZEN_SOURCE, attacker.getUUID());
        } else {
            data.remove(FROZEN_SOURCE);
        }
        target.addEffect(new MobEffectInstance(ModMobEffects.FROZEN, duration, frozenAmplifier(clampedLevel, 1), false, false, false));
    }

    public static boolean isFrozenOrChilled(LivingEntity target) {
        if (target == null) return false;
        return frozenState(target) != null || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    public static boolean isIcebound(LivingEntity target) {
        FrozenState state = frozenState(target);
        return state != null && state.phase() == 1;
    }

    public static int frozenPhase(LivingEntity target) {
        FrozenState state = frozenState(target);
        return state == null ? 0 : state.phase();
    }

    public static int frozenLevel(LivingEntity target) {
        FrozenState state = frozenState(target);
        return state == null ? 0 : state.level();
    }

    public static boolean isInternalDamage(LivingEntity entity) {
        return entity != null && entity.getPersistentData().getBoolean(INTERNAL_DAMAGE);
    }

    public static void onWeaponAttack(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (attacker == null || target == null || weapon.isEmpty() || attacker.level().isClientSide) return;

        if (has(weapon, SynergyRegistry.BLOODFIRE)) applyBloodfire(attacker, target, weapon);
        if (has(weapon, SynergyRegistry.FROSTBITE)) applyFrostbite(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.VENOM_BURST)) applyVenomBurst(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.SHATTER)) applyShatter(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.ICE_PRISON)) applyIcePrison(attacker, target, weapon);
        if (has(weapon, SynergyRegistry.TEMPEST)) applyTempest(attacker, target, weapon, baseDamage);
        if (has(weapon, SynergyRegistry.BERSERK)) applyBerserk(attacker, target);
        if (has(weapon, SynergyRegistry.REAPER)) applyReaper(attacker, target, weapon);
        if (has(weapon, SynergyRegistry.SOULBURN)) applySoulburnSpread(attacker, target, weapon);
    }

    public static float onIncomingWeaponDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource source, float amount) {
        if (attacker == null || target == null || weapon.isEmpty() || source == null || isInternalDamage(attacker)) {
            return amount;
        }

        float out = amount;
        if (isIcebound(target) && canBreakFrozen(attacker, target)) {
            shatterFrozen(target, attacker);
        }

        if (has(weapon, SynergyRegistry.CORROSION) && (target.hasEffect(MobEffects.POISON) || target.hasEffect(MobEffects.WEAKNESS))) {
            double bonus = RunicConfig.corrosionBonusDamageMultiplier() + RunicConfig.corrosionArmorIgnorePercent();
            out *= (float) (1.0D + Math.max(0.0D, bonus) * synergyMultiplier(weapon)
                    * pairScale(weapon, RuneStatType.POISON_CHANCE, RuneStatType.WEAKENING_CHANCE));
        }

        long now = attacker.level().getGameTime();
        if (has(weapon, SynergyRegistry.EXECUTIONERS_FURY)) {
            double ratio = target.getMaxHealth() <= 0.0F ? 1.0D : target.getHealth() / target.getMaxHealth();
            if (ratio <= 0.10D || attacker.getPersistentData().getLong(FURY_UNTIL) > now) {
                out *= (float) (1.0D + RunicConfig.executionersFuryDamageBonusPercent()
                        * pairScale(weapon, RuneStatType.ATTACK_DAMAGE, RuneStatType.ATTACK_SPEED));
            }
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
        CompoundTag data = target.getPersistentData();
        if (data.hasUUID(BLOODFIRE_MARK)) {
            spawnBloodfireDeathParticles(target);
            LivingEntity bloodfireAttacker = resolveMarkedSource(target, BLOODFIRE_MARK);
            if (bloodfireAttacker instanceof Player player) {
                spreadBloodfireOnDeath(player, target);
            }
        }
        if (data.hasUUID(VENOM_BURST_MARK) && target.hasEffect(MobEffects.POISON)) {
            spawnVenomBurstDeathEffect(target, resolveMarkedSource(target, VENOM_BURST_MARK));
        }
        clearFrozenState(target);

        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

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

        int charges = data.getInt(TEMPEST_CHARGE);
        if (charges > 0) {
            applyTempestDeathTransfer(attacker, target, charges);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide) return;
        FrozenState state = frozenState(entity);
        if (state == null) {
            clearFrozenState(entity);
            return;
        }

        if (state.phase() == 1) {
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
            entity.hurtMarked = true;
        }

        if (entity.level().getGameTime() >= state.endsAt()) {
            if (state.phase() == 1) {
                shatterFrozen(entity, resolveFrozenSource(entity));
            } else {
                clearFrozenState(entity);
                entity.removeEffect(ModMobEffects.FROZEN);
            }
        }
    }

    private static void applyBloodfire(Player attacker, LivingEntity target, ItemStack weapon) {
        int fireTicks = (int) Math.round(RunicConfig.bloodfireFireSeconds() * 20
                * Math.max(1.0D, stat(weapon, RuneStatType.FLAME_CHANCE) / 100.0D));
        if (target.hasEffect(ModMobEffects.BLEEDING)) {
            applyBloodfireBurn(attacker, target, weapon, Math.max(20, fireTicks));
            spreadBloodfire(attacker, target, weapon);
        }
        double bleedChance = RunicConfig.bloodfireBleedChance() * Math.max(1.0D, stat(weapon, RuneStatType.BLEEDING_CHANCE) / 100.0D);
        if (target.isOnFire() && attacker.getRandom().nextDouble() <= bleedChance) {
            target.addEffect(new MobEffectInstance(ModMobEffects.BLEEDING,
                    RunicConfig.bloodfireBleedDurationTicks(), 0, false, false, true));
        }
    }

    private static void applyFrostbite(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (!target.hasEffect(ModMobEffects.BLEEDING)) return;
        int scaled = (int) Math.round(60.0D * Math.max(0.0D, RunicConfig.frostbiteFreezeBonusMultiplier()) * synergyMultiplier(weapon)
                * pairScale(weapon, RuneStatType.FREEZING_CHANCE, RuneStatType.BLEEDING_CHANCE));
        int level = scaled >= 60 ? 2 : 1;
        markFrozen(target, attacker, level);
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

        float damage = Math.max(target.getMaxHealth() * 0.5F, target.getHealth());
        dealInternal(attacker, target, damage);
    }

    private static void applyIcePrison(Player attacker, LivingEntity target, ItemStack weapon) {
        if (!isFrozenOrChilled(target)) return;
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (data.getLong(ICE_PRISON_COOLDOWN) > now) return;
        data.putLong(ICE_PRISON_COOLDOWN, now + RunicConfig.icePrisonCooldownTicks());

        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.icePrisonRadius() * synergyMultiplier(weapon))) {
            summonVisualLightning(attacker, nearby);
            dealInternal(attacker, nearby, Math.max(2.0F, stat(weapon, RuneStatType.SHOCKING_CHANCE) * 0.15F));
        }
    }

    private static void applyVenomBurst(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage) {
        if (!target.hasEffect(MobEffects.POISON)) return;
        if (attacker.getRandom().nextDouble() > RunicConfig.venomBurstChance()) return;
        markUuid(target.getPersistentData(), VENOM_BURST_MARK, attacker.getUUID());
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
        target.getPersistentData().putInt(TEMPEST_CHARGE, target.getPersistentData().getInt(TEMPEST_CHARGE) + 1);
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

    private static void applyBerserk(Player attacker, LivingEntity target) {
        boolean critical = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger();
        if (!critical) return;
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getInt(BERSERK_HITS) + 1;
        if (hits < Math.max(3, RunicConfig.berserkHitsRequired())) {
            data.putInt(BERSERK_HITS, hits);
            return;
        }
        data.putInt(BERSERK_HITS, hits);
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                RunicConfig.berserkDurationTicks(), 2, false, false, true));
        attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                RunicConfig.berserkDurationTicks(), 2, false, false, true));
        data.remove(BERSERK_HITS);
    }

    private static void applyBloodfireBurn(LivingEntity attacker, LivingEntity target, ItemStack weapon, int fireTicks) {
        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), fireTicks));
        markUuid(target.getPersistentData(), BLOODFIRE_MARK, attacker.getUUID());
        float damage = Math.max(1.0F, stat(weapon, RuneStatType.FLAME_CHANCE) * 0.05F);
        target.hurt(target.damageSources().onFire(), damage);
    }

    private static void spreadBloodfire(Player attacker, LivingEntity source, ItemStack weapon) {
        if (!(source.level() instanceof ServerLevel level)) return;
        AABB touchBox = source.getBoundingBox().inflate(0.05D);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, touchBox, entity ->
                entity != source
                        && entity.isAlive()
                        && entity instanceof Monster
                        && !entity.isAlliedTo(attacker)
                        && entity.getBoundingBox().intersects(touchBox))) {
            applyBloodfireBurn(attacker, nearby, weapon, 20);
        }
    }

    private static void spreadBloodfireOnDeath(Player attacker, LivingEntity source) {
        for (LivingEntity nearby : nearbyHostiles(attacker, source, 3.5D)) {
            applyBloodfireBurn(attacker, nearby, ItemStack.EMPTY, 60);
        }
    }

    private static void applyReaper(Player attacker, LivingEntity target, ItemStack weapon) {
        if (!target.hasEffect(MobEffects.WEAKNESS)) return;
        float leech = Math.max(0.0F, stat(weapon, RuneStatType.LEECHING_CHANCE));
        if (leech <= 0.0F) return;
        float drained = Math.max(1.0F, target.getMaxHealth() * (leech / 100.0F) * 0.2F);
        dealInternal(attacker, target, drained * 2.0F);
        attacker.heal(drained);
    }

    private static void applySoulburnSpread(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        if (target.isOnFire()) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                    RunicConfig.soulburnWitherDurationTicks(), RunicConfig.soulburnWitherAmplifier(), false, false, true));
        }
        if (target.hasEffect(MobEffects.WITHER)) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
        }
        if (target.isOnFire() || target.hasEffect(MobEffects.WITHER)) {
            markUuid(target.getPersistentData(), SOULBURN_MARK, attacker.getUUID());
            applySoulburn(attacker, target);
        }
    }

    private static void applySoulburn(LivingEntity attacker, LivingEntity target) {
        long now = target.level().getGameTime();
        CompoundTag data = attacker.getPersistentData();
        if (data.getLong(SOULBURN_COOLDOWN) > now) return;
        data.putLong(SOULBURN_COOLDOWN, now + RunicConfig.soulburnCooldownTicks());

        for (LivingEntity nearby : nearbyHostiles(attacker, target, RunicConfig.soulburnRadius())) {
            nearby.setRemainingFireTicks(Math.max(nearby.getRemainingFireTicks(), 80));
            nearby.addEffect(new MobEffectInstance(MobEffects.WITHER,
                    RunicConfig.soulburnWitherDurationTicks(),
                    RunicConfig.soulburnWitherAmplifier(),
                    false,
                    false,
                    true));
        }
    }

    private static void applyTempestDeathTransfer(LivingEntity attacker, LivingEntity target, int charges) {
        List<LivingEntity> targets = nearbyHostiles(attacker, target, RunicConfig.tempestRadius());
        if (targets.isEmpty()) return;
        float damage = Math.max(1.0F, charges * 2.0F / targets.size());
        for (LivingEntity nearby : targets) {
            summonVisualLightning(attacker, nearby);
            dealInternal(attacker, nearby, damage);
        }
    }

    private static void summonVisualLightning(LivingEntity attacker, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) return;
        lightning.moveTo(target.getX(), target.getY(), target.getZ());
        lightning.setVisualOnly(true);
        if (attacker instanceof net.minecraft.server.level.ServerPlayer player) {
            lightning.setCause(player);
        }
        level.addFreshEntity(lightning);
    }

    private static void spawnBloodfireDeathParticles(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        level.sendParticles(
                ModParticles.BLOOD_DROP.value(),
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                36,
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                Math.max(0.35D, target.getBbHeight() * 0.35D),
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                0.18D
        );
        level.sendParticles(
                ParticleTypes.FLAME,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                32,
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                Math.max(0.35D, target.getBbHeight() * 0.35D),
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                0.12D
        );
    }

    private static void spawnVenomBurstDeathEffect(LivingEntity target, LivingEntity owner) {
        if (!(target.level() instanceof ServerLevel level)) return;
        level.sendParticles(
                ParticleTypes.SNEEZE,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                40,
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                Math.max(0.35D, target.getBbHeight() * 0.35D),
                Math.max(0.35D, target.getBbWidth() * 0.5D),
                0.14D
        );
        AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        cloud.setParticle(ParticleTypes.SNEEZE);
        cloud.setRadius((float) RunicConfig.venomBurstRadius());
        cloud.setDuration(80);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON,
                RunicConfig.venomBurstPoisonDurationTicks(), 0, false, false, true));
        if (owner instanceof Player player) {
            cloud.setOwner(player);
        }
        level.addFreshEntity(cloud);
    }

    private static void dealInternal(LivingEntity attacker, LivingEntity target, float amount) {
        if (amount <= 0.0F || target == null || !target.isAlive()) return;
        CompoundTag data = attacker == null ? null : attacker.getPersistentData();
        if (data != null) {
            data.putBoolean(INTERNAL_DAMAGE, true);
        }
        try {
            target.hurt(target.damageSources().magic(), amount);
        } finally {
            if (data != null) {
                data.remove(INTERNAL_DAMAGE);
            }
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
            if (SynergyRegistry.JUGGERNAUT.equals(id) && !RunicItemTargets.isArmor(stack)) continue;
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

    private static float stat(ItemStack stack, RuneStatType type) {
        RuneStats stats = RuneStats.get(stack);
        return stats == null || stats.isEmpty() ? 0.0F : Math.max(0.0F, stats.get(type));
    }

    private static double pairScale(ItemStack stack, RuneStatType a, RuneStatType b) {
        double first = Math.max(1.0D, stat(stack, a) / Math.max(1.0D, a.etchingMaxPercent()));
        double second = Math.max(1.0D, stat(stack, b) / Math.max(1.0D, b.etchingMaxPercent()));
        return (first + second) * 0.5D;
    }

    private static boolean canBreakFrozen(LivingEntity attacker, LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        return !data.hasUUID(FROZEN_SOURCE) || data.getUUID(FROZEN_SOURCE).equals(attacker.getUUID());
    }

    private static void shatterFrozen(LivingEntity target, LivingEntity attacker) {
        FrozenState state = frozenState(target);
        if (state == null || state.phase() != 1) return;

        int level = state.level();
        spawnFrozenShatterParticles(target);
        float ratio = level >= 2 ? 0.35F : 0.20F;
        float floor = level >= 2 ? 8.0F : 4.0F;
        dealInternal(attacker, target, Math.max(floor, target.getMaxHealth() * ratio));

        int recovery = phaseTwoDurationTicks(target, level);
        CompoundTag data = target.getPersistentData();
        data.putInt(FROZEN_PHASE, 2);
        data.putInt(FROZEN_LEVEL, level);
        data.putLong(FROZEN_PHASE_END, target.level().getGameTime() + recovery);
        target.addEffect(new MobEffectInstance(ModMobEffects.FROZEN, recovery, frozenAmplifier(level, 2), false, false, false));
    }

    private static void spawnFrozenShatterParticles(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                36,
                Math.max(0.2D, target.getBbWidth() * 0.45D),
                Math.max(0.25D, target.getBbHeight() * 0.35D),
                Math.max(0.2D, target.getBbWidth() * 0.45D),
                0.08D
        );
    }

    private static LivingEntity resolveFrozenSource(LivingEntity target) {
        return resolveMarkedSource(target, FROZEN_SOURCE);
    }

    private static LivingEntity resolveMarkedSource(LivingEntity target, String key) {
        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID(key) || !(target.level() instanceof ServerLevel level)) {
            return null;
        }
        return level.getEntity(data.getUUID(key)) instanceof LivingEntity living ? living : null;
    }

    private static FrozenState frozenState(LivingEntity target) {
        if (target == null) return null;
        MobEffectInstance effect = target.getEffect(ModMobEffects.FROZEN);
        CompoundTag data = target.getPersistentData();
        int phase = effect == null ? data.getInt(FROZEN_PHASE) : frozenPhase(effect.getAmplifier());
        int level = effect == null ? data.getInt(FROZEN_LEVEL) : frozenLevel(effect.getAmplifier());
        long endsAt = data.getLong(FROZEN_PHASE_END);
        if (phase <= 0 || level <= 0 || endsAt <= 0L) {
            return null;
        }
        return new FrozenState(phase, level, endsAt);
    }

    private static int frozenPhase(int amplifier) {
        return amplifier >= 2 ? 2 : 1;
    }

    private static int frozenLevel(int amplifier) {
        return amplifier % 2 == 0 ? 1 : 2;
    }

    private static int frozenAmplifier(int level, int phase) {
        return phase <= 1 ? level - 1 : level + 1;
    }

    private static int phaseOneDurationTicks(LivingEntity target, int level) {
        int base = level >= 2 ? 80 : 40;
        return target instanceof Player ? base / 2 : base;
    }

    private static int phaseTwoDurationTicks(LivingEntity target, int level) {
        int base = level >= 2 ? 80 : 40;
        return target instanceof Player ? base / 2 : base;
    }

    private static void clearFrozenState(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        data.remove(FROZEN_PHASE);
        data.remove(FROZEN_LEVEL);
        data.remove(FROZEN_PHASE_END);
        data.remove(FROZEN_SOURCE);
    }

    private record FrozenState(int phase, int level, long endsAt) {}
}
