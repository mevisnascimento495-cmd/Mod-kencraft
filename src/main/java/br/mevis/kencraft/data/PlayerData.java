package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerData(Race race, int jio, int maxJio) {
    public static final PlayerData DEFAULT = new PlayerData(Race.NONE, 0, 0);

    private static final Codec<Race> RACE_CODEC =
            Codec.STRING.xmap(Race::valueOf, Race::name);

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RACE_CODEC.fieldOf("race").forGetter(PlayerData::race),
                    Codec.INT.fieldOf("jio").forGetter(PlayerData::jio),
                    Codec.INT.fieldOf("maxJio").forGetter(PlayerData::maxJio)
            ).apply(instance, PlayerData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean hasRace() {
        return race != Race.NONE;
    }

    public static PlayerData forRinka() {
        return new PlayerData(Race.RINKA, 0, 0);
    }

    public static PlayerData forHuman() {
        return new PlayerData(Race.HUMAN, 100, 100);
    }
}
