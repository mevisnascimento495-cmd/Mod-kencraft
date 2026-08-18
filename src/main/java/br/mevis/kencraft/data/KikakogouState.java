package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KikakogouState(String type, boolean active, int remainingTicks, int cooldownTicks) {
    public static final KikakogouState DEFAULT = new KikakogouState("NONE", false, 0, 0);
    public static final Codec<KikakogouState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type", "NONE").forGetter(KikakogouState::type),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(KikakogouState::active),
            Codec.INT.optionalFieldOf("remainingTicks", 0).forGetter(KikakogouState::remainingTicks),
            Codec.INT.optionalFieldOf("cooldownTicks", 0).forGetter(KikakogouState::cooldownTicks)
    ).apply(instance, KikakogouState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KikakogouState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, KikakogouState::type,
            ByteBufCodecs.BOOL, KikakogouState::active,
            ByteBufCodecs.VAR_INT, KikakogouState::remainingTicks,
            ByteBufCodecs.VAR_INT, KikakogouState::cooldownTicks,
            KikakogouState::new);
}
