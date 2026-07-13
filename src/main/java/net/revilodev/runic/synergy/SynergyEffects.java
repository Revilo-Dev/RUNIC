package net.revilodev.runic.synergy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class SynergyEffects {
    private static final String INTERNAL_DAMAGE = "runic_synergy_internal_damage";
    private static final String FROZEN_UNTIL = "runic_frozen_until";
    private static final String FROZEN_SLOW_APPLIED = "runic_frozen_slow_applied";
    private static final String SHATTER_COOLDOWN = "runic_shatter_cd";
    private static final String ICE_PRISON_COOLDOWN = "runic_ice_prison_cd";
    private static final String SOULBURN_COOLDOWN = "runic_soulburn_cd";
    private static final String TEMPEST_HITS = "runic_tempest_hits";
    private static final String TEMPEST_CHARGE = "runic_tempest_charge";
    private static final String BERSERK_HITS = "runic_berserk_hits";
    private static final String BERSERK_TARGET = "runic_berserk_target";
    private static final String EXECUTION_MARK = "runic_execution_mark";
    private static final String REAPER_MARK = "runic_reaper_mark";
    private static final String BLOODFIRE_MARK = "runic_bloodfire_mark";
    private static final String SOULBURN_MARK = "runic_soulburn_mark";
    private static final String JUGGERNAUT_COOLDOWN = "runic_juggernaut_cd";
    private static final String FURY_UNTIL = "runic_executioners_fury_until";
    private static final Map<UUID, List<FrozenBlock>> FROZEN_BLOCKS = new HashMap<>();

    private SynergyEffects() {}

    public static void markFrozen(LivingEntity target, int durationTicks) {
        if (target == null || target.level().isClientSide) return;
        int duration = Math.max(0, target instanceof Player ? durationTicks / 2 : durationTicks);
        target.getPersistentData().putLong(FROZEN_UNTIL, target.level().getGameTime() + duration);
        target.getPersistentData().remove(FROZEN_SLOW_APPLIED);
        target.addEffect(new MobEffectInstance(ModMobEffects.FROZEN, duration, 0, false, false, false));
        placeFrozenBlocks(target);
    }

    public static boolean isFrozenOrChilled(LivingEntity target) {
        if (target == null) return false;
        if (target.hasEffect(ModMobEffects.FROZEN)) return true;
        long now = target.level().getGameTime();
        if (target.getPersistentData().getLong(FROZEN_UNTIL) > now) return true;
        return target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
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
        }
        clearFrozenBlocks(target);

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
        CompoundTag data = entity.getPersistentData();
        long until = data.getLong(FROZEN_UNTIL);
        if (entity.hasEffect(ModMobEffects.FROZEN)) {
            placeFrozenBlocks(entity);
            entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
            entity.hurtMarked = true;
            return;
        }
        clearFrozenBlocks(entity);
        if (until > 0L && entity.level().getGameTime() >= until && !data.getBoolean(FROZEN_SLOW_APPLIED)) {
            int slow = entity instanceof Player ? 20 : 40;
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slow, 1, false, false, true));
            data.putBoolean(FROZEN_SLOW_APPLIED, true);
            data.remove(FROZEN_UNTIL);
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
        int duration = (int) Math.round(60.0D * Math.max(0.0D, RunicConfig.frostbiteFreezeBonusMultiplier()) * synergyMultiplier(weapon)
                * pairScale(weapon, RuneStatType.FREEZING_CHANCE, RuneStatType.BLEEDING_CHANCE));
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
        UUID last = data.hasUUID(BERSERK_TARGET) ? data.getUUID(BERSERK_TARGET) : null;
        if (!target.getUUID().equals(last)) {
            data.putUUID(BERSERK_TARGET, target.getUUID());
            data.putInt(BERSERK_HITS, 0);
        }
        int hits = data.getInt(BERSERK_HITS) + 1;
        if (hits < 3) {
            data.putInt(BERSERK_HITS, hits);
            return;
        }
        data.putInt(BERSERK_HITS, hits);
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                40, 2, false, false, true));
        attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                40, 1, false, false, true));
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
    }

    private static void placeFrozenBlocks(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        UUID id = target.getUUID();
        if (FROZEN_BLOCKS.containsKey(id)) return;

        BlockPos base = BlockPos.containing(target.getX(), target.getY(), target.getZ());
        List<FrozenBlock> placed = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            BlockPos pos = base.above(y);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) continue;
            placed.add(new FrozenBlock(pos.immutable(), state));
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        }
        if (!placed.isEmpty()) {
            FROZEN_BLOCKS.put(id, placed);
        }
    }

    private static void clearFrozenBlocks(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        List<FrozenBlock> placed = FROZEN_BLOCKS.remove(target.getUUID());
        if (placed == null) return;
        for (FrozenBlock block : placed) {
            if (level.getBlockState(block.pos()).is(Blocks.ICE)) {
                level.setBlock(block.pos(), block.previous(), 3);
            }
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

    private record FrozenBlock(BlockPos pos, BlockState previous) {}
}
