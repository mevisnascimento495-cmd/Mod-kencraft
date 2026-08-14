package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Server-authoritative animation command replicated to tracking clients. */
public record JioAnimationData(String technique, int ability, long startTick, int duration) {
    public static final JioAnimationData DEFAULT = new JioAnimationData("NONE", 0, -1L, 0);

    public static final Codec<JioAnimationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("technique", "NONE").forGetter(JioAnimationData::technique),
            Codec.INT.optionalFieldOf("ability", 0).forGetter(JioAnimationData::ability),
            Codec.LONG.optionalFieldOf("startTick", -1L).forGetter(JioAnimationData::startTick),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(JioAnimationData::duration)
    ).apply(instance, JioAnimationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JioAnimationData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, JioAnimationData::technique,
            ByteBufCodecs.VAR_INT, JioAnimationData::ability,
            ByteBufCodecs.VAR_LONG, JioAnimationData::startTick,
            ByteBufCodecs.VAR_INT, JioAnimationData::duration,
            JioAnimationData::new
    );

    public boolean activeAt(long gameTime) {
        return duration > 0 && startTick >= 0 && gameTime >= startTick && gameTime < startTick + duration;
    }

    public float progressAt(long gameTime) {
        if (duration <= 0 || startTick < 0) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, (gameTime - startTick) / (float) duration));
    }

    public float impactEnvelopeAt(long gameTime) {
        float p = progressAt(gameTime);
        if (!activeAt(gameTime)) return 0.0F;
        if (p < 0.5F) return p * 2.0F;
        return (1.0F - p) * 2.0F;
    }
}
