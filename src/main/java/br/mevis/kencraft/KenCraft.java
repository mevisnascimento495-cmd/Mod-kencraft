package br.mevis.kencraft;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.entity.*;
import br.mevis.kencraft.event.ChatSelectionHandler;
import br.mevis.kencraft.event.KenCraftEffects;
import br.mevis.kencraft.event.KenCraftNpcSpawn;
import br.mevis.kencraft.event.PlayerLoginHandler;
import br.mevis.kencraft.item.KenCraftItems;
import br.mevis.kencraft.menu.KenCraftMenus;
import br.mevis.kencraft.world.AbandonedHospitalStructureGenerator;
import br.mevis.kencraft.world.ArfBaseStructureGenerator;
import br.mevis.kencraft.world.MinamoriStructureGenerator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(KenCraft.MOD_ID)
public class KenCraft {
 public static final String MOD_ID="kencraft";
 public KenCraft(IEventBus modEventBus){
  ModAttachments.ATTACHMENT_TYPES.register(modEventBus);KenCraftEntities.ENTITY_TYPES.register(modEventBus);KenCraftItems.ARMOR_MATERIALS.register(modEventBus);KenCraftItems.ITEMS.register(modEventBus);KenCraftEffects.EFFECTS.register(modEventBus);KenCraftMenus.MENUS.register(modEventBus);
  NeoForge.EVENT_BUS.register(PlayerLoginHandler.class);NeoForge.EVENT_BUS.register(ChatSelectionHandler.class);NeoForge.EVENT_BUS.register(GameEvents.class);
 }
 @EventBusSubscriber(modid=MOD_ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class ModEvents{
  @SubscribeEvent public static void createAttributes(EntityAttributeCreationEvent event){
   event.put(KenCraftEntities.RINKA.get(),RinkaEntity.createAttributes().build());
   event.put(KenCraftEntities.RANK_C_RINKA.get(),RankCRinkaEntity.createAttributes().build());
   event.put(KenCraftEntities.RINKA_HUNGRY.get(),RinkaHungryEntity.createAttributes().build());
   event.put(KenCraftEntities.RISHIN.get(),RishinEntity.createAttributes().build());
   event.put(KenCraftEntities.AODAI.get(),AodaiEntity.createAttributes().build());
   event.put(KenCraftEntities.ARF_INVESTIGATOR.get(),ArfInvestigatorEntity.createAttributes().build());
   event.put(KenCraftEntities.ARF_GENERAL.get(),ArfGeneralEntity.createAttributes().build());
   event.put(KenCraftEntities.AKIO_GINSHO.get(),ArfGeneralEntity.createAttributes().build());
   event.put(KenCraftEntities.ONOKI.get(),OnokiEntity.createAttributes().build());
   event.put(KenCraftEntities.SHIN_HOMARE.get(),KenCraftEntities.HomareEntity.createAttributes().build());
   event.put(KenCraftEntities.KAORI_HOMARE.get(),KenCraftEntities.HomareEntity.createAttributes().build());
   event.put(KenCraftEntities.INTERIOR_SPIRIT.get(),InteriorSpiritEntity.createAttributes().build());
   event.put(KenCraftEntities.ARTIFACT_SHOP.get(),ArtifactShopEntity.createAttributes().build());
  }
  @SubscribeEvent public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event){
   if(CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())){event.accept(KenCraftItems.JINSUIKAKU.get());event.accept(KenCraftItems.JINSUIKAKU_RANK_C.get());}
   if(CreativeModeTabs.SPAWN_EGGS.equals(event.getTabKey())){
    event.accept(KenCraftItems.RINKA_SPAWN_EGG.get());event.accept(KenCraftItems.RANK_C_RINKA_SPAWN_EGG.get());event.accept(KenCraftItems.RISHIN_SPAWN_EGG.get());event.accept(KenCraftItems.AODAI_SPAWN_EGG.get());event.accept(KenCraftItems.ARF_INVESTIGATOR_SPAWN_EGG.get());event.accept(KenCraftItems.ARF_GENERAL_SPAWN_EGG.get());event.accept(KenCraftItems.SHIN_HOMARE_SPAWN_EGG.get());event.accept(KenCraftItems.KAORI_HOMARE_SPAWN_EGG.get());event.accept(KenCraftItems.AKIO_GINSHO_SPAWN_EGG.get());event.accept(KenCraftItems.ONOKI_SPAWN_EGG.get());event.accept(KenCraftItems.RINKA_HUNGRY_SPAWN_EGG.get());event.accept(KenCraftItems.INTERIOR_SPIRIT_SPAWN_EGG.get());event.accept(KenCraftItems.ARTIFACT_SHOP_SPAWN_EGG.get());
   }
   if(CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())){event.accept(KenCraftItems.SPIRIT_NECKLACE.get());event.accept(KenCraftItems.VITALITY_RING.get());event.accept(KenCraftItems.PERCEPTION_AMULET.get());event.accept(KenCraftItems.PARADISE_FRAGMENT.get());}
   if(CreativeModeTabs.COMBAT.equals(event.getTabKey())){event.accept(KenCraftItems.ARF_UNIFORM_CHESTPLATE.get());event.accept(KenCraftItems.ARF_UNIFORM_LEGGINGS.get());}
  }
 }
 public static final class GameEvents{
  private static final int MAX_SEARCH_RADIUS_CHUNKS=64,MAX_CANDIDATE_CHUNKS_TO_LOAD=16;
  @SubscribeEvent public static void registerCommands(RegisterCommandsEvent event){
   event.getDispatcher().register(Commands.literal("kencraft")
    .then(Commands.literal("locate")
     .then(Commands.literal("minamori").executes(ctx->locateMinamori(ctx.getSource().getPlayerOrException())))
     .then(Commands.literal("hospital").executes(ctx->locateHospital(ctx.getSource().getPlayerOrException())))
     .then(Commands.literal("arf").executes(ctx->locateArfBase(ctx.getSource().getPlayerOrException()))))
    .then(Commands.literal("spawn").requires(s->s.hasPermission(2)).then(Commands.argument("entity",StringArgumentType.word()).suggests((c,b)->{for(var key:net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.keySet())if(key.getNamespace().equals(MOD_ID))b.suggest(key.getPath());return b.buildFuture();}).executes(ctx->spawnEntity(ctx.getSource().getPlayerOrException(),StringArgumentType.getString(ctx,"entity"))))));
  }
  private static int spawnEntity(ServerPlayer player,String id){
   var key=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID,id);var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
   if(type==null){player.sendSystemMessage(Component.literal("§cNPC/entidade não encontrada: "+id));return 0;}
   var entity=type.create(player.serverLevel());if(entity==null){player.sendSystemMessage(Component.literal("§cNão foi possível criar: "+id));return 0;}
   entity.moveTo(player.getX(),player.getY(),player.getZ(),player.getYRot(),player.getXRot());player.serverLevel().addFreshEntity(entity);player.sendSystemMessage(Component.literal("§aCriado: "+id));return Command.SINGLE_SUCCESS;
  }
  private static int locateArfBase(ServerPlayer player){ServerLevel level=player.serverLevel();int cx=player.chunkPosition().x,cz=player.chunkPosition().z;for(int r=0;r<=64;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){int px=(cx+dx)*16+8,pz=(cz+dz)*16+8;if(ArfBaseStructureGenerator.generateAt(level,px,pz)){int y=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz)-1;player.teleportTo(level,px+.5D,y+3D,pz+.5D,player.getYRot(),player.getXRot());player.sendSystemMessage(Component.literal("§aTeletransportado para a base da ARF."));return Command.SINGLE_SUCCESS;}}player.sendSystemMessage(Component.literal("§cNão foi possível encontrar uma base da ARF próxima."));return 0;}
  private static int locateHospital(ServerPlayer player){ServerLevel level=player.serverLevel();int cx=player.chunkPosition().x,cz=player.chunkPosition().z;for(int r=0;r<=64;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){int x=cx+dx,z=cz+dz;if(Math.floorMod(AbandonedHospitalStructureGenerator.hashForChunk(level.getSeed(),x,z),AbandonedHospitalStructureGenerator.CHANCE_DENOMINATOR())!=0)continue;level.getChunk(x,z);int px=x*16+8,pz=z*16+8;int y=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz)-1;if(AbandonedHospitalStructureGenerator.generateAt(level,px,pz)){player.teleportTo(level,px+.5D,y+3D,pz+.5D,player.getYRot(),player.getXRot());player.sendSystemMessage(Component.literal("§aTeletransportado para um hospital abandonado."));return Command.SINGLE_SUCCESS;}}player.sendSystemMessage(Component.literal("§cNão foi possível encontrar um hospital abandonado próximo."));return 0;}
  private static int locateMinamori(ServerPlayer player){ServerLevel level=player.serverLevel();int x=player.chunkPosition().x,z=player.chunkPosition().z,candidates=0;KenCraftNpcSpawn.setStructureLocateInProgress(true);try{for(int r=0;r<=MAX_SEARCH_RADIUS_CHUNKS;r++){for(int dx=-r;dx<=r;dx++){int[] zs=r==0?new int[]{0}:new int[]{-r,r};for(int dz:zs){if(tryMinamoriChunk(level,x+dx,z+dz,player))return Command.SINGLE_SUCCESS;if(MinamoriStructureGenerator.hashForChunk(level.getSeed(),x+dx,z+dz)%MinamoriStructureGenerator.CHANCE_DENOMINATOR()==0&&++candidates>=MAX_CANDIDATE_CHUNKS_TO_LOAD)return 0;}}for(int dz=-r+1;dz<=r-1;dz++)for(int dx:new int[]{-r,r})if(tryMinamoriChunk(level,x+dx,z+dz,player))return Command.SINGLE_SUCCESS;}}finally{KenCraftNpcSpawn.setStructureLocateInProgress(false);}player.sendSystemMessage(Component.literal("§cNão foi possível encontrar uma Minamori num raio de 1024 blocos."));return 0;}
  private static boolean tryMinamoriChunk(ServerLevel level,int chunkX,int chunkZ,ServerPlayer player){long hash=MinamoriStructureGenerator.hashForChunk(level.getSeed(),chunkX,chunkZ);if(Math.floorMod(hash,MinamoriStructureGenerator.CHANCE_DENOMINATOR())!=0)return false;level.getChunk(chunkX,chunkZ);int x=chunkX*16+8,z=chunkZ*16+8,y=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;BlockPos marker=new BlockPos(x,y,z);if(!level.getBlockState(marker).is(net.minecraft.world.level.block.Blocks.LODESTONE)&&!MinamoriStructureGenerator.generateAt(level,x,z))return false;player.teleportTo(level,x+.5D,y+3D,z+.5D,player.getYRot(),player.getXRot());player.sendSystemMessage(Component.literal("§aTeletransportado para a estrutura Minamori."));return true;}
 }
}