package net.revilodev.runic.network.payload;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.compat.RunicCompat;
import net.revilodev.runic.event.EnchantBlacklist;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.List;

// supports reroll etching payload

// supports reroll etching payload
public record RerollEtchingPayload() implements CustomPacketPayload {
    public static final Type<RerollEtchingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "reroll_etching"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RerollEtchingPayload> STREAM_CODEC =
            StreamCodec.unit(new RerollEtchingPayload());

    private static final int XP_COST_LEVELS = 1;
    private static final int LAPIS_COST = 1;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(RerollEtchingPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EnchantmentMenu menu)) return;

            Slot etchingSlot = menu.getSlot(0);
            Slot lapisSlot = menu.getSlot(1);
            ItemStack input = etchingSlot.getItem();
            ItemStack lapis = lapisSlot.getItem();

            if (!input.is(ModItems.BLANK_ETCHING.get()) && !input.is(ModItems.ETCHING.get())) return;
            if (!player.getAbilities().instabuild) {
                if (player.experienceLevel < XP_COST_LEVELS) return;
                if (lapis.isEmpty() || !lapis.is(Items.LAPIS_LAZULI) || lapis.getCount() < LAPIS_COST) return;
            }

            ItemStack rolled = randomEtching(player.getRandom(), player);
            if (rolled.isEmpty()) return;

            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-XP_COST_LEVELS);
                lapis.shrink(LAPIS_COST);
                if (lapis.isEmpty()) lapisSlot.set(ItemStack.EMPTY);
                else lapisSlot.setChanged();
            }

            etchingSlot.set(rolled);
            etchingSlot.setChanged();
            menu.broadcastChanges();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        });
    }


    private static ItemStack randomEtching(RandomSource random, ServerPlayer player) {
        List<RuneStatType> stats = new ArrayList<>();
        for (RuneStatType type : RuneStatType.values()) {
            if (!EnchantBlacklist.isStatBlacklisted(type) && RunicCompat.isStatAvailable(type)) {
                stats.add(type);
            }
        }

        List<Holder<Enchantment>> effects = new ArrayList<>();
        var registry = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (ResourceLocation id : EtchingItem.allowedEffectIds()) {
            registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id))
                    .filter(holder -> EtchingItem.isEffectEnchantment(holder) && !EnchantBlacklist.isBlacklisted(holder))
                    .ifPresent(effects::add);
        }

        int total = stats.size() + effects.size();
        if (total <= 0) return ItemStack.EMPTY;

        int roll = random.nextInt(total);
        if (roll < stats.size()) {
            return EtchingItem.createStatEtching(random, stats.get(roll));
        }
        return EtchingItem.createEffectEtching(effects.get(roll - stats.size()));
    }
}
