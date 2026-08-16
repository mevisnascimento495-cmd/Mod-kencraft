package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid=KenCraft.MOD_ID,bus=EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcSpawn {
    private static final double RANK_C_CHANCE=0.03D, RINKA_CHANCE=0.07D, RISHIN_CHANCE=0.05D, AODAI_CHANCE=0.05D, ARF_CHANCE=0.08D, GENERAL_CHANCE=0.01D;
    private static final int RADIUS=4, MAX_NIGHT_RINKA=3, MAX_RANK_C=1, MAX_RISHIN=2, MAX_AODAI=1, MAX_ARF=2, MAX_GENERAL=1;
    private static final double PLAYER_CHECK_RADIUS=128.0D;
    private static volatile boolean structureLocateInProgress;
    private KenCraftNpcSpawn() {}

    public static void setStructureLocateInProgress(boolean active) { structureLocateInProgress = active; }

    @SubscribeEvent public static void onChunkLoad(ChunkEvent.Load event){
        if (structureLocateInProgress) return;
        if(!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        if(!level.dimensionType().natural() || level.getDifficulty().getId()==0) return;
        ChunkPos cp=chunk.getPos();
        if(!hasNearbyPlayer(level, cp)) return;

        // Roll first. Entity searches over a 9x9 chunk area are intentionally
        // avoided unless this chunk actually wins a spawn probability check.
        double roll=ThreadLocalRandom.current().nextDouble();
        if(roll<AODAI_CHANCE){
            int aodai=level.getEntitiesOfClass(AodaiEntity.class,nearbyBounds(level,cp),e->true).size();
            if(aodai<MAX_AODAI && local(level,cp,AodaiEntity.class).isEmpty()){spawn(level,cp,KenCraftEntities.AODAI.get());return;}
        }

        boolean night=isNight(level);
        if(night){
            if(roll<RANK_C_CHANCE){
                int r=level.getEntitiesOfClass(RinkaEntity.class,nearbyBounds(level,cp),e->true).size();
                int c=level.getEntitiesOfClass(RankCRinkaEntity.class,nearbyBounds(level,cp),e->true).size();
                if(c<MAX_RANK_C && r+c<MAX_NIGHT_RINKA && local(level,cp,RankCRinkaEntity.class).isEmpty()){spawn(level,cp,KenCraftEntities.RANK_C_RINKA.get());return;}
            }
            if(roll<RANK_C_CHANCE+RISHIN_CHANCE){
                int rs=level.getEntitiesOfClass(RishinEntity.class,nearbyBounds(level,cp),e->true).size();
                if(rs<MAX_RISHIN && local(level,cp,RishinEntity.class).isEmpty()){spawn(level,cp,KenCraftEntities.RISHIN.get());return;}
            }
            if(roll<RANK_C_CHANCE+RISHIN_CHANCE+RINKA_CHANCE){
                int r=level.getEntitiesOfClass(RinkaEntity.class,nearbyBounds(level,cp),e->true).size();
                int c=level.getEntitiesOfClass(RankCRinkaEntity.class,nearbyBounds(level,cp),e->true).size();
                if(r+c<MAX_NIGHT_RINKA && local(level,cp,RinkaEntity.class).isEmpty()) spawn(level,cp,KenCraftEntities.RINKA.get());
            }
            return;
        }

        for(Entity e:local(level,cp,RinkaEntity.class)) e.discard();
        for(Entity e:local(level,cp,RankCRinkaEntity.class)) e.discard();
        if(roll<GENERAL_CHANCE){
            int g=level.getEntitiesOfClass(ArfGeneralEntity.class,nearbyBounds(level,cp),e->true).size();
            if(g<MAX_GENERAL){spawn(level,cp,KenCraftEntities.ARF_GENERAL.get());return;}
        }
        if(roll<GENERAL_CHANCE+ARF_CHANCE){
            int ai=level.getEntitiesOfClass(ArfInvestigatorEntity.class,nearbyBounds(level,cp),e->true).size();
            if(ai<MAX_ARF && local(level,cp,ArfInvestigatorEntity.class).isEmpty()) spawn(level,cp,KenCraftEntities.ARF_INVESTIGATOR.get());
        }
    }

    private static boolean hasNearbyPlayer(ServerLevel level, ChunkPos cp){
        double cx=cp.getMiddleBlockX()+0.5D, cz=cp.getMiddleBlockZ()+0.5D;
        double max=PLAYER_CHECK_RADIUS*PLAYER_CHECK_RADIUS;
        for(var player:level.players()){
            double dx=player.getX()-cx, dz=player.getZ()-cz;
            if(dx*dx+dz*dz<=max) return true;
        }
        return false;
    }

    private static boolean isNight(ServerLevel l){long t=l.getDayTime()%24000L;return t>=13000L&&t<23000L;}
    private static <T extends Entity> List<T> local(ServerLevel l,ChunkPos cp,Class<T> c){return l.getEntitiesOfClass(c,chunkBounds(l,cp),e->true);}
    private static void spawn(ServerLevel l,ChunkPos cp,net.minecraft.world.entity.EntityType<?> type){BlockPos p=find(l,cp);if(p==null)return;Entity e=type.create(l);if(e==null)return;e.moveTo(p,ThreadLocalRandom.current().nextFloat()*360F,0);if(e instanceof net.minecraft.world.entity.Mob m)m.finalizeSpawn(l,l.getCurrentDifficultyAt(p),MobSpawnType.NATURAL,null);l.addFreshEntity(e);}
    private static net.minecraft.world.phys.AABB chunkBounds(ServerLevel l,ChunkPos p){return new net.minecraft.world.phys.AABB(p.getMinBlockX(),l.getMinBuildHeight(),p.getMinBlockZ(),p.getMaxBlockX()+1,l.getMaxBuildHeight(),p.getMaxBlockZ()+1);}
    private static net.minecraft.world.phys.AABB nearbyBounds(ServerLevel l,ChunkPos c){int x=(c.x-RADIUS)<<4,z=(c.z-RADIUS)<<4,X=((c.x+RADIUS+1)<<4),Z=((c.z+RADIUS+1)<<4);return new net.minecraft.world.phys.AABB(x,l.getMinBuildHeight(),z,X,l.getMaxBuildHeight(),Z);}
    private static BlockPos find(ServerLevel l,ChunkPos cp){for(int i=0;i<8;i++){int x=cp.getMinBlockX()+ThreadLocalRandom.current().nextInt(16),z=cp.getMinBlockZ()+ThreadLocalRandom.current().nextInt(16),y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos p=new BlockPos(x,y,z);if(l.getBlockState(p.below()).isSolid()&&l.getBlockState(p).isAir())return p;}return null;}
}
