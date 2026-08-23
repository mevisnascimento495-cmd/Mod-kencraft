package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Persistent story progress for the KenCraft narrative. */
public record StoryProgress(
        int stage,
        String onokiPath,
        int onokiRankCKills,
        int onokiJinsuikakuRankC,
        int onokiRishinRinkaKills,
        int onokiArfGeneralKills,
        boolean onokiChatOpen,
        boolean onokiAodaiHeartReady,
        boolean onokiAkioHeartReady,
        boolean onokiMissionReady
) {
    public static final StoryProgress DEFAULT = new StoryProgress(0, "NONE", 0, 0, 0, 0, false, false, false, false);

    public static final Codec<StoryProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("stage", 0).forGetter(StoryProgress::stage),
            Codec.STRING.optionalFieldOf("onokiPath", "NONE").forGetter(StoryProgress::onokiPath),
            Codec.INT.optionalFieldOf("onokiRankCKills", 0).forGetter(StoryProgress::onokiRankCKills),
            Codec.INT.optionalFieldOf("onokiJinsuikakuRankC", 0).forGetter(StoryProgress::onokiJinsuikakuRankC),
            Codec.INT.optionalFieldOf("onokiRishinRinkaKills", 0).forGetter(StoryProgress::onokiRishinRinkaKills),
            Codec.INT.optionalFieldOf("onokiArfGeneralKills", 0).forGetter(StoryProgress::onokiArfGeneralKills),
            Codec.BOOL.optionalFieldOf("onokiChatOpen", false).forGetter(StoryProgress::onokiChatOpen),
            Codec.BOOL.optionalFieldOf("onokiAodaiHeartReady", false).forGetter(StoryProgress::onokiAodaiHeartReady),
            Codec.BOOL.optionalFieldOf("onokiAkioHeartReady", false).forGetter(StoryProgress::onokiAkioHeartReady),
            Codec.BOOL.optionalFieldOf("onokiMissionReady", false).forGetter(StoryProgress::onokiMissionReady)
    ).apply(i, StoryProgress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StoryProgress> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.stage());
                buf.writeUtf(value.onokiPath(), 16);
                buf.writeVarInt(value.onokiRankCKills());
                buf.writeVarInt(value.onokiJinsuikakuRankC());
                buf.writeVarInt(value.onokiRishinRinkaKills());
                buf.writeVarInt(value.onokiArfGeneralKills());
                buf.writeBoolean(value.onokiChatOpen());
                buf.writeBoolean(value.onokiAodaiHeartReady());
                buf.writeBoolean(value.onokiAkioHeartReady());
                buf.writeBoolean(value.onokiMissionReady());
            },
            buf -> new StoryProgress(
                    buf.readVarInt(), buf.readUtf(16), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                    buf.readBoolean(), buf.readBoolean()
            )
    );

    public StoryProgress withStage(int nextStage) {
        return new StoryProgress(Math.max(0, nextStage), onokiPath, onokiRankCKills, onokiJinsuikakuRankC,
                onokiRishinRinkaKills, onokiArfGeneralKills, onokiChatOpen, onokiAodaiHeartReady,
                onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress startOnokiPath(String path) {
        return new StoryProgress(stage, path, 0, 0, 0, 0, false, false, false, false);
    }

    public StoryProgress withOnokiChatOpen(boolean open) {
        return new StoryProgress(stage, onokiPath, onokiRankCKills, onokiJinsuikakuRankC, onokiRishinRinkaKills,
                onokiArfGeneralKills, open, onokiAodaiHeartReady, onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress withOnokiRankCKills(int value) {
        return copy(value, onokiJinsuikakuRankC, onokiRishinRinkaKills, onokiArfGeneralKills,
                onokiChatOpen, onokiAodaiHeartReady, onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress withOnokiJinsuikakuRankC(int value) {
        return copy(onokiRankCKills, value, onokiRishinRinkaKills, onokiArfGeneralKills,
                onokiChatOpen, onokiAodaiHeartReady, onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress withOnokiRishinRinkaKills(int value) {
        return copy(onokiRankCKills, onokiJinsuikakuRankC, value, onokiArfGeneralKills,
                onokiChatOpen, onokiAodaiHeartReady, onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress withOnokiArfGeneralKills(int value) {
        return copy(onokiRankCKills, onokiJinsuikakuRankC, onokiRishinRinkaKills, value,
                onokiChatOpen, onokiAodaiHeartReady, onokiAkioHeartReady, onokiMissionReady);
    }

    public StoryProgress withOnokiHearts(boolean aodai, boolean akio) {
        return copy(onokiRankCKills, onokiJinsuikakuRankC, onokiRishinRinkaKills, onokiArfGeneralKills,
                onokiChatOpen, aodai, akio, onokiMissionReady);
    }

    public StoryProgress withOnokiMissionReady(boolean ready) {
        return copy(onokiRankCKills, onokiJinsuikakuRankC, onokiRishinRinkaKills, onokiArfGeneralKills,
                onokiChatOpen, onokiAodaiHeartReady, onokiAkioHeartReady, ready);
    }

    private StoryProgress copy(int rankC, int jinsuikaku, int rishinRinka, int generals, boolean chatOpen,
                               boolean aodaiHeart, boolean akioHeart, boolean ready) {
        return new StoryProgress(stage, onokiPath, Math.max(0, rankC), Math.max(0, jinsuikaku),
                Math.max(0, rishinRinka), Math.max(0, generals), chatOpen, aodaiHeart, akioHeart, ready);
    }
}
