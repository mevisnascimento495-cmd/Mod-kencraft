package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persistent spiritual power state. SUJO is the first release state of a Jio technique. */
public record SpiritualState(String state) {
    public static final String NONE = "NONE";
    public static final String SUJO = "SUJO";
    public static final String IMPURO = "IMPURO";
    public static final String PURO = "PURO";
    public static final SpiritualState DEFAULT = new SpiritualState(NONE);

    public static final Codec<SpiritualState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("state", NONE).forGetter(SpiritualState::state)
    ).apply(i, SpiritualState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritualState> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean isSujo() { return SUJO.equals(state); }
}
