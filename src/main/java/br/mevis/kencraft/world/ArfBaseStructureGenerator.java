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
    }

    private static void buildBase(ServerLevel level, BlockPos o) {
        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++) {
            set(level, o.offset(x, 0, z), Blocks.STONE_BRICKS);
            set(level, o.offset(x, 1, z), Blocks.SMOOTH_STONE);
        }

        for (int y = 2; y <= 8; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, o.offset(x, y, 0), Blocks.SMOOTH_STONE);
                set(level, o.offset(x, y, DEPTH - 1), Blocks.SMOOTH_STONE);
            }
            for (int z = 0; z < DEPTH; z++) {
                set(level, o.offset(0, y, z), Blocks.SMOOTH_STONE);
                set(level, o.offset(WIDTH - 1, y, z), Blocks.SMOOTH_STONE);
            }
        }

        for (int x = 3; x <= 19; x += 2) {
            set(level, o.offset(x, 4, 0), Blocks.IRON_BARS);
            set(level, o.offset(x, 5, 0), Blocks.IRON_BARS);
            set(level, o.offset(x, 6, 0), Blocks.IRON_BARS);
        }

        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++) set(level, o.offset(x, 9, z), Blocks.DEEPSLATE_TILES);

        // frontal ARF: entrada reforçada e fachada militar
        for (int y = 2; y <= 5; y++) for (int x = 9; x <= 13; x++) set(level, o.offset(x, y, 0), Blocks.AIR);
        for (int x = 2; x <= 6; x++) for (int y = 4; y <= 6; y++) set(level, o.offset(x, y, 0), Blocks.GLASS_PANE);
        for (int x = 16; x <= 20; x++) for (int y = 4; y <= 6; y++) set(level, o.offset(x, y, 0), Blocks.GLASS_PANE);
        for (int z = 4; z <= 14; z += 2) for (int y = 4; y <= 6; y++) set(level, o.offset(0, y, z), Blocks.GLASS_PANE);
        for (int x = 2; x <= 20; x++) for (int z = 2; z <= 16; z++) set(level, o.offset(x, 6, z), Blocks.SMOOTH_STONE);
        for (int x = 2; x <= 20; x++) for (int z = 2; z <= 16; z++) if ((x + z) % 7 == 0) set(level, o.offset(x, 6, z), Blocks.GRAY_CARPET);

        // hall principal, escadas e posto de confronto
        for (int z = 4; z <= 10; z++) {
            set(level, o.offset(3, 2, z), Blocks.SPRUCE_STAIRS);
            set(level, o.offset(3, 3, z), Blocks.SPRUCE_STAIRS);
            set(level, o.offset(3, 4, z), Blocks.SPRUCE_STAIRS);
            set(level, o.offset(3, 5, z), Blocks.SPRUCE_STAIRS);
        }
        for (int y = 2; y <= 5; y++) set(level, o.offset(3, y, 3), Blocks.AIR);

        // área de comando
        for (int x = 7; x <= 15; x++) set(level, o.offset(x, 2, 4), Blocks.DARK_OAK_PLANKS);
        for (int x = 7; x <= 15; x++) set(level, o.offset(x, 3, 4), Blocks.SPRUCE_SLAB);
        for (int x : new int[]{8, 10, 12, 14}) set(level, o.offset(x, 2, 5), Blocks.SPRUCE_FENCE);
        set(level, o.offset(11, 2, 5), Blocks.LECTERN);
        set(level, o.offset(13, 2, 5), Blocks.BELL);

        for (int x = 6; x <= 16; x++) for (int z = 9; z <= 15; z++) if ((x + z) % 2 == 0) set(level, o.offset(x, 2, z), Blocks.RED_CARPET);
        for (int x : new int[]{6, 16}) for (int z = 10; z <= 14; z += 2) set(level, o.offset(x, 3, z), Blocks.IRON_BARS);
        for (int z = 3; z <= 15; z += 2) {
            set(level, o.offset(20, 2, z), Blocks.BARREL);
            set(level, o.offset(19, 2, z), Blocks.CHEST);
        }
        for (int y = 3; y <= 5; y++) for (int z = 3; z <= 15; z += 2) set(level, o.offset(20, y, z), Blocks.SPRUCE_TRAPDOOR);

        // armamento e luz de vigia
        for (int x : new int[]{7, 11, 15}) for (int z : new int[]{9, 13}) {
            set(level, o.offset(x, 7, z), Blocks.DARK_OAK_PLANKS);
            set(level, o.offset(x + 1, 7, z), Blocks.DARK_OAK_PLANKS);
            set(level, o.offset(x, 8, z), Blocks.SPRUCE_SLAB);
            set(level, o.offset(x + 1, 8, z), Blocks.SPRUCE_SLAB);
        }
        for (int x : new int[]{7, 11, 15}) set(level, o.offset(x, 8, 5), Blocks.BOOKSHELF);
        for (int x : new int[]{5, 11, 17}) for (int z : new int[]{7, 15}) {
            set(level, o.offset(x, 5, z), Blocks.CHAIN);
            set(level, o.offset(x, 4, z), Blocks.LANTERN);
        }

        // sinal da ARF e base de comando
        drawArf(level, o.offset(7, 3, -1));
        drawArfPanel(level, o.offset(8, 6, 0));
        set(level, o.offset(11, 1, 9), Blocks.LODESTONE);

        // entrada reforçada da base: mantém o vão central totalmente livre
        for (int y = 2; y <= 5; y++) {
            set(level, o.offset(9, y, 0), Blocks.IRON_BLOCK);
            set(level, o.offset(14, y, 0), Blocks.IRON_BLOCK);
        }
        // Reaplica o vão depois dos elementos da fachada para garantir a passagem.
        for (int y = 2; y <= 5; y++) {
            for (int x = 10; x <= 13; x++) {
                set(level, o.offset(x, y, 0), Blocks.AIR);
            }
        }
    

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
