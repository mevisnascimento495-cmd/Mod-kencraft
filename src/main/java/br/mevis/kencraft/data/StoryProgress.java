package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
        boolean onokiMissionReady,
        int onokiEvolutionTicks,
        boolean onokiEvolutionComplete,
        boolean onokiShopOpen
) {
    public static final StoryProgress DEFAULT = new StoryProgress(0,"NONE",0,0,0,0,false,false,false,false,0,false,false);
    public static final Codec<StoryProgress> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.INT.optionalFieldOf("stage",0).forGetter(StoryProgress::stage),
            Codec.STRING.optionalFieldOf("onokiPath","NONE").forGetter(StoryProgress::onokiPath),
            Codec.INT.optionalFieldOf("onokiRankCKills",0).forGetter(StoryProgress::onokiRankCKills),
            Codec.INT.optionalFieldOf("onokiJinsuikakuRankC",0).forGetter(StoryProgress::onokiJinsuikakuRankC),
            Codec.INT.optionalFieldOf("onokiRishinRinkaKills",0).forGetter(StoryProgress::onokiRishinRinkaKills),
            Codec.INT.optionalFieldOf("onokiArfGeneralKills",0).forGetter(StoryProgress::onokiArfGeneralKills),
            Codec.BOOL.optionalFieldOf("onokiChatOpen",false).forGetter(StoryProgress::onokiChatOpen),
            Codec.BOOL.optionalFieldOf("onokiAodaiHeartReady",false).forGetter(StoryProgress::onokiAodaiHeartReady),
            Codec.BOOL.optionalFieldOf("onokiAkioHeartReady",false).forGetter(StoryProgress::onokiAkioHeartReady),
            Codec.BOOL.optionalFieldOf("onokiMissionReady",false).forGetter(StoryProgress::onokiMissionReady),
            Codec.INT.optionalFieldOf("onokiEvolutionTicks",0).forGetter(StoryProgress::onokiEvolutionTicks),
            Codec.BOOL.optionalFieldOf("onokiEvolutionComplete",false).forGetter(StoryProgress::onokiEvolutionComplete),
            Codec.BOOL.optionalFieldOf("onokiShopOpen",false).forGetter(StoryProgress::onokiShopOpen)
    ).apply(i,StoryProgress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,StoryProgress> STREAM_CODEC=StreamCodec.of((b,v)->{
        b.writeVarInt(v.stage()); b.writeUtf(v.onokiPath(),16); b.writeVarInt(v.onokiRankCKills()); b.writeVarInt(v.onokiJinsuikakuRankC());
        b.writeVarInt(v.onokiRishinRinkaKills()); b.writeVarInt(v.onokiArfGeneralKills()); b.writeBoolean(v.onokiChatOpen());
        b.writeBoolean(v.onokiAodaiHeartReady()); b.writeBoolean(v.onokiAkioHeartReady()); b.writeBoolean(v.onokiMissionReady());
        b.writeVarInt(v.onokiEvolutionTicks()); b.writeBoolean(v.onokiEvolutionComplete()); b.writeBoolean(v.onokiShopOpen());
    },b->new StoryProgress(b.readVarInt(),b.readUtf(16),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readBoolean(),b.readBoolean()));

    public StoryProgress withStage(int v){return copy(Math.max(0,v),onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress startOnokiPath(String p){return copy(stage,p,0,0,0,0,false,false,false,false,0,false,false);}
    public StoryProgress withOnokiChatOpen(boolean v){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,v,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress withOnokiRankCKills(int v){return copy(stage,onokiPath,Math.max(0,v),onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress withOnokiJinsuikakuRankC(int v){return copy(stage,onokiPath,onokiRankCKills,Math.max(0,v),onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress withOnokiRishinRinkaKills(int v){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,Math.max(0,v),onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress withOnokiArfGeneralKills(int v){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,Math.max(0,v),onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress withOnokiHearts(boolean a,boolean b){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,a,b,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress startEvolution(){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,false,onokiAodaiHeartReady,onokiAkioHeartReady,true,400,false,false);}
    public StoryProgress tickEvolution(){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,Math.max(0,onokiEvolutionTicks-1),onokiEvolutionComplete,onokiShopOpen);}
    public StoryProgress finishEvolution(){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,false,onokiAodaiHeartReady,onokiAkioHeartReady,true,0,true,false);}
    public StoryProgress withShopOpen(boolean v){return copy(stage,onokiPath,onokiRankCKills,onokiJinsuikakuRankC,onokiRishinRinkaKills,onokiArfGeneralKills,onokiChatOpen,onokiAodaiHeartReady,onokiAkioHeartReady,onokiMissionReady,onokiEvolutionTicks,onokiEvolutionComplete,v);}
    private StoryProgress copy(int a,String b,int c,int d,int e,int f,boolean g,boolean h,boolean i,boolean j,int k,boolean l,boolean m){return new StoryProgress(a,b,c,d,e,f,g,h,i,j,k,l,m);}
}
