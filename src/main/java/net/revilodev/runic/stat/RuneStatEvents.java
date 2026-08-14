package net.revilodev.runic.stat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
// responds to stat events
public final class RuneStatEvents {
    private static final String STONE_TICK = "runic_stone_tick";

    private RuneStatEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource src = event.getSource();

        float amount = event.getAmount();
        if (amount <= 0.0F) return;

        float aegis = getTotal(target, RuneStatType.AEGIS);
        if (aegis > 0.0F && target.getRandom().nextFloat() <= aegis / 100.0F) {
            event.setAmount(0.0F);
            return;
        }

        float generic = getTotal(target, RuneStatType.RESISTANCE);
        float fire = getTotal(target, RuneStatType.FIRE_RESISTANCE);
        float blast = getTotal(target, RuneStatType.BLAST_RESISTANCE);
        float proj = getTotal(target, RuneStatType.PROJECTILE_RESISTANCE);

        amount *= reduce(generic);

        if (src.is(DamageTypeTags.IS_FIRE)) {
            amount *= reduce(fire);
        }
        if (src.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= reduce(proj);
        }
        if (src.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= reduce(blast);
        }

        float stone = getTotal(target, RuneStatType.STONE);
        long now = target.level().getGameTime();
        long stoneUntil = target.getPersistentData().getLong(STONE_TICK);
        if (stone > 0.0F && stoneUntil > now) {
            amount *= reduce(stone);
        }

        if (stone > 0.0F && target.getMaxHealth() > 0.0F && amount >= target.getMaxHealth() * 0.25F) {
            target.getPersistentData().putLong(STONE_TICK, now + 80L);
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
        }

        event.setAmount(amount);
    }

    @SubscribeEvent
    // responds to entity join
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        if (entity instanceof ServerPlayer player) {
            // Re-apply runic attributes once on join so persisted runic stats
            // always restore their modifiers (notably MAX_HEALTH on armor).
            reapplyRunicInventory(player);
        }

        float runicHealth = getTotal(entity, RuneStatType.HEALTH);
        if (runicHealth <= 0.0F) return;

        AttributeInstance maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        float current = entity.getHealth();
        float max = entity.getMaxHealth();
        float base = (float) maxHealthAttr.getBaseValue();

        if (max <= base + 0.01F) return;
        if (current > base + 0.01F) return;
        if (current <= 0.0F) return;

        entity.setHealth(Math.min(max, current + (max - base)));
    }

    @SubscribeEvent
    // responds to living tick
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        if (entity instanceof Player player && player.tickCount % 10 == 0) {
            reconcileRunicInventory(player);
        }

        ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
        if (leggings.isEmpty()) return;

        float jump = RuneStats.get(leggings).get(RuneStatType.JUMP_HEIGHT);
        if (jump <= 0) return;

        int amplifier = (int)(jump / 10f);
        if (amplifier < 0) amplifier = 0;

        entity.addEffect(new MobEffectInstance(
                MobEffects.JUMP,
                5,
                amplifier,
                false,
                false,
                false
        ));
    }

    private static void reconcileRunicInventory(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (!RuneStats.needsRebuildForCurrentItem(stack)) continue;

            RuneStats stats = RuneStats.get(stack);
            if (stats == null || stats.isEmpty()) continue;
            RuneStats.set(stack, stats);
        }
    }

    private static void reapplyRunicInventory(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            RuneStats stats = RuneStats.get(stack);
            if (stats == null || stats.isEmpty()) continue;
            RuneStats.set(stack, stats);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        RuneStats stats = RuneStats.get(crafted);
        if (stats == null || stats.isEmpty()) return;

        // Rebuild runic attributes against the crafted item's current base item.
        // This keeps smithing-upgraded gear (diamond -> netherite, etc.) aligned
        // with the new tier's base attack damage/speed/range while preserving runic bonuses.
        RuneStats.set(crafted, stats);
    }

    private static float getTotal(LivingEntity e, RuneStatType type) {
        return RuneStats.getTotalFromEquipment(e, type);
    }

    private static float reduce(float percent) {
        if (percent <= 0.0F) return 1.0F;
        return 1.0F - Math.min(percent, 90.0F) / 100.0F;
    }
}
