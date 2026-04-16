package net.revilodev.runic.stat;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuneStatType implements StringRepresentable {
    ATTACK_SPEED("attack_speed", 10, 28, 5, 14, 0.0F),
    ATTACK_DAMAGE("attack_damage", 2.0F, 3.5F, 1.0F, 2.0F, 0.0F, false, 0.5F),
    ATTACK_RANGE("attack_range", 6, 14, 3, 7, 20.0F),
    MOVEMENT_SPEED("movement_speed", 8, 18, 4, 9, 30.0F),
    SWEEPING_RANGE("sweeping_range", 6, 12, 3, 6, 35.0F),
    DURABILITY("durability", 12, 100, 6, 50, 0.0F),
    RESISTANCE("resistance", 4, 8, 2, 4, 15.0F),
    FIRE_RESISTANCE("fire_resistance", 4, 8, 2, 4, 15.0F),
    BLAST_RESISTANCE("blast_resistance", 4, 8, 2, 4, 15.0F),
    PROJECTILE_RESISTANCE("projectile_resistance", 4, 8, 2, 4, 15.0F),
    KNOCKBACK_RESISTANCE("knockback_resistance", 4, 8, 2, 4, 15.0F),
    MINING_SPEED("mining_speed", 12, 80, 6, 40, 0.0F),
    UNDEAD_DAMAGE("undead_damage", 10, 32, 5, 16, 0.0F),
    NETHER_DAMAGE("nether_damage", 10, 32, 5, 16, 0.0F),
    HEALTH("health", 2.0F, 4.0F, 1.0F, 2.0F, 0.0F, false),
    STUN_CHANCE("stun_chance", 6, 12, 3, 6, 35.0F),
    FLAME_CHANCE("flame_chance", 6, 12, 3, 6, 35.0F),
    BLEEDING_CHANCE("bleeding_chance", 6, 12, 3, 6, 60.0F),
    SHOCKING_CHANCE("shocking_chance", 6, 12, 3, 6, 60.0F),
    POISON_CHANCE("poison_chance", 6, 12, 3, 6, 60.0F),
    WITHERING_CHANCE("withering_chance", 6, 12, 3, 6, 60.0F),
    WEAKENING_CHANCE("weakening_chance", 6, 12, 3, 6, 60.0F),
    DRAW_SPEED("draw_speed", 10, 24, 5, 12, 0.0F),
    TOUGHNESS("toughness", 2.0F, 4.0F, 1.0F, 2.0F, 20.0F, false),
    FREEZING_CHANCE("freezing_chance", 6, 12, 3, 6, 35.0F),
    LEECHING_CHANCE("leeching_chance", 1.0F, 3.0F, 1.0F, 3.0F, 18.0F),
    BONUS_CHANCE("bonus_chance", 1.0F, 4.0F, 1.0F, 2.0F, 35.0F, false),
    FANGS("fangs", 8, 15, 4, 8, 35.0F),
    STONE("stone", 10, 20, 5, 10, 30.0F),
    AEGIS("aegis", 3, 8, 1, 4, 12.0F),
    JUMP_HEIGHT("jump_height", 6, 18, 3, 9, 25.0F),
    POWER("power", 8, 25, 4, 13, 0.0F);

    private static final Map<String, RuneStatType> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(RuneStatType::id, t -> t));

    public static final Codec<RuneStatType> CODEC = Codec.STRING.xmap(
            RuneStatType::byIdOrThrow,
            RuneStatType::id
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RuneStatType> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RuneStatType decode(RegistryFriendlyByteBuf buf) {
                    return byIdOrThrow(buf.readUtf());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RuneStatType value) {
                    buf.writeUtf(value.id);
                }
            };

    private final String id;
    private final float minPercent;
    private final float maxPercent;
    private final float etchingMinPercent;
    private final float etchingMaxPercent;
    private final float capPercent;
    private final boolean percentBased;
    private final float rollStep;

    RuneStatType(String id, float minPercent, float maxPercent, float etchingMinPercent, float etchingMaxPercent, float capPercent) {
        this(id, minPercent, maxPercent, etchingMinPercent, etchingMaxPercent, capPercent, true, 1.0F);
    }

    RuneStatType(String id, float minPercent, float maxPercent, float etchingMinPercent, float etchingMaxPercent, float capPercent, boolean percentBased) {
        this(id, minPercent, maxPercent, etchingMinPercent, etchingMaxPercent, capPercent, percentBased, 1.0F);
    }

    RuneStatType(String id, float minPercent, float maxPercent, float etchingMinPercent, float etchingMaxPercent, float capPercent, boolean percentBased, float rollStep) {
        this.id = id;
        this.minPercent = minPercent;
        this.maxPercent = Math.max(maxPercent, minPercent);
        this.etchingMinPercent = etchingMinPercent;
        this.etchingMaxPercent = Math.max(etchingMaxPercent, etchingMinPercent);
        this.capPercent = capPercent;
        this.percentBased = percentBased;
        this.rollStep = Math.max(0.1F, rollStep);
    }

    public String id() {
        return this.id;
    }

    public float minPercent() {
        return this.minPercent;
    }

    public float maxPercent() {
        return this.maxPercent;
    }

    public float etchingMinPercent() {
        return this.etchingMinPercent;
    }

    public float etchingMaxPercent() {
        return this.etchingMaxPercent;
    }

    public float cap() {
        return this.capPercent;
    }

    public boolean isPercentBased() {
        return this.percentBased;
    }

    public float rollStep() {
        return this.rollStep;
    }

    public float roll(RandomSource random) {
        return rollRange(random, this.minPercent, this.maxPercent);
    }

    public float rollEtching(RandomSource random) {
        return rollRange(random, this.etchingMinPercent, this.etchingMaxPercent);
    }

    private float rollRange(RandomSource random, float min, float max) {
        if (max <= min) return min;
        float span = max - min;
        int steps = Math.max(1, (int) Math.floor(span / this.rollStep + 0.0001F));
        int pick = random.nextInt(steps + 1);
        float value = min + pick * this.rollStep;
        return Math.min(max, value);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public static RuneStatType byId(String id) {
        return BY_ID.get(id);
    }

    public static RuneStatType byIdOrThrow(String id) {
        RuneStatType t = BY_ID.get(id);
        if (t == null) throw new IllegalArgumentException("Unknown RuneStatType: " + id);
        return t;
    }

    public static Map<RuneStatType, Float> emptyMap() {
        return new EnumMap<>(RuneStatType.class);
    }
}
