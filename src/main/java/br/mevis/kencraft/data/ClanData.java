package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Persistent clan assignment and the short reveal animation state. */
public record ClanData(String clan, boolean readyToRoll, boolean rolling, int rollTicks) {
    public static final ClanData DEFAULT = new ClanData("NONE", false, false, 0);

    public static final Codec<ClanData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("clan", "NONE").forGetter(ClanData::clan),
            Codec.BOOL.optionalFieldOf("readyToRoll", false).forGetter(ClanData::readyToRoll),
            Codec.BOOL.optionalFieldOf("rolling", false).forGetter(ClanData::rolling),
            Codec.INT.optionalFieldOf("rollTicks", 0).forGetter(ClanData::rollTicks)
    ).apply(instance, ClanData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClanData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeUtf(value.clan());
                buf.writeBoolean(value.readyToRoll());
                buf.writeBoolean(value.rolling());
                buf.writeVarInt(value.rollTicks());
            },
            buf -> new ClanData(buf.readUtf(32), buf.readBoolean(), buf.readBoolean(), buf.readVarInt())
    );

    public boolean hasClan() {
        return !"NONE".equals(clan);
    }

    public ClanData prepare() {
        return new ClanData(clan, true, false, 0);
    }

    public ClanData startRoll() {
        return new ClanData(clan, false, true, 0);
    }

    public ClanData tickRoll() {
        return new ClanData(clan, false, true, rollTicks + 1);
    }

    public ClanData assign(String selected) {
        return new ClanData(selected, false, false, 0);
    }
}
