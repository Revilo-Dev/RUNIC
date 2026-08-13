package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;
import net.revilodev.runic.synergy.SynergyEffects;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RuneOnHitHandler {

    private static final ResourceLocation BLEEDING_ID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "bleeding");
    private static final ResourceLocation STUN_ID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stunning");
    private static final ResourceLocation SHOCK_ID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "shocking");

    private RuneOnHitHandler() {}

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (player.level().isClientSide) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        RuneStats stats = RuneStats.get(weapon);

        RandomSource rand = player.getRandom();

        if (stats != null && !stats.isEmpty()) {
            tryRollEffect(stats, RuneStatType.BLEEDING_CHANCE, rand,
                    () -> applyCustomEffectOrSkip(target, BLEEDING_ID, 100, 0));

            tryRollEffect(stats, RuneStatType.STUN_CHANCE, rand,
                    () -> applyCustomEffectOrSkip(target, STUN_ID, 60, 0));

            tryRollEffect(stats, RuneStatType.SHOCKING_CHANCE, rand,
                    () -> {
                        summonShockingLightning(player, target);
                        if (!applyCustomEffectOrSkip(target, SHOCK_ID, 80, 0)) {
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                        }
                    });

            tryRollEffect(stats, RuneStatType.POISON_CHANCE, rand,
                    () -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0)));

            tryRollEffect(stats, RuneStatType.WEAKENING_CHANCE, rand,
                    () -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0)));

            tryRollEffect(stats, RuneStatType.FLAME_CHANCE, rand,
                    () -> target.setRemainingFireTicks(80));

            tryRollEffect(stats, RuneStatType.WITHERING_CHANCE, rand,
                    () -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0)));

            tryRollEffect(stats, RuneStatType.FREEZING_CHANCE, rand,
                    () -> {
                        int level = stats.get(RuneStatType.FREEZING_CHANCE) <= RuneStatType.FREEZING_CHANCE.etchingMaxPercent() ? 1 : 2;
                        SynergyEffects.markFrozen(target, player, level);
                    });
        }

        SynergyEffects.onWeaponAttack(player, target, weapon, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private static void tryRollEffect(RuneStats stats,
                                      RuneStatType type,
                                      RandomSource rand,
                                      Runnable onSuccess) {
        float pct = stats.get(type);
        if (pct <= 0.0F) return;
        float chance = pct / 100.0F;
        if (rand.nextFloat() <= chance) onSuccess.run();
    }

    private static boolean applyCustomEffectOrSkip(LivingEntity target,
                                                   ResourceLocation id,
                                                   int duration,
                                                   int amplifier) {

        ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id);
        var holderOpt = BuiltInRegistries.MOB_EFFECT.getHolder(key);
        if (holderOpt.isEmpty()) {
            return false;
        }

        Holder<MobEffect> effect = holderOpt.get();

        target.addEffect(new MobEffectInstance(
                effect,
                duration,
                amplifier,
                false,
                false,
                true
        ));

        return true;
    }

    private static void summonShockingLightning(Player player, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) {
            return;
        }

        lightning.moveTo(target.getX(), target.getY(), target.getZ());
        // Visual-only lightning cannot ignite blocks and does not damage entities.
        lightning.setVisualOnly(true);
        if (player instanceof ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }

        level.addFreshEntity(lightning);
    }
}
