package net.revilodev.runic.relic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.network.payload.RelicPowerStatusPayload;
import net.revilodev.runic.runes.RunicItemTargets;
import net.revilodev.runic.stat.RuneStats;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
// applies relic effects
public final class RelicEffects {
    private static final String WARDEN_PULSE_COOLDOWN = "runic_warden_pulse_cooldown";
    private static final String RELIC_POWER_COOLDOWN = "runic_relic_power_cooldown";
    private static final String RUNIC_WITHER_SKULL = "runic_wither_skull";
    private static final String GUARDIAN_BEAM_UNTIL = "runic_guardian_beam_until";
    private static final String GUARDIAN_BEAM_OWNER = "runic_guardian_beam_owner";
    private static final int RELIC_POWER_COOLDOWN_TICKS = 200;
    private static final int RELIC_POWER_CAST_TICKS = 40;
    private static Method guardianSetActiveAttackTarget;

    private RelicEffects() {}


    public static void useRelicPower(ServerPlayer player) {
        if (player == null || player.level().isClientSide) return;
        ResourceLocation relic = activeFullSet(player);
        if (relic == null) return;

        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong(RELIC_POWER_COOLDOWN) > now) return;

        boolean used = false;
        boolean guardianPower = false;
        if (RelicRegistry.DRAGON_HEART.equals(relic)) {
            used = useDragonBreath(player);
        } else if (RelicRegistry.ELDER_GUARDIANS_EYE.equals(relic)) {
            used = useGuardianBeam(player);
            guardianPower = used;
        } else if (RelicRegistry.WITHER_CHARGE.equals(relic)) {
            used = useWitherBullet(player);
        } else if (RelicRegistry.WARDENS_SOUL.equals(relic)) {
            used = useSonicBoom(player);
        }

