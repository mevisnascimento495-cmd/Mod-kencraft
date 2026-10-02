package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TalentData(String talent, int strengthTier, int activeTicks) {
    public static final TalentData DEFAULT = new TalentData("NONE", 0, 0);
    public static final Codec<TalentData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("talent", "NONE").forGetter(TalentData::talent),
            Codec.INT.optionalFieldOf("strengthTier", 0).forGetter(TalentData::strengthTier),
            Codec.INT.optionalFieldOf("activeTicks", 0).forGetter(TalentData::activeTicks)
    ).apply(i, TalentData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TalentData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public String talent() { return talent == null ? "NONE" : talent; }
    public boolean hasTalent() { return !"NONE".equals(talent()); }
    public TalentData withTalent(String value) { return new TalentData(value == null ? "NONE" : value, strengthTier, activeTicks); }
    public TalentData withStrengthTier(int tier) { return new TalentData(talent(), Math.max(0, Math.min(5, tier)), activeTicks); }
    public TalentData withActiveTicks(int ticks) { return new TalentData(talent(), strengthTier, Math.max(0, ticks)); }
}
