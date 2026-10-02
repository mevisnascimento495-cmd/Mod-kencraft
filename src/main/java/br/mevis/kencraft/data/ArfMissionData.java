package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Persistent ARF mission progression, separate from the official investigator class. */
public record ArfMissionData(
        int missionId,
        boolean active,
        int completedMissions,
        int reputation
) {
    public static final ArfMissionData DEFAULT = new ArfMissionData(0, false, 0, 0);

    public static final Codec<ArfMissionData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("missionId", 0).forGetter(ArfMissionData::missionId),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(ArfMissionData::active),
            Codec.INT.optionalFieldOf("completedMissions", 0).forGetter(ArfMissionData::completedMissions),
            Codec.INT.optionalFieldOf("reputation", 0).forGetter(ArfMissionData::reputation)
    ).apply(i, ArfMissionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArfMissionData> STREAM_CODEC =
            StreamCodec.of(
                    (buf, value) -> {
                        buf.writeVarInt(value.missionId());
                        buf.writeBoolean(value.active());
                        buf.writeVarInt(value.completedMissions());
                        buf.writeVarInt(value.reputation());
                    },
                    buf -> new ArfMissionData(
                            buf.readVarInt(),
                            buf.readBoolean(),
                            buf.readVarInt(),
                            buf.readVarInt()
                    )
            );

    public boolean mission1Completed() {
        return completedMissions >= 1;
    }

    public String rankName() {
        if (completedMissions >= 2) return "Mestre";
        if (completedMissions >= 1) return "Aprendiz";
        return "Estagiário";
    }

    public ArfMissionData startMission1() {
        if (active || mission1Completed()) return this;
        return new ArfMissionData(1, true, completedMissions, reputation);
    }

    public ArfMissionData completeMission1() {
        if (!active || missionId != 1) return this;
        return new ArfMissionData(0, false, Math.max(1, completedMissions), reputation + 100);
    }
}