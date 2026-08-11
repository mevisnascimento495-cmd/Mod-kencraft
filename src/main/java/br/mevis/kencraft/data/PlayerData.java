package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persistent KenCraft data attached to each player. */
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
        int life,
        int mentalXp,
        int physicalXp
) {
    public static final int MIN_STATUS = 1;
    public static final int MAX_STATUS = 20;

    public static final PlayerData DEFAULT = new PlayerData(
            Race.NONE, 0, 0,
            MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS,
            MIN_STATUS, MIN_STATUS, MIN_STATUS,
            0, 0
    );

    private static final Codec<Race> RACE_CODEC = Codec.STRING.xmap(Race::valueOf, Race::name);

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
                    Codec.INT.optionalFieldOf("life", MIN_STATUS).forGetter(PlayerData::life),
                    Codec.INT.optionalFieldOf("mentalXp", 0).forGetter(PlayerData::mentalXp),
                    Codec.INT.optionalFieldOf("physicalXp", 0).forGetter(PlayerData::physicalXp)
            ).apply(instance, PlayerData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean hasRace() {
        return race != Race.NONE;
    }

    public static int spentPoints(int level) {
        return Math.max(0, Math.min(MAX_STATUS, level) - MIN_STATUS);
    }

    public double jioMultiplier() {
        return 1.0D + (spentPoints(spiritualDevelopment) * 0.03D);
    }

    public int calculatedHumanMaxJio() {
        return race == Race.HUMAN ? (int) Math.round(100.0D * jioMultiplier()) : maxJio;
    }

    public static PlayerData forRinka() {
        return new PlayerData(Race.RINKA, 0, 0,
                MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS,
                MIN_STATUS, MIN_STATUS, MIN_STATUS, 0, 0);
    }

    public static PlayerData forHuman() {
        return new PlayerData(Race.HUMAN, 100, 100,
                MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS, MIN_STATUS,
                MIN_STATUS, MIN_STATUS, MIN_STATUS, 0, 0);
    }

    public PlayerData withStatus(String attribute, int value) {
        value = Math.max(MIN_STATUS, Math.min(MAX_STATUS, value));
        return switch (attribute) {
            case "strength" -> new PlayerData(race, jio, maxJio, value, defense, intelligence, speed, genetics, perception, spiritualDevelopment, life, mentalXp, physicalXp);
            case "defense" -> new PlayerData(race, jio, maxJio, strength, value, intelligence, speed, genetics, perception, spiritualDevelopment, life, mentalXp, physicalXp);
            case "intelligence" -> new PlayerData(race, jio, maxJio, strength, defense, value, speed, genetics, perception, spiritualDevelopment, life, mentalXp, physicalXp);
            case "speed" -> new PlayerData(race, jio, maxJio, strength, defense, intelligence, value, genetics, perception, spiritualDevelopment, life, mentalXp, physicalXp);
            case "genetics" -> new PlayerData(race, jio, maxJio, strength, defense, intelligence, speed, value, perception, spiritualDevelopment, life, mentalXp, physicalXp);
            case "perception" -> new PlayerData(race, jio, maxJio, strength, defense, intelligence, speed, genetics, value, spiritualDevelopment, life, mentalXp, physicalXp);
            case "spiritual" -> new PlayerData(race, jio, maxJio, strength, defense, intelligence, speed, genetics, perception, value, life, mentalXp, physicalXp);
            case "life" -> new PlayerData(race, jio, maxJio, strength, defense, intelligence, speed, genetics, perception, spiritualDevelopment, value, mentalXp, physicalXp);
            default -> this;
        };
    }

    public PlayerData withXp(int mental, int physical) {
        return new PlayerData(race, jio, maxJio, strength, defense, intelligence, speed, genetics,
                perception, spiritualDevelopment, life, Math.max(0, mental), Math.max(0, physical));
    }

    public PlayerData withJio(int current, int max) {
        return new PlayerData(race, Math.max(0, current), Math.max(0, max), strength, defense,
                intelligence, speed, genetics, perception, spiritualDevelopment, life, mentalXp, physicalXp);
    }
}