        if (used) {
            player.getPersistentData().putLong(RELIC_POWER_COOLDOWN, now + RELIC_POWER_COOLDOWN_TICKS);
            int duration = guardianPower ? RELIC_POWER_CAST_TICKS : 0;
            PacketDistributor.sendToPlayer(player, new RelicPowerStatusPayload(duration, duration,
                    RELIC_POWER_COOLDOWN_TICKS, RELIC_POWER_COOLDOWN_TICKS));
        }
    }

    public static float modifyOutgoingDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, float amount) {
        if (attacker == null || target == null || weapon.isEmpty() || amount <= 0.0F) {
            return amount;
        }

        amount = applyDragonHeartDamage(attacker, target, weapon, amount);
        amount = applyElderGuardianDamage(attacker, target, weapon, amount);
        amount = applyWitherChargeDamage(target, weapon, amount);
        amount = applyWardensSoulDamage(target, weapon, amount);
        return amount;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    // responds to attack entity
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        ResourceLocationHelper helper = new ResourceLocationHelper(weapon);

        if (helper.is(RelicRegistry.DRAGON_HEART)) {
            int extraSeconds = RunicConfig.dragonHeartBurnDurationBonusSeconds();
            if (extraSeconds > 0 && (target.getRemainingFireTicks() > 0 || helper.hasFireTrigger())) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 0) + (extraSeconds * 20));
            }
        }

        if (helper.is(RelicRegistry.WITHER_CHARGE) && target.hasEffect(MobEffects.WITHER)) {
            MobEffectInstance current = target.getEffect(MobEffects.WITHER);
            if (current != null) {
                int extended = (int) Math.round(current.getDuration() * (RunicConfig.witherChargeWitherDurationBonusPercent() / 100.0D));
                if (extended > 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, current.getDuration() + extended, current.getAmplifier(), false, false, true));
                }
            }
        }
        if (helper.is(RelicRegistry.WITHER_CHARGE)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, false, true));
        }

        maybeApplyExtraDurability(player, weapon);
    }

    @SubscribeEvent
    // responds to item attributes
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation relic = RunicItemData.getRelicId(stack);
        if (relic == null) return;

        EquipmentSlot armorSlot = RunicItemTargets.armorSlot(stack);
        if (armorSlot != null) {
            EquipmentSlotGroup group = slotGroup(armorSlot);
            if (RelicRegistry.ELDER_GUARDIANS_EYE.equals(relic)) {
                addModifier(event, Attributes.WATER_MOVEMENT_EFFICIENCY, "elder_guardian_water_movement", 0.15D, AttributeModifier.Operation.ADD_VALUE, group);
            } else if (RelicRegistry.DRAGON_HEART.equals(relic)) {
                addModifier(event, Attributes.ARMOR, "dragon_resistance", 1.0D, AttributeModifier.Operation.ADD_VALUE, group);
                addModifier(event, Attributes.KNOCKBACK_RESISTANCE, "dragon_knockback_resistance", 0.05D, AttributeModifier.Operation.ADD_VALUE, group);
            } else if (RelicRegistry.WARDENS_SOUL.equals(relic)) {
                addModifier(event, Attributes.KNOCKBACK_RESISTANCE, "warden_knockback_resistance", 0.08D, AttributeModifier.Operation.ADD_VALUE, group);
            } else if (RelicRegistry.WITHER_CHARGE.equals(relic)) {
                addModifier(event, Attributes.MOVEMENT_SPEED, "wither_movement_speed", 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, group);
            }
            return;
        }

        if (!RunicItemTargets.isWeapon(stack)) return;
        if (RelicRegistry.DRAGON_HEART.equals(relic)) {
            addModifier(event, Attributes.ATTACK_DAMAGE, "dragon_attack_damage", 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, EquipmentSlotGroup.MAINHAND);
            addModifier(event, Attributes.ATTACK_SPEED, "dragon_attack_speed", 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, EquipmentSlotGroup.MAINHAND);
        } else if (RelicRegistry.WARDENS_SOUL.equals(relic)) {
            addModifier(event, Attributes.SWEEPING_DAMAGE_RATIO, "warden_sweeping_range", 0.20D, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
            addModifier(event, Attributes.ENTITY_INTERACTION_RANGE, "warden_attack_distance", 1.0D, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
        } else if (RelicRegistry.WITHER_CHARGE.equals(relic)) {
            addModifier(event, Attributes.ATTACK_DAMAGE, "wither_attack_damage", 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, EquipmentSlotGroup.MAINHAND);
        }
    }

    @SubscribeEvent
    // responds to incoming damage
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (armorCount(entity, RelicRegistry.WITHER_CHARGE) > 0 && event.getSource().is(DamageTypes.WITHER)) {
            event.setCanceled(true);
            return;
        }

        double reduction = 0.0D;
        if (event.getSource().is(DamageTypes.MAGIC) || event.getSource().is(DamageTypes.INDIRECT_MAGIC)) {
            reduction += 0.05D * armorCount(entity, RelicRegistry.DRAGON_HEART);
        }
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getSource().is(DamageTypeTags.IS_FIRE)) {
            reduction += 0.05D * armorCount(entity, RelicRegistry.WARDENS_SOUL);
        }
        if (reduction > 0.0D) {
            event.setAmount((float) (event.getAmount() * Math.max(0.0D, 1.0D - Math.min(0.8D, reduction))));
        }
    }

    @SubscribeEvent
    // responds to break speed
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        if (!RelicRegistry.ELDER_GUARDIANS_EYE.equals(RunicItemData.getRelicId(tool))) {
            return;
        }

        double percent = RunicConfig.elderGuardiansEyeMiningSpeedBonusPercent() / 100.0D;
        if (percent <= 0.0D) {
            return;
        }

        if (player.isUnderWater() || tool == player.getMainHandItem()) {
            event.setNewSpeed((float) (event.getNewSpeed() * (1.0D + percent)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPostDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (attacker != null) {
            maybeDragonHeartAura(attacker, target);
            maybeElderGuardianSlow(attacker, target);
            maybeWitherPulse(attacker, target, event.getNewDamage());
        }
        maybeWardensSoulPulse(target, event.getNewDamage());
    }

    @SubscribeEvent
    public static void onArmorHurt(ArmorHurtEvent event) {
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack armor = event.getArmorItemStack(slot);
            if (armor.isEmpty()) {
                continue;
            }
            float adjusted = applyDurabilityModifier(armor, event.getNewDamage(slot), event.getEntity().getRandom());
            event.setNewDamage(slot, adjusted);
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        maybeApplyExtraDurability(event.getEntity(), event.getBow());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        maybeApplyExtraDurability(player, player.getMainHandItem());
    }

    @SubscribeEvent
    // responds to projectile impact
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof WitherSkull skull) || skull.level().isClientSide) return;
        if (!skull.getPersistentData().getBoolean(RUNIC_WITHER_SKULL)) return;

        AreaEffectCloud cloud = new AreaEffectCloud(skull.level(), skull.getX(), skull.getY(), skull.getZ());
        if (skull.getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        cloud.setParticle(ParticleTypes.SMOKE);
        cloud.setRadius(3.0F);
        cloud.setDuration(160);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false, true));
        skull.level().addFreshEntity(cloud);
    }

    @SubscribeEvent
    // responds to entity tick
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide
                && armorCount(living, RelicRegistry.WITHER_CHARGE) > 0 && living.hasEffect(MobEffects.WITHER)) {
            living.removeEffect(MobEffects.WITHER);
        }

        if (!(event.getEntity() instanceof Guardian guardian) || guardian.level().isClientSide) return;
        long until = guardian.getPersistentData().getLong(GUARDIAN_BEAM_UNTIL);
        if (until <= 0L) return;
        guardian.setInvisible(true);
        if (guardian.level().getGameTime() >= until) {
            guardian.discard();
            return;
        }
        if (guardian.getPersistentData().hasUUID(GUARDIAN_BEAM_OWNER) && guardian.level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(guardian.getPersistentData().getUUID(GUARDIAN_BEAM_OWNER));
            if (owner == null || !owner.isAlive()) {
                guardian.discard();
                return;
            }
            Vec3 source = owner.getEyePosition().subtract(0.0D, 0.35D, 0.0D);
            guardian.setPos(source.x, source.y, source.z);
        }
    }

    // applies durability modifier
    public static float applyDurabilityModifier(ItemStack stack, float baseDamage, RandomSource random) {
        if (stack.isEmpty() || baseDamage <= 0.0F) {
            return Math.max(0.0F, baseDamage);
        }

        double modified = baseDamage;
        if (GearAttributes.has(stack, GearAttribute.BRITTLE)) {
            modified *= 1.10D;
        }
        int reinforced = GearAttributes.getLevel(stack, GearAttribute.REINFORCED);
        if (reinforced > 0) {
            modified *= Math.max(0.0D, 1.0D - ((RunicConfig.reinforcedDurabilityLossReductionPercent() / 100.0D) * reinforced));
        }
        modified *= MythicRuneRegistry.ruinDurabilityMultiplier(stack);

        modified += rollExtraDurabilityDamage(stack, random);
        return (float) Math.max(0.0D, modified);
    }

    public static int rollExtraDurabilityDamage(ItemStack stack, RandomSource random) {
        double penalty = RelicRegistry.durabilityPenaltyPercent(stack);
        if (penalty <= 0.0D) {
            return 0;
        }
        return random.nextDouble() < penalty ? 1 : 0;
    }

    private static void maybeApplyExtraDurability(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack.isEmpty() || !stack.isDamageableItem() || entity.level().isClientSide) {
            return;
        }
        int extra = rollExtraDurabilityDamage(stack, entity.getRandom());
        if (extra > 0) {
            stack.hurtAndBreak(extra, entity, entity.getUsedItemHand() == null ? EquipmentSlot.MAINHAND : LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        }
    }

    private static float applyDragonHeartDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, float amount) {
        double weaponBonus = RelicRegistry.DRAGON_HEART.equals(RunicItemData.getRelicId(weapon))
                ? RunicConfig.dragonHeartFireDamageBonusPercent() / 100.0D
                : 0.0D;
        double setBonus = RelicRegistry.hasFullRelicSet(attacker, RelicRegistry.DRAGON_HEART)
                ? RunicConfig.dragonHeartFullSetFireDamageBonusPercent() / 100.0D
                : 0.0D;
        if (weaponBonus <= 0.0D && setBonus <= 0.0D) {
            return amount;
        }

        return (float) (amount * (1.0D + weaponBonus + setBonus));
    }

    private static float applyElderGuardianDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, float amount) {
        double weaponBonus = RelicRegistry.ELDER_GUARDIANS_EYE.equals(RunicItemData.getRelicId(weapon))
                ? RunicConfig.elderGuardiansEyeUnderwaterDamageBonusPercent() / 100.0D
                : 0.0D;
        if (weaponBonus <= 0.0D) {
            return amount;
        }

        if (target.isUnderWater()) {
            return (float) (amount * (1.0D + weaponBonus));
        }
        return amount;
    }

    private static float applyWitherChargeDamage(LivingEntity target, ItemStack weapon, float amount) {
        double weaponBonus = RelicRegistry.WITHER_CHARGE.equals(RunicItemData.getRelicId(weapon))
                ? RunicConfig.witherChargeDamageToWitheredBonusPercent() / 100.0D
                : 0.0D;
        if (weaponBonus <= 0.0D || !target.hasEffect(MobEffects.WITHER)) {
            return amount;
        }
        return (float) (amount * (1.0D + weaponBonus));
    }

    private static float applyWardensSoulDamage(LivingEntity target, ItemStack weapon, float amount) {
        if (!RelicRegistry.WARDENS_SOUL.equals(RunicItemData.getRelicId(weapon))) {
            return amount;
        }

        if (target.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES) || target.getMaxHealth() >= RunicConfig.wardensSoulHighHealthThreshold()) {
            return (float) (amount * (1.0D + (RunicConfig.wardensSoulBossDamageBonusPercent() / 100.0D)));
        }
        return amount;
    }

    private static void maybeDragonHeartAura(LivingEntity attacker, LivingEntity target) {
        if (!RelicRegistry.hasFullRelicSet(attacker, RelicRegistry.DRAGON_HEART)) {
            return;
        }
        if (attacker.getRandom().nextDouble() > RunicConfig.dragonHeartFullSetIgniteAuraChance()) {
            return;
        }

        double radius = RunicConfig.dragonHeartFullSetIgniteAuraRadius();
        for (Mob nearby : attacker.level().getEntitiesOfClass(Mob.class, new AABB(target.blockPosition()).inflate(radius), mob -> mob != target && mob.isAlive())) {
            nearby.setRemainingFireTicks(Math.max(nearby.getRemainingFireTicks(), 60));
        }
    }

    private static void maybeElderGuardianSlow(LivingEntity attacker, LivingEntity target) {
        if (!RelicRegistry.hasFullRelicSet(attacker, RelicRegistry.ELDER_GUARDIANS_EYE)) {
            return;
        }
        if (attacker.getRandom().nextDouble() > RunicConfig.elderGuardiansEyeFullSetSlowChance()) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, RunicConfig.elderGuardiansEyeFullSetSlowDurationTicks(), attacker.isUnderWater() ? 1 : 0, false, false, true));
    }

    private static void maybeWitherPulse(LivingEntity attacker, LivingEntity target, float damage) {
        if (damage <= 0.0F || !RelicRegistry.hasFullRelicSet(attacker, RelicRegistry.WITHER_CHARGE)) {
            return;
        }
        if (attacker.getRandom().nextDouble() > RunicConfig.witherChargeFullSetWitherPulseChance()) {
            return;
        }

        double radius = RunicConfig.witherChargeFullSetWitherPulseRadius();
        for (Mob nearby : attacker.level().getEntitiesOfClass(Mob.class, new AABB(target.blockPosition()).inflate(radius), mob -> mob != target && mob.isAlive())) {
            nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, false, true));
        }
    }


    private static void maybeWardensSoulPulse(LivingEntity wearer, float damageTaken) {
        if (wearer.level().isClientSide || damageTaken <= 0.0F) {
            return;
        }

        boolean anyWardenSoul = false;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (RelicRegistry.WARDENS_SOUL.equals(RunicItemData.getRelicId(wearer.getItemBySlot(slot)))) {
                anyWardenSoul = true;
                break;
            }
        }
        if (!anyWardenSoul) {
            return;
        }

        boolean fullSet = RelicRegistry.hasFullRelicSet(wearer, RelicRegistry.WARDENS_SOUL);
        double chance = RunicConfig.wardensSoulHeavyDamagePulseChance();
        if (wearer.getRandom().nextDouble() > chance) {
            return;
        }

        long gameTime = wearer.level().getGameTime();
        long cooldownUntil = wearer.getPersistentData().getLong(WARDEN_PULSE_COOLDOWN);
        if (gameTime < cooldownUntil) {
            return;
        }

        double damage = fullSet ? RunicConfig.wardensSoulFullSetSonicPulseDamage() : Math.max(1.0D, RunicConfig.wardensSoulFullSetSonicPulseDamage() * 0.5D);
        double radius = 4.0D;
        for (Mob nearby : wearer.level().getEntitiesOfClass(Mob.class, new AABB(wearer.blockPosition()).inflate(radius), Mob::isAlive)) {
            nearby.hurt(wearer.damageSources().sonicBoom(wearer), (float) damage);
        }

        wearer.getPersistentData().putLong(WARDEN_PULSE_COOLDOWN, gameTime + RunicConfig.wardensSoulFullSetSonicPulseCooldownTicks());
    }

    private static ResourceLocation activeFullSet(LivingEntity wearer) {
        for (ResourceLocation id : List.of(RelicRegistry.DRAGON_HEART, RelicRegistry.ELDER_GUARDIANS_EYE,
                RelicRegistry.WITHER_CHARGE, RelicRegistry.WARDENS_SOUL)) {
            if (RelicRegistry.hasFullRelicSet(wearer, id)) return id;
        }
        return null;
    }

    private static boolean useDragonBreath(ServerPlayer player) {
        Vec3 direction = aimDirection(player, 32.0D);
        DragonFireball fireball = new DragonFireball(player.level(), player, direction);
        fireball.setPos(player.getEyePosition().add(direction.scale(1.5D)));
        fireball.setDeltaMovement(direction.scale(0.7D));
        player.level().addFreshEntity(fireball);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean useGuardianBeam(ServerPlayer player) {
        LivingEntity target = targetUnderCrosshair(player, 24.0D);
        if (target == null) return false;

        spawnGuardianBeam(player, target);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, RELIC_POWER_CAST_TICKS + 40, 2, false, false, true));
        target.hurt(player.damageSources().indirectMagic(player, player), 14.0F);
        player.level().playSound(null, target.blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean useWitherBullet(ServerPlayer player) {
        Vec3 direction = aimDirection(player, 32.0D);
        WitherSkull skull = new WitherSkull(player.level(), player, direction);
        skull.setPos(player.getEyePosition().add(direction.scale(1.2D)));
        skull.setDeltaMovement(direction.scale(0.8D));
        skull.getPersistentData().putBoolean(RUNIC_WITHER_SKULL, true);
        player.level().addFreshEntity(skull);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }


    private static boolean useSonicBoom(ServerPlayer player) {
        LivingEntity target = targetUnderCrosshair(player, 20.0D);
        Vec3 start = player.getEyePosition();
        Vec3 direction;
        double distance;
        if (target != null) {
            Vec3 end = target.getEyePosition();
            direction = end.subtract(start).normalize();
            distance = start.distanceTo(end);
        } else {
            direction = aimDirection(player, 20.0D);
            distance = 20.0D;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            int count = Mth.floor(distance) + 7;
            for (int i = 1; i < count; i++) {
                Vec3 pos = start.add(direction.scale(i));
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);
        if (target != null && target.hurt(player.damageSources().sonicBoom(player), (float) RunicConfig.wardensSoulFullSetSonicPulseDamage())) {
            double vertical = 0.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double horizontal = 2.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            target.push(direction.x() * horizontal, direction.y() * vertical, direction.z() * horizontal);
        }
        return true;
    }

    private static Vec3 aimDirection(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        HitResult hit = player.level().clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS ? eye.add(look.scale(range)) : hit.getLocation();
        Vec3 direction = target.subtract(eye);
        return direction.lengthSqr() < 1.0E-6D ? look : direction.normalize();
    }

    // runs target under crosshair
    private static LivingEntity targetUnderCrosshair(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
            AABB targetBox = entity.getBoundingBox().inflate(entity.getPickRadius() + 0.35D);
            Optional<Vec3> clip = targetBox.clip(eye, end);
            if (clip.isEmpty()) continue;
            double distance = eye.distanceToSqr(clip.get());
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        return best;
    }

    // spawns guardian beam
    private static void spawnGuardianBeam(ServerPlayer player, LivingEntity target) {
        Guardian guardian = new Guardian(EntityType.GUARDIAN, player.level());
        Vec3 source = player.getEyePosition().subtract(0.0D, 0.35D, 0.0D);
        guardian.setPos(source.x, source.y, source.z);
        guardian.setNoAi(true);
        guardian.setNoGravity(true);
        guardian.setSilent(true);
        guardian.setInvulnerable(true);
        guardian.setInvisible(true);
        guardian.setTarget(target);
        guardian.getPersistentData().putUUID(GUARDIAN_BEAM_OWNER, player.getUUID());
        guardian.getPersistentData().putLong(GUARDIAN_BEAM_UNTIL, player.level().getGameTime() + RELIC_POWER_CAST_TICKS);
        player.level().addFreshEntity(guardian);
        setGuardianBeamTarget(guardian, target.getId());
    }

    private static int armorCount(LivingEntity entity, ResourceLocation relicId) {
        int count = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (relicId.equals(RunicItemData.getRelicId(entity.getItemBySlot(slot)))) {
                count++;
            }
        }
        return count;
    }

    private static EquipmentSlotGroup slotGroup(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ANY;
        };
    }

    private static void addModifier(ItemAttributeModifierEvent event, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                    String path, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "relic/" + path);
        event.addModifier(attribute, new AttributeModifier(id, amount, operation), slot);
    }

    private static void setGuardianBeamTarget(Guardian guardian, int targetId) {
        try {
            if (guardianSetActiveAttackTarget == null) {
                guardianSetActiveAttackTarget = Guardian.class.getDeclaredMethod("setActiveAttackTarget", int.class);
                guardianSetActiveAttackTarget.setAccessible(true);
            }
            guardianSetActiveAttackTarget.invoke(guardian, targetId);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private record ResourceLocationHelper(ItemStack stack) {
        boolean is(net.minecraft.resources.ResourceLocation relicId) {
            return relicId.equals(RunicItemData.getRelicId(stack));
        }

        boolean hasFireTrigger() {
            return hasFireTrigger(stack);
        }

        static boolean hasFireTrigger(ItemStack stack) {
            return RuneStats.get(stack).get(net.revilodev.runic.stat.RuneStatType.FLAME_CHANCE) > 0.0F
                    || stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().value().description().getString().toLowerCase().contains("fire"));
        }
    }
}
