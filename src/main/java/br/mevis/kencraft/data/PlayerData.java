package br.mevis.kencraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persistent KenCraft data attached to each player. */
public record PlayerData(
        Race race, int jio, int maxJio, int strength, int defense, int intelligence, int speed,
        int genetics, int perception, int spiritualDevelopment, int life, int mentalXp, int physicalXp,
        int arfMissionKills, int arfClass, String rinkaClass, int jinsuikakuConsumed, int jinsuikakuRankCConsumed,
        String kikanType, String jioTechnique, int jioAbilitySlot
) {
    public static final int MIN_STATUS = 1;
    public static final int MAX_STATUS = 20;
    private record ProgressionData(String rinkaClass,int jinsuikakuConsumed,int jinsuikakuRankCConsumed,String kikanType,String jioTechnique,int jioAbilitySlot){
        private static final ProgressionData DEFAULT=new ProgressionData("NONE",0,0,"NONE","NONE",0);
        private static final Codec<ProgressionData> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.STRING.optionalFieldOf("rinkaClass","NONE").forGetter(ProgressionData::rinkaClass),
                Codec.INT.optionalFieldOf("jinsuikakuConsumed",0).forGetter(ProgressionData::jinsuikakuConsumed),
                Codec.INT.optionalFieldOf("jinsuikakuRankCConsumed",0).forGetter(ProgressionData::jinsuikakuRankCConsumed),
                Codec.STRING.optionalFieldOf("kikanType","NONE").forGetter(ProgressionData::kikanType),
                Codec.STRING.optionalFieldOf("jioTechnique","NONE").forGetter(ProgressionData::jioTechnique),
                Codec.INT.optionalFieldOf("jioAbilitySlot",0).forGetter(ProgressionData::jioAbilitySlot)
        ).apply(i,ProgressionData::new));
    }
    public static final PlayerData DEFAULT=new PlayerData(Race.NONE,0,0,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,0,0,-1,0,"NONE",0,0,"NONE","NONE",0);
    private static final Codec<Race> RACE_CODEC=Codec.STRING.xmap(Race::valueOf,Race::name);
    public static final Codec<PlayerData> CODEC=RecordCodecBuilder.create(i->i.group(
            RACE_CODEC.fieldOf("race").forGetter(PlayerData::race),
            Codec.INT.fieldOf("jio").forGetter(PlayerData::jio),Codec.INT.fieldOf("maxJio").forGetter(PlayerData::maxJio),
            Codec.INT.optionalFieldOf("strength",MIN_STATUS).forGetter(PlayerData::strength),Codec.INT.optionalFieldOf("defense",MIN_STATUS).forGetter(PlayerData::defense),
            Codec.INT.optionalFieldOf("intelligence",MIN_STATUS).forGetter(PlayerData::intelligence),Codec.INT.optionalFieldOf("speed",MIN_STATUS).forGetter(PlayerData::speed),
            Codec.INT.optionalFieldOf("genetics",MIN_STATUS).forGetter(PlayerData::genetics),Codec.INT.optionalFieldOf("perception",MIN_STATUS).forGetter(PlayerData::perception),
            Codec.INT.optionalFieldOf("spiritualDevelopment",MIN_STATUS).forGetter(PlayerData::spiritualDevelopment),Codec.INT.optionalFieldOf("life",MIN_STATUS).forGetter(PlayerData::life),
            Codec.INT.optionalFieldOf("mentalXp",0).forGetter(PlayerData::mentalXp),Codec.INT.optionalFieldOf("physicalXp",0).forGetter(PlayerData::physicalXp),
            Codec.INT.optionalFieldOf("arfMissionKills",-1).forGetter(PlayerData::arfMissionKills),Codec.INT.optionalFieldOf("arfClass",0).forGetter(PlayerData::arfClass),
            ProgressionData.CODEC.optionalFieldOf("progression",ProgressionData.DEFAULT).forGetter(d->new ProgressionData(d.rinkaClass(),d.jinsuikakuConsumed(),d.jinsuikakuRankCConsumed(),d.kikanType(),d.jioTechnique(),d.jioAbilitySlot()))
    ).apply(i,(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritual,life,mental,physical,mission,arfClass,p)->new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritual,life,mental,physical,mission,arfClass,p.rinkaClass(),p.jinsuikakuConsumed(),p.jinsuikakuRankCConsumed(),p.kikanType(),normalizeTechnique(p.jioTechnique()),p.jioAbilitySlot())));
    public static final StreamCodec<RegistryFriendlyByteBuf,PlayerData> STREAM_CODEC=ByteBufCodecs.fromCodecWithRegistries(CODEC);

    /** Only the three current Jio techniques are valid. */
    public static String normalizeTechnique(String t){
        if (t == null) return "NONE";
        return switch (t.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "seishin dan" -> "Seishin dan";
            case "hakai satsu totetsu: seimei kui" -> "Hakai satsu Totetsu: Seimei kui";
            case "kata kyoka" -> "Kata kyoka";
            default -> "NONE";
        };
    }

    /** Always expose the canonical value to every caller, even when an old save still contains legacy text. */
    @Override
    public String jioTechnique() {
        return normalizeTechnique(jioTechnique);
    }

    public boolean hasRace(){return race!=Race.NONE;}
    public static int spentPoints(int level){return Math.max(0,Math.min(MAX_STATUS,level)-MIN_STATUS);}
    public double jioMultiplier(){return 1.0D+spentPoints(spiritualDevelopment)*0.03D;}
    public int calculatedHumanMaxJio(){return race==Race.HUMAN?(int)Math.round(100.0D*jioMultiplier()):maxJio;}
    public static PlayerData forRinka(){return new PlayerData(Race.RINKA,0,0,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,0,0,-1,0,"NONE",0,0,"NONE","NONE",0);}
    public static PlayerData forHuman(){return new PlayerData(Race.HUMAN,100,100,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,MIN_STATUS,0,0,-1,0,"NONE",0,0,"NONE","NONE",0);}
    public PlayerData withStatus(String a,int v){v=Math.max(MIN_STATUS,Math.min(MAX_STATUS,v));return switch(a){case "strength"->copy(v,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life);case "defense"->copy(strength,v,intelligence,speed,genetics,perception,spiritualDevelopment,life);case "intelligence"->copy(strength,defense,v,speed,genetics,perception,spiritualDevelopment,life);case "speed"->copy(strength,defense,intelligence,v,genetics,perception,spiritualDevelopment,life);case "genetics"->copy(strength,defense,intelligence,speed,v,perception,spiritualDevelopment,life);case "perception"->copy(strength,defense,intelligence,speed,genetics,v,spiritualDevelopment,life);case "spiritual"->copy(strength,defense,intelligence,speed,genetics,perception,v,life);case "life"->copy(strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,v);default->this;};}
    private PlayerData copy(int s,int d,int in,int sp,int g,int p,int sd,int l){return new PlayerData(race,jio,maxJio,s,d,in,sp,g,p,sd,l,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withXp(int m,int p){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,Math.max(0,m),Math.max(0,p),arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withJio(int c,int m){return new PlayerData(race,Math.max(0,c),Math.max(0,m),strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withArfMissionKills(int k){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,k,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withArfClass(int c){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,Math.max(0,c),rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withRinkaClass(String c){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,c==null?"NONE":c,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withJinsuikakuConsumed(int a){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,Math.max(0,a),jinsuikakuRankCConsumed,kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withJinsuikakuRankCConsumed(int a){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,Math.max(0,a),kikanType,jioTechnique,jioAbilitySlot);}
    public PlayerData withKikanType(String t){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,t==null?"NONE":t,jioTechnique,jioAbilitySlot);}
    public PlayerData withJioTechnique(String t){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,normalizeTechnique(t),0);}
    public PlayerData withJioAbilitySlot(int s){return new PlayerData(race,jio,maxJio,strength,defense,intelligence,speed,genetics,perception,spiritualDevelopment,life,mentalXp,physicalXp,arfMissionKills,arfClass,rinkaClass,jinsuikakuConsumed,jinsuikakuRankCConsumed,kikanType,jioTechnique,Math.max(0,Math.min(2,s)));}
    public boolean canUseKikan(){return race==Race.RINKA&&(rinkaClass.equals("C")||rinkaClass.equals("B")||rinkaClass.equals("A")||rinkaClass.equals("S"));}
}
