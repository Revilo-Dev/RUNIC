package net.revilodev.runic.mythic;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.revilodev.runic.RunicConfig;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.gear.RunicItemData;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class MythicRuneEffects {
    private static final String COMBAT_UNTIL = "runic_mythic_combat_until";
    private static final String VOID_NEXT = "runic_void_next_tick";

    private MythicRuneEffects() {}

    public static float modifyOutgoingDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, float amount) {
        if (attacker == null || target == null || weapon.isEmpty() || amount <= 0.0F) {
            return amount;
        }

        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.RUIN)) {
            amount *= (float) (1.0D + (RunicConfig.ruinDamageBonusPercent() / 100.0D));
        }
        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.VOID)
                && target.getMaxHealth() > 0.0F
                && (target.getHealth() / target.getMaxHealth()) <= RunicConfig.voidLowHealthThreshold()) {
            amount *= (float) (1.0D + (RunicConfig.voidDamageBonusPercent() / 100.0D));
        }
        return amount;
    }

    @SubscribeEvent
    public static void onPostDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null || attacker.level().isClientSide || event.getNewDamage() <= 0.0F) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        long combatUntil = attacker.level().getGameTime() + 120L;
        attacker.getPersistentData().putLong(COMBAT_UNTIL, combatUntil);
        target.getPersistentData().putLong(COMBAT_UNTIL, combatUntil);

        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.RUIN)
                && attacker.getRandom().nextDouble() < RunicConfig.ruinExtraCorruptionChance()) {
            RunicItemData.addCorruption(weapon, RunicConfig.ruinExtraCorruptionAmount());
        }

        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.HUNGER)
                && target.isAlive()
                && attacker.getRandom().nextDouble() < RunicConfig.hungerExtraCorruptionOnHitChance()) {
            RunicItemData.addCorruption(weapon, RunicConfig.hungerExtraCorruptionAmount());
        }

    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.HUNGER) && weapon.isDamageableItem()) {
            weapon.setDamageValue(Math.max(0, weapon.getDamageValue() - RunicConfig.hungerDurabilityRestoreOnKill()));
        }
        if (RunicItemData.hasMythicRune(weapon, MythicRuneRegistry.ASCENDANCE)
                && event.getEntity().getMaxHealth() >= RunicConfig.ascendanceTargetMaxHealthThreshold()) {
            attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, RunicConfig.ascendanceDurationTicks(), 0, false, false, true));
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, RunicConfig.ascendanceDurationTicks(), 0, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide) {
            return;
        }
        long now = living.level().getGameTime();
        if (living.getPersistentData().getLong(COMBAT_UNTIL) < now) {
            return;
        }

        long next = living.getPersistentData().getLong(VOID_NEXT);
        if (next > now) {
            return;
        }

        int interval = Math.max(1, RunicConfig.voidCombatCorruptionIntervalTicks());
        living.getPersistentData().putLong(VOID_NEXT, now + interval);
        for (ItemStack stack : collectVoidStacks(living)) {
            RunicItemData.addCorruption(stack, RunicConfig.voidCombatCorruptionAmount());
        }
    }

    // collects void stacks
    private static List<ItemStack> collectVoidStacks(LivingEntity living) {
        List<ItemStack> out = new ArrayList<>();
        ItemStack mainhand = living.getMainHandItem();
        if (RunicItemData.hasMythicRune(mainhand, MythicRuneRegistry.VOID)) {
            out.add(mainhand);
        }
        if (living instanceof Player player) {
            ItemStack offhand = player.getOffhandItem();
            if (RunicItemData.hasMythicRune(offhand, MythicRuneRegistry.VOID)) {
                out.add(offhand);
            }
        }
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack armor = living.getItemBySlot(slot);
            if (RunicItemData.hasMythicRune(armor, MythicRuneRegistry.VOID)) {
                out.add(armor);
            }
        }
        return out;
    }
}
