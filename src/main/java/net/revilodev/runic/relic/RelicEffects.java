package net.revilodev.runic.relic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.gear.RunicItemData;
import net.revilodev.runic.mythic.MythicRuneRegistry;
import net.revilodev.runic.stat.RuneStats;

import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RelicEffects {
    private static final String WARDEN_PULSE_COOLDOWN = "runic_warden_pulse_cooldown";

    private RelicEffects() {}

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

        maybeApplyExtraDurability(player, weapon);
    }

    @SubscribeEvent
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
        double armorBonus = armorContribution(attacker, RelicRegistry.DRAGON_HEART) * (RunicConfig.dragonHeartFireDamageBonusPercent() / 100.0D);
        double setBonus = RelicRegistry.hasFullRelicSet(attacker, RelicRegistry.DRAGON_HEART)
                ? RunicConfig.dragonHeartFullSetFireDamageBonusPercent() / 100.0D
                : 0.0D;
        if (weaponBonus <= 0.0D && armorBonus <= 0.0D && setBonus <= 0.0D) {
            return amount;
        }

        if (target.getRemainingFireTicks() > 0 || ResourceLocationHelper.hasFireTrigger(weapon)) {
            return (float) (amount * (1.0D + weaponBonus + armorBonus + setBonus));
        }
        return amount;
    }

    private static float applyElderGuardianDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, float amount) {
        double weaponBonus = RelicRegistry.ELDER_GUARDIANS_EYE.equals(RunicItemData.getRelicId(weapon))
                ? RunicConfig.elderGuardiansEyeUnderwaterDamageBonusPercent() / 100.0D
                : 0.0D;
        double armorBonus = armorContribution(attacker, RelicRegistry.ELDER_GUARDIANS_EYE)
                * (RunicConfig.elderGuardiansEyeUnderwaterDamageBonusPercent() / 100.0D);
        if (weaponBonus <= 0.0D && armorBonus <= 0.0D) {
            return amount;
        }

        if (attacker.isUnderWater() || target.isUnderWater()) {
            return (float) (amount * (1.0D + weaponBonus + armorBonus));
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

    private static double armorContribution(LivingEntity entity, ResourceLocation relicId) {
        int count = RelicRegistry.countEquippedRelics(entity, relicId);
        if (count <= 0) {
            return 0.0D;
        }
        return (double) count / (double) Math.max(1, RunicConfig.relicFullSetRequiredCount()) * 0.5D;
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
