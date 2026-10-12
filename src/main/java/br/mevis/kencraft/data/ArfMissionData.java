package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ArfMissionData(
        int missionId,
        boolean active,
        int completedMissions,
        int reputation,
        int trainingStage,
        int rishinCentralKills,
        boolean rishinCovenEntered,
        int covenEntryX,
        int covenEntryY,
        int covenEntryZ,
        boolean mitsoEventTriggered
) {
    public static final ArfMissionData DEFAULT =
            new ArfMissionData(0, false, 0, 0, 1, 0, false, 0, 0, 0, false);

    public static final Codec<ArfMissionData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("missionId", 0).forGetter(ArfMissionData::missionId),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(ArfMissionData::active),
            Codec.INT.optionalFieldOf("completedMissions", 0).forGetter(ArfMissionData::completedMissions),
            Codec.INT.optionalFieldOf("reputation", 0).forGetter(ArfMissionData::reputation),
            Codec.INT.optionalFieldOf("trainingStage", 1).forGetter(ArfMissionData::trainingStage),
            Codec.INT.optionalFieldOf("rishinCentralKills", 0).forGetter(ArfMissionData::rishinCentralKills),
            Codec.BOOL.optionalFieldOf("rishinCovenEntered", false).forGetter(ArfMissionData::rishinCovenEntered),
            Codec.INT.optionalFieldOf("covenEntryX", 0).forGetter(ArfMissionData::covenEntryX),
            Codec.INT.optionalFieldOf("covenEntryY", 0).forGetter(ArfMissionData::covenEntryY),
            Codec.INT.optionalFieldOf("covenEntryZ", 0).forGetter(ArfMissionData::covenEntryZ),
            Codec.BOOL.optionalFieldOf("mitsoEventTriggered", false).forGetter(ArfMissionData::mitsoEventTriggered)
    ).apply(i, ArfMissionData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArfMissionData> STREAM_CODEC =
            StreamCodec.of((buf, value) -> {
                buf.writeVarInt(value.missionId());
                buf.writeBoolean(value.active());
                buf.writeVarInt(value.completedMissions());
                buf.writeVarInt(value.reputation());
                buf.writeVarInt(value.trainingStage());
                buf.writeVarInt(value.rishinCentralKills());
                buf.writeBoolean(value.rishinCovenEntered());
                buf.writeVarInt(value.covenEntryX());
                buf.writeVarInt(value.covenEntryY());
                buf.writeVarInt(value.covenEntryZ());
                buf.writeBoolean(value.mitsoEventTriggered());
            }, buf -> new ArfMissionData(
                    buf.readVarInt(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean()
            ));

    public int stage() {
        return Math.max(1, Math.min(3, trainingStage));
    }

    public String rankName() {
        return switch (stage()) {
            case 2 -> "Aprendiz";
            case 3 -> "Mestre";
            default -> "Estagiário";
        };
    }

    public boolean mission1Completed() { return completedMissions >= 1; }
    public boolean rankUpPending() { return !active && stage() < 3 && (completedMissions == 10 || completedMissions == 20); }
    public boolean allRegularMissionsCompleted() { return completedMissions >= 30; }

    public int nextTaskId() {
        if (rankUpPending()) return completedMissions == 10 ? 11 : 22;
        if (completedMissions < 10) return completedMissions + 1;
        if (completedMissions < 20) return completedMissions + 2;
        return completedMissions + 3;
    }

    public ArfMissionData startNextTask() {
        if (active || allRegularMissionsCompleted()) return this;
        return copy(nextTaskId(), true, completedMissions, reputation, stage(),
                rishinCentralKills, rishinCovenEntered, covenEntryX, covenEntryY, covenEntryZ, mitsoEventTriggered);
    }

    public ArfMissionData completeCurrentTask() {
        if (!active) return this;
        if (missionId == 11 || missionId == 22) {
            return copy(0, false, completedMissions, reputation, Math.min(3, stage() + 1),
                    rishinCentralKills, rishinCovenEntered, covenEntryX, covenEntryY, covenEntryZ, mitsoEventTriggered);
        }
        int completed = Math.min(30, completedMissions + 1);
        int reward = regularMissionReward(completedMissions, reputation);
        return copy(0, false, completed, Math.min(200, reputation + reward), stage(),
                rishinCentralKills, rishinCovenEntered, covenEntryX, covenEntryY, covenEntryZ, mitsoEventTriggered);
    }

    public ArfMissionData withRishinCentralKills(int count) {
        return copy(missionId, active, completedMissions, reputation, trainingStage,
                Math.max(0, count), rishinCovenEntered, covenEntryX, covenEntryY, covenEntryZ, mitsoEventTriggered);
    }

    public ArfMissionData enteredRishinCoven(int x, int y, int z) {
        return copy(missionId, active, completedMissions, reputation, trainingStage,
                rishinCentralKills, true, x, y, z, mitsoEventTriggered);
    }

    public ArfMissionData exitedRishinCoven() {
        return copy(missionId, active, completedMissions, reputation, trainingStage,
                rishinCentralKills, false, covenEntryX, covenEntryY, covenEntryZ, mitsoEventTriggered);
    }

    public ArfMissionData withMitsoEventTriggered(boolean value) {
        return copy(missionId, active, completedMissions, reputation, trainingStage,
                rishinCentralKills, rishinCovenEntered, covenEntryX, covenEntryY, covenEntryZ, value);
    }

    public ArfMissionData clearRishinInvestigation() {
        return copy(missionId, active, completedMissions, reputation, trainingStage, 0, false, 0, 0, 0, false);
    }

    private static ArfMissionData copy(int id, boolean isActive, int completed, int rep, int stage,
                                       int centralKills, boolean entered, int x, int y, int z, boolean eventTriggered) {
        return new ArfMissionData(id, isActive, completed, rep, stage, centralKills, entered, x, y, z, eventTriggered);
    }

    public static int regularMissionReward(int completedBefore, int currentReputation) {
        if (completedBefore == 0) return 100;
        if (currentReputation >= 200) return 0;
        return Math.min(5, 200 - currentReputation);
    }
}
