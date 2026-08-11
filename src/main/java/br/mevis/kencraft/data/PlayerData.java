package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persistent KenCraft data attached to each player.
 *
 * Status levels are intentionally stored from 1 to 20. A fresh character
 * starts at level 1 in every status and therefore has 0 spent status points.
 * XP pools are kept separate from status levels so future NPCs/rewards can
 * grant mental or physical XP without automatically changing attributes.
 */
public record PlayerData(
        Race race,
        int jio,
        int maxJio,
        int strength,
        int defense,
        int intelligence,
        int speed,
        int genetics,
        int perception,
        int spiritualDevelopment,
        int mentalXp,
        int physicalXp
) {
    public static final int MIN_STATUS = 1;
    public static final int MAX_STATUS = 20;

    /** Default values used for players that have not selected a race yet. */
    public static final PlayerData DEFAULT = new PlayerData(
            Race.NONE,
            0,
            0,
            MIN_STATUS,
            MIN_STATUS,
            MIN_STATUS,
            MIN_STATUS,
            MIN_STATUS,
            MIN_STATUS,
            MIN_STATUS,
            0,
            0
    );

    private static final Codec<Race> RACE_CODEC =
            Codec.STRING.xmap(Race::valueOf, Race::name);

    // optionalFieldOf keeps existing KenCraft worlds compatible when these
    // new fields are first introduced. Old saves get the defaults below.
    private static Codec<Integer> intWithDefault(int value) {
        return Codec.INT;
    }

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RACE_CODEC.fieldOf("race").forGetter(PlayerData::race),
                    Codec.INT.fieldOf("jio").forGetter(PlayerData::jio),
                    Codec.INT.fieldOf("maxJio").forGetter(PlayerData::maxJio),
                    Codec.INT.optionalFieldOf("strength", MIN_STATUS).forGetter(PlayerData::strength),
                    Codec.INT.optionalFieldOf("defense", MIN_STATUS).forGetter(PlayerData::defense),
                    Codec.INT.optionalFieldOf("intelligence", MIN_STATUS).forGetter(PlayerData::intelligence),
                    Codec.INT.optionalFieldOf("speed", MIN_STATUS).forGetter(PlayerData::speed),
                    Codec.INT.optionalFieldOf("genetics", MIN_STATUS).forGetter(PlayerData::genetics),
                    Codec.INT.optionalFieldOf("perception", MIN_STATUS).forGetter(PlayerData::perception),
                    Codec.INT.optionalFieldOf("spiritualDevelopment", MIN_STATUS).forGetter(PlayerData::spiritualDevelopment),
                    Codec.INT.optionalFieldOf("mentalXp", 0).forGetter(PlayerData::mentalXp),
                    Codec.INT.optionalFieldOf("physicalXp", 0).forGetter(PlayerData::physicalXp)
            ).apply(instance, PlayerData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean hasRace() {
        return race != Race.NONE;
    }

    /** Number of points spent in an attribute at its current level. */
    public static int spentPoints(int level) {
        return Math.max(0, Math.min(MAX_STATUS, level) - MIN_STATUS);
    }

    /**
     * Jio multiplier for humans. Each spent spiritual-development point adds
     * 3% over the base Jio amount. At the starting level (1), the multiplier
     * remains 1.00x.
     */
    public double jioMultiplier() {
        return 1.0D + (spentPoints(spiritualDevelopment) * 0.03D);
    }

    public int calculatedHumanMaxJio() {
        if (race != Race.HUMAN) {
            return maxJio;
        }
        return (int) Math.round(100.0D * jioMultiplier());
    }

    public static PlayerData forRinka() {
        return new PlayerData(
                Race.RINKA,
                0,
                0,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                0,
                0
        );
    }

    public static PlayerData forHuman() {
        return new PlayerData(
                Race.HUMAN,
                100,
                100,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                MIN_STATUS,
                0,
                0
        );
    }
}
