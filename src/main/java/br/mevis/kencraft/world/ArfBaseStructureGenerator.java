package br.mevis.kencraft.world;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/** Naturally generated ARF headquarters. Generation is deferred until the footprint is already loaded. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ArfBaseStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 160;
    private static final int WIDTH = 23;
    private static final int DEPTH = 19;
    private static final int HEIGHT = 14;
    private static final int MAX_PENDING = 6;
    private static final int GENERATION_INTERVAL_TICKS = 20;
    private static final Deque<PendingGeneration> PENDING = new ArrayDeque<>();
    private static int generationCooldown;

    private ArfBaseStructureGenerator() {}

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        ChunkAccess chunk = event.getChunk();
        long hash = mix(level.getSeed() ^ ((long) chunk.getPos().x * 341873128712L) ^ ((long) chunk.getPos().z * 132897987541L));
        if (Math.floorMod(hash, CHANCE_DENOMINATOR) != 0) return;
        PendingGeneration candidate = new PendingGeneration(level, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ());
        if (!PENDING.contains(candidate) && PENDING.size() < MAX_PENDING) PENDING.addLast(candidate);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (generationCooldown > 0) {
            generationCooldown--;
            return;
        }
        if (PENDING.isEmpty() || event.getServer().getTickCount() % GENERATION_INTERVAL_TICKS != 0) return;
        PendingGeneration candidate = PENDING.pollFirst();
        if (candidate == null || candidate.level().getServer() != event.getServer()) return;

        for (ServerPlayer player : candidate.level().players()) {
            if (player.isSprinting() && player.distanceToSqr(candidate.centerX() + 0.5D, player.getY(), candidate.centerZ() + 0.5D) < 192.0D * 192.0D) {
                PENDING.addLast(candidate);
                return;
            }
        }
        if (!footprintLoaded(candidate.level(), candidate.centerX(), candidate.centerZ())) {
            PENDING.addLast(candidate);
            return;
        }
        if (generateAt(candidate.level(), candidate.centerX(), candidate.centerZ())) generationCooldown = GENERATION_INTERVAL_TICKS;
    }

    public static boolean generateAt(ServerLevel level, int centerX, int centerZ) {
        if (!footprintLoaded(level, centerX, centerZ)) return false;
        if (!StructurePlacementRules.isFarEnough(level, centerX, centerZ)) return false;
        return placeBase(level, centerX, centerZ);
    }

    private static boolean footprintLoaded(ServerLevel level, int centerX, int centerZ) {
        int minChunkX = Math.floorDiv(centerX - 11, 16);
        int maxChunkX = Math.floorDiv(centerX + 11, 16);
        int minChunkZ = Math.floorDiv(centerZ - 9, 16);
        int maxChunkZ = Math.floorDiv(centerZ + 9, 16);
        for (int cx = minChunkX; cx <= maxChunkX; cx++) for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
            if (chunk == null) return false;
        }
        return true;
    }

    private static boolean placeBase(ServerLevel level, int centerX, int centerZ) {
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        if (groundY < level.getMinBuildHeight() + 3 || groundY > level.getMaxBuildHeight() - HEIGHT - 2) return false;
        if (level.getBlockState(new BlockPos(centerX, groundY, centerZ)).is(Blocks.LODESTONE)) return true;
        if (!validGround(level, centerX, groundY, centerZ)) return false;

        for (int x : new int[]{centerX - 10, centerX + 10}) for (int z : new int[]{centerZ - 8, centerZ + 8}) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            if (Math.abs(y - groundY) > 2 || !validGround(level, x, y, z)) return false;
        }

        BlockPos origin = new BlockPos(centerX - 11, groundY + 1, centerZ - 9);
        buildBase(level, origin);
        StructureDecoration.addEntrance(level, origin, WIDTH);
        spawnAkio(level, origin.offset(11, 1, 14));
        spawn(level, origin.offset(5, 1, 3), KenCraftEntities.ARF_INVESTIGATOR.get());
        spawn(level, origin.offset(17, 1, 3), KenCraftEntities.ARF_INVESTIGATOR.get());
        MerchantStructureSpawner.trySpawn(level, centerX, centerZ, groundY, WIDTH, DEPTH);
        StructureLoot.arf(level, origin.offset(19, 2, 4), origin.offset(19, 2, 6), origin.offset(19, 2, 8));
        return true;
    }

    private static boolean validGround(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return !state.is(Blocks.WATER) && !state.is(Blocks.LAVA) && state.isSolid();



        // REWORK ARF: torre de comando, núcleo blindado e cobertura técnica
        for (int y = 10; y <= 13; y++) {
            for (int x = 5; x <= 17; x++) {
                set(level, o.offset(x, y, 5), Blocks.DEEPSLATE_TILES);
                set(level, o.offset(x, y, 13), Blocks.DEEPSLATE_TILES);
            }
            for (int z = 5; z <= 13; z++) {
                set(level, o.offset(5, y, z), Blocks.DEEPSLATE_TILES);
                set(level, o.offset(17, y, z), Blocks.DEEPSLATE_TILES);
            }
        }
        for (int x = 6; x <= 16; x++) for (int z = 6; z <= 12; z++) {
            if (x == 11 || z == 9) set(level, o.offset(x, 10, z), Blocks.IRON_BLOCK);
        }
        for (int x = 8; x <= 14; x++) for (int y = 11; y <= 12; y++)
            set(level, o.offset(x, y, 9), Blocks.GLASS_PANE);
        for (int z = 6; z <= 12; z += 2) {
            set(level, o.offset(6, 11, z), Blocks.IRON_BARS);
            set(level, o.offset(16, 11, z), Blocks.IRON_BARS);
        }
        for (int x = 4; x <= 18; x++) {
            set(level, o.offset(x, 13, 5), Blocks.IRON_BLOCK);
            set(level, o.offset(x, 13, 13), Blocks.IRON_BLOCK);
        }
        for (int z = 6; z <= 12; z++) {
            set(level, o.offset(4, 13, z), Blocks.IRON_BLOCK);
            set(level, o.offset(18, 13, z), Blocks.IRON_BLOCK);
        }
        set(level, o.offset(11, 11, 9), Blocks.BEACON);
        set(level, o.offset(11, 12, 9), Blocks.BLACK_CONCRETE);

        // Pórtico frontal alto: transforma a entrada em um acesso de quartel fortificado.
        for (int y = 2; y <= 8; y++) {
            set(level, o.offset(8, y, 0), Blocks.IRON_BLOCK);
            set(level, o.offset(15, y, 0), Blocks.IRON_BLOCK);
        }
        for (int x = 8; x <= 15; x++) {
            set(level, o.offset(x, 8, 0), Blocks.IRON_BLOCK);
            set(level, o.offset(x, 9, 0), Blocks.DEEPSLATE_TILES);
        }
        for (int x = 10; x <= 13; x++) {
            for (int y = 6; y <= 8; y++) set(level, o.offset(x, y, 0), Blocks.AIR);
        }
        for (int x = 7; x <= 16; x += 3) {
            set(level, o.offset(x, 2, -1), Blocks.POLISHED_BLACKSTONE);
            set(level, o.offset(x, 3, -1), Blocks.IRON_BARS);
        }

    }

    private static void drawArf(ServerLevel level, BlockPos base) {
        String[] glyphs = {
                "11110/10001/10001/11110/10100/10010/10001",
                "11110/10001/10001/11110/10100/10010/10001",
                "11110/10001/10001/11110/10100/10010/10001"
        };
        int cursor = 0;
        for (String glyph : glyphs) {
            String[] rows = glyph.split("/");
            for (int row = 0; row < rows.length; row++) for (int col = 0; col < rows[row].length(); col++)
                if (rows[row].charAt(col) == '1') set(level, base.offset(cursor + col, row, 0), Blocks.BLACK_CONCRETE);
            cursor += rows[0].length() + 1;
        }
    }

    private static void drawArfPanel(ServerLevel level, BlockPos base) {
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 3; y++) {
                set(level, base.offset(x, y, 0), Blocks.BLACK_CONCRETE);
            }
        }
        for (int x = 1; x < 6; x++) {
            set(level, base.offset(x, 1, 0), Blocks.GRAY_CONCRETE);
        }
        set(level, base.offset(3, 0, 0), Blocks.RED_CONCRETE);
        set(level, base.offset(3, 2, 0), Blocks.RED_CONCRETE);
    }

    private static void spawnAkio(ServerLevel level, BlockPos pos) {
        Entity entity = KenCraftEntities.AKIO_GINSHO.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 180.0F, 0.0F);
        entity.setCustomName(Component.literal("General ARF Akio Ginshō"));
        entity.setCustomNameVisible(true);
        level.addFreshEntity(entity);
    }

    private static void spawn(ServerLevel level, BlockPos pos, net.minecraft.world.entity.EntityType<? extends Entity> type) {
        Entity entity = type.create(level);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(entity);
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private record PendingGeneration(ServerLevel level, int centerX, int centerZ) {}
}
