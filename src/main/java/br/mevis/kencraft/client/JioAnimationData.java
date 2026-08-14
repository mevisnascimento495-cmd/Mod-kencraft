package br.mevis.kencraft.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Server-synchronized combat animation state so every client can render a player's Jio attack. */
public record JioAnimationData(String technique, int ability, long startTick, int duration) {
    public static final JioAnimationData DEFAULT = new JioAnimationData("NONE", 0, 0L, 0);

    public static final Codec<JioAnimationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("technique", "NONE").forGetter(JioAnimationData::technique),
            Codec.INT.optionalFieldOf("ability", 0).forGetter(JioAnimationData::ability),
            Codec.LONG.optionalFieldOf("startTick", 0L).forGetter(JioAnimationData::startTick),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(JioAnimationData::duration)
    ).apply(i, JioAnimationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JioAnimationData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean active(long currentTick) {
        return duration > 0 && currentTick >= startTick && currentTick < startTick + duration;
    }

    public float progress(long currentTick) {
        if (!active(currentTick)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, (currentTick - startTick) / (float) duration));
    }

    public float impactEnvelope(long currentTick) {
        float p = progress(currentTick);
        if (!active(currentTick)) return 0.0F;
        return p < 0.5F ? p * 2.0F : (1.0F - p) * 2.0F;
    }
}
