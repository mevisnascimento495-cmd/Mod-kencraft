package br.mevis.kencraft;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.entity.*;
import br.mevis.kencraft.event.ChatSelectionHandler;
import br.mevis.kencraft.event.PlayerLoginHandler;
import br.mevis.kencraft.item.KenCraftItems;
import br.mevis.kencraft.world.MinamoriStructureGenerator;
import com.mojang.brigadier.Command;
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
    public static final String MOD_ID = "kencraft";
    public KenCraft(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        KenCraftEntities.ENTITY_TYPES.register(modEventBus);
        KenCraftItems.ARMOR_MATERIALS.register(modEventBus);
        KenCraftItems.ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.register(PlayerLoginHandler.class);
        NeoForge.EVENT_BUS.register(ChatSelectionHandler.class);
        NeoForge.EVENT_BUS.register(MinamoriStructureGenerator.class);
    }
    @EventBusSubscriber(modid=MOD_ID,bus=EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent public static void createAttributes(EntityAttributeCreationEvent event) {
            event.put(KenCraftEntities.RINKA.get(), RinkaEntity.createAttributes().build());
            event.put(KenCraftEntities.RANK_C_RINKA.get(), RankCRinkaEntity.createAttributes().build());
            event.put(KenCraftEntities.RISHIN.get(), RishinEntity.createAttributes().build());
            event.put(KenCraftEntities.AODAI.get(), AodaiEntity.createAttributes().build());
            event.put(KenCraftEntities.ARF_INVESTIGATOR.get(), ArfInvestigatorEntity.createAttributes().build());
            event.put(KenCraftEntities.ARF_GENERAL.get(), ArfGeneralEntity.createAttributes().build());
            event.put(KenCraftEntities.SHIN_HOMARE.get(), KenCraftEntities.HomareEntity.createAttributes().build());
            event.put(KenCraftEntities.KAORI_HOMARE.get(), KenCraftEntities.HomareEntity.createAttributes().build());
        }
        @SubscribeEvent public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
            if (CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())) {
                event.accept(KenCraftItems.JINSUIKAKU.get());
                event.accept(KenCraftItems.JINSUIKAKU_RANK_C.get());
            }
            if (CreativeModeTabs.SPAWN_EGGS.equals(event.getTabKey())) {
                event.accept(KenCraftItems.RINKA_SPAWN_EGG.get());
                event.accept(KenCraftItems.RANK_C_RINKA_SPAWN_EGG.get());
                event.accept(KenCraftItems.RISHIN_SPAWN_EGG.get());
                event.accept(KenCraftItems.AODAI_SPAWN_EGG.get());
                event.accept(KenCraftItems.ARF_INVESTIGATOR_SPAWN_EGG.get());
                event.accept(KenCraftItems.ARF_GENERAL_SPAWN_EGG.get());
                event.accept(KenCraftItems.SHIN_HOMARE_SPAWN_EGG.get());
                event.accept(KenCraftItems.KAORI_HOMARE_SPAWN_EGG.get());
            }
            if (CreativeModeTabs.COMBAT.equals(event.getTabKey())) {
                event.accept(KenCraftItems.ARF_UNIFORM_CHESTPLATE.get());
                event.accept(KenCraftItems.ARF_UNIFORM_LEGGINGS.get());
            }
        }
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static final class GameEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                Commands.literal("kencraft")
                    .then(Commands.literal("locate")
                        .then(Commands.literal("minamori")
                            .executes(ctx -> locateMinamori(ctx.getSource().getPlayerOrException())))
                    )
            );
        }

        private static int locateMinamori(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            int centerChunkX = player.chunkPosition().x;
            int centerChunkZ = player.chunkPosition().z;
            int maxRadius = 32;

            for (int radius = 0; radius <= maxRadius; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    int[] zs = radius == 0 ? new int[]{0} : new int[]{-radius, radius};
                    for (int dz : zs) {
                        if (tryMinamoriChunk(level, centerChunkX + dx, centerChunkZ + dz, player)) return Command.SINGLE_SUCCESS;
                    }
                }
                for (int dz = -radius + 1; dz <= radius - 1; dz++) {
                    int[] xs = {-radius, radius};
                    for (int dx : xs) {
                        if (tryMinamoriChunk(level, centerChunkX + dx, centerChunkZ + dz, player)) return Command.SINGLE_SUCCESS;
                    }
                }
            }

            player.sendSystemMessage(Component.literal("§cNenhuma estrutura Minamori foi encontrada em um raio de 512 blocos."));
            return 0;
        }

        private static boolean tryMinamoriChunk(ServerLevel level, int chunkX, int chunkZ, ServerPlayer player) {
            long hash = mix(level.getSeed() ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L));
            if (Math.floorMod(hash, 96) != 0) return false;

            level.getChunk(chunkX, chunkZ);
            int centerX = chunkX * 16 + 8;
            int centerZ = chunkZ * 16 + 8;
            int groundY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
            BlockPos marker = new BlockPos(centerX, groundY, centerZ);
            if (!level.getBlockState(marker).is(net.minecraft.world.level.block.Blocks.LODESTONE)) return false;

            player.teleportTo(level, centerX + 0.5D, groundY + 2.0D, centerZ + 0.5D, player.getYRot(), player.getXRot());
            player.sendSystemMessage(Component.literal("§aTeletransportado para a estrutura Minamori."));
            return true;
        }

        private static long mix(long value) {
            value ^= value >>> 33; value *= 0xff51afd7ed558ccdL; value ^= value >>> 33; value *= 0xc4ceb9fe1a85ec53L; value ^= value >>> 33; return value;
        }
    }
}
