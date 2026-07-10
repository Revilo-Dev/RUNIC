package net.revilodev.runic.runes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.mythic.MythicRuneEffects;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.relic.RelicEffects;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;
import net.revilodev.runic.synergy.SynergyEffects;

import java.util.Random;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class CombatHandler {
    private static final Random RNG = new Random();
    private static final String ROOT = "runic";
    private static final String LEECHING_FROM_ETCHING = "leeching_from_etching";
    private static final String CRITICAL_HIT = "runic_critical_hit";

    private CombatHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        float leech = RuneStats.get(event.getEntity().getMainHandItem()).get(RuneStatType.LEECHING_CHANCE);
        if (event.isCriticalHit() && leech > 0.0F) {
            event.getEntity().getPersistentData().putBoolean(CRITICAL_HIT, true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        SynergyEffects.onIncomingDefensiveDamage(target, event.getAmount());

        LivingEntity attacker = resolveAttacker(source);
        if (attacker == null) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        RuneStats stats = RuneStats.get(weapon);

        float amount = event.getAmount();
        if (!stats.isEmpty()) {
            amount = applyCreatureBonuses(stats, target, amount);

            if (source.getDirectEntity() instanceof AbstractArrow) {
                float power = stats.get(RuneStatType.POWER);
                if (power > 0.0F) {
                    amount *= 1.0F + power / 100.0F;
                }
            }

            maybeSpawnFangs(attacker, target, source, stats.get(RuneStatType.FANGS));

            float leech = stats.get(RuneStatType.LEECHING_CHANCE);
            if (leech > 0.0F && consumeCriticalHit(attacker) && RNG.nextFloat() <= leech / 100.0F) {
                boolean fromEtching = isLeechingFromEtching(weapon);
                float ratio = fromEtching ? 0.05F : 0.10F;
                float drain = Math.max(0.5F, target.getMaxHealth() * ratio);
                attacker.heal(drain);
                spawnLeechTrail(attacker, target);
            }
        }

        amount = RelicEffects.modifyOutgoingDamage(attacker, target, weapon, amount);
        amount = MythicRuneEffects.modifyOutgoingDamage(attacker, target, weapon, amount);
        amount = SynergyEffects.onIncomingWeaponDamage(attacker, target, weapon, source, amount);
        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        SynergyEffects.onLivingDeath(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onBleedingHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(ModMobEffects.BLEEDING)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDamageFromStunned(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModMobEffects.STUNNING)) {
            event.setNewDamage(event.getNewDamage() * 0.5F);
        }
    }

    @SubscribeEvent
    public static void onApplyStunChance(LivingDamageEvent.Pre event) {
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        RuneStats stats = RuneStats.get(attacker.getMainHandItem());
        if (stats.isEmpty()) return;

        float chance = stats.get(RuneStatType.STUN_CHANCE);
        if (chance <= 0.0F || RNG.nextFloat() > chance / 100.0F) return;

        event.getEntity().addEffect(new MobEffectInstance(ModMobEffects.STUNNING, 40, 0, false, false, true));
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof AbstractArrow arrow && arrow.getOwner() instanceof LivingEntity owner) {
            return owner;
        }
        return source.getEntity() instanceof LivingEntity living ? living : null;
    }

    private static boolean consumeCriticalHit(LivingEntity attacker) {
        CompoundTag data = attacker.getPersistentData();
        if (!data.getBoolean(CRITICAL_HIT)) {
            return false;
        }
        data.remove(CRITICAL_HIT);
        return true;
    }

    private static float applyCreatureBonuses(RuneStats stats, LivingEntity target, float amount) {
        ResourceLocation id = target.getType().builtInRegistryHolder().key().location();
        String path = id.getPath();

        float bonus = 0.0F;
        if (path.contains("zombie") || path.contains("skeleton") || path.contains("wither") || path.contains("phantom") || path.contains("drowned")) {
            bonus += stats.get(RuneStatType.UNDEAD_DAMAGE);
        }
        if (path.contains("blaze") || path.contains("ghast") || path.contains("magma") || path.contains("piglin") || path.contains("hoglin")) {
            bonus += stats.get(RuneStatType.NETHER_DAMAGE);
        }

        return bonus > 0.0F ? amount * (1.0F + bonus / 100.0F) : amount;
    }

    private static void maybeSpawnFangs(LivingEntity attacker, LivingEntity target, DamageSource source, float chance) {
        if (chance <= 0.0F || RNG.nextFloat() > chance / 100.0F) {
            return;
        }
        if (source.getDirectEntity() instanceof AbstractArrow) {
            return;
        }
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 start = attacker.position();
        Vec3 end = target.position();
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() < 0.0001D) {
            EvokerFangs fangs = new EvokerFangs(level, target.getX(), target.getY(), target.getZ(), attacker.getYRot(), 0, attacker);
            level.addFreshEntity(fangs);
            return;
        }

        Vec3 step = delta.normalize().scale(1.25D);
        float yaw = (float) Math.atan2(step.z, step.x);
        int count = 5;
        for (int i = 0; i < count; i++) {
            Vec3 pos = start.add(step.scale(i + 1));
            EvokerFangs fangs = new EvokerFangs(level, pos.x, target.getY(), pos.z, yaw, i * 2, attacker);
            level.addFreshEntity(fangs);
        }
    }

    private static void spawnLeechTrail(LivingEntity attacker, LivingEntity target) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 start = target.getBoundingBox().getCenter();
        Vec3 end = attacker.getEyePosition().add(0.0D, -0.25D, 0.0D);
        Vec3 delta = end.subtract(start);
        double distance = delta.length();
        if (distance < 0.001D) {
            return;
        }

        Vec3 velocity = delta.normalize().scale(0.55D);
        int steps = Math.max(4, Math.min(8, (int) Math.ceil(distance * 2.0D)));

        for (int i = 0; i < steps; i++) {
            double progress = (double) i / Math.max(1, steps - 1);
            Vec3 point = start.lerp(end, progress);
            level.sendParticles(
                    ModParticles.BLOOD_DROP.value(),
                    point.x,
                    point.y,
                    point.z,
                    0,
                    velocity.x,
                    velocity.y,
                    velocity.z,
                    1.0D
            );
        }
    }

    private static boolean isLeechingFromEtching(ItemStack weapon) {
        CustomData data = weapon.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = data.copyTag();
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag runic = root.getCompound(ROOT);
        return runic.getBoolean(LEECHING_FROM_ETCHING);
    }
}
