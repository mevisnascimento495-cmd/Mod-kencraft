package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.ArfGeneralEntity;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcCommand {
    public static final String ARF_NAME = "Investigador da ARF";
    public static final String ARF_GENERAL_NAME = "Investigador ARF General";
    public static final String RINKA_NAME = "Rinka";

    private KenCraftNpcCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("npcs")
                        .executes(context -> spawnNpcs(context.getSource()))));
    }

    private static int spawnNpcs(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();
        RinkaEntity rinka = KenCraftEntities.RINKA.get().create(level);
        ArfInvestigatorEntity arf = KenCraftEntities.ARF_INVESTIGATOR.get().create(level);
        ArfGeneralEntity general = KenCraftEntities.ARF_GENERAL.get().create(level);
        if (rinka == null || arf == null || general == null) return 0;

        rinka.moveTo(base.offset(-3, 0, 0), 0, 0);
        rinka.finalizeSpawn(level, level.getCurrentDifficultyAt(rinka.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(rinka);

        arf.moveTo(base.offset(3, 0, 0), 0, 0);
        arf.finalizeSpawn(level, level.getCurrentDifficultyAt(arf.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(arf);

        general.moveTo(base.offset(0, 0, 3), 0, 0);
        general.finalizeSpawn(level, level.getCurrentDifficultyAt(general.blockPosition()), MobSpawnType.COMMAND, null);
        level.addFreshEntity(general);

        source.sendSuccess(() -> Component.literal("KenCraft: Rinka, Investigador da ARF e General da ARF criados."), true);
        return 1;
    }
}
